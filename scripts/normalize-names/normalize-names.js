#!/usr/bin/env node
//
// One-time migration: normalize name columns to Title Case.
//
// Scope: users.first_name / users.last_name / users.middle_name,
//        children.first_name / children.last_name, guardians.name.
// Explicitly NOT touched: email, password, role, users.suffix (Jr./Sr./II/III/IV/V —
// word-capitalizing would corrupt Roman numerals into e.g. "Iii").
//
// Usage:
//   cd scripts/normalize-names && npm install
//
//   Connection: set DATABASE_URL (a full postgres:// connection string, e.g. your
//   Supabase pooler URI) or the discrete PGHOST/PGPORT/PGUSER/PGPASSWORD/PGDATABASE
//   env vars. SSL is on by default (required by Supabase) — set PGSSLMODE=disable
//   to turn it off for a local Postgres.
//
//   node normalize-names.js                 # dry run only — prints old -> new, writes nothing
//   node normalize-names.js --apply          # prints the same preview, then asks to confirm before writing
//   node normalize-names.js --apply --yes    # applies without the interactive confirmation (for scripted/CI use)
//
// Safe to re-run: only rows whose normalized value differs from the stored value
// are touched, and the transform is idempotent, so a second run finds nothing to change.

const { Client } = require('pg')
const readline = require('readline')

const TARGETS = [
  { table: 'users', idColumn: 'id', columns: ['first_name', 'last_name', 'middle_name'] },
  { table: 'children', idColumn: 'id', columns: ['first_name', 'last_name'] },
  { table: 'guardians', idColumn: 'id', columns: ['name'] },
]

// Connector words common in Filipino surnames (e.g. "Juan dela Cruz") that are
// conventionally kept lowercase mid-name. We don't rewrite around these — too easy
// to get wrong for a real surname like "Delos Santos" — we just flag rows that
// contain one so they get a manual look in the preview.
const FILIPINO_PARTICLES = /\b(dela|de la|de los|del|delos|ng)\b/i

function capitalizeSegment(segment) {
  if (segment.length === 0) return segment
  return segment[0].toUpperCase() + segment.slice(1).toLowerCase()
}

function capitalizeApostrophed(part) {
  return part.split("'").map(capitalizeSegment).join("'")
}

function capitalizeHyphenated(word) {
  return word.split('-').map(capitalizeApostrophed).join('-')
}

function capitalizeWords(value) {
  if (value === null || value === undefined) return value
  const trimmed = value.replace(/\s+/g, ' ').trim()
  if (trimmed === '') return trimmed
  return trimmed.split(' ').map(capitalizeHyphenated).join(' ')
}

function buildClientConfig() {
  const ssl = process.env.PGSSLMODE === 'disable' ? false : { rejectUnauthorized: false }
  if (process.env.DATABASE_URL) {
    return { connectionString: process.env.DATABASE_URL, ssl }
  }
  return {
    host: process.env.PGHOST,
    port: process.env.PGPORT ? Number(process.env.PGPORT) : 5432,
    user: process.env.PGUSER,
    password: process.env.PGPASSWORD,
    database: process.env.PGDATABASE,
    ssl,
  }
}

async function collectDiffs(client) {
  const diffs = []
  for (const target of TARGETS) {
    const { table, idColumn, columns } = target
    const sql = `SELECT ${idColumn}, ${columns.join(', ')} FROM ${table} ORDER BY ${idColumn}`
    const { rows } = await client.query(sql)

    for (const row of rows) {
      const changes = {}
      for (const col of columns) {
        const oldValue = row[col]
        const newValue = capitalizeWords(oldValue)
        if (newValue !== oldValue && newValue !== null && newValue !== undefined) {
          changes[col] = { oldValue, newValue }
        }
      }
      if (Object.keys(changes).length > 0) {
        diffs.push({ table, id: row[idColumn], changes })
      }
    }
  }
  return diffs
}

function printDiffs(diffs) {
  let current = null
  let count = 0
  for (const diff of diffs) {
    if (diff.table !== current) {
      current = diff.table
      console.log(`\n=== ${current} ===`)
    }
    for (const [col, { oldValue, newValue }] of Object.entries(diff.changes)) {
      count++
      const flag = FILIPINO_PARTICLES.test(oldValue) ? '  ⚠ contains a connector word (dela/de la/del/...) — verify casing convention' : ''
      console.log(`  id=${diff.id}  ${col}: ${JSON.stringify(oldValue)} -> ${JSON.stringify(newValue)}${flag}`)
    }
  }
  console.log(`\n${count} column value(s) across ${diffs.length} row(s) would change.`)
}

function confirm(question) {
  const rl = readline.createInterface({ input: process.stdin, output: process.stdout })
  return new Promise(resolve => {
    rl.question(question, answer => {
      rl.close()
      resolve(/^y(es)?$/i.test(answer.trim()))
    })
  })
}

async function applyDiffs(client, diffs) {
  await client.query('BEGIN')
  try {
    for (const diff of diffs) {
      const cols = Object.keys(diff.changes)
      const setClause = cols.map((col, i) => `${col} = $${i + 1}`).join(', ')
      const values = cols.map(col => diff.changes[col].newValue)
      const sql = `UPDATE ${diff.table} SET ${setClause} WHERE id = $${cols.length + 1}`
      await client.query(sql, [...values, diff.id])
    }
    await client.query('COMMIT')
  } catch (err) {
    await client.query('ROLLBACK')
    throw err
  }
}

async function main() {
  const args = process.argv.slice(2)
  const apply = args.includes('--apply')
  const skipConfirm = args.includes('--yes')

  const client = new Client(buildClientConfig())
  await client.connect()

  try {
    const diffs = await collectDiffs(client)

    if (diffs.length === 0) {
      console.log('✓ No changes needed — all target name columns are already normalized.')
      return
    }

    printDiffs(diffs)

    if (!apply) {
      console.log('\nDry run only — no changes were written. Re-run with --apply to write these changes.')
      return
    }

    if (!skipConfirm) {
      const ok = await confirm('\nApply these changes? (y/N) ')
      if (!ok) {
        console.log('Aborted — no changes were written.')
        return
      }
    }

    await applyDiffs(client, diffs)
    console.log(`\n✓ Applied. ${diffs.length} row(s) updated.`)
  } finally {
    await client.end()
  }
}

main().catch(err => {
  console.error('Migration failed:', err)
  process.exitCode = 1
})

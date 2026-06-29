const RAILWAY = 'https://daycarelog-production.up.railway.app/api'

export default async function handler(req, res) {
  const segments = req.query.path || []
  const pathStr = Array.isArray(segments) ? segments.join('/') : segments

  const targetUrl = new URL(`${RAILWAY}/${pathStr}`)
  const qs = { ...req.query }
  delete qs.path
  Object.entries(qs).forEach(([k, v]) => targetUrl.searchParams.set(k, v))

  const headers = {}
  if (req.headers['authorization']) headers['authorization'] = req.headers['authorization']
  headers['content-type'] = 'application/json'

  const opts = { method: req.method, headers }
  if (req.method !== 'GET' && req.method !== 'HEAD' && req.body) {
    opts.body = JSON.stringify(req.body)
  }

  try {
    const upstream = await fetch(targetUrl.toString(), opts)
    const ct = upstream.headers.get('content-type') || ''
    res.status(upstream.status)
    if (ct.includes('application/json')) {
      res.json(await upstream.json())
    } else {
      res.send(await upstream.text())
    }
  } catch (err) {
    res.status(502).json({ message: err.message })
  }
}

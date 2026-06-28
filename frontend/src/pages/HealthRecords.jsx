import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { classifyNutritionalStatus } from '../utils/nutritionalStatus'
import NutritionalStatusBadge from '../components/NutritionalStatusBadge'
import toast from 'react-hot-toast'

export default function HealthRecords() {
  const [records,  setRecords]  = useState([])
  const [children, setChildren] = useState({})
  const [loading,  setLoading]  = useState(true)
  const [search,   setSearch]   = useState('')

  useEffect(() => {
    async function load() {
      try {
        const [kids, recs] = await Promise.all([api.children.list(), api.health.list()])
        const map = {}; kids.forEach(k => { map[k.id] = k })
        setChildren(map); setRecords(recs)
      } catch { toast.error('Failed to load') }
      setLoading(false)
    }
    load()
  }, [])

  const filtered = records.filter(r => {
    const child = children[r.childId]
    return child && `${child.firstName} ${child.lastName}`.toLowerCase().includes(search.toLowerCase())
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-gray-900">Health Records</h1>
        <Link to="/health/new" className="bg-primary-600 hover:bg-primary-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition-colors">+ Add Record</Link>
      </div>

      <input type="text" placeholder="Search by child name…" value={search} onChange={e => setSearch(e.target.value)}
        className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-x-auto">
          {filtered.length === 0 ? <p className="text-center text-gray-400 py-12">No health records found.</p> : (
            <table className="w-full text-sm min-w-[560px]">
              <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                <tr>
                  <th className="text-left px-4 py-3">Child</th>
                  <th className="text-left px-4 py-3">Date</th>
                  <th className="text-left px-4 py-3">Weight</th>
                  <th className="text-left px-4 py-3">Height</th>
                  <th className="text-left px-4 py-3">Status</th>
                  <th className="text-left px-4 py-3">Remarks</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {filtered.map(r => {
                  const child = children[r.childId]
                  const status = child ? classifyNutritionalStatus(r.weightKg, child.dateOfBirth, child.sex) : null
                  return (
                    <tr key={r.id}>
                      <td className="px-4 py-3 font-medium text-gray-900">{child ? `${child.firstName} ${child.lastName}` : '—'}</td>
                      <td className="px-4 py-3">{new Date(r.measurementDate + 'T00:00:00').toLocaleDateString('en-PH')}</td>
                      <td className="px-4 py-3">{r.weightKg ? `${r.weightKg} kg` : '—'}</td>
                      <td className="px-4 py-3">{r.heightCm ? `${r.heightCm} cm` : '—'}</td>
                      <td className="px-4 py-3">{status ? <NutritionalStatusBadge status={status} /> : '—'}</td>
                      <td className="px-4 py-3 text-gray-400">{r.remarks || '—'}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  )
}

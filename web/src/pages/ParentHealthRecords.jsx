import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { classifyNutritionalStatus } from '../utils/nutritionalStatus'
import NutritionalStatusBadge from '../components/NutritionalStatusBadge'
import { HeartIcon } from '../components/icons'
import Pagination from '../components/Pagination'
import { usePagination } from '../utils/usePagination'
import toast from 'react-hot-toast'

export default function ParentHealthRecords() {
  const [records,     setRecords]     = useState([])
  const [children,    setChildren]    = useState([])
  const [childFilter, setChildFilter] = useState('all')
  const [loading,     setLoading]     = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const [kids, recs] = await Promise.all([api.children.mine(), api.health.mine()])
        setChildren(kids)
        setRecords(recs)
      } catch { toast.error('Failed to load health records') }
      setLoading(false)
    }
    load()
  }, [])

  const childMap = Object.fromEntries(children.map(c => [c.id, c]))
  const filtered = childFilter === 'all' ? records : records.filter(r => r.childId === Number(childFilter))
  const { page, setPage, totalPages, paged } = usePagination(filtered)

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-[22px] font-bold text-gray-900">Health Records</h1>
        {children.length > 1 && (
          <select value={childFilter} onChange={e => setChildFilter(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400">
            <option value="all">All children</option>
            {children.map(c => <option key={c.id} value={c.id}>{c.firstName} {c.lastName}</option>)}
          </select>
        )}
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200/70 overflow-x-auto">
          {filtered.length === 0 ? (
            <div className="text-center py-16">
              <span className="inline-flex w-12 h-12 rounded-full bg-gray-100 text-gray-400 items-center justify-center mb-3">
                <HeartIcon width={22} height={22} />
              </span>
              <p className="font-medium text-gray-500">No health records found</p>
            </div>
          ) : (
            <table className="w-full text-sm min-w-[560px]">
              <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
                <tr>
                  <th className="text-left px-4 py-2.5 font-medium">Child</th>
                  <th className="text-left px-4 py-2.5 font-medium">Date</th>
                  <th className="text-left px-4 py-2.5 font-medium">Weight</th>
                  <th className="text-left px-4 py-2.5 font-medium">Height</th>
                  <th className="text-left px-4 py-2.5 font-medium">Status</th>
                  <th className="text-left px-4 py-2.5 font-medium">Remarks</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {paged.map(r => {
                  const child = childMap[r.childId]
                  const status = child ? classifyNutritionalStatus(r.weightKg, child.dateOfBirth, child.sex) : null
                  return (
                    <tr key={r.id} className="hover:bg-gray-50/60 transition-colors duration-150">
                      <td className="px-4 py-2.5 font-medium text-gray-900">{child ? `${child.firstName} ${child.lastName}` : '—'}</td>
                      <td className="px-4 py-2.5 text-gray-600">{new Date(r.measurementDate + 'T00:00:00').toLocaleDateString('en-PH')}</td>
                      <td className="px-4 py-2.5 text-gray-600">{r.weightKg ? `${r.weightKg} kg` : '—'}</td>
                      <td className="px-4 py-2.5 text-gray-600">{r.heightCm ? `${r.heightCm} cm` : '—'}</td>
                      <td className="px-4 py-2.5">{status ? <NutritionalStatusBadge status={status} /> : '—'}</td>
                      <td className="px-4 py-2.5 text-gray-400">{r.remarks || '—'}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </div>
      )}
    </div>
  )
}

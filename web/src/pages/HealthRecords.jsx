import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { classifyNutritionalStatus } from '../utils/nutritionalStatus'
import NutritionalStatusBadge from '../components/NutritionalStatusBadge'
import { HeartIcon, TrashIcon } from '../components/icons'
import Pagination from '../components/Pagination'
import { usePagination } from '../utils/usePagination'
import toast from 'react-hot-toast'

export default function HealthRecords() {
  const [records,       setRecords]       = useState([])
  const [children,      setChildren]      = useState({})
  const [immunizations, setImmunizations] = useState([])
  const [schedule,      setSchedule]      = useState([])
  const [loading,       setLoading]       = useState(true)
  const [search,        setSearch]        = useState('')

  useEffect(() => {
    async function load() {
      try {
        const [kids, recs, imms, sched] = await Promise.all([
          api.children.list(), api.health.list(), api.immunizations.list(), api.immunizations.schedule(),
        ])
        const map = {}; kids.forEach(k => { map[k.id] = k })
        setChildren(map); setRecords(recs); setImmunizations(imms); setSchedule(sched)
      } catch { toast.error('Failed to load') }
      setLoading(false)
    }
    load()
  }, [])

  const totalExpectedDoses = schedule.reduce((sum, v) => sum + v.expectedDoses, 0)
  function immunizationSummary(childId) {
    return immunizations.filter(i => i.childId === childId).length
  }

  async function handleDelete(recordId) {
    if (!window.confirm('Move this health record to the Recycle Bin?')) return
    try {
      await api.health.delete(recordId)
      setRecords(prev => prev.filter(r => r.id !== recordId))
      toast.success('Moved to Recycle Bin')
    } catch (e) { toast.error(e.message) }
  }

  const filtered = records.filter(r => {
    const child = children[r.childId]
    return child && `${child.firstName} ${child.lastName}`.toLowerCase().includes(search.toLowerCase())
  })
  const { page, setPage, totalPages, paged } = usePagination(filtered)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-[22px] font-bold text-gray-900">Health Records</h1>
        <p className="text-gray-500 text-sm mt-1">Weight and height are recorded from a child's own profile (Children → Edit).</p>
      </div>

      <input type="text" placeholder="Search by child name…" value={search} onChange={e => setSearch(e.target.value)}
        className="w-full border border-gray-200 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" />

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
            <table className="w-full text-sm min-w-[900px]">
              <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
                <tr>
                  <th className="text-left px-4 py-3 font-medium">Child</th>
                  <th className="text-left px-4 py-3 font-medium">Date</th>
                  <th className="text-left px-4 py-3 font-medium">Weight</th>
                  <th className="text-left px-4 py-3 font-medium">Height</th>
                  <th className="text-left px-4 py-3 font-medium">Status</th>
                  <th className="text-left px-4 py-3 font-medium">Blood Type</th>
                  <th className="text-left px-4 py-3 font-medium">Immunizations</th>
                  <th className="text-left px-4 py-3 font-medium">Remarks</th>
                  <th className="text-left px-4 py-3 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {paged.map(r => {
                  const child = children[r.childId]
                  const status = child ? classifyNutritionalStatus(r.weightKg, child.dateOfBirth, child.sex) : null
                  const dosesGiven = child ? immunizationSummary(child.id) : 0
                  return (
                    <tr key={r.id} className="hover:bg-gray-50/60 transition-colors duration-150">
                      <td className="px-4 py-3 font-medium text-gray-900">{child ? `${child.firstName} ${child.lastName}` : '—'}</td>
                      <td className="px-4 py-3 text-gray-600">{new Date(r.measurementDate + 'T00:00:00').toLocaleDateString('en-PH')}</td>
                      <td className="px-4 py-3 text-gray-600">{r.weightKg ? `${r.weightKg} kg` : '—'}</td>
                      <td className="px-4 py-3 text-gray-600">{r.heightCm ? `${r.heightCm} cm` : '—'}</td>
                      <td className="px-4 py-3">{status ? <NutritionalStatusBadge status={status} /> : '—'}</td>
                      <td className="px-4 py-3 text-gray-600">{child?.bloodType || '—'}</td>
                      <td className="px-4 py-3">
                        {totalExpectedDoses === 0 ? '—' : (
                          <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${dosesGiven >= totalExpectedDoses ? 'bg-green-100 text-green-800' : 'bg-orange-100 text-orange-700'}`}>
                            {dosesGiven} / {totalExpectedDoses}
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-3 text-gray-400">{r.remarks || '—'}</td>
                      <td className="px-4 py-3">
                        <button onClick={() => handleDelete(r.id)} className="text-gray-300 hover:text-red-500 transition-colors" title="Delete">
                          <TrashIcon width={16} height={16} />
                        </button>
                      </td>
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

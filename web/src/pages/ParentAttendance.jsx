import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import toast from 'react-hot-toast'

const STATUS_COLORS = { present: 'bg-green-100 text-green-700', absent: 'bg-red-100 text-red-700', late: 'bg-yellow-100 text-yellow-700', excused: 'bg-blue-100 text-blue-700' }

export default function ParentAttendance() {
  const [children,   setChildren]   = useState([])
  const [records,    setRecords]    = useState([])
  const [childFilter, setChildFilter] = useState('all')
  const [loading,    setLoading]    = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const [kids, att] = await Promise.all([api.children.mine(), api.attendance.mine()])
        setChildren(kids)
        setRecords(att)
      } catch { toast.error('Failed to load attendance') }
      setLoading(false)
    }
    load()
  }, [])

  const childMap = Object.fromEntries(children.map(c => [c.id, c]))
  const filtered = childFilter === 'all' ? records : records.filter(r => r.childId === Number(childFilter))

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-[22px] font-bold text-gray-900">Attendance</h1>
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
      ) : filtered.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200/70 text-center py-16">
          <p className="font-medium text-gray-500">No attendance records yet</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200/70 overflow-x-auto">
          <table className="w-full text-sm min-w-[460px]">
            <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
              <tr>
                <th className="text-left px-4 py-3 font-medium">Child</th>
                <th className="text-left px-4 py-3 font-medium">Date</th>
                <th className="text-left px-4 py-3 font-medium">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filtered.map(r => (
                <tr key={r.id} className="hover:bg-gray-50/60 transition-colors duration-150">
                  <td className="px-4 py-3 font-medium text-gray-900">
                    {childMap[r.childId] ? `${childMap[r.childId].firstName} ${childMap[r.childId].lastName}` : '—'}
                  </td>
                  <td className="px-4 py-3 text-gray-600">{new Date(r.date + 'T00:00:00').toLocaleDateString('en-PH', { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' })}</td>
                  <td className="px-4 py-3">
                    <span className={`px-3 py-1 rounded-lg text-xs font-medium capitalize ${STATUS_COLORS[r.status] ?? 'bg-gray-100 text-gray-500'}`}>
                      {r.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

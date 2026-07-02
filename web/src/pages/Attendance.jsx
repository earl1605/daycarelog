import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import toast from 'react-hot-toast'

const STATUS_OPTS   = ['present', 'absent', 'late', 'excused']
const STATUS_COLORS = { present: 'bg-green-100 text-green-700', absent: 'bg-red-100 text-red-700', late: 'bg-yellow-100 text-yellow-700', excused: 'bg-blue-100 text-blue-700' }

export default function Attendance() {
  const [children, setChildren] = useState([])
  const [records,  setRecords]  = useState({})
  const [date,     setDate]     = useState(new Date().toISOString().split('T')[0])
  const [saving,   setSaving]   = useState(false)
  const [loading,  setLoading]  = useState(true)

  useEffect(() => {
    api.children.list().then(kids => { setChildren(kids.filter(c => c.enrollmentStatus === 'active')); setLoading(false) })
  }, [])

  useEffect(() => {
    if (!date) return
    api.attendance.getByDate(date).then(data => {
      const map = {}
      data.forEach(r => { map[r.childId] = r })
      setRecords(map)
    })
  }, [date])

  function setStatus(childId, status) {
    setRecords(prev => ({ ...prev, [childId]: { ...(prev[childId] ?? {}), status } }))
  }

  async function saveAll() {
    setSaving(true)
    try {
      const bulk = children.map(c => ({ childId: c.id, date, status: records[c.id]?.status ?? 'absent' }))
      await api.attendance.saveBulk(bulk)
      toast.success('Attendance saved!')
    } catch (e) { toast.error(e.message) }
    setSaving(false)
  }

  const present = Object.values(records).filter(r => r.status === 'present').length
  const absent  = Object.values(records).filter(r => r.status === 'absent').length

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-gray-900">Attendance</h1>
        <input type="date" value={date} onChange={e => setDate(e.target.value)}
          className="border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
      </div>

      <div className="flex gap-4 flex-wrap">
        {[['Present', present, 'text-green-600'], ['Absent', absent, 'text-red-500'], ['Total', children.length, 'text-gray-700']].map(([l,v,c]) => (
          <div key={l} className="bg-white rounded-xl border border-gray-100 shadow-sm px-5 py-3 flex items-center gap-3">
            <p className={`text-2xl font-extrabold ${c}`}>{v}</p>
            <p className="text-sm text-gray-500">{l}</p>
          </div>
        ))}
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr><th className="text-left px-4 py-3">Name</th><th className="text-left px-4 py-3">Status</th></tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {children.map(c => (
                <tr key={c.id}>
                  <td className="px-4 py-3 font-medium text-gray-900">{c.firstName} {c.lastName}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 flex-wrap">
                      {STATUS_OPTS.map(s => (
                        <button key={s} onClick={() => setStatus(c.id, s)}
                          className={`px-3 py-1 rounded-lg text-xs font-medium capitalize transition-all ${records[c.id]?.status === s ? STATUS_COLORS[s] + ' ring-2 ring-offset-1 ring-current' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'}`}>
                          {s}
                        </button>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="flex justify-end">
        <button onClick={saveAll} disabled={saving}
          className="bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold px-6 py-2.5 rounded-xl transition-colors">
          {saving ? 'Saving…' : 'Save Attendance'}
        </button>
      </div>
    </div>
  )
}

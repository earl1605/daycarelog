import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../lib/api'
import { formatAge, classifyNutritionalStatus } from '../utils/nutritionalStatus'
import NutritionalStatusBadge from '../components/NutritionalStatusBadge'
import toast from 'react-hot-toast'

const TAB = { OVERVIEW: 'overview', HEALTH: 'health', ATTENDANCE: 'attendance' }

export default function ChildDetail() {
  const { id }    = useParams()
  const navigate  = useNavigate()
  const [child,   setChild]   = useState(null)
  const [health,  setHealth]  = useState([])
  const [att,     setAtt]     = useState([])
  const [tab,     setTab]     = useState(TAB.OVERVIEW)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      api.children.get(id),
      api.health.getByChild(id),
      api.attendance.getByChild(id),
    ]).then(([c, h, a]) => { setChild(c); setHealth(h); setAtt(a) })
      .catch(() => toast.error('Failed to load'))
      .finally(() => setLoading(false))
  }, [id])

  async function handleDelete() {
    if (!window.confirm('Delete this child and all their records?')) return
    await api.children.delete(id)
    toast.success('Child deleted')
    navigate('/children')
  }

  if (loading) return <div className="flex items-center justify-center h-64"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
  if (!child)  return <div className="text-center py-20 text-gray-400">Child not found. <Link to="/children" className="text-primary-600">Back</Link></div>

  const latestHealth = health[0]
  const status = latestHealth ? classifyNutritionalStatus(latestHealth.weightKg, child.dateOfBirth, child.sex) : null

  const tabs = [
    { key: TAB.OVERVIEW, label: 'Overview' },
    { key: TAB.HEALTH,   label: `Health (${health.length})` },
    { key: TAB.ATTENDANCE, label: `Attendance (${att.length})` },
  ]

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-start gap-4">
        <button onClick={() => navigate(-1)} className="mt-1 text-gray-400 hover:text-gray-700">← Back</button>
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <div className="w-14 h-14 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-2xl">
              {child.firstName[0]}{child.lastName[0]}
            </div>
            <div>
              <h1 className="text-2xl font-extrabold text-gray-900">{child.firstName} {child.lastName}</h1>
              <p className="text-gray-500 text-sm">{child.sex} · {formatAge(child.dateOfBirth)}</p>
              {status && <NutritionalStatusBadge status={status} />}
            </div>
          </div>
        </div>
        <div className="flex gap-2">
          <Link to={`/children/${id}/edit`} className="text-sm border border-gray-200 px-3 py-1.5 rounded-lg hover:bg-gray-50">Edit</Link>
          <button onClick={handleDelete} className="text-sm text-red-500 border border-red-200 px-3 py-1.5 rounded-lg hover:bg-red-50">Delete</button>
        </div>
      </div>

      <div className="flex gap-1 bg-gray-100 p-1 rounded-xl">
        {tabs.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`flex-1 text-sm font-medium py-2 rounded-lg transition-colors ${tab === t.key ? 'bg-white shadow-sm text-gray-900' : 'text-gray-500 hover:text-gray-700'}`}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === TAB.OVERVIEW && (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 grid grid-cols-2 gap-4 text-sm">
          {[
            ['Date of Birth', new Date(child.dateOfBirth + 'T00:00:00').toLocaleDateString('en-PH')],
            ['Age',           formatAge(child.dateOfBirth)],
            ['Sex',           child.sex],
            ['Enrollment Date', child.enrollmentDate ? new Date(child.enrollmentDate + 'T00:00:00').toLocaleDateString('en-PH') : '—'],
            ['Status',        child.enrollmentStatus],
            ['Address',       child.address || '—'],
            ['Latest Weight', latestHealth ? `${latestHealth.weightKg} kg` : '—'],
            ['Latest Height', latestHealth ? `${latestHealth.heightCm} cm` : '—'],
          ].map(([label, value]) => (
            <div key={label}>
              <p className="text-gray-400">{label}</p>
              <p className="font-medium text-gray-900 capitalize">{value}</p>
            </div>
          ))}
        </div>
      )}

      {tab === TAB.HEALTH && (
        <div className="space-y-3">
          <div className="flex justify-between items-center">
            <h2 className="font-semibold text-gray-900">Health Records</h2>
            <Link to={`/health/new?child=${id}`} className="text-sm bg-primary-600 text-white px-3 py-1.5 rounded-lg hover:bg-primary-700">+ Add</Link>
          </div>
          {health.length === 0 ? <p className="text-gray-400 text-sm">No records yet.</p> : health.map(r => (
            <div key={r.id} className="bg-white rounded-xl border border-gray-100 p-4 flex gap-4 text-sm">
              <div className="flex-1">
                <p className="font-medium text-gray-900">{new Date(r.measurementDate + 'T00:00:00').toLocaleDateString('en-PH')}</p>
                <p className="text-gray-500">Weight: {r.weightKg}kg · Height: {r.heightCm}cm</p>
                {r.remarks && <p className="text-gray-400 text-xs mt-1">{r.remarks}</p>}
              </div>
              {r.nutritionalStatus && <span className="self-start bg-green-100 text-green-800 text-xs font-medium px-2 py-0.5 rounded-full">{r.nutritionalStatus}</span>}
            </div>
          ))}
        </div>
      )}

      {tab === TAB.ATTENDANCE && (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          {att.length === 0 ? <p className="text-gray-400 text-sm p-4">No records yet.</p> : (
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                <tr>
                  <th className="text-left px-4 py-3">Date</th>
                  <th className="text-left px-4 py-3">Status</th>
                  <th className="text-left px-4 py-3">Time In</th>
                  <th className="text-left px-4 py-3">Time Out</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {att.map(a => (
                  <tr key={a.id}>
                    <td className="px-4 py-3">{new Date(a.date + 'T00:00:00').toLocaleDateString('en-PH')}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${a.status === 'present' ? 'bg-green-100 text-green-700' : a.status === 'absent' ? 'bg-red-100 text-red-700' : 'bg-yellow-100 text-yellow-700'}`}>{a.status}</span>
                    </td>
                    <td className="px-4 py-3 text-gray-400">{a.timeIn ?? '—'}</td>
                    <td className="px-4 py-3 text-gray-400">{a.timeOut ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  )
}

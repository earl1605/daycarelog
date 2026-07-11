import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../lib/api'
import { formatAge, classifyNutritionalStatus } from '../utils/nutritionalStatus'
import NutritionalStatusBadge from '../components/NutritionalStatusBadge'
import ImmunizationChecklist from '../components/ImmunizationChecklist'
import GrowthChart from '../components/GrowthChart'
import { AlertTriangleIcon, TrashIcon } from '../components/icons'
import toast from 'react-hot-toast'

const TAB = { OVERVIEW: 'overview', HEALTH: 'health', IMMUNIZATIONS: 'immunizations', ATTENDANCE: 'attendance' }

// Server computes nutritional_status as NORMAL/UNDERWEIGHT/SEVERELY_UNDERWEIGHT/OVERWEIGHT
// (see HealthRecordService) -- map to the {label, color} shape NutritionalStatusBadge expects.
const STATUS_DISPLAY = {
  NORMAL:               { label: 'Normal',               color: 'green'  },
  UNDERWEIGHT:          { label: 'Underweight',           color: 'orange' },
  SEVERELY_UNDERWEIGHT: { label: 'Severely Underweight',  color: 'red'    },
  OVERWEIGHT:           { label: 'Overweight',             color: 'yellow' },
}

export default function ChildDetail() {
  const { id }    = useParams()
  const navigate  = useNavigate()
  const [child,   setChild]   = useState(null)
  const [health,  setHealth]  = useState([])
  const [att,     setAtt]     = useState([])
  const [immunizations, setImmunizations] = useState([])
  const [schedule,       setSchedule]     = useState([])
  const [tab,     setTab]     = useState(TAB.OVERVIEW)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      api.children.get(id),
      api.health.getByChild(id),
      api.attendance.getByChild(id),
      api.immunizations.getByChild(id),
      api.immunizations.schedule(),
    ]).then(([c, h, a, im, sch]) => { setChild(c); setHealth(h); setAtt(a); setImmunizations(im); setSchedule(sch) })
      .catch(() => toast.error('Failed to load'))
      .finally(() => setLoading(false))
  }, [id])

  async function handleDeleteImmunization(immId) {
    if (!window.confirm('Move this immunization record to the Recycle Bin?')) return
    try {
      await api.immunizations.delete(immId)
      setImmunizations(prev => prev.filter(r => r.id !== immId))
      toast.success('Moved to Recycle Bin')
    } catch (e) { toast.error(e.message) }
  }

  async function handleDeleteHealthRecord(recordId) {
    if (!window.confirm('Move this health record to the Recycle Bin?')) return
    try {
      await api.health.delete(recordId)
      setHealth(prev => prev.filter(r => r.id !== recordId))
      toast.success('Moved to Recycle Bin')
    } catch (e) { toast.error(e.message) }
  }

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
  const totalExpectedDoses = schedule.reduce((sum, v) => sum + v.expectedDoses, 0)

  const tabs = [
    { key: TAB.OVERVIEW, label: 'Overview' },
    { key: TAB.HEALTH,   label: `Health (${health.length})` },
    { key: TAB.IMMUNIZATIONS, label: `Immunizations (${immunizations.length})` },
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

      {child.allergies && (
        <div className="flex items-start gap-3 bg-red-50 border border-red-200 text-red-800 rounded-xl px-4 py-3">
          <AlertTriangleIcon className="shrink-0 mt-0.5 text-red-600" />
          <div>
            <p className="font-semibold text-sm">Allergies</p>
            <p className="text-sm">{child.allergies}</p>
          </div>
        </div>
      )}

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
            ['Blood Type',    child.bloodType || '—'],
            ['Medical Conditions', child.medicalConditions || '—'],
            ['Latest Weight', latestHealth ? `${latestHealth.weightKg} kg` : '—'],
            ['Latest Height', latestHealth ? `${latestHealth.heightCm} cm` : '—'],
          ].map(([label, value]) => (
            <div key={label}>
              <p className="text-gray-400">{label}</p>
              <p className="font-medium text-gray-900 capitalize">{value}</p>
            </div>
          ))}
          {schedule.length > 0 && (
            <div className="col-span-2">
              <p className="text-gray-400 mb-2">Immunizations ({immunizations.length} / {totalExpectedDoses} doses)</p>
              <div className="flex flex-wrap gap-2">
                {schedule.map(v => {
                  const given = immunizations.filter(i => i.vaccineName === v.name).length
                  const complete = given >= v.expectedDoses
                  return (
                    <span key={v.name}
                      className={`text-xs font-medium px-2.5 py-1 rounded-full ${complete ? 'bg-green-100 text-green-800' : given > 0 ? 'bg-orange-100 text-orange-700' : 'bg-gray-100 text-gray-500'}`}>
                      {v.name} {given}/{v.expectedDoses}
                    </span>
                  )
                })}
              </div>
            </div>
          )}
        </div>
      )}

      {tab === TAB.HEALTH && (
        <div className="space-y-3">
          <div className="flex justify-between items-center">
            <h2 className="font-semibold text-gray-900">Health Records</h2>
            <Link to={`/health/new?child=${id}`} className="text-sm bg-primary-600 text-white px-3 py-1.5 rounded-lg hover:bg-primary-700">+ Add</Link>
          </div>

          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <h3 className="text-sm font-medium text-gray-500 mb-3">Growth Over Time</h3>
            <GrowthChart records={health} />
          </div>

          {health.length === 0 ? <p className="text-gray-400 text-sm">No records yet.</p> : health.map(r => (
            <div key={r.id} className="bg-white rounded-xl border border-gray-100 p-4 flex gap-4 text-sm items-start">
              <div className="flex-1">
                <p className="font-medium text-gray-900">{new Date(r.measurementDate + 'T00:00:00').toLocaleDateString('en-PH')}</p>
                <p className="text-gray-500">Weight: {r.weightKg}kg · Height: {r.heightCm}cm</p>
                {r.remarks && <p className="text-gray-400 text-xs mt-1">{r.remarks}</p>}
              </div>
              {r.nutritionalStatus && <NutritionalStatusBadge status={STATUS_DISPLAY[r.nutritionalStatus]} />}
              <button onClick={() => handleDeleteHealthRecord(r.id)} className="text-gray-300 hover:text-red-500 transition-colors">
                <TrashIcon width={16} height={16} />
              </button>
            </div>
          ))}
        </div>
      )}

      {tab === TAB.IMMUNIZATIONS && (
        <div className="space-y-5">
          <div className="flex justify-between items-center">
            <h2 className="font-semibold text-gray-900">Immunizations</h2>
            <Link to={`/immunizations/new?child=${id}`} className="text-sm bg-primary-600 text-white px-3 py-1.5 rounded-lg hover:bg-primary-700">+ Add</Link>
          </div>

          <ImmunizationChecklist schedule={schedule} records={immunizations} />

          {immunizations.length > 0 && (
            <div className="space-y-2">
              <h3 className="text-sm font-medium text-gray-500">Records</h3>
              {immunizations.map(r => (
                <div key={r.id} className="bg-white rounded-xl border border-gray-100 p-4 flex gap-4 text-sm items-start">
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">{r.vaccineName} · Dose {r.doseNumber}</p>
                    <p className="text-gray-500">{new Date(r.dateGiven + 'T00:00:00').toLocaleDateString('en-PH')}{r.administeredBy ? ` · ${r.administeredBy}` : ''}</p>
                    {r.notes && <p className="text-gray-400 text-xs mt-1">{r.notes}</p>}
                  </div>
                  <button onClick={() => handleDeleteImmunization(r.id)} className="text-gray-300 hover:text-red-500 transition-colors">
                    <TrashIcon width={16} height={16} />
                  </button>
                </div>
              ))}
            </div>
          )}
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

import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../lib/api'
import { toLocalDateString } from '../utils/date'
import toast from 'react-hot-toast'

export default function ImmunizationForm() {
  const navigate    = useNavigate()
  const [params]    = useSearchParams()
  const preChildId  = params.get('child') ?? ''
  const [children, setChildren] = useState([])
  const [schedule, setSchedule] = useState([])
  const [form, setForm] = useState({ childId: preChildId, vaccineName: '', doseNumber: '', dateGiven: toLocalDateString(), administeredBy: '', notes: '' })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.children.list().then(setChildren).catch(() => {})
    api.immunizations.schedule().then(setSchedule).catch(() => {})
  }, [])

  const selectedVaccine = schedule.find(v => v.name === form.vaccineName)

  function set(field) { return e => setForm(f => ({ ...f, [field]: e.target.value })) }

  function setVaccine(e) {
    setForm(f => ({ ...f, vaccineName: e.target.value, doseNumber: '' }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.childId || !form.vaccineName || !form.doseNumber || !form.dateGiven) {
      toast.error('Child, vaccine, dose, and date are required')
      return
    }
    setLoading(true)
    try {
      await api.immunizations.create({
        ...form,
        childId: Number(form.childId),
        doseNumber: Number(form.doseNumber),
      })
      toast.success('Immunization record saved')
      navigate(-1)
    } catch (e) { toast.error(e.message) }
    setLoading(false)
  }

  const inputClass = "w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
  const labelClass = "block text-xs font-medium text-gray-700 mb-1"

  return (
    <div className="max-w-lg mx-auto space-y-5">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-gray-700">← Back</button>
        <h1 className="text-[22px] font-bold text-gray-900">Record Immunization</h1>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-gray-200/70 p-5 space-y-4">
        <div>
          <label className={labelClass}>Child *</label>
          <select value={form.childId} onChange={set('childId')} className={`${inputClass} bg-white`}>
            <option value="">Select child…</option>
            {children.map(c => <option key={c.id} value={c.id}>{c.firstName} {c.lastName}</option>)}
          </select>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className={labelClass}>Vaccine *</label>
            <select value={form.vaccineName} onChange={setVaccine} className={`${inputClass} bg-white`}>
              <option value="">Select vaccine…</option>
              {schedule.map(v => <option key={v.name} value={v.name}>{v.name}</option>)}
            </select>
          </div>
          <div>
            <label className={labelClass}>Dose *</label>
            <select value={form.doseNumber} onChange={set('doseNumber')} disabled={!selectedVaccine} className={`${inputClass} bg-white disabled:bg-gray-50`}>
              <option value="">Select…</option>
              {selectedVaccine && Array.from({ length: selectedVaccine.expectedDoses }, (_, i) => i + 1).map(d => (
                <option key={d} value={d}>Dose {d}</option>
              ))}
            </select>
          </div>
        </div>
        <div>
          <label className={labelClass}>Date Given *</label>
          <input type="date" value={form.dateGiven} onChange={set('dateGiven')} className={inputClass} />
        </div>
        <div>
          <label className={labelClass}>Administered By</label>
          <input type="text" value={form.administeredBy} onChange={set('administeredBy')} placeholder="e.g. Nurse Santos" className={inputClass} />
        </div>
        <div>
          <label className={labelClass}>Notes</label>
          <textarea value={form.notes} onChange={set('notes')} rows={3} className={`${inputClass} resize-none`} placeholder="Optional notes…" />
        </div>
        <div className="flex gap-3 pt-2">
          <button type="button" onClick={() => navigate(-1)} className="flex-1 border border-gray-200 text-gray-700 text-sm font-medium py-2.5 rounded-lg hover:bg-gray-50 transition-colors">Cancel</button>
          <button type="submit" disabled={loading} className="flex-1 bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white text-sm font-semibold py-2.5 rounded-lg transition-colors">
            {loading ? 'Saving…' : 'Save Record'}
          </button>
        </div>
      </form>
    </div>
  )
}

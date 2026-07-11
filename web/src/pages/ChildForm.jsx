import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../lib/api'
import GuardiansSection from '../components/GuardiansSection'
import { toLocalDateString } from '../utils/date'
import { handleCapitalizedNameInput } from '../utils/capitalizeFirstLetters'
import { classifyNutritionalStatus } from '../utils/nutritionalStatus'
import toast from 'react-hot-toast'

const BLOOD_TYPES = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-']

const empty = {
  firstName: '', lastName: '', dateOfBirth: '', sex: '', address: '',
  enrollmentDate: toLocalDateString(), enrollmentStatus: 'active',
  allergies: '', medicalConditions: '', bloodType: '',
  weightKg: '', heightCm: '', measurementDate: toLocalDateString(), remarks: '',
}

export default function ChildForm() {
  const { id }   = useParams()
  const navigate = useNavigate()
  const isEdit   = Boolean(id)
  const [form, setForm]   = useState(empty)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!isEdit) return
    api.children.get(id).then(data => setForm({
      ...empty, ...data,
      dateOfBirth: data.dateOfBirth?.split('T')[0] ?? data.dateOfBirth,
      enrollmentDate: data.enrollmentDate?.split('T')[0] ?? data.enrollmentDate,
    })).catch(() => toast.error('Failed to load child'))
  }, [id, isEdit])

  function set(field) { return e => setForm(f => ({ ...f, [field]: e.target.value })) }
  function setCapitalized(field) { return handleCapitalizedNameInput(v => setForm(f => ({ ...f, [field]: v }))) }

  const preview = (form.weightKg && form.dateOfBirth)
    ? classifyNutritionalStatus(parseFloat(form.weightKg), form.dateOfBirth, form.sex)
    : null
  const colorMap = { green: 'bg-green-100 text-green-800', yellow: 'bg-yellow-100 text-yellow-800', orange: 'bg-orange-100 text-orange-800', red: 'bg-red-100 text-red-800', gray: 'bg-gray-100 text-gray-600' }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.firstName || !form.lastName || !form.dateOfBirth || !form.sex) { toast.error('Please fill in all required fields'); return }
    setLoading(true)
    try {
      const child = isEdit ? await api.children.update(id, form) : await api.children.create(form)
      if (form.weightKg || form.heightCm) {
        await api.health.create({
          childId: child.id,
          measurementDate: form.measurementDate || toLocalDateString(),
          weightKg: form.weightKg ? parseFloat(form.weightKg) : null,
          heightCm: form.heightCm ? parseFloat(form.heightCm) : null,
          remarks: form.remarks || null,
        })
      }
      toast.success(isEdit ? 'Child updated' : 'Child enrolled')
      navigate('/children')
    } catch (e) { toast.error(e.message) }
    setLoading(false)
  }

  const field = (label, key, type = 'text', opts = {}) => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
      <input type={type} value={form[key]} onChange={set(key)}
        className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" {...opts} />
    </div>
  )

  const nameField = (label, key, opts = {}) => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
      <input type="text" value={form[key]} onChange={setCapitalized(key)} autoCapitalize="words"
        className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" {...opts} />
    </div>
  )

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-gray-700">← Back</button>
        <h1 className="text-2xl font-extrabold text-gray-900">{isEdit ? 'Edit Child' : 'Enroll New Child'}</h1>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-5">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          {nameField('First Name *', 'firstName', { placeholder: 'Juan' })}
          {nameField('Last Name *',  'lastName',  { placeholder: 'Dela Cruz' })}
          {field('Date of Birth *', 'dateOfBirth', 'date')}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Sex *</label>
            <select value={form.sex} onChange={set('sex')} className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
              <option value="">Select…</option>
              <option>Male</option>
              <option>Female</option>
            </select>
          </div>
          {nameField('Address', 'address', { placeholder: 'Barangay, City' })}
          {field('Enrollment Date', 'enrollmentDate', 'date')}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Status</label>
            <select value={form.enrollmentStatus} onChange={set('enrollmentStatus')} className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Blood Type</label>
            <select value={form.bloodType} onChange={set('bloodType')} className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
              <option value="">— Unknown —</option>
              {BLOOD_TYPES.map(t => <option key={t}>{t}</option>)}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Allergies</label>
            <textarea value={form.allergies} onChange={set('allergies')} rows={3} placeholder="e.g. Peanuts, penicillin…"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Medical Conditions</label>
            <textarea value={form.medicalConditions} onChange={set('medicalConditions')} rows={3} placeholder="e.g. Asthma…"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none" />
          </div>
        </div>

        <div className="border-t border-gray-100 pt-5 space-y-4">
          <div>
            <h2 className="text-sm font-semibold text-gray-900">Record a Measurement</h2>
            <p className="text-xs text-gray-400 mt-0.5">Optional — leave weight and height blank to save the rest without adding a new health record.</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
            {field('Weight (kg)', 'weightKg', 'number', { step: '0.01', placeholder: '12.5' })}
            {field('Height (cm)', 'heightCm', 'number', { step: '0.1', placeholder: '90.0' })}
            {field('Measurement Date', 'measurementDate', 'date')}
          </div>
          {preview && <div className={`text-sm px-4 py-2.5 rounded-xl font-medium ${colorMap[preview.color]}`}>Estimated Nutritional Status: {preview.label}</div>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Remarks</label>
            <textarea value={form.remarks} onChange={set('remarks')} rows={2} placeholder="Optional notes about this measurement…"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none" />
          </div>
        </div>

        <div className="flex gap-3 pt-2">
          <button type="button" onClick={() => navigate(-1)} className="flex-1 border border-gray-200 text-gray-700 font-medium py-2.5 rounded-xl hover:bg-gray-50 transition-colors">Cancel</button>
          <button type="submit" disabled={loading} className="flex-1 bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold py-2.5 rounded-xl transition-colors">
            {loading ? 'Saving…' : isEdit ? 'Save Changes' : 'Enroll Child'}
          </button>
        </div>
      </form>

      {isEdit && <GuardiansSection childId={id} />}
    </div>
  )
}

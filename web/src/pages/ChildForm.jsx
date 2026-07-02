import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../lib/api'
import GuardiansSection from '../components/GuardiansSection'
import { toLocalDateString } from '../utils/date'
import toast from 'react-hot-toast'

const empty = { firstName: '', lastName: '', dateOfBirth: '', sex: '', address: '', enrollmentDate: toLocalDateString(), enrollmentStatus: 'active' }

export default function ChildForm() {
  const { id }   = useParams()
  const navigate = useNavigate()
  const isEdit   = Boolean(id)
  const [form, setForm]   = useState(empty)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!isEdit) return
    api.children.get(id).then(data => setForm({ ...data, dateOfBirth: data.dateOfBirth?.split('T')[0] ?? data.dateOfBirth, enrollmentDate: data.enrollmentDate?.split('T')[0] ?? data.enrollmentDate }))
      .catch(() => toast.error('Failed to load child'))
  }, [id, isEdit])

  function set(field) { return e => setForm(f => ({ ...f, [field]: e.target.value })) }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.firstName || !form.lastName || !form.dateOfBirth || !form.sex) { toast.error('Please fill in all required fields'); return }
    setLoading(true)
    try {
      if (isEdit) await api.children.update(id, form)
      else await api.children.create(form)
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

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-gray-700">← Back</button>
        <h1 className="text-2xl font-extrabold text-gray-900">{isEdit ? 'Edit Child' : 'Enroll New Child'}</h1>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-5">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          {field('First Name *', 'firstName', 'text', { placeholder: 'Juan' })}
          {field('Last Name *',  'lastName',  'text', { placeholder: 'Dela Cruz' })}
          {field('Date of Birth *', 'dateOfBirth', 'date')}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Sex *</label>
            <select value={form.sex} onChange={set('sex')} className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
              <option value="">Select…</option>
              <option>Male</option>
              <option>Female</option>
            </select>
          </div>
          {field('Address', 'address', 'text', { placeholder: 'Barangay, City' })}
          {field('Enrollment Date', 'enrollmentDate', 'date')}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Status</label>
            <select value={form.enrollmentStatus} onChange={set('enrollmentStatus')} className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
            </select>
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

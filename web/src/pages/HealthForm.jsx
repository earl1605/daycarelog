import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../lib/api'
import { classifyNutritionalStatus } from '../utils/nutritionalStatus'
import toast from 'react-hot-toast'

export default function HealthForm() {
  const navigate    = useNavigate()
  const [params]    = useSearchParams()
  const preChildId  = params.get('child') ?? ''
  const [children, setChildren] = useState([])
  const [form, setForm] = useState({ childId: preChildId, measurementDate: new Date().toISOString().split('T')[0], weightKg: '', heightCm: '', remarks: '' })
  const [loading, setLoading] = useState(false)
  const [preview, setPreview] = useState(null)

  useEffect(() => { api.children.list().then(setChildren).catch(() => {}) }, [])

  useEffect(() => {
    if (!form.childId || !form.weightKg) { setPreview(null); return }
    const child = children.find(c => String(c.id) === String(form.childId))
    if (child) setPreview(classifyNutritionalStatus(parseFloat(form.weightKg), child.dateOfBirth, child.sex))
  }, [form.childId, form.weightKg, children])

  function set(field) { return e => setForm(f => ({ ...f, [field]: e.target.value })) }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.childId || !form.measurementDate) { toast.error('Child and date are required'); return }
    setLoading(true)
    try {
      await api.health.create({
        ...form,
        childId: Number(form.childId),
        weightKg: form.weightKg ? parseFloat(form.weightKg) : null,
        heightCm: form.heightCm ? parseFloat(form.heightCm) : null,
        nutritionalStatus: preview?.label ?? null,
      })
      toast.success('Health record saved')
      navigate('/health')
    } catch (e) { toast.error(e.message) }
    setLoading(false)
  }

  const colorMap = { green: 'bg-green-100 text-green-800', yellow: 'bg-yellow-100 text-yellow-800', orange: 'bg-orange-100 text-orange-800', red: 'bg-red-100 text-red-800', gray: 'bg-gray-100 text-gray-600' }

  return (
    <div className="max-w-lg mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-gray-700">← Back</button>
        <h1 className="text-2xl font-extrabold text-gray-900">New Health Record</h1>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-5">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Child *</label>
          <select value={form.childId} onChange={set('childId')} className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
            <option value="">Select child…</option>
            {children.map(c => <option key={c.id} value={c.id}>{c.firstName} {c.lastName}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Date *</label>
          <input type="date" value={form.measurementDate} onChange={set('measurementDate')} className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Weight (kg)</label>
            <input type="number" step="0.01" value={form.weightKg} onChange={set('weightKg')} placeholder="12.5" className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Height (cm)</label>
            <input type="number" step="0.1" value={form.heightCm} onChange={set('heightCm')} placeholder="90.0" className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
          </div>
        </div>
        {preview && <div className={`text-sm px-4 py-2.5 rounded-xl font-medium ${colorMap[preview.color]}`}>Nutritional Status: {preview.label}</div>}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Remarks</label>
          <textarea value={form.remarks} onChange={set('remarks')} rows={3} className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none" placeholder="Optional notes…" />
        </div>
        <div className="flex gap-3 pt-2">
          <button type="button" onClick={() => navigate(-1)} className="flex-1 border border-gray-200 text-gray-700 font-medium py-2.5 rounded-xl hover:bg-gray-50 transition-colors">Cancel</button>
          <button type="submit" disabled={loading} className="flex-1 bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold py-2.5 rounded-xl transition-colors">
            {loading ? 'Saving…' : 'Save Record'}
          </button>
        </div>
      </form>
    </div>
  )
}

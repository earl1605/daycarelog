import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { toLocalDateString } from '../utils/date'
import { handleCapitalizedNameInput } from '../utils/capitalizeFirstLetters'
import { PlusIcon, UsersIcon } from '../components/icons'
import Pagination from '../components/Pagination'
import { usePagination } from '../utils/usePagination'
import toast from 'react-hot-toast'

const BLOOD_TYPES = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-']

const emptyForm = {
  guardianUserId: '', firstName: '', lastName: '', dateOfBirth: '', sex: '', enrollmentStatus: 'active',
  bloodType: '', allergies: '', medicalConditions: '',
}

export default function Children() {
  const [children, setChildren] = useState([])
  const [guardians, setGuardians] = useState([])
  const [loading,  setLoading]  = useState(true)
  const [search,   setSearch]   = useState('')
  const [form,     setForm]     = useState(emptyForm)
  const [creating, setCreating] = useState(false)

  useEffect(() => { load() }, [])

  async function load() {
    setLoading(true)
    try {
      const [kids, accts] = await Promise.all([api.children.list(), api.guardians.listAccounts()])
      setChildren(kids)
      setGuardians(accts)
    } catch { toast.error('Failed to load children') }
    setLoading(false)
  }

  function set(field) { return e => setForm(f => ({ ...f, [field]: e.target.value })) }
  function setCapitalized(field) { return handleCapitalizedNameInput(v => setForm(f => ({ ...f, [field]: v }))) }

  async function handleCreate(e) {
    e.preventDefault()
    if (!form.firstName.trim() || !form.lastName.trim() || !form.dateOfBirth || !form.sex) {
      toast.error('Please fill in all required fields'); return
    }
    setCreating(true)
    try {
      const child = await api.children.create({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        dateOfBirth: form.dateOfBirth,
        sex: form.sex,
        enrollmentStatus: form.enrollmentStatus,
        enrollmentDate: toLocalDateString(),
        bloodType: form.bloodType,
        allergies: form.allergies.trim(),
        medicalConditions: form.medicalConditions.trim(),
      })
      if (form.guardianUserId) {
        const acct = guardians.find(g => String(g.userId) === form.guardianUserId)
        if (acct) {
          await api.guardians.add(child.id, {
            name: acct.name, email: acct.email, relationship: acct.relationship,
            contactNumber: acct.contactNumber, address: acct.address, createPortalAccount: true,
          })
        }
      }
      toast.success('Child enrolled')
      setForm(emptyForm)
      await load()
    } catch (e) { toast.error(e.message) }
    setCreating(false)
  }

  function guardianNamesFor(childId) {
    return guardians.filter(g => g.children.some(c => c.id === childId)).map(g => g.name).join(', ')
  }

  const filtered = children.filter(c => `${c.firstName} ${c.lastName}`.toLowerCase().includes(search.toLowerCase()))
  const { page, setPage, totalPages, paged } = usePagination(filtered)

  const inputClass = "w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
  const labelClass = "block text-xs font-medium text-gray-700 mb-1"

  return (
    <div className="space-y-5">
      <h1 className="text-[22px] font-bold text-gray-900">Children</h1>

      <div className="bg-white rounded-xl border border-gray-200/70 p-5 space-y-4">
        <h2 className="flex items-center gap-2 text-[15px] font-bold text-gray-900">
          <PlusIcon width={16} height={16} className="text-primary-600" /> Add a Child
        </h2>

        <form onSubmit={handleCreate} className="space-y-3">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
            <div>
              <label className={labelClass}>Guardian:</label>
              <select value={form.guardianUserId} onChange={set('guardianUserId')} className={`${inputClass} bg-white`}>
                <option value="">----------</option>
                {guardians.map(g => <option key={g.userId} value={g.userId}>{g.name}</option>)}
              </select>
            </div>
            <div>
              <label className={labelClass}>First name:</label>
              <input type="text" value={form.firstName} onChange={setCapitalized('firstName')} placeholder="Juan" autoCapitalize="words" className={inputClass} />
            </div>
            <div>
              <label className={labelClass}>Last name:</label>
              <input type="text" value={form.lastName} onChange={setCapitalized('lastName')} placeholder="Dela Cruz" autoCapitalize="words" className={inputClass} />
            </div>
            <div>
              <label className={labelClass}>Date of birth:</label>
              <input type="date" value={form.dateOfBirth} onChange={set('dateOfBirth')} className={inputClass} />
            </div>
            <div>
              <label className={labelClass}>Sex:</label>
              <select value={form.sex} onChange={set('sex')} className={`${inputClass} bg-white`}>
                <option value="">----------</option>
                <option>Male</option>
                <option>Female</option>
              </select>
            </div>
            <div>
              <label className={labelClass}>Status:</label>
              <select value={form.enrollmentStatus} onChange={set('enrollmentStatus')} className={`${inputClass} bg-white`}>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
              </select>
            </div>
            <div>
              <label className={labelClass}>Blood Type:</label>
              <select value={form.bloodType} onChange={set('bloodType')} className={`${inputClass} bg-white`}>
                <option value="">— Unknown —</option>
                {BLOOD_TYPES.map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className={labelClass}>Allergies:</label>
              <textarea value={form.allergies} onChange={set('allergies')} rows={2} placeholder="e.g. Peanuts, penicillin…" className={`${inputClass} resize-none`} />
            </div>
            <div>
              <label className={labelClass}>Medical Conditions:</label>
              <textarea value={form.medicalConditions} onChange={set('medicalConditions')} rows={2} placeholder="e.g. Asthma…" className={`${inputClass} resize-none`} />
            </div>
          </div>

          <button type="submit" disabled={creating}
            className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold text-sm py-2.5 rounded-lg transition-colors">
            {creating ? 'Adding…' : 'Add Child'}
          </button>
        </form>
      </div>

      <input type="text" placeholder="Search by name…" value={search} onChange={e => setSearch(e.target.value)}
        className="w-full border border-gray-200 rounded-lg px-4 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" />

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200/70 overflow-x-auto">
            <table className="w-full text-sm min-w-[720px]">
              <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
                <tr>
                  <th className="text-left px-4 py-2.5 font-medium">Name</th>
                  <th className="text-left px-4 py-2.5 font-medium">Date of Birth</th>
                  <th className="text-left px-4 py-2.5 font-medium">Sex</th>
                  <th className="text-left px-4 py-2.5 font-medium">Status</th>
                  <th className="text-left px-4 py-2.5 font-medium">Guardian</th>
                  <th className="text-left px-4 py-2.5 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="text-center py-16">
                      <span className="inline-flex w-12 h-12 rounded-full bg-gray-100 text-gray-400 items-center justify-center mb-3">
                        <UsersIcon width={22} height={22} />
                      </span>
                      <p className="font-medium text-gray-500">No children enrolled yet.</p>
                    </td>
                  </tr>
                ) : paged.map(c => (
                  <tr key={c.id} className="hover:bg-gray-50/60 transition-colors duration-150">
                    <td className="px-4 py-2.5 font-medium text-gray-900">{c.firstName} {c.lastName}</td>
                    <td className="px-4 py-2.5 text-gray-600">{new Date(c.dateOfBirth + 'T00:00:00').toLocaleDateString('en-PH')}</td>
                    <td className="px-4 py-2.5 text-gray-600">{c.sex}</td>
                    <td className="px-4 py-2.5">
                      <span className={`px-2 py-1 rounded-full text-xs font-semibold capitalize ${c.enrollmentStatus === 'active' ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                        {c.enrollmentStatus}
                      </span>
                    </td>
                    <td className="px-4 py-2.5 text-gray-600">{guardianNamesFor(c.id) || '—'}</td>
                    <td className="px-4 py-2.5">
                      <div className="flex flex-wrap gap-1.5">
                        <Link to={`/children/${c.id}`}
                          className="inline-flex items-center px-2.5 py-1 rounded-lg bg-gray-100 text-gray-600 text-xs font-semibold hover:bg-gray-200 transition-colors duration-150">
                          View
                        </Link>
                        <Link to={`/children/${c.id}/edit`}
                          className="inline-flex items-center px-2.5 py-1 rounded-lg bg-blue-50 text-blue-600 text-xs font-semibold hover:bg-blue-100 transition-colors duration-150">
                          Edit
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </div>
      )}
    </div>
  )
}

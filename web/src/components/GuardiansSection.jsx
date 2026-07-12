import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { PlusIcon, TrashIcon, KeyIcon, CopyIcon } from './icons'
import { handleCapitalizedNameInput } from '../utils/capitalizeFirstLetters'
import { validateEmailFormat, getEmailTypoSuggestion } from '../utils/emailValidation'
import toast from 'react-hot-toast'

const emptyForm = { name: '', relationship: '', contactNumber: '', isPrimary: false, createPortalAccount: false, email: '' }

export default function GuardiansSection({ childId }) {
  const [guardians,   setGuardians]   = useState([])
  const [loading,     setLoading]     = useState(true)
  const [showForm,    setShowForm]    = useState(false)
  const [form,        setForm]        = useState(emptyForm)
  const [saving,      setSaving]      = useState(false)
  const [deletingId,  setDeletingId]  = useState(null)
  const [tempPassword, setTempPassword] = useState(null)
  const [emailError,      setEmailError]      = useState('')
  const [emailSuggestion, setEmailSuggestion] = useState('')

  useEffect(() => {
    api.guardians.list(childId).then(setGuardians).catch(e => toast.error(e.message)).finally(() => setLoading(false))
  }, [childId])

  function set(field) { return e => setForm(f => ({ ...f, [field]: e.target.type === 'checkbox' ? e.target.checked : e.target.value })) }
  function setCapitalized(field) { return handleCapitalizedNameInput(v => setForm(f => ({ ...f, [field]: v }))) }

  function setEmailField(e) {
    setForm(f => ({ ...f, email: e.target.value }))
    setEmailError('')
    setEmailSuggestion('')
  }

  function handleEmailBlur() {
    const result = validateEmailFormat(form.email)
    setEmailError(result.valid ? '' : result.message)
    setEmailSuggestion(getEmailTypoSuggestion(form.email) || '')
  }

  function acceptEmailSuggestion() {
    setForm(f => ({ ...f, email: emailSuggestion }))
    setEmailSuggestion('')
    setEmailError('')
  }

  async function handleAdd(e) {
    e.preventDefault()
    if (!form.name.trim()) { toast.error('Guardian name is required'); return }
    if (form.createPortalAccount && !form.email.trim()) { toast.error('Email is required to create a portal account'); return }
    if (form.createPortalAccount) {
      const emailCheck = validateEmailFormat(form.email)
      if (!emailCheck.valid) { setEmailError(emailCheck.message); toast.error(emailCheck.message); return }
    }
    setSaving(true)
    try {
      const res = await api.guardians.add(childId, {
        name: form.name.trim(),
        relationship: form.relationship.trim(),
        contactNumber: form.contactNumber.trim(),
        isPrimary: form.isPrimary,
        createPortalAccount: form.createPortalAccount,
        email: form.email.trim(),
      })
      setGuardians(g => [...g, res.guardian])
      setShowForm(false)
      setForm(emptyForm)
      setEmailError('')
      setEmailSuggestion('')
      if (res.tempPassword) {
        setTempPassword({ name: res.guardian.name, password: res.tempPassword })
      } else {
        toast.success('Guardian added')
      }
    } catch (e) { toast.error(e.message) }
    setSaving(false)
  }

  async function handleDelete(id) {
    setDeletingId(id)
    try {
      await api.guardians.delete(childId, id)
      setGuardians(g => g.filter(x => x.id !== id))
      toast.success('Guardian removed')
    } catch (e) { toast.error(e.message) }
    setDeletingId(null)
  }

  function copyPassword() {
    navigator.clipboard?.writeText(tempPassword.password)
    toast.success('Copied to clipboard')
  }

  return (
    <div className="bg-white rounded-xl border border-gray-200/70 p-5 space-y-4">
      <div className="flex items-center justify-between gap-3">
        <div>
          <h2 className="text-[15px] font-bold text-gray-900">Guardians</h2>
          <p className="text-sm text-gray-500 mt-0.5">Contacts for this child, optionally with parent portal access.</p>
        </div>
        <button type="button" onClick={() => setShowForm(s => !s)}
          className="inline-flex items-center gap-1.5 bg-primary-600 hover:bg-primary-700 text-white text-sm font-semibold px-3.5 py-2 rounded-lg transition-colors duration-150 shrink-0">
          <PlusIcon width={15} height={15} /> Add Guardian
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-16"><div className="w-6 h-6 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : guardians.length === 0 && !showForm ? (
        <p className="text-sm text-gray-400 text-center py-4">No guardians added yet.</p>
      ) : (
        <div className="space-y-2">
          {guardians.map(g => (
            <div key={g.id} className="flex items-center justify-between gap-3 bg-[#FAFAFA] border border-gray-200/70 rounded-xl px-4 py-3">
              <div className="min-w-0">
                <p className="font-medium text-gray-900 truncate">
                  {g.name} {g.isPrimary && <span className="ml-1.5 text-xs font-medium text-primary-600 bg-primary-50 px-1.5 py-0.5 rounded">Primary</span>}
                </p>
                <p className="text-sm text-gray-500 truncate">
                  {[g.relationship, g.contactNumber].filter(Boolean).join(' · ') || '—'}
                  {g.userId && <span className="ml-1.5 text-xs text-blue-600">· Portal access</span>}
                </p>
              </div>
              <button type="button" onClick={() => handleDelete(g.id)} disabled={deletingId === g.id}
                className="p-2 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors duration-150 shrink-0" title="Remove">
                <TrashIcon width={16} height={16} />
              </button>
            </div>
          ))}
        </div>
      )}

      {showForm && (
        <form onSubmit={handleAdd} className="border border-gray-200/70 rounded-lg p-4 space-y-3">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Name *</label>
              <input type="text" value={form.name} onChange={setCapitalized('name')} placeholder="Maria Dela Cruz" autoCapitalize="words"
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Relationship</label>
              <input type="text" value={form.relationship} onChange={setCapitalized('relationship')} placeholder="Mother" autoCapitalize="words"
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Contact Number</label>
              <input type="text" value={form.contactNumber} onChange={set('contactNumber')} placeholder="09xxxxxxxxx"
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <label className="flex items-center gap-2 text-sm text-gray-700 pt-5">
              <input type="checkbox" checked={form.isPrimary} onChange={set('isPrimary')} className="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
              Primary guardian
            </label>
          </div>

          <label className="flex items-center gap-2 text-sm text-gray-700">
            <input type="checkbox" checked={form.createPortalAccount} onChange={set('createPortalAccount')} className="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
            Create a parent portal account for this guardian
          </label>
          {form.createPortalAccount && (
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Email *</label>
              <input type="email" value={form.email} onChange={setEmailField} onBlur={handleEmailBlur} placeholder="parent@example.com"
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
              {emailError && <p className="text-xs text-red-600 mt-1">{emailError}</p>}
              {emailSuggestion && (
                <button type="button" onClick={acceptEmailSuggestion}
                  className="text-xs text-primary-600 hover:underline mt-1">
                  Did you mean <span className="font-semibold">{emailSuggestion}</span>?
                </button>
              )}
              <p className="text-xs text-gray-400 mt-1">If this email already has a parent account, this child is linked to it instead of creating a new one.</p>
            </div>
          )}

          <div className="flex gap-3">
            <button type="button" onClick={() => { setShowForm(false); setForm(emptyForm); setEmailError(''); setEmailSuggestion('') }}
              className="flex-1 border border-gray-200 text-gray-700 text-sm font-medium py-2.5 rounded-lg hover:bg-gray-50 transition-colors">Cancel</button>
            <button type="submit" disabled={saving}
              className="flex-1 bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white text-sm font-semibold py-2.5 rounded-lg transition-colors">
              {saving ? 'Saving…' : 'Add Guardian'}
            </button>
          </div>
        </form>
      )}

      {tempPassword && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-sm p-6 animate-scale-in">
            <span className="mx-auto flex w-12 h-12 rounded-full bg-blue-50 text-blue-600 items-center justify-center mb-3">
              <KeyIcon width={22} height={22} />
            </span>
            <h2 className="text-[17px] font-bold text-gray-900 text-center mb-1">Temporary password</h2>
            <p className="text-sm text-gray-500 text-center mb-4">
              Parent portal account for <span className="font-semibold text-gray-700">{tempPassword.name}</span>. This is shown only once — copy it now and share it securely.
            </p>
            <div className="flex items-center gap-2 mb-5">
              <code className="flex-1 bg-[#FAFAFA] border border-gray-200 rounded-lg px-3 py-2.5 text-sm font-mono text-gray-800 text-center select-all">
                {tempPassword.password}
              </code>
              <button onClick={copyPassword}
                className="p-2.5 rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 transition-colors duration-150" title="Copy">
                <CopyIcon width={16} height={16} />
              </button>
            </div>
            <button onClick={() => setTempPassword(null)}
              className="w-full py-2.5 rounded-lg bg-primary-600 hover:bg-primary-700 text-white text-sm font-semibold transition-colors duration-150">
              Done
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

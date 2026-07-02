import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { PlusIcon, KeyIcon, CopyIcon, TrashIcon, AlertTriangleIcon, UsersIcon } from '../components/icons'
import { handleCapitalizedNameInput } from '../utils/capitalizeFirstLetters'
import toast from 'react-hot-toast'

const emptyForm = {
  firstName: '', lastName: '', middleName: '', suffix: '',
  email: '', contactNumber: '', address: '', relationship: '', childId: '',
}

export default function Guardians() {
  const [accounts,  setAccounts]  = useState([])
  const [children,  setChildren]  = useState([])
  const [loading,   setLoading]   = useState(true)
  const [search,    setSearch]    = useState('')
  const [form,      setForm]      = useState(emptyForm)
  const [creating,  setCreating]  = useState(false)
  const [resettingId, setResettingId] = useState(null)
  const [confirmUserId, setConfirmUserId] = useState(null)
  const [removing,  setRemoving]  = useState(null)
  const [tempPassword, setTempPassword] = useState(null) // { name, password }

  useEffect(() => { load() }, [])

  async function load() {
    setLoading(true)
    try {
      const [accts, kids] = await Promise.all([api.guardians.listAccounts(), api.children.list()])
      setAccounts(accts)
      setChildren(kids.filter(c => c.enrollmentStatus === 'active'))
    } catch (e) { toast.error(e.message) }
    setLoading(false)
  }

  function set(field) { return e => setForm(f => ({ ...f, [field]: e.target.value })) }
  function setCapitalized(field) { return handleCapitalizedNameInput(v => setForm(f => ({ ...f, [field]: v }))) }

  async function handleCreate(e) {
    e.preventDefault()
    if (!form.firstName.trim() || !form.lastName.trim()) { toast.error('First and last name are required'); return }
    if (!form.email.trim()) { toast.error('Email is required'); return }
    if (!form.childId) { toast.error('Please select a child'); return }

    const name = [form.firstName, form.middleName, form.lastName, form.suffix].map(s => s.trim()).filter(Boolean).join(' ')
    setCreating(true)
    try {
      const res = await api.guardians.add(form.childId, {
        name,
        relationship: form.relationship.trim(),
        contactNumber: form.contactNumber.trim(),
        address: form.address.trim(),
        email: form.email.trim(),
        createPortalAccount: true,
      })
      setForm(emptyForm)
      await load()
      if (res.tempPassword) setTempPassword({ name, password: res.tempPassword })
      else toast.success('Child linked to existing guardian account')
    } catch (e) { toast.error(e.message) }
    setCreating(false)
  }

  async function handleResetPassword(account) {
    setResettingId(account.userId)
    try {
      const res = await api.users.resetPassword(account.userId)
      setTempPassword({ name: account.name, password: res.tempPassword })
    } catch (e) { toast.error(e.message) }
    setResettingId(null)
  }

  async function handleRemove(userId) {
    setConfirmUserId(null)
    setRemoving(userId)
    try {
      await api.guardians.removeAccount(userId)
      toast.success('Guardian account removed')
      setAccounts(a => a.filter(x => x.userId !== userId))
    } catch (e) { toast.error(e.message) }
    setRemoving(null)
  }

  function copyPassword() {
    navigator.clipboard?.writeText(tempPassword.password)
    toast.success('Copied to clipboard')
  }

  const filtered = accounts.filter(a => a.name?.toLowerCase().includes(search.toLowerCase()))
  const confirmTarget = accounts.find(a => a.userId === confirmUserId)

  return (
    <div className="space-y-6">
      <h1 className="text-[22px] font-bold text-gray-900">Guardians</h1>

      <div className="bg-white rounded-xl border border-gray-200/70 p-6 space-y-5">
        <div>
          <h2 className="flex items-center gap-2 text-[17px] font-bold text-gray-900">
            <PlusIcon width={18} height={18} className="text-primary-600" /> Create a Guardian Account
          </h2>
          <p className="text-sm text-gray-500 mt-1">
            Creates a new Parent/Guardian login and generates a temporary password to share with them. Public self-registration can never create a Parent/Guardian account.
          </p>
        </div>

        <form onSubmit={handleCreate} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">First name:</label>
              <input type="text" value={form.firstName} onChange={setCapitalized('firstName')} autoCapitalize="words"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Last name:</label>
              <input type="text" value={form.lastName} onChange={setCapitalized('lastName')} autoCapitalize="words"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Middle name:</label>
              <input type="text" value={form.middleName} onChange={setCapitalized('middleName')} autoCapitalize="words"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Suffix:</label>
              <input type="text" value={form.suffix} onChange={setCapitalized('suffix')} autoCapitalize="words"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Email:</label>
              <input type="email" value={form.email} onChange={set('email')}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Contact number:</label>
              <input type="text" value={form.contactNumber} onChange={set('contactNumber')}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Address:</label>
              <input type="text" value={form.address} onChange={setCapitalized('address')} autoCapitalize="words"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Relationship to child:</label>
              <input type="text" value={form.relationship} onChange={setCapitalized('relationship')} placeholder="Mother, Father, Guardian…" autoCapitalize="words"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Child:</label>
              <select value={form.childId} onChange={set('childId')}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-primary-500">
                <option value="">Select a child…</option>
                {children.map(c => <option key={c.id} value={c.id}>{c.firstName} {c.lastName}</option>)}
              </select>
            </div>
          </div>

          <button type="submit" disabled={creating}
            className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-colors">
            {creating ? 'Creating…' : 'Create Guardian Account'}
          </button>
        </form>
      </div>

      <input type="text" placeholder="Search by name…" value={search} onChange={e => setSearch(e.target.value)}
        className="w-full border border-gray-200 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" />

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200/70 overflow-x-auto">
          {filtered.length === 0 ? (
            <div className="text-center py-16">
              <span className="inline-flex w-12 h-12 rounded-full bg-gray-100 text-gray-400 items-center justify-center mb-3">
                <UsersIcon width={22} height={22} />
              </span>
              <p className="font-medium text-gray-500">No guardian accounts found</p>
            </div>
          ) : (
            <table className="w-full text-sm min-w-[720px]">
              <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
                <tr>
                  <th className="text-left px-4 py-3 font-medium">Name</th>
                  <th className="text-left px-4 py-3 font-medium">Email</th>
                  <th className="text-left px-4 py-3 font-medium">Contact Number</th>
                  <th className="text-left px-4 py-3 font-medium">Address</th>
                  <th className="text-left px-4 py-3 font-medium">Children</th>
                  <th className="text-left px-4 py-3 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.map(a => (
                  <tr key={a.userId} className="hover:bg-gray-50/60 transition-colors duration-150">
                    <td className="px-4 py-3 font-medium text-gray-900">{a.name}</td>
                    <td className="px-4 py-3 text-gray-500">{a.email}</td>
                    <td className="px-4 py-3 text-gray-600">{a.contactNumber || '—'}</td>
                    <td className="px-4 py-3 text-gray-600">{a.address || '—'}</td>
                    <td className="px-4 py-3 text-gray-600">{a.children.map(c => `${c.firstName} ${c.lastName}`).join(', ') || '—'}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-1.5">
                        <button
                          onClick={() => handleResetPassword(a)}
                          disabled={resettingId === a.userId}
                          className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-blue-50 text-blue-600 text-xs font-semibold hover:bg-blue-100 transition-colors duration-150 disabled:opacity-50"
                        >
                          {resettingId === a.userId
                            ? <span className="w-3 h-3 border-2 border-blue-400 border-t-transparent rounded-full animate-spin" />
                            : <KeyIcon width={13} height={13} />}
                          Reset
                        </button>
                        <button
                          onClick={() => setConfirmUserId(a.userId)}
                          disabled={removing === a.userId}
                          className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-red-50 text-red-600 text-xs font-semibold hover:bg-red-100 transition-colors duration-150 disabled:opacity-50"
                        >
                          {removing === a.userId
                            ? <span className="w-3 h-3 border-2 border-red-400 border-t-transparent rounded-full animate-spin" />
                            : <TrashIcon width={13} height={13} />}
                          Remove
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* One-time temp password modal */}
      {tempPassword && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-sm p-6 animate-scale-in">
            <span className="mx-auto flex w-12 h-12 rounded-full bg-blue-50 text-blue-600 items-center justify-center mb-3">
              <KeyIcon width={22} height={22} />
            </span>
            <h2 className="text-[17px] font-bold text-gray-900 text-center mb-1">Temporary password</h2>
            <p className="text-sm text-gray-500 text-center mb-4">
              For <span className="font-semibold text-gray-700">{tempPassword.name}</span>. This is shown only once — copy it now and share it securely.
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

      {/* Confirm remove modal */}
      {confirmUserId && confirmTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-sm p-6 animate-scale-in">
            <span className="mx-auto flex w-12 h-12 rounded-full bg-red-50 text-red-600 items-center justify-center mb-3">
              <AlertTriangleIcon width={22} height={22} />
            </span>
            <h2 className="text-[17px] font-bold text-gray-900 text-center mb-1">Remove guardian account?</h2>
            <p className="text-sm text-gray-500 text-center mb-5">
              <span className="font-semibold text-gray-700">{confirmTarget.name}</span> will lose access to all linked children's records. Their login is not deleted and can still be managed from Users.
            </p>
            <div className="flex gap-3">
              <button onClick={() => setConfirmUserId(null)}
                className="flex-1 py-2.5 rounded-lg border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors duration-150">
                Cancel
              </button>
              <button onClick={() => handleRemove(confirmUserId)}
                className="flex-1 py-2.5 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-semibold transition-colors duration-150">
                Yes, Remove
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

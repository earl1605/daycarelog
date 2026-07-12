import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { PlusIcon, KeyIcon, PauseIcon, PlayIcon, TrashIcon, AlertTriangleIcon, CopyIcon } from '../components/icons'
import Pagination from '../components/Pagination'
import { usePagination } from '../utils/usePagination'
import { handleCapitalizedNameInput } from '../utils/capitalizeFirstLetters'
import toast from 'react-hot-toast'

const ROLES = ['admin', 'staff']

const emptyForm = { email: '', firstName: '', lastName: '', middleName: '', suffix: '', role: 'staff' }

export default function Users() {
  const { isAdmin, user } = useAuth()
  const [users,      setUsers]      = useState([])
  const [loading,    setLoading]    = useState(true)
  const [saving,     setSaving]     = useState(null)
  const [deleting,   setDeleting]   = useState(null)
  const [confirmId,  setConfirmId]  = useState(null)
  const [togglingId, setTogglingId] = useState(null)
  const [resettingId, setResettingId] = useState(null)

  const [showCreate, setShowCreate] = useState(false)
  const [creating,   setCreating]   = useState(false)
  const [form,       setForm]       = useState(emptyForm)

  const [tempPassword, setTempPassword] = useState(null)

  useEffect(() => {
    if (!isAdmin) { setLoading(false); return }
    api.users.list().then(setUsers).catch(e => toast.error(e.message)).finally(() => setLoading(false))
  }, [isAdmin])

  async function changeRole(id, role) {
    setSaving(id)
    try {
      await api.users.updateRole(id, role)
      toast.success('Role updated')
      setUsers(u => u.map(p => p.id === id ? { ...p, role } : p))
    } catch (e) { toast.error(e.message) }
    setSaving(null)
  }

  async function toggleActive(target) {
    setTogglingId(target.id)
    try {
      if (target.isActive) {
        await api.users.deactivate(target.id)
        toast.success('Account deactivated')
      } else {
        await api.users.reactivate(target.id)
        toast.success('Account reactivated')
      }
      setUsers(u => u.map(p => p.id === target.id ? { ...p, isActive: !target.isActive } : p))
    } catch (e) { toast.error(e.message) }
    setTogglingId(null)
  }

  async function handleResetPassword(target) {
    setResettingId(target.id)
    try {
      const res = await api.users.resetPassword(target.id)
      setTempPassword({ name: target.fullName || target.email, password: res.tempPassword })
    } catch (e) { toast.error(e.message) }
    setResettingId(null)
  }

  async function handleDelete(id) {
    setConfirmId(null)
    setDeleting(id)
    try {
      await api.users.delete(id)
      toast.success('User deleted')
      setUsers(u => u.filter(p => p.id !== id))
    } catch (e) { toast.error(e.message) }
    setDeleting(null)
  }

  async function handleCreate(e) {
    e.preventDefault()
    if (!form.email.trim() || !form.firstName.trim() || !form.lastName.trim()) {
      toast.error('Email, first name, and last name are required'); return
    }
    setCreating(true)
    try {
      const res = await api.users.create({
        email: form.email.trim(),
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        middleName: form.middleName.trim(),
        suffix: form.suffix.trim(),
        role: form.role,
      })
      setUsers(u => [...u, res.user])
      setShowCreate(false)
      setForm(emptyForm)
      setTempPassword({ name: res.user.fullName || res.user.email, password: res.tempPassword })
    } catch (e) { toast.error(e.message) }
    setCreating(false)
  }

  function copyPassword() {
    navigator.clipboard?.writeText(tempPassword.password)
    toast.success('Copied to clipboard')
  }

  if (!isAdmin) return <div className="text-center py-20 text-gray-400">Admin access required.</div>

  const { page, setPage, totalPages, paged } = usePagination(users)
  const confirmTarget = users.find(u => u.id === confirmId)

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-[22px] font-bold text-gray-900">Users</h1>
        <button
          onClick={() => setShowCreate(true)}
          className="inline-flex items-center gap-1.5 bg-primary-600 hover:bg-primary-700 text-white text-sm font-semibold px-4 py-2.5 rounded-lg transition-colors duration-150"
        >
          <PlusIcon width={16} height={16} /> Add Staff
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48">
          <div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200/70 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
              <tr>
                <th className="text-left px-4 py-2.5 font-medium">Name</th>
                <th className="text-left px-4 py-2.5 font-medium hidden sm:table-cell">Email</th>
                <th className="text-left px-4 py-2.5 font-medium">Role</th>
                <th className="text-left px-4 py-2.5 font-medium">Status</th>
                <th className="text-left px-4 py-2.5 font-medium hidden md:table-cell">Joined</th>
                <th className="text-left px-4 py-2.5 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {paged.map(u => {
                const isSelf = u.id === user?.id
                return (
                  <tr key={u.id} className="hover:bg-gray-50/60 transition-colors duration-150">
                    <td className="px-4 py-2.5 font-medium text-gray-900">
                      {u.fullName || u.email.split('@')[0]}
                      {isSelf && <span className="ml-2 text-xs text-primary-700 font-normal">(you)</span>}
                    </td>
                    <td className="px-4 py-2.5 text-gray-500 hidden sm:table-cell">{u.email}</td>
                    <td className="px-4 py-2.5">
                      <select
                        value={u.role}
                        disabled={isSelf || saving === u.id}
                        onChange={e => changeRole(u.id, e.target.value)}
                        className="border border-gray-200 rounded-lg px-2 py-1 text-xs bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400 disabled:opacity-50"
                      >
                        {ROLES.map(r => <option key={r}>{r}</option>)}
                      </select>
                    </td>
                    <td className="px-4 py-2.5">
                      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${u.isActive ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-600'}`}>
                        {u.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-4 py-2.5 text-gray-400 hidden md:table-cell">{new Date(u.createdAt).toLocaleDateString('en-PH')}</td>
                    <td className="px-4 py-2.5">
                      <div className="flex flex-wrap gap-1.5">
                        <button
                          onClick={() => handleResetPassword(u)}
                          disabled={resettingId === u.id}
                          className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-blue-50 text-blue-600 text-xs font-semibold hover:bg-blue-100 transition-colors duration-150 disabled:opacity-50"
                        >
                          {resettingId === u.id
                            ? <span className="w-3 h-3 border-2 border-blue-400 border-t-transparent rounded-full animate-spin" />
                            : <KeyIcon width={13} height={13} />}
                          Reset
                        </button>
                        {!isSelf && (
                          <button
                            onClick={() => toggleActive(u)}
                            disabled={togglingId === u.id}
                            className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-semibold transition-colors duration-150 disabled:opacity-50 ${
                              u.isActive ? 'bg-amber-50 text-amber-600 hover:bg-amber-100' : 'bg-green-50 text-green-600 hover:bg-green-100'
                            }`}
                          >
                            {togglingId === u.id
                              ? <span className="w-3 h-3 border-2 border-current border-t-transparent rounded-full animate-spin" />
                              : (u.isActive ? <PauseIcon width={13} height={13} /> : <PlayIcon width={13} height={13} />)}
                            {u.isActive ? 'Deactivate' : 'Reactivate'}
                          </button>
                        )}
                        {!isSelf && (
                          <button
                            onClick={() => setConfirmId(u.id)}
                            disabled={deleting === u.id}
                            className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-red-50 text-red-600 text-xs font-semibold hover:bg-red-100 transition-colors duration-150 disabled:opacity-50"
                          >
                            {deleting === u.id
                              ? <span className="w-3 h-3 border-2 border-red-400 border-t-transparent rounded-full animate-spin" />
                              : <TrashIcon width={13} height={13} />}
                            Delete
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </div>
      )}

      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <form onSubmit={handleCreate} className="bg-white rounded-xl shadow-2xl w-full max-w-sm p-6 animate-scale-in space-y-3">
            <h2 className="text-[17px] font-bold text-gray-900">Add staff account</h2>
            <p className="text-xs text-gray-500 -mt-2">A temporary password will be generated and shown once.</p>
            <div className="grid grid-cols-2 gap-2">
              <input value={form.firstName} onChange={handleCapitalizedNameInput(v => setForm(f => ({ ...f, firstName: v })))}
                placeholder="First name" className="border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" autoCapitalize="words" />
              <input value={form.lastName} onChange={handleCapitalizedNameInput(v => setForm(f => ({ ...f, lastName: v })))}
                placeholder="Last name" className="border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" autoCapitalize="words" />
            </div>
            <div className="grid grid-cols-2 gap-2">
              <input value={form.middleName} onChange={handleCapitalizedNameInput(v => setForm(f => ({ ...f, middleName: v })))}
                placeholder="Middle name" className="border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" autoCapitalize="words" />
              <input value={form.suffix} onChange={handleCapitalizedNameInput(v => setForm(f => ({ ...f, suffix: v })))}
                placeholder="Suffix" className="border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" autoCapitalize="words" />
            </div>
            <input type="email" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
              placeholder="Email address" className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" />
            <select value={form.role} onChange={e => setForm(f => ({ ...f, role: e.target.value }))}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-900 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400">
              {ROLES.map(r => <option key={r}>{r}</option>)}
            </select>
            <div className="flex gap-3 pt-1">
              <button type="button" onClick={() => { setShowCreate(false); setForm(emptyForm) }}
                className="flex-1 py-2.5 rounded-lg border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors duration-150">
                Cancel
              </button>
              <button type="submit" disabled={creating}
                className="flex-1 py-2.5 rounded-lg bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white text-sm font-semibold transition-colors duration-150">
                {creating ? 'Creating…' : 'Create account'}
              </button>
            </div>
          </form>
        </div>
      )}

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

      {confirmId && confirmTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-sm p-6 animate-scale-in">
            <span className="mx-auto flex w-12 h-12 rounded-full bg-red-50 text-red-600 items-center justify-center mb-3">
              <AlertTriangleIcon width={22} height={22} />
            </span>
            <h2 className="text-[17px] font-bold text-gray-900 text-center mb-1">Delete account?</h2>
            <p className="text-sm text-gray-500 text-center mb-5">
              <span className="font-semibold text-gray-700">{confirmTarget.fullName || confirmTarget.email}</span> will be permanently removed and cannot be recovered.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setConfirmId(null)}
                className="flex-1 py-2.5 rounded-lg border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors duration-150"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDelete(confirmId)}
                className="flex-1 py-2.5 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-semibold transition-colors duration-150"
              >
                Yes, Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

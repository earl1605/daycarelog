import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
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

  const [tempPassword, setTempPassword] = useState(null) // { name, password }

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

  const confirmTarget = users.find(u => u.id === confirmId)

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-gray-900">Users</h1>
        <button
          onClick={() => setShowCreate(true)}
          className="bg-primary-600 hover:bg-primary-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition-colors"
        >
          + Add Staff
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48">
          <div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>
                <th className="text-left px-4 py-3">Name</th>
                <th className="text-left px-4 py-3 hidden sm:table-cell">Email</th>
                <th className="text-left px-4 py-3">Role</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="text-left px-4 py-3 hidden md:table-cell">Joined</th>
                <th className="text-left px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {users.map(u => {
                const isSelf = u.id === user?.id
                return (
                  <tr key={u.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-4 py-3 font-medium text-gray-900">
                      {u.fullName || u.email.split('@')[0]}
                      {isSelf && <span className="ml-2 text-xs text-primary-600 font-normal">(you)</span>}
                    </td>
                    <td className="px-4 py-3 text-gray-500 hidden sm:table-cell">{u.email}</td>
                    <td className="px-4 py-3">
                      <select
                        value={u.role}
                        disabled={isSelf || saving === u.id}
                        onChange={e => changeRole(u.id, e.target.value)}
                        className="border border-gray-200 rounded-lg px-2 py-1 text-xs bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 disabled:opacity-50"
                      >
                        {ROLES.map(r => <option key={r}>{r}</option>)}
                      </select>
                    </td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${u.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'}`}>
                        {u.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-400 hidden md:table-cell">{new Date(u.createdAt).toLocaleDateString('en-PH')}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-1.5">
                        <button
                          onClick={() => handleResetPassword(u)}
                          disabled={resettingId === u.id}
                          className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-blue-50 text-blue-600 text-xs font-semibold hover:bg-blue-100 transition-colors disabled:opacity-50"
                        >
                          {resettingId === u.id
                            ? <span className="w-3 h-3 border-2 border-blue-400 border-t-transparent rounded-full animate-spin" />
                            : '🔑'}
                          Reset
                        </button>
                        {!isSelf && (
                          <button
                            onClick={() => toggleActive(u)}
                            disabled={togglingId === u.id}
                            className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-semibold transition-colors disabled:opacity-50 ${
                              u.isActive ? 'bg-amber-50 text-amber-600 hover:bg-amber-100' : 'bg-green-50 text-green-600 hover:bg-green-100'
                            }`}
                          >
                            {togglingId === u.id
                              ? <span className="w-3 h-3 border-2 border-current border-t-transparent rounded-full animate-spin" />
                              : (u.isActive ? '⏸' : '▶')}
                            {u.isActive ? 'Deactivate' : 'Reactivate'}
                          </button>
                        )}
                        {!isSelf && (
                          <button
                            onClick={() => setConfirmId(u.id)}
                            disabled={deleting === u.id}
                            className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-red-50 text-red-600 text-xs font-semibold hover:bg-red-100 transition-colors disabled:opacity-50"
                          >
                            {deleting === u.id
                              ? <span className="w-3 h-3 border-2 border-red-400 border-t-transparent rounded-full animate-spin" />
                              : '🗑'}
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
        </div>
      )}

      {/* Add staff modal */}
      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <form onSubmit={handleCreate} className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 animate-scale-in space-y-3">
            <h2 className="text-lg font-extrabold text-gray-900">Add staff account</h2>
            <p className="text-xs text-gray-500 -mt-2">A temporary password will be generated and shown once.</p>
            <div className="grid grid-cols-2 gap-2">
              <input value={form.firstName} onChange={e => setForm(f => ({ ...f, firstName: e.target.value }))}
                placeholder="First name" className="border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
              <input value={form.lastName} onChange={e => setForm(f => ({ ...f, lastName: e.target.value }))}
                placeholder="Last name" className="border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div className="grid grid-cols-2 gap-2">
              <input value={form.middleName} onChange={e => setForm(f => ({ ...f, middleName: e.target.value }))}
                placeholder="Middle name" className="border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
              <input value={form.suffix} onChange={e => setForm(f => ({ ...f, suffix: e.target.value }))}
                placeholder="Suffix" className="border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <input type="email" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
              placeholder="Email address" className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            <select value={form.role} onChange={e => setForm(f => ({ ...f, role: e.target.value }))}
              className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-primary-500">
              {ROLES.map(r => <option key={r}>{r}</option>)}
            </select>
            <div className="flex gap-3 pt-1">
              <button type="button" onClick={() => { setShowCreate(false); setForm(emptyForm) }}
                className="flex-1 py-2.5 rounded-xl border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors">
                Cancel
              </button>
              <button type="submit" disabled={creating}
                className="flex-1 py-2.5 rounded-xl bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white text-sm font-semibold transition-colors">
                {creating ? 'Creating…' : 'Create account'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* One-time temp password modal */}
      {tempPassword && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 animate-scale-in">
            <div className="text-4xl text-center mb-3">🔑</div>
            <h2 className="text-lg font-extrabold text-gray-900 text-center mb-1">Temporary password</h2>
            <p className="text-sm text-gray-500 text-center mb-4">
              For <span className="font-semibold text-gray-700">{tempPassword.name}</span>. This is shown only once — copy it now and share it securely.
            </p>
            <div className="flex items-center gap-2 mb-5">
              <code className="flex-1 bg-gray-50 border border-gray-200 rounded-xl px-3 py-2.5 text-sm font-mono text-gray-800 text-center select-all">
                {tempPassword.password}
              </code>
              <button onClick={copyPassword}
                className="px-3 py-2.5 rounded-xl border border-gray-200 text-sm hover:bg-gray-50 transition-colors" title="Copy">
                📋
              </button>
            </div>
            <button onClick={() => setTempPassword(null)}
              className="w-full py-2.5 rounded-xl bg-primary-600 hover:bg-primary-700 text-white text-sm font-semibold transition-colors">
              Done
            </button>
          </div>
        </div>
      )}

      {/* Confirm delete modal */}
      {confirmId && confirmTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 animate-scale-in">
            <div className="text-4xl text-center mb-3">⚠️</div>
            <h2 className="text-lg font-extrabold text-gray-900 text-center mb-1">Delete account?</h2>
            <p className="text-sm text-gray-500 text-center mb-5">
              <span className="font-semibold text-gray-700">{confirmTarget.fullName || confirmTarget.email}</span> will be permanently removed and cannot be recovered.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setConfirmId(null)}
                className="flex-1 py-2.5 rounded-xl border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDelete(confirmId)}
                className="flex-1 py-2.5 rounded-xl bg-red-600 hover:bg-red-700 text-white text-sm font-semibold transition-colors"
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

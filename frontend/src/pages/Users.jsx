import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import toast from 'react-hot-toast'

const ROLES = ['admin', 'teacher', 'staff']

const ROLE_BADGE = {
  admin:   'bg-purple-100 text-purple-700',
  teacher: 'bg-blue-100 text-blue-700',
  staff:   'bg-gray-100 text-gray-600',
}

export default function Users() {
  const { isAdmin, user } = useAuth()
  const [users,      setUsers]      = useState([])
  const [loading,    setLoading]    = useState(true)
  const [saving,     setSaving]     = useState(null)
  const [deleting,   setDeleting]   = useState(null)
  const [confirmId,  setConfirmId]  = useState(null)

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

  if (!isAdmin) return <div className="text-center py-20 text-gray-400">Admin access required.</div>

  const confirmTarget = users.find(u => u.id === confirmId)

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-extrabold text-gray-900">Users</h1>

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
                    <td className="px-4 py-3 text-gray-400 hidden md:table-cell">{new Date(u.createdAt).toLocaleDateString('en-PH')}</td>
                    <td className="px-4 py-3">
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
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
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

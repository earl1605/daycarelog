import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import toast from 'react-hot-toast'

const ROLES = ['admin', 'teacher', 'staff']

export default function Users() {
  const { isAdmin, user } = useAuth()
  const [users,   setUsers]   = useState([])
  const [loading, setLoading] = useState(true)
  const [saving,  setSaving]  = useState(null)

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

  if (!isAdmin) return <div className="text-center py-20 text-gray-400">Admin access required.</div>

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-extrabold text-gray-900">Users</h1>

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-x-auto">
          <table className="w-full text-sm min-w-[480px]">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>
                <th className="text-left px-4 py-3">Name</th>
                <th className="text-left px-4 py-3">Email</th>
                <th className="text-left px-4 py-3">Role</th>
                <th className="text-left px-4 py-3">Joined</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {users.map(u => (
                <tr key={u.id}>
                  <td className="px-4 py-3 font-medium text-gray-900">
                    {u.fullName || u.email.split('@')[0]}
                    {u.id === user?.id && <span className="ml-2 text-xs text-primary-600 font-normal">(you)</span>}
                  </td>
                  <td className="px-4 py-3 text-gray-500">{u.email}</td>
                  <td className="px-4 py-3">
                    <select value={u.role} disabled={u.id === user?.id || saving === u.id} onChange={e => changeRole(u.id, e.target.value)}
                      className="border border-gray-200 rounded-lg px-2 py-1 text-xs bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 disabled:opacity-50">
                      {ROLES.map(r => <option key={r}>{r}</option>)}
                    </select>
                  </td>
                  <td className="px-4 py-3 text-gray-400">{new Date(u.createdAt).toLocaleDateString('en-PH')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

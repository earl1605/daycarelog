import { useState } from 'react'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import toast from 'react-hot-toast'

export default function Settings() {
  const { user, refreshUser } = useAuth()
  const [fullName,  setFullName]  = useState(user?.fullName ?? '')
  const [curPass,   setCurPass]   = useState('')
  const [newPass,   setNewPass]   = useState('')
  const [confirm,   setConfirm]   = useState('')
  const [saving,    setSaving]    = useState(false)

  async function saveName(e) {
    e.preventDefault()
    if (!fullName.trim()) { toast.error('Name cannot be empty'); return }
    setSaving(true)
    try {
      await api.users.updateProfile(user.id, { fullName })
      refreshUser({ fullName })
      toast.success('Name updated')
    } catch (e) { toast.error(e.message) }
    setSaving(false)
  }

  async function savePassword(e) {
    e.preventDefault()
    if (newPass.length < 6) { toast.error('Password must be at least 6 characters'); return }
    if (newPass !== confirm) { toast.error('Passwords do not match'); return }
    setSaving(true)
    try {
      await api.users.updateProfile(user.id, { currentPassword: curPass, newPassword: newPass })
      toast.success('Password changed')
      setCurPass(''); setNewPass(''); setConfirm('')
    } catch (e) { toast.error(e.message) }
    setSaving(false)
  }

  return (
    <div className="max-w-lg mx-auto space-y-6">
      <h1 className="text-2xl font-extrabold text-gray-900">Settings</h1>

      <form onSubmit={saveName} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-4">
        <h2 className="font-bold text-gray-900">Profile</h2>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Full Name</label>
          <input type="text" value={fullName} onChange={e => setFullName(e.target.value)}
            className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
          <input type="email" value={user?.email ?? ''} disabled className="w-full border border-gray-100 bg-gray-50 rounded-xl px-4 py-2.5 text-sm text-gray-400" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Role</label>
          <input type="text" value={user?.role ?? ''} disabled className="w-full border border-gray-100 bg-gray-50 rounded-xl px-4 py-2.5 text-sm text-gray-400 capitalize" />
        </div>
        <button type="submit" disabled={saving} className="bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold px-5 py-2.5 rounded-xl transition-colors text-sm">
          {saving ? 'Saving…' : 'Save Changes'}
        </button>
      </form>

      <form onSubmit={savePassword} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-4">
        <h2 className="font-bold text-gray-900">Change Password</h2>
        {[['Current Password', curPass, setCurPass], ['New Password', newPass, setNewPass], ['Confirm New Password', confirm, setConfirm]].map(([label, val, setter]) => (
          <div key={label}>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
            <input type="password" value={val} onChange={e => setter(e.target.value)}
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
          </div>
        ))}
        <button type="submit" disabled={saving} className="bg-gray-800 hover:bg-gray-900 disabled:opacity-60 text-white font-semibold px-5 py-2.5 rounded-xl transition-colors text-sm">
          {saving ? 'Saving…' : 'Update Password'}
        </button>
      </form>
    </div>
  )
}

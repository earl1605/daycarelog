import { useRef, useState } from 'react'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { handleCapitalizedNameInput } from '../utils/capitalizeFirstLetters'
import toast from 'react-hot-toast'

function resizeImage(file, maxSize = 256) {
  return new Promise(resolve => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      const scale = Math.min(maxSize / img.width, maxSize / img.height, 1)
      const canvas = document.createElement('canvas')
      canvas.width  = img.width  * scale
      canvas.height = img.height * scale
      canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height)
      URL.revokeObjectURL(url)
      resolve(canvas.toDataURL('image/jpeg', 0.85))
    }
    img.src = url
  })
}

export default function Settings() {
  const { user, refreshUser } = useAuth()
  const [firstName,  setFirstName]  = useState(user?.firstName  ?? '')
  const [lastName,   setLastName]   = useState(user?.lastName   ?? '')
  const [middleName, setMiddleName] = useState(user?.middleName ?? '')
  const [suffix,     setSuffix]     = useState(user?.suffix     ?? '')
  const [curPass,    setCurPass]    = useState('')
  const [newPass,    setNewPass]    = useState('')
  const [confirm,    setConfirm]    = useState('')
  const [saving,     setSaving]     = useState(false)
  const [preview,    setPreview]    = useState(user?.profilePhoto ?? null)
  const fileRef = useRef()

  const initial = (firstName?.[0] ?? user?.email?.[0] ?? 'U').toUpperCase()

  async function handlePhoto(e) {
    const file = e.target.files?.[0]
    if (!file) return
    const base64 = await resizeImage(file)
    setPreview(base64)
    setSaving(true)
    try {
      await api.users.updateProfile(user.id, { profilePhoto: base64 })
      refreshUser({ profilePhoto: base64 })
      toast.success('Photo updated')
    } catch (err) { toast.error(err.message) }
    setSaving(false)
  }

  async function saveName(e) {
    e.preventDefault()
    if (!firstName.trim() || !lastName.trim()) { toast.error('First and last name are required'); return }
    setSaving(true)
    try {
      await api.users.updateProfile(user.id, { firstName, lastName, middleName, suffix })
      refreshUser({ firstName, lastName, middleName, suffix })
      toast.success('Profile updated')
    } catch (err) { toast.error(err.message) }
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
    } catch (err) { toast.error(err.message) }
    setSaving(false)
  }

  const inputClass = "w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
  const disabledClass = "w-full border border-gray-100 bg-gray-50 rounded-xl px-4 py-2.5 text-sm text-gray-400"
  const btnClass = "bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold px-5 py-2.5 rounded-xl transition-colors text-sm"

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-2xl font-extrabold text-gray-900 mb-6">Settings</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Profile */}
        <form onSubmit={saveName} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-4 transition-all duration-200 hover:shadow-md hover:border-primary-200">
          <h2 className="font-bold text-gray-900">Profile</h2>

          {/* Avatar inside profile card */}
          <div className="flex flex-col items-center py-2">
            <button type="button" onClick={() => fileRef.current.click()}
              className="relative group w-20 h-20 rounded-full overflow-hidden ring-4 ring-primary-100 hover:ring-primary-300 transition-all">
              {preview
                ? <img src={preview} alt="Profile" className="w-full h-full object-cover" />
                : <div className="w-full h-full bg-primary-100 text-primary-700 flex items-center justify-center text-2xl font-bold">{initial}</div>
              }
              <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                <span className="text-white text-xs font-medium">Change</span>
              </div>
            </button>
            <p className="text-xs text-gray-400 mt-1.5">Click to change photo</p>
            <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={handlePhoto} />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">First Name <span className="text-red-400">*</span></label>
              <input type="text" value={firstName} onChange={handleCapitalizedNameInput(setFirstName)} className={inputClass} placeholder="Juan" autoCapitalize="words" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Last Name <span className="text-red-400">*</span></label>
              <input type="text" value={lastName} onChange={handleCapitalizedNameInput(setLastName)} className={inputClass} placeholder="dela Cruz" autoCapitalize="words" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Middle Name</label>
              <input type="text" value={middleName} onChange={handleCapitalizedNameInput(setMiddleName)} className={inputClass} placeholder="Santos" autoCapitalize="words" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Suffix</label>
              <select value={suffix} onChange={e => setSuffix(e.target.value)} className={inputClass}>
                <option value="">— None —</option>
                <option value="Jr.">Jr.</option>
                <option value="Sr.">Sr.</option>
                <option value="II">II</option>
                <option value="III">III</option>
                <option value="IV">IV</option>
                <option value="V">V</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
            <input type="email" value={user?.email ?? ''} disabled className={disabledClass} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Role</label>
            <input type="text" value={user?.role ?? ''} disabled className={`${disabledClass} capitalize`} />
          </div>

          <button type="submit" disabled={saving} className={btnClass}>
            {saving ? 'Saving…' : 'Save Changes'}
          </button>
        </form>

        {/* Change Password */}
        <form onSubmit={savePassword} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-4 transition-all duration-200 hover:shadow-md hover:border-primary-200">
          <h2 className="font-bold text-gray-900">Change Password</h2>
          {[['Current Password', curPass, setCurPass], ['New Password', newPass, setNewPass], ['Confirm New Password', confirm, setConfirm]].map(([label, val, setter]) => (
            <div key={label}>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
              <input type="password" value={val} onChange={e => setter(e.target.value)} className={inputClass} />
            </div>
          ))}
          <button type="submit" disabled={saving} className={btnClass}>
            {saving ? 'Saving…' : 'Update Password'}
          </button>
        </form>

      </div>
    </div>
  )
}

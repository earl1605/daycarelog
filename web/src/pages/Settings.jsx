import { useRef, useState } from 'react'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { handleCapitalizedNameInput } from '../utils/capitalizeFirstLetters'
import { UsersIcon, KeyIcon, EyeIcon, EyeOffIcon } from '../components/icons'
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
  const [showPass,   setShowPass]   = useState({ curPass: false, newPass: false, confirm: false })
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
  const btnClass = "bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold px-5 py-2.5 rounded-xl transition-colors text-sm"
  const roleColors = { admin: 'bg-violet-50 text-violet-700', staff: 'bg-blue-50 text-blue-700', parent: 'bg-amber-50 text-amber-700' }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-[22px] font-bold text-gray-900">Settings</h1>
        <p className="text-gray-500 text-sm mt-1">Manage your profile, photo, and account security.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">

        <div className="bg-white rounded-xl border border-gray-200/70 overflow-hidden">
          <div className="p-6 flex items-center gap-4 bg-[#FAFAFA] border-b border-gray-200/70">
            <button type="button" onClick={() => fileRef.current.click()}
              className="relative group w-16 h-16 rounded-full overflow-hidden ring-4 ring-white shadow-sm shrink-0">
              {preview
                ? <img src={preview} alt="Profile" className="w-full h-full object-cover" />
                : <div className="w-full h-full bg-primary-100 text-primary-700 flex items-center justify-center text-xl font-bold">{initial}</div>
              }
              <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                <span className="text-white text-[10px] font-medium">Change</span>
              </div>
            </button>
            <div className="min-w-0 flex-1">
              <p className="font-semibold text-gray-900 truncate">{[firstName, middleName, lastName, suffix].filter(Boolean).join(' ') || 'User'}</p>
              <p className="text-sm text-gray-500 truncate">{user?.email}</p>
              <span className={`inline-block mt-1.5 px-2 py-0.5 rounded-full text-xs font-semibold capitalize ${roleColors[user?.role] ?? 'bg-gray-100 text-gray-600'}`}>
                {user?.role ?? '—'}
              </span>
            </div>
            <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={handlePhoto} />
          </div>

          <form onSubmit={saveName} className="p-6 space-y-5">
            <div>
              <h2 className="flex items-center gap-2 text-[15px] font-bold text-gray-900">
                <UsersIcon width={16} height={16} className="text-primary-600" /> Personal Information
              </h2>
              <p className="text-xs text-gray-400 mt-1">Click your photo above to change it.</p>
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
                <select value={suffix} onChange={e => setSuffix(e.target.value)} className={`${inputClass} bg-white`}>
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

            <div className="flex justify-end pt-1">
              <button type="submit" disabled={saving} className={btnClass}>
                {saving ? 'Saving…' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>

        <form onSubmit={savePassword} className="bg-white rounded-xl border border-gray-200/70 p-6 space-y-5">
          <div>
            <h2 className="flex items-center gap-2 text-[15px] font-bold text-gray-900">
              <KeyIcon width={16} height={16} className="text-primary-600" /> Change Password
            </h2>
            <p className="text-xs text-gray-400 mt-1">Use at least 6 characters. You'll need your current password to confirm.</p>
          </div>

          {[['Current Password', curPass, setCurPass, 'curPass'], ['New Password', newPass, setNewPass, 'newPass'], ['Confirm New Password', confirm, setConfirm, 'confirm']].map(([label, val, setter, key]) => (
            <div key={label}>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
              <div className="relative">
                <input
                  type={showPass[key] ? 'text' : 'password'}
                  value={val}
                  onChange={e => setter(e.target.value)}
                  className={`${inputClass} pr-10`}
                />
                <button
                  type="button"
                  onClick={() => setShowPass(s => ({ ...s, [key]: !s[key] }))}
                  className="absolute inset-y-0 right-0 flex items-center px-3 text-gray-400 hover:text-gray-600"
                  tabIndex={-1}
                  aria-label={showPass[key] ? `Hide ${label.toLowerCase()}` : `Show ${label.toLowerCase()}`}
                >
                  {showPass[key] ? <EyeOffIcon width={17} height={17} /> : <EyeIcon width={17} height={17} />}
                </button>
              </div>
            </div>
          ))}

          <div className="flex justify-end pt-1">
            <button type="submit" disabled={saving} className={btnClass}>
              {saving ? 'Saving…' : 'Update Password'}
            </button>
          </div>
        </form>

      </div>
    </div>
  )
}

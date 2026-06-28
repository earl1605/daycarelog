import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import toast from 'react-hot-toast'

function EyeIcon({ open }) {
  return open ? (
    <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
    </svg>
  ) : (
    <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
      <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
  )
}

const inputClass = "w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition"

export default function Register() {
  const { signUp }     = useAuth()
  const navigate       = useNavigate()
  const [firstName,   setFirstName]   = useState('')
  const [lastName,    setLastName]    = useState('')
  const [middleName,  setMiddleName]  = useState('')
  const [suffix,      setSuffix]      = useState('')
  const [email,       setEmail]       = useState('')
  const [password,    setPassword]    = useState('')
  const [confirm,     setConfirm]     = useState('')
  const [loading,     setLoading]     = useState(false)
  const [role,        setRole]        = useState('staff')
  const [showPass,    setShowPass]    = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!firstName.trim() || !lastName.trim() || !email || !password || !confirm) {
      toast.error('Please fill in all required fields'); return
    }
    if (!role) { toast.error('Please select a role'); return }
    if (password.length < 6) { toast.error('Password must be at least 6 characters'); return }
    if (password !== confirm) { toast.error('Passwords do not match'); return }
    setLoading(true)
    const { error } = await signUp(email, password, firstName.trim(), lastName.trim(), middleName.trim(), suffix.trim(), role)
    setLoading(false)
    if (error) toast.error(error.message)
    else { toast.success('Account created! Please sign in to continue.'); navigate('/login') }
  }

  return (
    <div className="min-h-screen flex bg-animated-gradient">
      <div className="hidden lg:flex flex-col justify-between w-1/2 p-12 text-white">
        <Link to="/" className="flex items-center">
          <span className="font-extrabold text-2xl tracking-wide">
            {'DaycareLog'.split('').map((char, i) => (
              <span key={i} className="wave-letter">{char}</span>
            ))}
          </span>
        </Link>
        <div>
          <div className="flex gap-3 mb-6">
            {['👶', '🏥', '📋'].map((icon, i) => (
              <div key={i} className={`w-14 h-14 rounded-2xl glass flex items-center justify-center text-2xl animate-float animation-delay-${i * 200}`}>{icon}</div>
            ))}
          </div>
          <h2 className="text-4xl font-extrabold leading-tight mb-4">Join hundreds of<br />barangay centers.</h2>
          <p className="text-green-100 text-lg">Start tracking, monitoring, and reporting for your daycare center today — completely free.</p>
          <ul className="mt-6 space-y-2 text-green-100 text-sm">
            {['Free to use', 'Secure & private', 'DOH-aligned reports', 'Works offline-friendly'].map(t => (
              <li key={t} className="flex items-center gap-2"><span className="text-green-300">✓</span>{t}</li>
            ))}
          </ul>
        </div>
        <p className="text-green-200 text-sm">© {new Date().getFullYear()} DaycareLog · Philippines</p>
      </div>

      <div className="flex-1 flex items-center justify-center p-6">
        <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl p-8 animate-scale-in">
          <div className="flex lg:hidden items-center mb-6">
            <span className="font-extrabold text-primary-700 text-xl tracking-wide">
              {'DaycareLog'.split('').map((char, i) => (
                <span key={i} className="wave-letter">{char}</span>
              ))}
            </span>
          </div>
          <Link to="/" className="inline-flex items-center gap-1.5 text-sm text-gray-400 hover:text-primary-600 transition-all duration-200 mb-6 group">
            <span className="group-hover:-translate-x-1 transition-transform duration-200 inline-block">←</span> Back to Home
          </Link>
          <h1 className="text-2xl font-extrabold text-gray-900 mb-1">Create your account</h1>
          <p className="text-gray-500 text-sm mb-6">Get started with DaycareLog for free</p>

          <form onSubmit={handleSubmit} className="space-y-4">

            {/* Name row 1 */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">First Name <span className="text-red-400">*</span></label>
                <input type="text" value={firstName} onChange={e => setFirstName(e.target.value)}
                  placeholder="Juan" className={inputClass} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Last Name <span className="text-red-400">*</span></label>
                <input type="text" value={lastName} onChange={e => setLastName(e.target.value)}
                  placeholder="dela Cruz" className={inputClass} />
              </div>
            </div>

            {/* Name row 2 */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Middle Name</label>
                <input type="text" value={middleName} onChange={e => setMiddleName(e.target.value)}
                  placeholder="Santos" className={inputClass} />
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

            {/* Role */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Role <span className="text-red-400">*</span></label>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { value: 'admin',   label: 'Admin',   desc: 'Center Administrator',  icon: '🛡️' },
                  { value: 'teacher', label: 'Teacher', desc: 'Daycare Worker',         icon: '📚' },
                  { value: 'staff',   label: 'Staff',   desc: 'Support Personnel',      icon: '👤' },
                ].map(r => (
                  <button
                    key={r.value}
                    type="button"
                    onClick={() => setRole(r.value)}
                    className={`flex flex-col items-center gap-1 p-3 rounded-xl border-2 text-center transition-all duration-150 ${
                      role === r.value
                        ? 'border-primary-500 bg-primary-50 text-primary-700'
                        : 'border-gray-200 hover:border-gray-300 text-gray-600'
                    }`}
                  >
                    <span className="text-xl">{r.icon}</span>
                    <span className="text-xs font-semibold leading-tight">{r.label}</span>
                    <span className="text-[10px] text-gray-400 leading-tight">{r.desc}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Email */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Email address <span className="text-red-400">*</span></label>
              <input type="email" value={email} onChange={e => setEmail(e.target.value)}
                placeholder="you@example.com" className={inputClass} />
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Password <span className="text-red-400">*</span></label>
              <div className="relative">
                <input type={showPass ? 'text' : 'password'} value={password} onChange={e => setPassword(e.target.value)}
                  placeholder="Min. 6 characters" className={`${inputClass} pr-11`} />
                <button type="button" onClick={() => setShowPass(p => !p)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                  <EyeIcon open={showPass} />
                </button>
              </div>
            </div>

            {/* Confirm password */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Confirm Password <span className="text-red-400">*</span></label>
              <div className="relative">
                <input type={showConfirm ? 'text' : 'password'} value={confirm} onChange={e => setConfirm(e.target.value)}
                  placeholder="Re-enter password" className={`${inputClass} pr-11`} />
                <button type="button" onClick={() => setShowConfirm(p => !p)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                  <EyeIcon open={showConfirm} />
                </button>
              </div>
            </div>

            <button type="submit" disabled={loading}
              className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-colors shadow-sm mt-2">
              {loading
                ? <span className="flex items-center justify-center gap-2"><span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />Creating account…</span>
                : 'Create Account'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-600 font-semibold hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}

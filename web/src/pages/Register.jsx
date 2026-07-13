import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { handleCapitalizedNameInput } from '../utils/capitalizeFirstLetters'
import { validateEmailFormat, getEmailTypoSuggestion } from '../utils/emailValidation'
import PasswordStrengthMeter from '../components/PasswordStrengthMeter'
import usePageTitle from '../hooks/usePageTitle'
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

const inputClass = "w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition"

export default function Register() {
  usePageTitle('Register')
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
  const [showPass,    setShowPass]    = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [formError,   setFormError]   = useState('')
  const [emailError,      setEmailError]      = useState('')
  const [emailSuggestion, setEmailSuggestion] = useState('')

  function handleEmailBlur() {
    const result = validateEmailFormat(email)
    setEmailError(result.valid ? '' : result.message)
    setEmailSuggestion(getEmailTypoSuggestion(email) || '')
  }

  function acceptEmailSuggestion() {
    setEmail(emailSuggestion)
    setEmailSuggestion('')
    setEmailError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setFormError('')
    if (!firstName.trim() || !lastName.trim() || !email || !password || !confirm) {
      setFormError('Please fill in all required fields'); return
    }
    const emailCheck = validateEmailFormat(email)
    if (!emailCheck.valid) { setEmailError(emailCheck.message); setFormError(emailCheck.message); return }
    if (password.length < 8) { setFormError('Password must be at least 8 characters'); return }
    if (password !== confirm) { setFormError('Passwords do not match'); return }
    setLoading(true)
    const { error } = await signUp(email, password, firstName.trim(), lastName.trim(), middleName.trim(), suffix.trim())
    setLoading(false)
    if (error) {
      setFormError(error.message)
      toast.error(error.message)
    } else {
      toast.success('Account created! Check your email to verify it.')
      navigate(`/check-email?email=${encodeURIComponent(email)}`)
    }
  }

  return (
    <div className="min-h-screen flex bg-animated-gradient">
      <div className="hidden lg:flex flex-col justify-between w-1/2 p-12 text-white">
        <Link to="/" className="flex items-center gap-3">
          <img src="/favicon.svg" alt="DaycareLog" className="w-11 h-11 flex-shrink-0" />
          <span className="font-extrabold text-4xl tracking-wide">
            {'DaycareLog'.split('').map((char, i) => (
              <span key={i} className="wave-letter">{char}</span>
            ))}
          </span>
        </Link>
        <div>
          <div className="flex gap-3 mb-5">
            {['👶', '🏥', '📋'].map((icon, i) => (
              <div key={i} className={`w-14 h-14 rounded-2xl glass flex items-center justify-center text-2xl animate-float animation-delay-${i * 200}`}>{icon}</div>
            ))}
          </div>
          <h2 className="text-4xl font-extrabold leading-tight mb-3">Join hundreds of<br />barangay centers.</h2>
          <p className="text-green-100 text-xl leading-relaxed mb-5 max-w-md">Start tracking, monitoring, and reporting for your daycare center today — completely free.</p>
          <ul className="space-y-2.5 text-green-100 text-base">
            {['Free to use', 'Secure & private', 'DOH-aligned reports', 'Works offline-friendly'].map(t => (
              <li key={t} className="flex items-center gap-2.5">
                <span className="flex-shrink-0 w-5 h-5 rounded-full bg-green-300/20 text-green-300 text-xs flex items-center justify-center">✓</span>
                {t}
              </li>
            ))}
          </ul>
        </div>
        <p className="text-green-200 text-sm">© {new Date().getFullYear()} DaycareLog · Philippines</p>
      </div>

      <div className="flex-1 flex items-start justify-center p-4 py-6 overflow-y-auto">
        <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl px-5 sm:px-7 py-5 animate-scale-in my-auto">
          <div className="flex lg:hidden items-center gap-2.5 mb-3">
            <img src="/favicon.svg" alt="DaycareLog" className="w-9 h-9 flex-shrink-0" />
            <span className="font-extrabold text-primary-700 text-3xl tracking-wide">
              {'DaycareLog'.split('').map((char, i) => (
                <span key={i} className="wave-letter">{char}</span>
              ))}
            </span>
          </div>
          <Link to="/" className="inline-flex items-center gap-1.5 text-xs text-gray-400 hover:text-primary-600 transition-all duration-200 mb-2 group">
            <span className="group-hover:-translate-x-1 transition-transform duration-200 inline-block">←</span> Back to Home
          </Link>
          <h1 className="text-xl font-extrabold text-gray-900 mb-0.5">Create your account</h1>
          <p className="text-gray-500 text-xs mb-3">Get started with DaycareLog for free</p>

          {formError && (
            <div className="mb-3 px-3 py-2.5 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700 font-medium">
              {formError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-2.5">

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">First Name <span className="text-red-400">*</span></label>
                <input type="text" value={firstName} onChange={handleCapitalizedNameInput(setFirstName)}
                  placeholder="Christian Earl" className={inputClass} autoComplete="given-name" autoCapitalize="words" />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Last Name <span className="text-red-400">*</span></label>
                <input type="text" value={lastName} onChange={handleCapitalizedNameInput(setLastName)}
                  placeholder="Mahumot" className={inputClass} autoComplete="family-name" autoCapitalize="words" />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Middle Name</label>
                <input type="text" value={middleName} onChange={handleCapitalizedNameInput(setMiddleName)}
                  placeholder="Villahermosa" className={inputClass} autoComplete="additional-name" autoCapitalize="words" />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Suffix</label>
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
              <label className="block text-xs font-medium text-gray-700 mb-1">Email address <span className="text-red-400">*</span></label>
              <input type="email" value={email}
                onChange={e => { setEmail(e.target.value); setEmailError(''); setEmailSuggestion('') }}
                onBlur={handleEmailBlur}
                placeholder="you@daycarelog.com" className={inputClass} autoComplete="email" />
              {emailError && <p className="text-xs text-red-600 mt-1">{emailError}</p>}
              {emailSuggestion && (
                <button type="button" onClick={acceptEmailSuggestion}
                  className="text-xs text-primary-600 hover:underline mt-1">
                  Did you mean <span className="font-semibold">{emailSuggestion}</span>?
                </button>
              )}
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Password <span className="text-red-400">*</span></label>
                <div className="relative">
                  <input type={showPass ? 'text' : 'password'} value={password} onChange={e => setPassword(e.target.value)}
                    placeholder="Min. 8 chars" className={`${inputClass} pr-9`} autoComplete="new-password" />
                  <button type="button" onClick={() => setShowPass(p => !p)}
                    className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                    <EyeIcon open={showPass} />
                  </button>
                </div>
                <PasswordStrengthMeter password={password} />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Confirm <span className="text-red-400">*</span></label>
                <div className="relative">
                  <input type={showConfirm ? 'text' : 'password'} value={confirm} onChange={e => setConfirm(e.target.value)}
                    placeholder="Re-enter" className={`${inputClass} pr-9`} autoComplete="new-password" />
                  <button type="button" onClick={() => setShowConfirm(p => !p)}
                    className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                    <EyeIcon open={showConfirm} />
                  </button>
                </div>
              </div>
            </div>

            <button type="submit" disabled={loading}
              className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold py-2.5 rounded-xl transition-colors shadow-sm">
              {loading
                ? <span className="flex items-center justify-center gap-2"><span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />Creating account…</span>
                : 'Create Account'}
            </button>
          </form>

          <p className="text-center text-xs text-gray-500 mt-3">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-600 font-semibold hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}

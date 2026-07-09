import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { api } from '../lib/api'
import { MailIcon } from '../components/icons'
import toast from 'react-hot-toast'

const RESEND_COOLDOWN_SECONDS = 60

function maskEmail(email) {
  if (!email || !email.includes('@')) return email ?? ''
  const [name, domain] = email.split('@')
  if (name.length <= 2) return `${name[0] ?? ''}***@${domain}`
  return `${name.slice(0, 2)}${'*'.repeat(Math.max(name.length - 2, 3))}@${domain}`
}

export default function CheckEmail() {
  const [params] = useSearchParams()
  const email = params.get('email') ?? ''
  const navigate = useNavigate()
  const { completeVerification, signOut } = useAuth()

  const [code,       setCode]       = useState('')
  const [verifying,  setVerifying]  = useState(false)
  const [error,      setError]      = useState('')
  const [resending,  setResending]  = useState(false)
  const [cooldown,   setCooldown]   = useState(0)
  const timerRef = useRef(null)

  useEffect(() => {
    if (cooldown <= 0) return
    timerRef.current = setTimeout(() => setCooldown(c => c - 1), 1000)
    return () => clearTimeout(timerRef.current)
  }, [cooldown])

  function goToDashboard(user) {
    navigate(user?.role === 'parent' ? '/parent/dashboard' : '/dashboard')
  }

  function handleSignOut() {
    signOut()
    navigate('/login')
  }

  async function handleVerify(e) {
    e.preventDefault()
    setError('')
    if (code.trim().length !== 6) { setError('Enter the 6-digit code from your email.'); return }
    setVerifying(true)
    try {
      const res = await api.auth.verifyByCode(email, code.trim())
      completeVerification(res.token, res.user)
      toast.success('Email verified!')
      goToDashboard(res.user)
    } catch (e) {
      if (e.code === 'TOO_MANY_ATTEMPTS') setError('Too many incorrect attempts. Request a new code below.')
      else if (e.code === 'TOKEN_EXPIRED') setError('This code has expired. Request a new one below.')
      else setError(e.message || 'Incorrect code.')
    }
    setVerifying(false)
  }

  async function handleResend() {
    if (cooldown > 0 || resending) return
    setResending(true)
    try {
      await api.auth.resendVerification(email)
      toast.success('If that email needs verification, a new code and link are on the way.')
      setCooldown(RESEND_COOLDOWN_SECONDS)
    } catch (e) {
      toast.error(e.message || 'Could not resend right now.')
    }
    setResending(false)
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-animated-gradient p-4">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl px-6 sm:px-8 py-8 animate-scale-in text-center">
        <span className="mx-auto flex w-14 h-14 rounded-full bg-primary-50 text-primary-600 items-center justify-center mb-4">
          <MailIcon width={26} height={26} />
        </span>
        <h1 className="text-xl font-extrabold text-gray-900 mb-1">Check your email</h1>
        <p className="text-sm text-gray-500 mb-6">
          We sent a verification link and a 6-digit code to<br />
          <span className="font-semibold text-gray-700">{maskEmail(email)}</span>
        </p>

        <form onSubmit={handleVerify} className="space-y-3 text-left">
          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">Verification code</label>
            <input
              type="text" inputMode="numeric" maxLength={6} value={code}
              onChange={e => setCode(e.target.value.replace(/\D/g, ''))}
              placeholder="123456"
              className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-center text-lg font-mono tracking-[0.4em] focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition"
            />
          </div>
          {error && (
            <div className="px-3 py-2.5 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700 font-medium">
              {error}
            </div>
          )}
          <button type="submit" disabled={verifying}
            className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold py-2.5 rounded-xl transition-colors shadow-sm">
            {verifying ? 'Verifying…' : 'Verify email'}
          </button>
        </form>

        <p className="text-xs text-gray-500 mt-4">
          Didn't get it?{' '}
          <button onClick={handleResend} disabled={cooldown > 0 || resending}
            className="text-primary-600 font-semibold hover:underline disabled:text-gray-400 disabled:no-underline disabled:cursor-not-allowed">
            {cooldown > 0 ? `Resend in ${cooldown}s` : resending ? 'Sending…' : 'Resend email'}
          </button>
        </p>

        <button onClick={handleSignOut} className="text-xs text-gray-400 hover:text-gray-600 mt-6 underline-offset-2 hover:underline">
          Not you? Sign out
        </button>
        <p className="text-xs text-gray-400 mt-2">
          Already clicked the link in another tab?{' '}
          <Link to="/login" className="text-primary-600 hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  )
}

import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../lib/api'
import usePageTitle from '../hooks/usePageTitle'
import { CheckIcon, AlertTriangleIcon, EyeIcon, EyeOffIcon } from '../components/icons'
import toast from 'react-hot-toast'

export default function ResetPassword() {
  usePageTitle('Reset Password')
  const [params] = useSearchParams()
  const token = params.get('token')
  const navigate = useNavigate()

  const [status,   setStatus]   = useState(token ? 'form' : 'missing-token')
  const [password, setPassword] = useState('')
  const [confirm,  setConfirm]  = useState('')
  const [showPass, setShowPass] = useState(false)
  const [error,    setError]    = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (password.length < 8) { setError('Password must be at least 8 characters.'); return }
    if (password !== confirm) { setError('Passwords do not match.'); return }

    setSubmitting(true)
    try {
      await api.auth.resetPasswordByToken(token, password)
      setStatus('success')
    } catch (e) {
      if (e.code === 'TOKEN_EXPIRED') setError('This reset link has expired. Request a new one.')
      else if (e.code === 'TOKEN_INVALID') setError('This reset link is invalid or has already been used.')
      else setError(e.message || 'Could not reset your password.')
      setStatus('error')
    }
    setSubmitting(false)
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-animated-gradient p-4">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl px-6 sm:px-8 py-8 animate-scale-in text-center">
        {status === 'missing-token' && (
          <>
            <span className="mx-auto flex w-14 h-14 rounded-full bg-red-50 text-red-600 items-center justify-center mb-4">
              <AlertTriangleIcon width={26} height={26} />
            </span>
            <h1 className="text-xl font-extrabold text-gray-900 mb-1">Invalid link</h1>
            <p className="text-sm text-gray-500 mb-6">This reset link is missing its token.</p>
            <Link to="/forgot-password" className="text-primary-600 font-semibold hover:underline text-sm">
              Request a new link
            </Link>
          </>
        )}

        {status === 'form' && (
          <>
            <h1 className="text-xl font-extrabold text-gray-900 mb-1">Set a new password</h1>
            <p className="text-sm text-gray-500 mb-6">Choose a new password for your account.</p>
            <form onSubmit={handleSubmit} className="space-y-4 text-left">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">New password</label>
                <div className="relative">
                  <input type={showPass ? 'text' : 'password'} value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" autoFocus
                    className="w-full border border-gray-200 rounded-xl px-4 py-3 pr-11 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition" />
                  <button type="button" onClick={() => setShowPass(p => !p)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                    {showPass ? <EyeOffIcon width={20} height={20} /> : <EyeIcon width={20} height={20} />}
                  </button>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Confirm new password</label>
                <input type={showPass ? 'text' : 'password'} value={confirm} onChange={e => setConfirm(e.target.value)} placeholder="••••••••"
                  className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition" />
              </div>
              {error && (
                <div className="px-3 py-2.5 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700 font-medium">
                  {error}
                </div>
              )}
              <button type="submit" disabled={submitting}
                className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-colors shadow-sm">
                {submitting ? 'Resetting…' : 'Reset password'}
              </button>
            </form>
          </>
        )}

        {status === 'success' && (
          <>
            <span className="mx-auto flex w-14 h-14 rounded-full bg-green-50 text-green-600 items-center justify-center mb-4">
              <CheckIcon width={26} height={26} />
            </span>
            <h1 className="text-xl font-extrabold text-gray-900 mb-1">Password reset!</h1>
            <p className="text-sm text-gray-500 mb-6">You can now sign in with your new password.</p>
            <button onClick={() => { toast.success('Password updated.'); navigate('/login') }}
              className="w-full bg-primary-600 hover:bg-primary-700 text-white font-semibold py-2.5 rounded-xl transition-colors shadow-sm">
              Go to sign in
            </button>
          </>
        )}

        {status === 'error' && (
          <>
            <span className="mx-auto flex w-14 h-14 rounded-full bg-red-50 text-red-600 items-center justify-center mb-4">
              <AlertTriangleIcon width={26} height={26} />
            </span>
            <h1 className="text-xl font-extrabold text-gray-900 mb-1">Reset failed</h1>
            <p className="text-sm text-gray-500 mb-6">{error}</p>
            <Link to="/forgot-password" className="text-primary-600 font-semibold hover:underline text-sm">
              Request a new link
            </Link>
          </>
        )}

        {(status === 'form' || status === 'missing-token' || status === 'error') && (
          <p className="text-xs text-gray-400 mt-6">
            <Link to="/login" className="text-primary-600 hover:underline">Back to sign in</Link>
          </p>
        )}
      </div>
    </div>
  )
}

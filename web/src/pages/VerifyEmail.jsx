import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { api } from '../lib/api'
import { CheckIcon, AlertTriangleIcon } from '../components/icons'
import toast from 'react-hot-toast'

export default function VerifyEmail() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const navigate = useNavigate()
  const { completeVerification } = useAuth()

  const [status,   setStatus]   = useState('loading')
  const [errorMsg, setErrorMsg] = useState('')
  const [user,     setUser]     = useState(null)
  const [resendEmail, setResendEmail] = useState('')
  const [resent,      setResent]      = useState(false)

  useEffect(() => {
    if (!token) {
      setStatus('error')
      setErrorMsg('This verification link is missing its token.')
      return
    }
    api.auth.verifyByToken(token)
      .then(res => {
        completeVerification(res.token, res.user)
        setUser(res.user)
        setStatus('success')
      })
      .catch(e => {
        setStatus('error')
        setErrorMsg(e.message || 'This verification link is invalid or has expired.')
      })
  }, [token])

  function continueToDashboard() {
    navigate(user?.role === 'parent' ? '/parent/dashboard' : '/dashboard')
  }

  async function handleResend(e) {
    e.preventDefault()
    if (!resendEmail.trim()) return
    try {
      await api.auth.resendVerification(resendEmail.trim())
      setResent(true)
      toast.success('If that email needs verification, a new link and code are on the way.')
    } catch (e) {
      toast.error(e.message || 'Could not resend right now.')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-animated-gradient p-4">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl px-6 sm:px-8 py-8 animate-scale-in text-center">
        {status === 'loading' && (
          <>
            <div className="w-10 h-10 border-4 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
            <p className="text-sm text-gray-500">Verifying your email…</p>
          </>
        )}

        {status === 'success' && (
          <>
            <span className="mx-auto flex w-14 h-14 rounded-full bg-green-50 text-green-600 items-center justify-center mb-4">
              <CheckIcon width={26} height={26} />
            </span>
            <h1 className="text-xl font-extrabold text-gray-900 mb-1">Email verified!</h1>
            <p className="text-sm text-gray-500 mb-6">Your account is ready to go.</p>
            <button onClick={continueToDashboard}
              className="w-full bg-primary-600 hover:bg-primary-700 text-white font-semibold py-2.5 rounded-xl transition-colors shadow-sm">
              Continue to dashboard
            </button>
          </>
        )}

        {status === 'error' && (
          <>
            <span className="mx-auto flex w-14 h-14 rounded-full bg-red-50 text-red-600 items-center justify-center mb-4">
              <AlertTriangleIcon width={26} height={26} />
            </span>
            <h1 className="text-xl font-extrabold text-gray-900 mb-1">Verification failed</h1>
            <p className="text-sm text-gray-500 mb-6">{errorMsg}</p>

            {resent ? (
              <p className="text-sm text-green-700 bg-green-50 border border-green-200 rounded-xl px-3 py-2.5">
                Check your email for a new link and code.
              </p>
            ) : (
              <form onSubmit={handleResend} className="space-y-3 text-left">
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    Enter your email to get a new link
                  </label>
                  <input type="email" value={resendEmail} onChange={e => setResendEmail(e.target.value)}
                    placeholder="you@example.com"
                    className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition" />
                </div>
                <button type="submit"
                  className="w-full bg-primary-600 hover:bg-primary-700 text-white font-semibold py-2.5 rounded-xl transition-colors shadow-sm">
                  Resend verification email
                </button>
              </form>
            )}

            <p className="text-xs text-gray-500 mt-4">
              <Link to="/login" className="text-primary-600 font-semibold hover:underline">Back to sign in</Link>
            </p>
          </>
        )}
      </div>
    </div>
  )
}

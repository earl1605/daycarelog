import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import usePageTitle from '../hooks/usePageTitle'
import { MailIcon } from '../components/icons'
import toast from 'react-hot-toast'

export default function ForgotPassword() {
  usePageTitle('Forgot Password')
  const [email,   setEmail]   = useState('')
  const [loading, setLoading] = useState(false)
  const [sent,    setSent]    = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!email.trim()) return
    setLoading(true)
    try {
      await api.auth.forgotPassword(email.trim())
    } catch (e) {
      // The endpoint never reveals whether the email exists, so a thrown
      // error here means something actually went wrong (network, 5xx) -
      // still show the same generic confirmation rather than leak that.
    }
    setLoading(false)
    setSent(true)
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-animated-gradient p-4">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl px-6 sm:px-8 py-8 animate-scale-in text-center">
        {sent ? (
          <>
            <span className="mx-auto flex w-14 h-14 rounded-full bg-primary-50 text-primary-600 items-center justify-center mb-4">
              <MailIcon width={26} height={26} />
            </span>
            <h1 className="text-xl font-extrabold text-gray-900 mb-1">Check your email</h1>
            <p className="text-sm text-gray-500 mb-6">
              If an account exists for <span className="font-semibold text-gray-700">{email.trim()}</span>,
              we've sent a link and a 6-digit code to reset your password.
            </p>
            <button onClick={() => { api.auth.forgotPassword(email.trim()).catch(() => {}); toast.success('Sent again.') }}
              className="text-sm text-primary-600 font-semibold hover:underline">
              Didn't get it? Send again
            </button>
            <p className="text-xs text-gray-400 mt-6">
              <Link to="/login" className="text-primary-600 hover:underline">Back to sign in</Link>
            </p>
          </>
        ) : (
          <>
            <h1 className="text-xl font-extrabold text-gray-900 mb-1">Forgot your password?</h1>
            <p className="text-sm text-gray-500 mb-6">
              Enter the email on your account and we'll send you a link to reset it.
            </p>
            <form onSubmit={handleSubmit} className="space-y-4 text-left">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Email address</label>
                <input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="you@daycarelog.com" autoFocus
                  className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition" />
              </div>
              <button type="submit" disabled={loading}
                className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-colors shadow-sm">
                {loading ? 'Sending…' : 'Send reset link'}
              </button>
            </form>
            <p className="text-sm text-gray-500 mt-6">
              <Link to="/login" className="text-primary-600 font-semibold hover:underline">Back to sign in</Link>
            </p>
          </>
        )}
      </div>
    </div>
  )
}

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import toast from 'react-hot-toast'

export default function Login() {
  const { signIn }    = useAuth()
  const navigate      = useNavigate()
  const [email,    setEmail]    = useState('')
  const [password, setPassword] = useState('')
  const [loading,  setLoading]  = useState(false)
  const [showPass, setShowPass] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!email || !password) { toast.error('Please fill in all fields'); return }
    setLoading(true)
    const { error } = await signIn(email, password)
    setLoading(false)
    if (error) toast.error(error.message)
    else { toast.success('Welcome back!'); navigate('/dashboard') }
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
          <div className="flex gap-3 mb-6">
            {['📝', '❤️', '📊'].map((e, i) => (
              <div key={i} className={`w-14 h-14 rounded-2xl glass flex items-center justify-center text-2xl animate-float animation-delay-${i * 200}`}>{e}</div>
            ))}
          </div>
          <h2 className="text-4xl font-extrabold leading-tight mb-4">Your barangay's<br />health data, simplified.</h2>
          <p className="text-green-100 text-lg">Enrollment, attendance, nutritional monitoring — all in one place.</p>
        </div>
        <p className="text-green-200 text-sm">© {new Date().getFullYear()} DaycareLog · Philippines</p>
      </div>

      <div className="flex-1 flex items-center justify-center p-4 sm:p-6">
        <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl p-5 sm:p-8 animate-scale-in">
          <div className="flex lg:hidden items-center gap-2.5 mb-6">
            <img src="/favicon.svg" alt="DaycareLog" className="w-9 h-9 flex-shrink-0" />
            <span className="font-extrabold text-primary-700 text-3xl tracking-wide">
              {'DaycareLog'.split('').map((char, i) => (
                <span key={i} className="wave-letter">{char}</span>
              ))}
            </span>
          </div>
          <Link to="/" className="inline-flex items-center gap-1.5 text-sm text-gray-400 hover:text-primary-600 transition-all duration-200 mb-6 group">
            <span className="group-hover:-translate-x-1 transition-transform duration-200 inline-block">←</span> Back to Home
          </Link>
          <h1 className="text-2xl font-extrabold text-gray-900 mb-1">Welcome back</h1>
          <p className="text-gray-500 text-sm mb-8">Sign in to your account to continue</p>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Email address</label>
              <input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="you@example.com"
                className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Password</label>
              <div className="relative">
                <input type={showPass ? 'text' : 'password'} value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••"
                  className="w-full border border-gray-200 rounded-xl px-4 py-3 pr-11 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition" />
                <button type="button" onClick={() => setShowPass(p => !p)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                  {showPass ? (
                    <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                    </svg>
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                      <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  )}
                </button>
              </div>
            </div>
            <button type="submit" disabled={loading}
              className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-colors shadow-sm">
              {loading ? <span className="flex items-center justify-center gap-2"><span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />Signing in…</span> : 'Sign In'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            Don't have an account?{' '}
            <Link to="/register" className="text-primary-600 font-semibold hover:underline">Register here</Link>
          </p>
        </div>
      </div>
    </div>
  )
}

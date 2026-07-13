import { useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import useInstallPrompt from '../hooks/useInstallPrompt'

const features = [
  { icon: '📝', title: 'Digital Enrollment', desc: 'Paperless child registration with complete guardian and health information.' },
  { icon: '📋', title: 'Daily Attendance', desc: 'One-tap check-in/out tracking with real-time attendance reports.' },
  { icon: '❤️', title: 'Health Monitoring', desc: 'Track weight, height, and nutritional status with DOH classification.' },
  { icon: '📊', title: 'Analytics & Reports', desc: 'Generate monthly and quarterly reports aligned with barangay requirements.' },
  { icon: '💉', title: 'Immunization Records', desc: 'Complete vaccine history and reminders for upcoming schedules.' },
  { icon: '⭐', title: 'Milestone Tracking', desc: "Document developmental milestones for each child's growth journey." },
]

const stats = [
  { value: '500+', label: 'Children Enrolled' },
  { value: '50+',  label: 'Barangay Centers' },
  { value: '98%',  label: 'DOH Compliance' },
  { value: '24/7', label: 'Data Access' },
]

const steps = [
  { num: '01', title: 'Register Your Center', desc: 'Create an account for your barangay daycare center in minutes.' },
  { num: '02', title: 'Enroll Children',      desc: 'Add children and guardians with complete health history.' },
  { num: '03', title: 'Track & Report',        desc: 'Monitor daily attendance, health, and generate DOH reports.' },
]

function useReveal() {
  const ref = useRef(null)
  useEffect(() => {
    const el = ref.current
    if (!el) return
    const obs = new IntersectionObserver(
      entries => entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('visible'); obs.unobserve(e.target) } }),
      { threshold: 0.15 }
    )
    el.querySelectorAll('.reveal, .reveal-left, .reveal-right').forEach(el => obs.observe(el))
    return () => obs.disconnect()
  }, [])
  return ref
}

export default function Landing() {
  const featRef  = useReveal()
  const stepsRef = useReveal()
  const appRef   = useReveal()
  const ctaRef   = useReveal()
  const { canPromptInstall, promptInstall, installed, isIos } = useInstallPrompt()

  async function handleInstall() {
    const accepted = await promptInstall()
    if (accepted) toast.success('DaycareLog installed!')
  }

  return (
    <div className="min-h-screen bg-animated-gradient text-white overflow-x-hidden">

      <div className="fixed inset-0 pointer-events-none select-none overflow-hidden z-0">
        <div className="absolute -top-24 -left-24 w-96 h-96 rounded-full bg-white/[0.03] animate-float-slow" />
        <div className="absolute top-1/3 -right-20 w-72 h-72 rounded-full bg-white/[0.03] animate-float animation-delay-400" />
        <div className="absolute bottom-0 left-1/4 w-56 h-56 rounded-full bg-white/[0.03] animate-float-fast animation-delay-700" />
        <div className="absolute top-20 right-1/3 w-32 h-32 rounded-full bg-white/[0.05] animate-float animation-delay-200" />
        <div className="absolute bottom-20 right-10 w-24 h-24 rounded-full bg-white/[0.05] animate-float-slow animation-delay-600" />
        <div className="absolute top-2/3 left-10 w-48 h-48 rounded-full bg-white/[0.03] animate-float animation-delay-1000" />
        <div className="absolute top-1/2 left-1/2 w-64 h-64 rounded-full bg-white/[0.03] animate-float-slow animation-delay-800" />
      </div>

      <nav className="fixed top-0 left-0 right-0 flex items-center justify-between px-4 md:px-12 py-3 md:py-4 z-20 bg-black/10 backdrop-blur-sm">
        <span className="flex items-center gap-2 sm:gap-3">
          <img src="/favicon.svg" alt="DaycareLog" className="w-8 h-8 sm:w-10 sm:h-10 flex-shrink-0" />
          <span className="text-white font-extrabold text-2xl sm:text-4xl tracking-wide text-shadow-soft">
            {'DaycareLog'.split('').map((char, i) => (
              <span key={i} className="wave-letter">{char}</span>
            ))}
          </span>
        </span>
        <div className="flex items-center gap-2">
          <Link to="/login"    className="glass text-white text-xs sm:text-sm px-3 sm:px-4 py-1.5 sm:py-2 rounded-xl hover:bg-white/20 hover:-translate-y-0.5 hover:shadow-lg active:scale-95 transition-all duration-200">Sign In</Link>
          <Link to="/register" className="bg-white text-primary-700 text-xs sm:text-sm font-semibold px-3 sm:px-4 py-1.5 sm:py-2 rounded-xl hover:bg-gray-50 hover:-translate-y-0.5 hover:shadow-xl active:scale-95 transition-all duration-200 shadow-lg">Get Started</Link>
        </div>
      </nav>

      <section className="relative min-h-screen flex flex-col items-center justify-center z-10 pt-24">
        <div className="relative z-10 text-center px-6 max-w-4xl mx-auto">
          <div className="inline-flex items-center gap-2 bg-black/20 backdrop-blur-sm text-white text-sm font-medium px-4 py-2 rounded-full mb-6 animate-fade-in">
            <span className="w-2 h-2 rounded-full bg-green-300 animate-pulse" />
            Trusted by Barangay Health Workers across the Philippines
          </div>

          <h1 className="text-4xl md:text-6xl lg:text-7xl font-extrabold text-white leading-tight mb-6 anim-hidden animate-fade-in-up animation-delay-200 text-shadow-strong">
            Modern Daycare
            <br />
            <span className="text-green-200">Management System</span>
          </h1>

          <p className="text-lg md:text-xl text-white max-w-2xl mx-auto mb-10 anim-hidden animate-fade-in-up animation-delay-400 text-shadow-soft">
            Digital enrollment, health tracking, and attendance monitoring for barangay daycare centers — built for Philippine LGUs.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center anim-hidden animate-fade-in-up animation-delay-600">
            <Link to="/register" className="bg-white text-primary-700 font-bold text-base px-8 py-4 rounded-2xl hover:bg-gray-50 transition-all shadow-xl hover:shadow-2xl hover:-translate-y-1 duration-200">
              Start Free Today →
            </Link>
            <Link to="/login" className="glass text-white font-semibold text-base px-8 py-4 rounded-2xl hover:bg-white/20 transition-all duration-200">
              Sign In to Dashboard
            </Link>
          </div>
        </div>

        <div className="absolute bottom-8 left-0 right-0 flex flex-col items-center gap-2 text-white/60 text-xs animate-bounce-slow">
          <span>Scroll down</span>
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </section>

      <section className="relative z-10 py-12">
        <div className="max-w-5xl mx-auto px-6 grid grid-cols-2 md:grid-cols-4 gap-6">
          {stats.map((s, i) => (
            <div key={i} className="text-center bg-black/25 backdrop-blur-sm rounded-2xl border border-white/25 py-6 px-4">
              <p className="text-3xl font-extrabold text-white text-shadow-strong">{s.value}</p>
              <p className="text-green-200 text-sm mt-1 font-medium">{s.label}</p>
            </div>
          ))}
        </div>
      </section>

      <section ref={featRef} className="relative z-10 py-24 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16 reveal">
            <span className="text-green-300 font-semibold text-sm uppercase tracking-widest text-shadow-soft">Features</span>
            <h2 className="text-3xl md:text-4xl font-extrabold text-white mt-2 text-shadow-strong">Everything you need in one place</h2>
            <p className="text-green-100 mt-3 max-w-xl mx-auto font-medium">Designed specifically for Philippine barangay daycare centers and health workers.</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {features.map((f, i) => (
              <div key={i} className={`feature-card bg-black/25 backdrop-blur-sm rounded-2xl border border-white/25 hover:bg-black/35 hover:border-green-300 p-6 reveal animation-delay-${(i % 4) * 100 + 100} transition-all duration-300`}>
                <div className="w-12 h-12 rounded-xl bg-white/20 flex items-center justify-center text-2xl mb-4">{f.icon}</div>
                <h3 className="font-bold text-white mb-2 text-shadow-soft">{f.title}</h3>
                <p className="text-green-100 text-sm leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section ref={stepsRef} className="relative z-10 py-24 px-6">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-16 reveal">
            <span className="text-green-300 font-semibold text-sm uppercase tracking-widest text-shadow-soft">Process</span>
            <h2 className="text-3xl md:text-4xl font-extrabold text-white mt-2 text-shadow-strong">How it works</h2>
          </div>
          <div className="grid md:grid-cols-3 gap-8">
            {steps.map((s, i) => (
              <div key={i} className={`reveal animation-delay-${i * 200 + 100} bg-black/25 backdrop-blur-sm rounded-2xl p-6 border border-white/25 hover:bg-black/35 transition-all duration-300`}>
                <div className="text-5xl font-extrabold text-white mb-3 text-shadow-strong">{s.num}</div>
                <h3 className="text-lg font-bold text-white mb-2 text-shadow-soft">{s.title}</h3>
                <p className="text-green-100 text-sm leading-relaxed">{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section ref={appRef} className="relative z-10 py-24 px-6">
        <div className="max-w-2xl mx-auto text-center reveal">
          <span className="text-green-300 font-semibold text-sm uppercase tracking-widest text-shadow-soft">Get the App</span>
          <h2 className="text-3xl md:text-4xl font-extrabold text-white mt-2 mb-4 text-shadow-strong">Download DaycareLog</h2>
          <p className="text-green-100 mb-8 max-w-xl mx-auto font-medium">
            Install it on your laptop or phone for one-tap access — no app store, no download size, works just like a native app.
          </p>

          <div className="flex items-center justify-center gap-10 mb-8">
            <div className="flex flex-col items-center gap-2 text-white/80">
              <span className="w-14 h-14 rounded-2xl glass flex items-center justify-center text-2xl">💻</span>
              <span className="text-xs font-medium">Laptop</span>
            </div>
            <div className="flex flex-col items-center gap-2 text-white/80">
              <span className="w-14 h-14 rounded-2xl glass flex items-center justify-center text-2xl">📱</span>
              <span className="text-xs font-medium">Phone</span>
            </div>
          </div>

          {installed ? (
            <p className="inline-flex items-center gap-2 glass text-white font-semibold px-6 py-3.5 rounded-2xl">
              ✓ Already installed on this device
            </p>
          ) : canPromptInstall ? (
            <button
              onClick={handleInstall}
              className="bg-white text-primary-700 font-bold text-base px-8 py-4 rounded-2xl hover:bg-gray-50 transition-all shadow-xl hover:shadow-2xl hover:-translate-y-1 duration-200"
            >
              ⬇ Download the App Now
            </button>
          ) : isIos ? (
            <div className="glass text-white text-sm px-6 py-4 rounded-2xl max-w-sm mx-auto">
              <p className="font-semibold mb-1">On iPhone/iPad:</p>
              <p className="text-green-100">Tap the Share button in Safari, then "Add to Home Screen".</p>
            </div>
          ) : (
            <p className="text-green-200 text-sm">Open this page in Chrome or Edge on your laptop or phone to install.</p>
          )}
        </div>
      </section>

      <section ref={ctaRef} className="relative z-10 py-24 px-6">
        <div className="max-w-2xl mx-auto text-center reveal">
          <h2 className="text-3xl md:text-4xl font-extrabold text-white mb-4 text-shadow-strong">
            Ready to modernize your daycare center?
          </h2>
          <p className="text-white mb-10 font-medium text-shadow-soft">
            Join hundreds of barangay health workers already using DaycareLog.
          </p>
          <Link to="/register" className="inline-block bg-white text-primary-700 font-bold text-lg px-10 py-4 rounded-2xl hover:bg-gray-50 transition-all shadow-2xl hover:-translate-y-1 duration-200">
            Create Your Free Account
          </Link>
        </div>
      </section>

      <footer className="relative z-10 bg-black/20 backdrop-blur-sm text-green-100 py-8 px-6 text-center text-sm">
        <div className="flex items-center justify-center gap-2 mb-1">
          <img src="/favicon.svg" alt="" className="w-6 h-6" />
          <p className="text-white font-semibold">DaycareLog</p>
        </div>
        <p>Barangay Enrollment & Health Tracking System · Philippines</p>
        <p className="mt-2 text-green-200 text-xs">© {new Date().getFullYear()} DaycareLog. All rights reserved.</p>
      </footer>
    </div>
  )
}

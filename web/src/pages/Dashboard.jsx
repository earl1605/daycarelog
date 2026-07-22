import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BarChart, Bar, LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { useTheme } from '../contexts/ThemeContext'
import { toLocalDateString } from '../utils/date'
import { computeNutritionalTrend, computeImmunizationDetail } from '../utils/healthTrends'
import StatCard from '../components/StatCard'
import RecentActivityWidget from '../components/RecentActivityWidget'
import { UsersIcon, CheckIcon, ClipboardIcon, CalendarIcon, BarChartIcon, PlusIcon, WaveIcon } from '../components/icons'
import toast from 'react-hot-toast'

// Status-severity colors (good/warning/serious/critical), not arbitrary categorical hues.
const STATUS_HEX = { Normal: '#0ca30c', Underweight: '#ec835a', 'Severely Underweight': '#d03b3b', Overweight: '#fab219' }
const IMMUNIZATION_BUCKET_HEX = { 'Fully Immunized': '#0ca30c', 'Partially Immunized': '#fab219', 'Not Started': '#d03b3b' }

export default function Dashboard() {
  const { user } = useAuth()
  const { theme } = useTheme()
  const isDark = theme === 'dark'
  const [children,     setChildren]     = useState([])
  const [chartData,    setChartData]    = useState([])
  const [todayAtt,     setTodayAtt]     = useState([])
  const [nutritionalTrend,   setNutritionalTrend]   = useState([])
  const [immunizationDetail, setImmunizationDetail] = useState({ buckets: [], perVaccine: [] })
  const [loading,      setLoading]      = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const today = toLocalDateString()
        const [kids, att, health, immunizations, schedule] = await Promise.all([
          api.children.list(),
          api.attendance.getByDate(today),
          api.health.list(),
          api.immunizations.list(),
          api.immunizations.schedule(),
        ])
        setChildren(kids)
        setTodayAtt(att)

        const activeKids = kids.filter(c => c.enrollmentStatus === 'active')

        const now = new Date()
        const mondayOffset = now.getDay() === 0 ? 6 : now.getDay() - 1
        const monday = new Date(now)
        monday.setDate(now.getDate() - mondayOffset)
        const days = Array.from({ length: 5 }, (_, i) => {
          const d = new Date(monday); d.setDate(monday.getDate() + i)
          return toLocalDateString(d)
        })
        const weekAtt = await api.attendance.getRange(days[0], days[4])
        setChartData(days.map(date => ({
          date: new Date(date + 'T00:00:00').toLocaleDateString('en-PH', { weekday: 'short' }),
          present: weekAtt.filter(a => a.date === date && a.status === 'present').length,
          absent:  weekAtt.filter(a => a.date === date && a.status === 'absent').length,
        })))

        setNutritionalTrend(computeNutritionalTrend(activeKids, health))
        setImmunizationDetail(computeImmunizationDetail(activeKids, immunizations, schedule))
      } catch (e) {
        toast.error('Failed to load dashboard')
      }
      setLoading(false)
    }
    load()
  }, [])

  const active       = children.filter(c => c.enrollmentStatus === 'active').length
  const presentToday = todayAtt.filter(a => a.status === 'present').length

  if (loading) return <div className="flex items-center justify-center h-64"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>

  const tooltipStyle = {
    borderRadius: '10px',
    border: `1px solid ${isDark ? '#374151' : '#E5E7EB'}`,
    boxShadow: '0 4px 16px rgba(0,0,0,0.06)',
    fontSize: '13px',
    backgroundColor: isDark ? '#1f2937' : '#fff',
    color: isDark ? '#f9fafb' : '#111827',
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-[22px] font-bold text-gray-900 flex items-center gap-2">
          Good {new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 17 ? 'afternoon' : 'evening'},{' '}
          {user?.fullName?.split(' ')[0] ?? 'there'}
          <WaveIcon width={22} height={22} className="text-amber-500" strokeWidth={2} />
        </h1>
        <p className="text-gray-500 text-sm mt-1">
          {new Date().toLocaleDateString('en-PH', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
        </p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <StatCard icon={UsersIcon}     label="Active Children"  value={active}          color="primary" />
        <StatCard icon={CheckIcon}     label="Present Today"    value={presentToday}    color="blue"    />
        <StatCard icon={ClipboardIcon} label="Total Enrolled"   value={children.length} color="violet"  />
        <StatCard icon={CalendarIcon}  label="Attendance Rate"
          value={active > 0 ? `${Math.round((presentToday / active) * 100)}%` : '—'} color="amber" />
      </div>

      <RecentActivityWidget />

      <div className="bg-white rounded-xl border border-gray-200/70 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-[15px] font-bold text-gray-900">Weekly Attendance</h2>
          <Link to="/attendance" className="text-primary-700 text-sm font-medium hover:underline">View all →</Link>
        </div>
        <ResponsiveContainer width="100%" height={170}>
          <BarChart data={chartData} barCategoryGap="30%" barGap={4}>
            <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#374151' : '#F0F0EE'} vertical={false} />
            <XAxis dataKey="date" tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} allowDecimals={false} width={24} />
            <Tooltip cursor={{ fill: isDark ? '#374151' : '#F7F7F5' }} contentStyle={tooltipStyle} />
            <Bar dataKey="present" name="Present" fill="#16a34a" radius={[4,4,0,0]} maxBarSize={40} />
            <Bar dataKey="absent"  name="Absent"  fill={isDark ? '#4b5563' : '#E5E7EB'} radius={[4,4,0,0]} maxBarSize={40} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-white rounded-xl border border-gray-200/70 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-[15px] font-bold text-gray-900">Nutritional Status Trend</h2>
          <Link to="/reports" className="text-primary-700 text-sm font-medium hover:underline">View report →</Link>
        </div>
        {active === 0 ? (
          <p className="text-gray-400 text-sm">No active children yet.</p>
        ) : (
          <ResponsiveContainer width="100%" height={190}>
            <LineChart data={nutritionalTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#374151' : '#F0F0EE'} vertical={false} />
              <XAxis dataKey="month" tick={{ fontSize: 11, fill: '#9CA3AF' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 11, fill: '#9CA3AF' }} axisLine={false} tickLine={false} domain={[0, 100]} unit="%" width={36} />
              <Tooltip contentStyle={tooltipStyle} formatter={value => `${value}%`} />
              <Legend wrapperStyle={{ fontSize: '11px' }} />
              {Object.entries(STATUS_HEX).map(([key, color]) => (
                <Line key={key} type="monotone" dataKey={key} stroke={color} strokeWidth={2} dot={{ r: 3 }} />
              ))}
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="bg-white rounded-xl border border-gray-200/70 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-[15px] font-bold text-gray-900">Immunization Coverage</h2>
          <Link to="/reports" className="text-primary-700 text-sm font-medium hover:underline">View report →</Link>
        </div>
        {active === 0 ? (
          <p className="text-gray-400 text-sm">No active children yet.</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 items-center">
            <ResponsiveContainer width="100%" height={190}>
              <PieChart>
                <Pie data={immunizationDetail.buckets} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={48} outerRadius={75} paddingAngle={2}>
                  {immunizationDetail.buckets.map(b => <Cell key={b.name} fill={IMMUNIZATION_BUCKET_HEX[b.name]} />)}
                </Pie>
                <Tooltip contentStyle={tooltipStyle} />
                <Legend verticalAlign="bottom" height={32} wrapperStyle={{ fontSize: '11px' }} />
              </PieChart>
            </ResponsiveContainer>
            <div className="space-y-1.5">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">By vaccine</p>
              {immunizationDetail.perVaccine.map(v => {
                const pct = v.total > 0 ? Math.round((v.covered / v.total) * 100) : 0
                return (
                  <div key={v.vaccine} className="flex items-center justify-between text-sm">
                    <span className="text-gray-600">{v.vaccine}</span>
                    <span className="font-medium text-gray-900">{v.covered}/{v.total} <span className="text-gray-400 font-normal">({pct}%)</span></span>
                  </div>
                )
              })}
            </div>
          </div>
        )}
      </div>

      <div>
        <h2 className="text-[15px] font-bold text-gray-900 mb-3">Quick Actions</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
          {[
            { to: '/children',     icon: PlusIcon,      label: 'Add Child' },
            { to: '/attendance',   icon: ClipboardIcon, label: 'Take Attendance' },
            { to: '/reports',      icon: BarChartIcon,  label: 'View Reports' },
          ].map(a => (
            <Link
              key={a.to}
              to={a.to}
              className="bg-[#FAFAFA] rounded-xl border border-gray-200/70 p-3 h-20 flex flex-col items-center justify-center gap-1.5 text-center transition-colors duration-150 hover:bg-gray-100/80 hover:border-gray-300"
            >
              <span className="w-7 h-7 rounded-full bg-primary-50 text-primary-600 flex items-center justify-center shrink-0">
                <a.icon width={15} height={15} />
              </span>
              <span className="text-[12px] font-medium text-gray-700">{a.label}</span>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}

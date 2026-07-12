import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Cell, LabelList, ResponsiveContainer } from 'recharts'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { useTheme } from '../contexts/ThemeContext'
import { toLocalDateString } from '../utils/date'
import { classifyNutritionalStatus } from '../utils/nutritionalStatus'
import StatCard from '../components/StatCard'
import { UsersIcon, CheckIcon, ClipboardIcon, CalendarIcon, BarChartIcon, PlusIcon } from '../components/icons'
import toast from 'react-hot-toast'

// Status-severity colors (good/warning/serious/critical), not arbitrary categorical hues --
// Unknown isn't a severity, so it gets neutral muted ink instead of a status role.
const STATUS_HEX = { Normal: '#0ca30c', Underweight: '#ec835a', 'Severely Underweight': '#d03b3b', Overweight: '#fab219', Unknown: '#898781' }

export default function Dashboard() {
  const { user } = useAuth()
  const { theme } = useTheme()
  const isDark = theme === 'dark'
  const [children,     setChildren]     = useState([])
  const [chartData,    setChartData]    = useState([])
  const [todayAtt,     setTodayAtt]     = useState([])
  const [healthRecords, setHealthRecords] = useState([])
  const [immunizationCoverage, setImmunizationCoverage] = useState([])
  const [loading,      setLoading]      = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const today = toLocalDateString()
        const [kids, att, health, report] = await Promise.all([
          api.children.list(),
          api.attendance.getByDate(today),
          api.health.list(),
          api.reports.monthly(today.slice(0, 7)),
        ])
        setChildren(kids)
        setTodayAtt(att)
        setHealthRecords(health)
        setImmunizationCoverage(report.immunizationCoverage ?? [])

        const now = new Date()
        const mondayOffset = now.getDay() === 0 ? 6 : now.getDay() - 1
        const monday = new Date(now)
        monday.setDate(now.getDate() - mondayOffset)
        const days = Array.from({ length: 5 }, (_, i) => {
          const d = new Date(monday); d.setDate(monday.getDate() + i)
          return toLocalDateString(d)
        })
        const start = days[0], end = days[4]
        const weekAtt = await api.attendance.getRange(start, end)
        setChartData(days.map(date => ({
          date: new Date(date + 'T00:00:00').toLocaleDateString('en-PH', { weekday: 'short' }),
          present: weekAtt.filter(a => a.date === date && a.status === 'present').length,
          absent:  weekAtt.filter(a => a.date === date && a.status === 'absent').length,
        })))
      } catch (e) {
        toast.error('Failed to load dashboard')
      }
      setLoading(false)
    }
    load()
  }, [])

  const active       = children.filter(c => c.enrollmentStatus === 'active').length
  const presentToday = todayAtt.filter(a => a.status === 'present').length

  const activeChildren = children.filter(c => c.enrollmentStatus === 'active')
  const latestHealthByChild = {}
  ;[...healthRecords].sort((a, b) => a.measurementDate.localeCompare(b.measurementDate))
    .forEach(r => { latestHealthByChild[r.childId] = r })
  const nutritionalCounts = { Normal: 0, Underweight: 0, 'Severely Underweight': 0, Overweight: 0, Unknown: 0 }
  activeChildren.forEach(c => {
    const r = latestHealthByChild[c.id]
    const status = r ? classifyNutritionalStatus(r.weightKg, c.dateOfBirth, c.sex) : null
    const label = status?.label ?? 'Unknown'
    nutritionalCounts[label] = (nutritionalCounts[label] ?? 0) + 1
  })
  const nutritionalChartData = Object.entries(nutritionalCounts).map(([name, value]) => ({ name, value }))

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
    <div className="space-y-8">
      <div>
        <h1 className="text-[22px] font-bold text-gray-900">
          Good {new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 17 ? 'afternoon' : 'evening'},{' '}
          {user?.fullName?.split(' ')[0] ?? 'there'} 👋
        </h1>
        <p className="text-gray-500 text-sm mt-1">
          {new Date().toLocaleDateString('en-PH', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
        </p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={UsersIcon}     label="Active Children"  value={active}          color="primary" />
        <StatCard icon={CheckIcon}     label="Present Today"    value={presentToday}    color="blue"    />
        <StatCard icon={ClipboardIcon} label="Total Enrolled"   value={children.length} color="violet"  />
        <StatCard icon={CalendarIcon}  label="Attendance Rate"
          value={active > 0 ? `${Math.round((presentToday / active) * 100)}%` : '—'} color="amber" />
      </div>

      <div className="bg-white rounded-xl border border-gray-200/70 p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-[17px] font-bold text-gray-900">Weekly Attendance</h2>
          <Link to="/attendance" className="text-primary-700 text-sm font-medium hover:underline">View all →</Link>
        </div>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={chartData} barCategoryGap="30%" barGap={4}>
            <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#374151' : '#F0F0EE'} vertical={false} />
            <XAxis dataKey="date" tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} allowDecimals={false} />
            <Tooltip cursor={{ fill: isDark ? '#374151' : '#F7F7F5' }} contentStyle={tooltipStyle} />
            <Bar dataKey="present" name="Present" fill="#16a34a" radius={[4,4,0,0]} maxBarSize={40} />
            <Bar dataKey="absent"  name="Absent"  fill={isDark ? '#4b5563' : '#E5E7EB'} radius={[4,4,0,0]} maxBarSize={40} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-white rounded-xl border border-gray-200/70 p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-[17px] font-bold text-gray-900">Nutritional Status</h2>
          <Link to="/reports" className="text-primary-700 text-sm font-medium hover:underline">View report →</Link>
        </div>
        {active === 0 ? (
          <p className="text-gray-400 text-sm">No active children yet.</p>
        ) : (
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={nutritionalChartData} barCategoryGap="25%">
              <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#374151' : '#F0F0EE'} vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#9CA3AF' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip cursor={{ fill: isDark ? '#374151' : '#F7F7F5' }} contentStyle={tooltipStyle} />
              <Bar dataKey="value" name="Children" radius={[4, 4, 0, 0]} maxBarSize={56}>
                {nutritionalChartData.map(d => <Cell key={d.name} fill={STATUS_HEX[d.name] ?? '#d1d5db'} />)}
                <LabelList dataKey="value" position="top" style={{ fontSize: 12, fontWeight: 600, fill: isDark ? '#f9fafb' : '#111827' }} />
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="bg-white rounded-xl border border-gray-200/70 p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-[17px] font-bold text-gray-900">Immunization Coverage</h2>
          <Link to="/reports" className="text-primary-700 text-sm font-medium hover:underline">View report →</Link>
        </div>
        {immunizationCoverage.length === 0 ? (
          <p className="text-gray-400 text-sm">No vaccine schedule data.</p>
        ) : (
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={immunizationCoverage} barCategoryGap="25%">
              <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#374151' : '#F0F0EE'} vertical={false} />
              <XAxis dataKey="vaccine" tick={{ fontSize: 11, fill: '#9CA3AF' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip
                cursor={{ fill: isDark ? '#374151' : '#F7F7F5' }}
                contentStyle={tooltipStyle}
                formatter={(value, _name, props) => [`${value} / ${props.payload.total}`, 'Covered']}
              />
              <Bar dataKey="covered" name="Covered" fill="#16a34a" radius={[4, 4, 0, 0]} maxBarSize={56} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      <div>
        <h2 className="text-[17px] font-bold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          {[
            { to: '/children',     icon: PlusIcon,      label: 'Add Child' },
            { to: '/attendance',   icon: ClipboardIcon, label: 'Take Attendance' },
            { to: '/reports',      icon: BarChartIcon,  label: 'View Reports' },
          ].map(a => (
            <Link
              key={a.to}
              to={a.to}
              className="bg-[#FAFAFA] rounded-xl border border-gray-200/70 p-4 h-28 flex flex-col items-center justify-center gap-2.5 text-center transition-colors duration-150 hover:bg-gray-100/80 hover:border-gray-300"
            >
              <span className="w-9 h-9 rounded-full bg-primary-50 text-primary-600 flex items-center justify-center shrink-0">
                <a.icon width={18} height={18} />
              </span>
              <span className="text-[13px] font-medium text-gray-700">{a.label}</span>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}

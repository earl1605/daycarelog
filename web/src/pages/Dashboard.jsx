import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { toLocalDateString } from '../utils/date'
import StatCard from '../components/StatCard'
import { UsersIcon, CheckIcon, ClipboardIcon, CalendarIcon, HeartIcon, BarChartIcon, PlusIcon } from '../components/icons'
import toast from 'react-hot-toast'

export default function Dashboard() {
  const { user } = useAuth()
  const [children,   setChildren]   = useState([])
  const [chartData,  setChartData]  = useState([])
  const [todayAtt,   setTodayAtt]   = useState([])
  const [loading,    setLoading]    = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const today = toLocalDateString()
        const [kids, att] = await Promise.all([
          api.children.list(),
          api.attendance.getByDate(today),
        ])
        setChildren(kids)
        setTodayAtt(att)

        // This week's Monday through Friday only — the daycare doesn't operate on
        // weekends, so the chart never shows Sat/Sun columns regardless of what day
        // today happens to be.
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

  if (loading) return <div className="flex items-center justify-center h-64"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>

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
            <CartesianGrid strokeDasharray="3 3" stroke="#F0F0EE" vertical={false} />
            <XAxis dataKey="date" tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} allowDecimals={false} />
            <Tooltip
              cursor={{ fill: '#F7F7F5' }}
              contentStyle={{ borderRadius: '10px', border: '1px solid #E5E7EB', boxShadow: '0 4px 16px rgba(0,0,0,0.06)', fontSize: '13px' }}
            />
            <Bar dataKey="present" name="Present" fill="#16a34a" radius={[4,4,0,0]} />
            <Bar dataKey="absent"  name="Absent"  fill="#E5E7EB" radius={[4,4,0,0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div>
        <h2 className="text-[17px] font-bold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {[
            { to: '/children',     icon: PlusIcon,      label: 'Add Child' },
            { to: '/attendance',   icon: ClipboardIcon, label: 'Take Attendance' },
            { to: '/health/new',   icon: HeartIcon,     label: 'Health Record' },
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

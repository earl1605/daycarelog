import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import StatCard from '../components/StatCard'
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
        const today = new Date().toISOString().split('T')[0]
        const [kids, att] = await Promise.all([
          api.children.list(),
          api.attendance.getByDate(today),
        ])
        setChildren(kids)
        setTodayAtt(att)

        const days = Array.from({ length: 7 }, (_, i) => {
          const d = new Date(); d.setDate(d.getDate() - (6 - i))
          return d.toISOString().split('T')[0]
        })
        const start = days[0], end = days[6]
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
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-extrabold text-gray-900">
          Good {new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 17 ? 'afternoon' : 'evening'},{' '}
          {user?.fullName?.split(' ')[0] ?? 'there'} 👋
        </h1>
        <p className="text-gray-500 text-sm mt-1">
          {new Date().toLocaleDateString('en-PH', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
        </p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon="👦" label="Active Children"  value={active}          color="primary" />
        <StatCard icon="✅" label="Present Today"     value={presentToday}    color="blue"    />
        <StatCard icon="📋" label="Total Enrolled"    value={children.length} color="purple"  />
        <StatCard icon="📅" label="Attendance Rate"
          value={active > 0 ? `${Math.round((presentToday / active) * 100)}%` : '—'} color="orange" />
      </div>

      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-bold text-gray-900">Weekly Attendance</h2>
          <Link to="/attendance" className="text-primary-600 text-sm font-medium hover:underline">View all →</Link>
        </div>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={chartData} barCategoryGap="30%" barGap={4}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
            <XAxis dataKey="date" tick={{ fontSize: 12, fill: '#6b7280' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 12, fill: '#6b7280' }} axisLine={false} tickLine={false} allowDecimals={false} />
            <Tooltip contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.1)' }} />
            <Bar dataKey="present" name="Present" fill="#16a34a" radius={[4,4,0,0]} />
            <Bar dataKey="absent"  name="Absent"  fill="#fca5a5" radius={[4,4,0,0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div>
        <h2 className="font-bold text-gray-900 mb-3">Quick Actions</h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {[
            { to: '/children/new', icon: '➕', label: 'Add Child' },
            { to: '/attendance',   icon: '📋', label: 'Take Attendance' },
            { to: '/health/new',   icon: '❤️', label: 'Health Record' },
            { to: '/reports',      icon: '📊', label: 'View Reports' },
          ].map(a => (
            <Link key={a.to} to={a.to} className="feature-card bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex flex-col items-center gap-2 text-center hover:border-primary-200">
              <span className="text-2xl">{a.icon}</span>
              <span className="text-sm font-medium text-gray-700">{a.label}</span>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}

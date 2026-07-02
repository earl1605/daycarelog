import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import StatCard from '../components/StatCard'
import { UsersIcon, CheckIcon, ClipboardIcon, HeartIcon } from '../components/icons'
import { formatAge } from '../utils/nutritionalStatus'
import toast from 'react-hot-toast'

export default function ParentDashboard() {
  const { user } = useAuth()
  const [children,   setChildren]   = useState([])
  const [attendance, setAttendance] = useState([])
  const [loading,    setLoading]    = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const [kids, att] = await Promise.all([api.children.mine(), api.attendance.mine()])
        setChildren(kids)
        setAttendance(att)
      } catch (e) {
        toast.error('Failed to load dashboard')
      }
      setLoading(false)
    }
    load()
  }, [])

  const today = new Date().toISOString().split('T')[0]
  const presentToday = attendance.filter(a => a.date === today && a.status === 'present').length

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

      <div className="grid grid-cols-2 gap-4">
        <StatCard icon={UsersIcon} label="My Children"   value={children.length} color="primary" />
        <StatCard icon={CheckIcon} label="Present Today" value={presentToday}    color="blue" />
      </div>

      <div>
        <h2 className="text-[17px] font-bold text-gray-900 mb-4">My Children</h2>
        {children.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-200/70 text-center py-16">
            <span className="inline-flex w-12 h-12 rounded-full bg-gray-100 text-gray-400 items-center justify-center mb-3">
              <UsersIcon width={22} height={22} />
            </span>
            <p className="font-medium text-gray-500">No children linked to your account yet</p>
            <p className="text-sm text-gray-400 mt-1">Contact the daycare staff to link your child's records.</p>
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2">
            {children.map(c => {
              const todayAtt = attendance.find(a => a.childId === c.id && a.date === today)
              const initials = `${c.firstName?.[0] ?? ''}${c.lastName?.[0] ?? ''}`.toUpperCase()
              return (
                <div key={c.id} className="bg-[#FAFAFA] rounded-xl border border-gray-200/70 p-5 flex items-center gap-4">
                  <div className="w-12 h-12 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-lg shrink-0">
                    {initials}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-gray-900 truncate">{c.firstName} {c.lastName}</p>
                    <p className="text-sm text-gray-500">{c.sex} · {formatAge(c.dateOfBirth)}</p>
                  </div>
                  <span className={`shrink-0 px-2.5 py-1 rounded-full text-xs font-medium capitalize ${
                    todayAtt?.status === 'present' ? 'bg-green-100 text-green-700' :
                    todayAtt ? 'bg-gray-100 text-gray-500' : 'bg-gray-100 text-gray-400'
                  }`}>
                    {todayAtt?.status ?? 'No record'}
                  </span>
                </div>
              )
            })}
          </div>
        )}
      </div>

      <div>
        <h2 className="text-[17px] font-bold text-gray-900 mb-4">Quick Links</h2>
        <div className="grid grid-cols-2 gap-4">
          <Link to="/parent/attendance"
            className="bg-[#FAFAFA] rounded-xl border border-gray-200/70 p-4 h-24 flex flex-col items-center justify-center gap-2.5 text-center transition-colors duration-150 hover:bg-gray-100/80 hover:border-gray-300">
            <span className="w-9 h-9 rounded-full bg-primary-50 text-primary-600 flex items-center justify-center shrink-0">
              <ClipboardIcon width={18} height={18} />
            </span>
            <span className="text-[13px] font-medium text-gray-700">Attendance</span>
          </Link>
          <Link to="/parent/health"
            className="bg-[#FAFAFA] rounded-xl border border-gray-200/70 p-4 h-24 flex flex-col items-center justify-center gap-2.5 text-center transition-colors duration-150 hover:bg-gray-100/80 hover:border-gray-300">
            <span className="w-9 h-9 rounded-full bg-primary-50 text-primary-600 flex items-center justify-center shrink-0">
              <HeartIcon width={18} height={18} />
            </span>
            <span className="text-[13px] font-medium text-gray-700">Health Records</span>
          </Link>
        </div>
      </div>
    </div>
  )
}

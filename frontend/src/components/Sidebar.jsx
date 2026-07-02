import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import toast from 'react-hot-toast'
import {
  HomeIcon, UsersIcon, ClipboardIcon, HeartIcon,
  BarChartIcon, SettingsIcon, ChevronDownIcon, LogOutIcon,
} from './icons'

const groups = [
  {
    label: 'Main',
    items: [
      { to: '/dashboard',  icon: HomeIcon,      label: 'Dashboard' },
      { to: '/children',   icon: UsersIcon,     label: 'Children' },
      { to: '/attendance', icon: ClipboardIcon, label: 'Attendance' },
      { to: '/health',     icon: HeartIcon,     label: 'Health Records' },
    ],
  },
  {
    label: 'Management',
    items: [
      { to: '/reports',  icon: BarChartIcon, label: 'Reports' },
      { to: '/settings', icon: SettingsIcon, label: 'Settings', chevron: true },
    ],
  },
]

const adminItem = { to: '/users', icon: UsersIcon, label: 'Users' }

export default function Sidebar({ open, onClose }) {
  const { user, signOut, isAdmin } = useAuth()
  const navigate = useNavigate()

  function handleSignOut() {
    signOut()
    toast.success('Signed out')
    navigate('/')
  }

  const linkClass = ({ isActive }) =>
    `group flex items-center gap-2.5 px-3 py-2 rounded-lg text-[14px] font-medium transition-colors duration-150 ${
      isActive
        ? 'bg-primary-100/70 text-gray-900'
        : 'text-gray-500 hover:bg-gray-900/[0.04] hover:text-gray-800'
    }`

  const displayName = [user?.firstName, user?.middleName, user?.lastName, user?.suffix].filter(Boolean).join(' ') || user?.email?.split('@')[0] || 'User'
  const initial = (user?.firstName?.[0] ?? user?.email?.[0] ?? 'U').toUpperCase()

  function renderItem(item) {
    const Icon = item.icon
    return (
      <NavLink key={item.to} to={item.to} className={linkClass} onClick={onClose}>
        {({ isActive }) => (
          <>
            <Icon className={isActive ? 'text-primary-700 shrink-0' : 'text-gray-400 shrink-0 group-hover:text-gray-500'} />
            <span className="flex-1 truncate">{item.label}</span>
            {item.chevron && <ChevronDownIcon width={14} height={14} className="text-gray-400 shrink-0" />}
          </>
        )}
      </NavLink>
    )
  }

  return (
    <>
      {open && <div className="fixed inset-0 bg-black/30 z-20 lg:hidden" onClick={onClose} />}

      <aside className={`fixed top-0 left-0 h-full w-64 bg-[#F7F7F5] border-r border-gray-200/70 z-30 flex flex-col transform transition-transform duration-300
        ${open ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0 lg:static lg:z-auto`}>

        <div className="flex items-center gap-2.5 px-4 py-4 border-b border-gray-200/70">
          <img src="/favicon.svg" alt="DaycareLog" className="w-7 h-7 flex-shrink-0" />
          <div className="min-w-0">
            <p className="font-bold text-gray-900 text-[15px] leading-tight truncate">DaycareLog</p>
            <p className="text-[11px] text-gray-400 tracking-wide uppercase truncate">Barangay System</p>
          </div>
        </div>

        <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-5">
          {groups.map((group, gi) => (
            <div key={group.label}>
              {gi > 0 && <div className="h-px bg-gray-200/70 mb-5 -mt-2.5 mx-1" />}
              <p className="px-3 pb-1.5 text-[11px] font-semibold text-gray-400 uppercase tracking-wider">{group.label}</p>
              <div className="space-y-0.5">
                {group.items.map(renderItem)}
              </div>
            </div>
          ))}

          {isAdmin && (
            <div>
              <div className="h-px bg-gray-200/70 mb-5 -mt-2.5 mx-1" />
              <p className="px-3 pb-1.5 text-[11px] font-semibold text-gray-400 uppercase tracking-wider">Admin</p>
              <div className="space-y-0.5">
                {renderItem(adminItem)}
              </div>
            </div>
          )}
        </nav>

        <div className="p-3 border-t border-gray-200/70">
          <div className="flex items-center gap-2.5 px-1 py-2 mb-1">
            {user?.profilePhoto ? (
              <img src={user.profilePhoto} alt="Profile" className="w-8 h-8 rounded-full object-cover flex-shrink-0 ring-2 ring-primary-100" />
            ) : (
              <div className="w-8 h-8 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center font-semibold text-xs flex-shrink-0">
                {initial}
              </div>
            )}
            <div className="flex-1 min-w-0">
              <p className="text-[13px] font-medium text-gray-900 truncate">{displayName}</p>
              <p className="text-[11px] text-gray-400 capitalize">{user?.role ?? 'staff'}</p>
            </div>
          </div>
          <button
            onClick={handleSignOut}
            className="w-full flex items-center gap-2.5 text-[13px] text-gray-500 hover:text-gray-800 hover:bg-gray-900/[0.04] rounded-lg py-2 px-3 text-left transition-colors duration-150"
          >
            <LogOutIcon width={16} height={16} className="shrink-0" />
            Sign out
          </button>
        </div>
      </aside>
    </>
  )
}

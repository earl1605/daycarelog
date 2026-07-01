import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import toast from 'react-hot-toast'

const navItems = [
  { to: '/dashboard',  icon: '🏠', label: 'Dashboard' },
  { to: '/children',   icon: '👦', label: 'Children' },
  { to: '/attendance', icon: '📋', label: 'Attendance' },
  { to: '/health',     icon: '❤️', label: 'Health' },
  { to: '/reports',    icon: '📊', label: 'Reports' },
  { to: '/settings',   icon: '⚙️', label: 'Settings' },
]

const adminItems = [
  { to: '/users', icon: '👥', label: 'Users' },
]

export default function Sidebar({ open, onClose }) {
  const { user, signOut, isAdmin } = useAuth()
  const navigate = useNavigate()

  function handleSignOut() {
    signOut()
    toast.success('Signed out')
    navigate('/')
  }

  const linkClass = ({ isActive }) =>
    `flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-colors ${
      isActive ? 'bg-primary-600 text-white shadow-sm' : 'text-gray-600 hover:bg-gray-100'
    }`

  const displayName = [user?.firstName, user?.middleName, user?.lastName, user?.suffix].filter(Boolean).join(' ') || user?.email?.split('@')[0] || 'User'
  const initial = (user?.firstName?.[0] ?? user?.email?.[0] ?? 'U').toUpperCase()

  return (
    <>
      {open && <div className="fixed inset-0 bg-black/30 z-20 lg:hidden" onClick={onClose} />}

      <aside className={`fixed top-0 left-0 h-full w-64 bg-white border-r border-gray-100 z-30 flex flex-col transform transition-transform duration-300
        ${open ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0 lg:static lg:z-auto`}>

        <div className="flex flex-col items-center justify-center px-4 py-5 border-b border-gray-100">
          <div className="flex items-center gap-2.5 mb-1">
            <img src="/favicon.svg" alt="DaycareLog" className="w-9 h-9 flex-shrink-0" />
            <p className="font-extrabold text-gray-900 text-2xl leading-tight tracking-wide">
              {'DaycareLog'.split('').map((char, i) => (
                <span key={i} className="wave-letter">{char}</span>
              ))}
            </p>
          </div>
          <p className="text-xs text-gray-400 tracking-widest uppercase">Barangay System</p>
        </div>

        <nav className="flex-1 overflow-y-auto p-4 space-y-1">
          {navItems.map(item => (
            <NavLink key={item.to} to={item.to} className={linkClass} onClick={onClose}>
              <span className="text-base">{item.icon}</span>{item.label}
            </NavLink>
          ))}
          {isAdmin && (
            <>
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider px-4 pt-4 pb-1">Admin</p>
              {adminItems.map(item => (
                <NavLink key={item.to} to={item.to} className={linkClass} onClick={onClose}>
                  <span className="text-base">{item.icon}</span>{item.label}
                </NavLink>
              ))}
            </>
          )}
        </nav>

        <div className="p-4 border-t border-gray-100">
          <div className="flex items-center gap-3 mb-3">
            {user?.profilePhoto ? (
              <img src={user.profilePhoto} alt="Profile" className="w-9 h-9 rounded-full object-cover flex-shrink-0 ring-2 ring-primary-200" />
            ) : (
              <div className="w-9 h-9 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-sm flex-shrink-0">
                {initial}
              </div>
            )}
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">{displayName}</p>
              <p className="text-xs text-gray-400 capitalize">{user?.role ?? 'staff'}</p>
            </div>
          </div>
          <button onClick={handleSignOut} className="w-full text-sm text-red-500 hover:text-red-700 hover:bg-red-50 rounded-lg py-2 px-3 text-left transition-colors">
            Sign out
          </button>
        </div>
      </aside>
    </>
  )
}

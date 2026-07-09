import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function ProtectedRoute() {
  const { user, isEmailVerified } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (!isEmailVerified) return <Navigate to={`/check-email?email=${encodeURIComponent(user.email)}`} replace />
  return <Outlet />
}

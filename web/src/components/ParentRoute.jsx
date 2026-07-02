import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function ParentRoute() {
  const { isParent } = useAuth()
  return isParent ? <Outlet /> : <Navigate to="/dashboard" replace />
}

import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function StaffRoute() {
  const { isStaff } = useAuth()
  return isStaff ? <Outlet /> : <Navigate to="/parent/dashboard" replace />
}

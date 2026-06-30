import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider }   from './contexts/AuthContext'
import ProtectedRoute     from './components/ProtectedRoute'
import AdminRoute         from './components/AdminRoute'
import Layout             from './components/Layout'
import Landing            from './pages/Landing'
import Login              from './pages/Login'
import Register           from './pages/Register'
import Dashboard          from './pages/Dashboard'
import Children           from './pages/Children'
import ChildForm          from './pages/ChildForm'
import ChildDetail        from './pages/ChildDetail'
import Attendance         from './pages/Attendance'
import HealthRecords      from './pages/HealthRecords'
import HealthForm         from './pages/HealthForm'
import Reports            from './pages/Reports'
import Users              from './pages/Users'
import Settings           from './pages/Settings'

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/"         element={<Landing />} />
        <Route path="/login"    element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route path="/dashboard"         element={<Dashboard />} />
            <Route path="/children"          element={<Children />} />
            <Route path="/children/new"      element={<ChildForm />} />
            <Route path="/children/:id"      element={<ChildDetail />} />
            <Route path="/children/:id/edit" element={<ChildForm />} />
            <Route path="/attendance"        element={<Attendance />} />
            <Route path="/health"            element={<HealthRecords />} />
            <Route path="/health/new"        element={<HealthForm />} />
            <Route path="/reports"           element={<Reports />} />
            <Route element={<AdminRoute />}>
              <Route path="/users"           element={<Users />} />
            </Route>
            <Route path="/settings"          element={<Settings />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}

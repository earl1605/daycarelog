import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider }   from './contexts/AuthContext'
import ProtectedRoute     from './components/ProtectedRoute'
import AdminRoute         from './components/AdminRoute'
import StaffRoute         from './components/StaffRoute'
import ParentRoute        from './components/ParentRoute'
import Layout             from './components/Layout'
import Landing            from './pages/Landing'
import Login              from './pages/Login'
import Register           from './pages/Register'
import CheckEmail         from './pages/CheckEmail'
import VerifyEmail        from './pages/VerifyEmail'
import Dashboard          from './pages/Dashboard'
import Children           from './pages/Children'
import ChildForm          from './pages/ChildForm'
import ChildDetail        from './pages/ChildDetail'
import Attendance         from './pages/Attendance'
import HealthRecords      from './pages/HealthRecords'
import ImmunizationForm   from './pages/ImmunizationForm'
import Reports            from './pages/Reports'
import Users              from './pages/Users'
import RecycleBin         from './pages/RecycleBin'
import Guardians          from './pages/Guardians'
import Settings           from './pages/Settings'
import ParentDashboard     from './pages/ParentDashboard'
import ParentAttendance    from './pages/ParentAttendance'
import ParentHealthRecords from './pages/ParentHealthRecords'
import ParentImmunizations from './pages/ParentImmunizations'

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/"             element={<Landing />} />
        <Route path="/login"        element={<Login />} />
        <Route path="/register"     element={<Register />} />
        <Route path="/check-email"  element={<CheckEmail />} />
        <Route path="/verify-email" element={<VerifyEmail />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route element={<StaffRoute />}>
              <Route path="/dashboard"         element={<Dashboard />} />
              <Route path="/children"          element={<Children />} />
              <Route path="/children/:id"      element={<ChildDetail />} />
              <Route path="/children/:id/edit" element={<ChildForm />} />
              <Route path="/attendance"        element={<Attendance />} />
              <Route path="/health"            element={<HealthRecords />} />
              <Route path="/immunizations/new" element={<ImmunizationForm />} />
              <Route path="/guardians"         element={<Guardians />} />
              <Route path="/reports"           element={<Reports />} />
              <Route element={<AdminRoute />}>
                <Route path="/users"           element={<Users />} />
                <Route path="/recycle-bin"     element={<RecycleBin />} />
              </Route>
            </Route>

            <Route element={<ParentRoute />}>
              <Route path="/parent/dashboard"  element={<ParentDashboard />} />
              <Route path="/parent/attendance" element={<ParentAttendance />} />
              <Route path="/parent/health"     element={<ParentHealthRecords />} />
              <Route path="/parent/immunizations" element={<ParentImmunizations />} />
            </Route>

            <Route path="/settings"          element={<Settings />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}

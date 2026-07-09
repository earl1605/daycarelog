import { createContext, useContext, useEffect, useState } from 'react'
import { api } from '../lib/api'

const AuthContext = createContext({})

export function AuthProvider({ children }) {
  const [user,    setUser]    = useState(() => {
    try { return JSON.parse(localStorage.getItem('dcl_user')) } catch { return null }
  })
  const [loading, setLoading] = useState(false)

  const isAdmin  = user?.role === 'admin'
  const isStaff  = user?.role === 'admin' || user?.role === 'staff'
  const isParent = user?.role === 'parent'
  // Strict !== false (not just falsy): a cached user object saved before this
  // field existed has emailVerified === undefined, and must still be treated as
  // verified - it predates the requirement, same as the backend's column default.
  const isEmailVerified = user?.emailVerified !== false

  function persistSession(token, user) {
    localStorage.setItem('dcl_token', token)
    localStorage.setItem('dcl_user',  JSON.stringify(user))
    setUser(user)
  }

  async function signIn(email, password) {
    try {
      const res = await api.auth.login(email, password)
      persistSession(res.token, res.user)
      return { data: res, error: null }
    } catch (e) {
      return { data: null, error: { message: e.message } }
    }
  }

  async function signUp(email, password, firstName, lastName, middleName, suffix) {
    try {
      const res = await api.auth.register(email, password, firstName, lastName, middleName, suffix)
      return { data: res, error: null }
    } catch (e) {
      return { data: null, error: { message: e.message } }
    }
  }

  function signOut() {
    localStorage.removeItem('dcl_token')
    localStorage.removeItem('dcl_user')
    setUser(null)
  }

  function refreshUser(updated) {
    const merged = { ...user, ...updated }
    localStorage.setItem('dcl_user', JSON.stringify(merged))
    setUser(merged)
  }

  // Called by the verify-email link page and the check-email code form once the
  // backend confirms verification - both return a fresh token (emailVerified: true)
  // and the updated user in the same shape as signIn's response.
  function completeVerification(token, user) {
    persistSession(token, user)
  }

  return (
    <AuthContext.Provider value={{
      user, loading, signIn, signUp, signOut, refreshUser, completeVerification,
      isAdmin, isStaff, isParent, isEmailVerified,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}

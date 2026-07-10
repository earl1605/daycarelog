import { createContext, useContext, useEffect, useState } from 'react'
import { api } from '../lib/api'

const AuthContext = createContext({})

export function AuthProvider({ children }) {
  const [user,    setUser]    = useState(() => {
    try { return JSON.parse(localStorage.getItem('dcl_user')) } catch { return null }
  })
  const [loading, setLoading] = useState(false)

  const isSuperAdmin = user?.role === 'super_admin'
  const isAdmin  = isSuperAdmin || user?.role === 'admin'
  const isStaff  = isAdmin || user?.role === 'staff'
  const isParent = user?.role === 'parent'
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

  function completeVerification(token, user) {
    persistSession(token, user)
  }

  return (
    <AuthContext.Provider value={{
      user, loading, signIn, signUp, signOut, refreshUser, completeVerification,
      isAdmin, isSuperAdmin, isStaff, isParent, isEmailVerified,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}

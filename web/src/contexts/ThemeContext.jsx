import { createContext, useContext, useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'

const ThemeContext = createContext({})

function getInitialTheme() {
  const stored = localStorage.getItem('dcl_theme')
  if (stored === 'light' || stored === 'dark') return stored
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(getInitialTheme)
  const location = useLocation()

  const isPublicPage = ['/', '/login', '/register'].includes(location.pathname)

  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark' && !isPublicPage)
    localStorage.setItem('dcl_theme', theme)
  }, [theme, isPublicPage])

  function toggleTheme() {
    setTheme(t => t === 'dark' ? 'light' : 'dark')
  }

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme() {
  return useContext(ThemeContext)
}

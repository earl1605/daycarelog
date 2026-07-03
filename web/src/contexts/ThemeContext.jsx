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

  // Landing is a public marketing page with its own fixed gradient look —
  // it should never switch to dark mode, regardless of the stored preference.
  const isLanding = location.pathname === '/'

  // Tailwind's class-based dark mode strategy: toggling this class on <html> is
  // what every `dark:` variant (and the .dark CSS overrides in index.css) key off.
  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark' && !isLanding)
    localStorage.setItem('dcl_theme', theme)
  }, [theme, isLanding])

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

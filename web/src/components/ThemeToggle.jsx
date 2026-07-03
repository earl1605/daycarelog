import { useTheme } from '../contexts/ThemeContext'
import { SunIcon, MoonIcon } from './icons'

export default function ThemeToggle() {
  const { theme, toggleTheme } = useTheme()
  const isDark = theme === 'dark'

  return (
    <button
      type="button"
      onClick={toggleTheme}
      title={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
      aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
      className="w-full flex items-center gap-2.5 text-[13px] text-gray-500 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 hover:bg-gray-900/[0.04] dark:hover:bg-white/[0.06] rounded-lg py-2 px-3 text-left transition-colors duration-150"
    >
      {isDark ? <SunIcon width={16} height={16} className="shrink-0" /> : <MoonIcon width={16} height={16} className="shrink-0" />}
      {isDark ? 'Light mode' : 'Dark mode'}
    </button>
  )
}

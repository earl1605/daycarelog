// Minimal outline icon set (stroke-based, ~1.75px) used across the app shell.
const base = {
  width: 18,
  height: 18,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.75,
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
}

export function HomeIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M3.5 10.5 12 3.5l8.5 7" />
      <path d="M5.5 9.5V19a1 1 0 0 0 1 1h3.5v-5.8h4V20H17.5a1 1 0 0 0 1-1V9.5" />
    </svg>
  )
}

export function UsersIcon(props) {
  return (
    <svg {...base} {...props}>
      <circle cx="9" cy="8.2" r="3" />
      <path d="M3.8 20c0-2.9 2.3-5.2 5.2-5.2s5.2 2.3 5.2 5.2" />
      <circle cx="17" cy="9" r="2.3" />
      <path d="M15.3 20c.2-2.2 1.8-3.9 3.7-4.3" />
    </svg>
  )
}

export function ClipboardIcon(props) {
  return (
    <svg {...base} {...props}>
      <rect x="5.5" y="4.5" width="13" height="16" rx="2" />
      <path d="M9 4.5V4a1.5 1.5 0 0 1 1.5-1.5h3A1.5 1.5 0 0 1 15 4v.5" />
      <path d="M9 11.3 11 13.3l4-4.6" />
    </svg>
  )
}

export function HeartIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M12 19.5s-6.8-4.3-9-8.4C1.3 8 2.6 5 5.4 4.3c1.9-.5 3.6.5 4.8 2.2 1.2-1.7 2.9-2.7 4.8-2.2 2.8.7 4.1 3.7 2.4 6.8-2.2 4.1-9 8.4-9 8.4Z" />
    </svg>
  )
}

export function BarChartIcon(props) {
  return (
    <svg {...base} {...props}>
      <rect x="4" y="12.5" width="3.4" height="7.5" rx="0.9" />
      <rect x="10.3" y="7" width="3.4" height="13" rx="0.9" />
      <rect x="16.6" y="10" width="3.4" height="10" rx="0.9" />
    </svg>
  )
}

export function SettingsIcon(props) {
  return (
    <svg {...base} {...props}>
      <circle cx="12" cy="12" r="2.8" />
      <path d="M19.4 13.2a7.7 7.7 0 0 0 0-2.4l1.9-1.4-1.9-3.3-2.3.9a7.6 7.6 0 0 0-2-1.2L14.7 3h-3.8l-.4 2.5a7.6 7.6 0 0 0-2 1.2l-2.3-.9-1.9 3.3 1.9 1.4a7.7 7.7 0 0 0 0 2.4l-1.9 1.4 1.9 3.3 2.3-.9a7.6 7.6 0 0 0 2 1.2l.4 2.5h3.8l.4-2.5a7.6 7.6 0 0 0 2-1.2l2.3.9 1.9-3.3Z" />
    </svg>
  )
}

export function ChevronDownIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="m6 9 6 6 6-6" />
    </svg>
  )
}

export function LogOutIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M9 21H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3" />
      <path d="m16 17 5-5-5-5" />
      <path d="M21 12H9" />
    </svg>
  )
}

export function MenuIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M4 6h16M4 12h16M4 18h16" />
    </svg>
  )
}

export function CheckIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="m5 13 4.5 4.5L19 8" />
    </svg>
  )
}

export function CalendarIcon(props) {
  return (
    <svg {...base} {...props}>
      <rect x="3.5" y="5" width="17" height="15.5" rx="2" />
      <path d="M3.5 9.5h17" />
      <path d="M8 3v4M16 3v4" />
    </svg>
  )
}

export function PlusIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M12 5v14M5 12h14" />
    </svg>
  )
}

export function KeyIcon(props) {
  return (
    <svg {...base} {...props}>
      <circle cx="7.5" cy="15.5" r="3.3" />
      <path d="M9.8 13.2 18.5 4.5" />
      <path d="M14.5 8.5 17 11" />
      <path d="M17.3 5.7 19.8 8.2" />
    </svg>
  )
}

export function PauseIcon(props) {
  return (
    <svg {...base} {...props}>
      <rect x="7" y="5" width="3.2" height="14" rx="1" />
      <rect x="13.8" y="5" width="3.2" height="14" rx="1" />
    </svg>
  )
}

export function PlayIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M7 5.3v13.4a1 1 0 0 0 1.5.87l11-6.7a1 1 0 0 0 0-1.74l-11-6.7A1 1 0 0 0 7 5.3Z" />
    </svg>
  )
}

export function TrashIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M5 7h14" />
      <path d="M9.5 7V5a1.5 1.5 0 0 1 1.5-1.5h2A1.5 1.5 0 0 1 14.5 5v2" />
      <path d="M7 7l.8 12a2 2 0 0 0 2 1.9h4.4a2 2 0 0 0 2-1.9L17 7" />
      <path d="M10.2 11v6M13.8 11v6" />
    </svg>
  )
}

export function AlertTriangleIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M12 4.5 21 19.5H3Z" />
      <path d="M12 10v4" />
      <circle cx="12" cy="16.8" r="0.75" />
    </svg>
  )
}

export function CopyIcon(props) {
  return (
    <svg {...base} {...props}>
      <rect x="8.5" y="8.5" width="11" height="11" rx="2" />
      <path d="M15.5 8.5V6.5A2 2 0 0 0 13.5 4.5h-8a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2" />
    </svg>
  )
}

export function SunIcon(props) {
  return (
    <svg {...base} {...props}>
      <circle cx="12" cy="12" r="4.2" />
      <path d="M12 2.5v2.3M12 19.2v2.3M4.5 12H2.2M21.8 12h-2.3M5.6 5.6l1.6 1.6M16.8 16.8l1.6 1.6M18.4 5.6l-1.6 1.6M7.2 16.8l-1.6 1.6" />
    </svg>
  )
}

export function MoonIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M20 13.8A8.5 8.5 0 1 1 10.2 4a6.8 6.8 0 0 0 9.8 9.8Z" />
    </svg>
  )
}

export function EyeIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M1.5 12S5 5 12 5s10.5 7 10.5 7-3.5 7-10.5 7S1.5 12 1.5 12Z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  )
}

export function MailIcon(props) {
  return (
    <svg {...base} {...props}>
      <rect x="3.5" y="5.5" width="17" height="13" rx="2" />
      <path d="M4.5 7l7.5 6 7.5-6" />
    </svg>
  )
}

export function EyeOffIcon(props) {
  return (
    <svg {...base} {...props}>
      <path d="M3 3l18 18" />
      <path d="M10.6 5.1A10.6 10.6 0 0 1 12 5c7 0 10.5 7 10.5 7a13.6 13.6 0 0 1-3.1 4M6.6 6.6C3.5 8.6 1.5 12 1.5 12S5 19 12 19a10.7 10.7 0 0 0 4.4-.9" />
      <path d="M9.9 10.1a3 3 0 0 0 4 4" />
    </svg>
  )
}

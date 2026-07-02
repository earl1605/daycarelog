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

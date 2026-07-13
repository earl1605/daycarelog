import { useEffect, useRef, useState } from 'react'

// Small hand-drawn icon set matching this app's existing outline/stroke
// style (see components/icons.jsx) - kept local to this file so the
// component stays dependency-free rather than pulling in an icon library
// for six shapes.
function HeartIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
      <path d="M12 19.5s-6.8-4.3-9-8.4C1.3 8 2.6 5 5.4 4.3c1.9-.5 3.6.5 4.8 2.2 1.2-1.7 2.9-2.7 4.8-2.2 2.8.7 4.1 3.7 2.4 6.8-2.2 4.1-9 8.4-9 8.4Z" />
    </svg>
  )
}

function StarIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
      <path d="M12 2.5l2.4 5.9 6.4.5-4.9 4.1 1.6 6.3L12 15.9l-5.5 3.4 1.6-6.3-4.9-4.1 6.4-.5L12 2.5Z" />
    </svg>
  )
}

function SunIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.75} strokeLinecap="round" strokeLinejoin="round" {...props}>
      <circle cx="12" cy="12" r="4.2" />
      <path d="M12 2.5v2.3M12 19.2v2.3M4.5 12H2.2M21.8 12h-2.3M5.6 5.6l1.6 1.6M16.8 16.8l1.6 1.6M18.4 5.6l-1.6 1.6M7.2 16.8l-1.6 1.6" />
    </svg>
  )
}

function SmileIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.75} strokeLinecap="round" strokeLinejoin="round" {...props}>
      <circle cx="12" cy="12" r="9" />
      <circle cx="9" cy="10" r="1" fill="currentColor" stroke="none" />
      <circle cx="15" cy="10" r="1" fill="currentColor" stroke="none" />
      <path d="M8 14.5c1 1.3 2.5 2 4 2s3-.7 4-2" />
    </svg>
  )
}

function BabyIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.75} strokeLinecap="round" strokeLinejoin="round" {...props}>
      <circle cx="12" cy="13" r="7.5" />
      <circle cx="9.3" cy="12.5" r="1" fill="currentColor" stroke="none" />
      <circle cx="14.7" cy="12.5" r="1" fill="currentColor" stroke="none" />
      <path d="M9.5 16c.8.8 1.6 1.1 2.5 1.1s1.7-.3 2.5-1.1" />
      <path d="M12 5.5c-1 0-1.8.7-1.8 1.6 0 .6.4 1.1.9 1.4" />
    </svg>
  )
}

function BlocksIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" {...props}>
      <rect x="3" y="11" width="8" height="8" rx="1.5" />
      <rect x="13" y="11" width="8" height="8" rx="1.5" />
      <rect x="8" y="3" width="8" height="8" rx="1.5" />
    </svg>
  )
}

const ICON_COMPONENTS = [HeartIcon, StarIcon, SunIcon, SmileIcon, BabyIcon, BlocksIcon]

// This app's actual brand greens (tailwind.config.js primary scale) - a
// light/dark pair from the real palette, not an arbitrary unrelated color.
const COLORS = ['#4ade80', '#15803d']

const ICON_COUNT = 8
const MIN_SIZE = 24
const MAX_SIZE = 56
const MIN_SPEED = 20 // px/s
const MAX_SPEED = 60 // px/s
const MIN_OPACITY = 0.15
const MAX_OPACITY = 0.35

function rand(min, max) {
  return min + Math.random() * (max - min)
}

function createIcon(width, height) {
  const size = rand(MIN_SIZE, MAX_SIZE)
  const angle = rand(0, Math.PI * 2)
  const speed = rand(MIN_SPEED, MAX_SPEED)
  return {
    Icon: ICON_COMPONENTS[Math.floor(Math.random() * ICON_COMPONENTS.length)],
    color: COLORS[Math.floor(Math.random() * COLORS.length)],
    size,
    opacity: rand(MIN_OPACITY, MAX_OPACITY),
    x: rand(0, Math.max(0, width - size)),
    y: rand(0, Math.max(0, height - size)),
    vx: Math.cos(angle) * speed,
    vy: Math.sin(angle) * speed,
  }
}

// Playful DVD-screensaver-style background layer for the landing page.
// Position updates happen via direct DOM mutation (ref.style.transform) in
// a requestAnimationFrame loop, not React state, so 8 bouncing icons never
// trigger a re-render - only transform: translate3d changes per frame,
// which stays on the GPU compositor.
export default function FloatingIcons() {
  const nodeRefs = useRef([])
  const dataRef = useRef([])
  const boundsRef = useRef({ width: 0, height: 0 })
  const frameRef = useRef(null)
  const lastTimeRef = useRef(null)
  const pausedRef = useRef(false)

  const [icons] = useState(() => {
    const width = typeof window !== 'undefined' ? window.innerWidth : 1280
    const height = typeof window !== 'undefined' ? window.innerHeight : 800
    return Array.from({ length: ICON_COUNT }, () => createIcon(width, height))
  })

  useEffect(() => {
    dataRef.current = icons.map(i => ({ x: i.x, y: i.y, vx: i.vx, vy: i.vy, size: i.size }))
    boundsRef.current = { width: window.innerWidth, height: window.innerHeight }

    function handleResize() {
      boundsRef.current = { width: window.innerWidth, height: window.innerHeight }
    }
    window.addEventListener('resize', handleResize)

    const prefersReducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
    if (prefersReducedMotion) {
      // Icons still render at their (randomized, static) initial position via
      // the inline transform in JSX below - just no motion is ever started.
      return () => window.removeEventListener('resize', handleResize)
    }

    function handleVisibilityChange() {
      pausedRef.current = document.hidden
      if (!document.hidden) lastTimeRef.current = null // avoid a huge dt jump on resume
    }
    document.addEventListener('visibilitychange', handleVisibilityChange)

    function tick(time) {
      if (lastTimeRef.current == null) lastTimeRef.current = time
      const dt = Math.min((time - lastTimeRef.current) / 1000, 0.1)
      lastTimeRef.current = time

      if (!pausedRef.current) {
        const { width, height } = boundsRef.current
        dataRef.current.forEach((d, i) => {
          d.x += d.vx * dt
          d.y += d.vy * dt

          if (d.x <= 0) { d.x = 0; d.vx = Math.abs(d.vx) }
          else if (d.x >= width - d.size) { d.x = width - d.size; d.vx = -Math.abs(d.vx) }

          if (d.y <= 0) { d.y = 0; d.vy = Math.abs(d.vy) }
          else if (d.y >= height - d.size) { d.y = height - d.size; d.vy = -Math.abs(d.vy) }

          const el = nodeRefs.current[i]
          if (el) el.style.transform = `translate3d(${d.x}px, ${d.y}px, 0)`
        })
      }

      frameRef.current = requestAnimationFrame(tick)
    }
    frameRef.current = requestAnimationFrame(tick)

    return () => {
      if (frameRef.current) cancelAnimationFrame(frameRef.current)
      window.removeEventListener('resize', handleResize)
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }, [icons])

  return (
    <div className="fixed inset-0 z-0 overflow-hidden pointer-events-none" aria-hidden="true">
      {icons.map((icon, i) => {
        const { Icon } = icon
        return (
          <div
            key={i}
            ref={el => { nodeRefs.current[i] = el }}
            className="absolute top-0 left-0 will-change-transform"
            style={{
              width: icon.size,
              height: icon.size,
              opacity: icon.opacity,
              color: icon.color,
              transform: `translate3d(${icon.x}px, ${icon.y}px, 0)`,
            }}
          >
            <Icon width="100%" height="100%" />
          </div>
        )
      })}
    </div>
  )
}

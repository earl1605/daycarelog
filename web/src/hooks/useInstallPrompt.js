import { useEffect, useState } from 'react'

function isStandalone() {
  return window.matchMedia?.('(display-mode: standalone)').matches
    || window.navigator.standalone === true // iOS Safari
}

function isIos() {
  return /iphone|ipad|ipod/i.test(window.navigator.userAgent)
}

// Wraps the browser's native "Add to Home Screen"/"Install" flow
// (beforeinstallprompt) for Chrome/Edge on desktop and Android. iOS Safari
// never fires that event - there's no programmatic install there, only the
// manual Share -> Add to Home Screen flow, so callers should show
// instructions instead when `canPromptInstall` is false and `isIos` is true.
//
// The event itself is captured as early as possible by an inline script in
// index.html (window.__pwaInstallEvent), since beforeinstallprompt can fire
// before this hook's own effect has a chance to attach a listener - a
// missed event never fires again for that page load. This hook just reads
// whatever the early script captured, plus stays subscribed for later.
export default function useInstallPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState(() => window.__pwaInstallEvent ?? null)
  const [installed, setInstalled] = useState(isStandalone)

  useEffect(() => {
    if (window.__pwaInstallEvent) setDeferredPrompt(window.__pwaInstallEvent)

    function handleInstallReady() {
      setDeferredPrompt(window.__pwaInstallEvent)
    }
    function handleInstallDone() {
      setInstalled(true)
      setDeferredPrompt(null)
    }
    // Native events too, in case the early inline script isn't present
    // (e.g. this hook used outside this app's index.html in the future).
    function handleBeforeInstallPrompt(e) {
      e.preventDefault()
      setDeferredPrompt(e)
    }
    window.addEventListener('pwa-install-ready', handleInstallReady)
    window.addEventListener('pwa-install-done', handleInstallDone)
    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt)
    window.addEventListener('appinstalled', handleInstallDone)
    return () => {
      window.removeEventListener('pwa-install-ready', handleInstallReady)
      window.removeEventListener('pwa-install-done', handleInstallDone)
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt)
      window.removeEventListener('appinstalled', handleInstallDone)
    }
  }, [])

  async function promptInstall() {
    if (!deferredPrompt) return false
    deferredPrompt.prompt()
    const { outcome } = await deferredPrompt.userChoice
    setDeferredPrompt(null)
    window.__pwaInstallEvent = null
    return outcome === 'accepted'
  }

  return {
    canPromptInstall: Boolean(deferredPrompt),
    promptInstall,
    installed,
    isIos: isIos(),
  }
}

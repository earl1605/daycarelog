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
export default function useInstallPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState(null)
  const [installed, setInstalled] = useState(isStandalone)

  useEffect(() => {
    function handleBeforeInstallPrompt(e) {
      e.preventDefault()
      setDeferredPrompt(e)
    }
    function handleAppInstalled() {
      setInstalled(true)
      setDeferredPrompt(null)
    }
    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt)
    window.addEventListener('appinstalled', handleAppInstalled)
    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt)
      window.removeEventListener('appinstalled', handleAppInstalled)
    }
  }, [])

  async function promptInstall() {
    if (!deferredPrompt) return false
    deferredPrompt.prompt()
    const { outcome } = await deferredPrompt.userChoice
    setDeferredPrompt(null)
    return outcome === 'accepted'
  }

  return {
    canPromptInstall: Boolean(deferredPrompt),
    promptInstall,
    installed,
    isIos: isIos(),
  }
}

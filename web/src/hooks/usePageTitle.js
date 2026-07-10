import { useEffect } from 'react'

export default function usePageTitle(title) {
  useEffect(() => {
    const previous = document.title
    document.title = `${title} · DaycareLog`
    return () => { document.title = previous }
  }, [title])
}

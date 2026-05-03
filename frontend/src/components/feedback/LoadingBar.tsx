import { useEffect, useState } from 'react'
import { loadingEmitter } from '@/lib/loadingEmitter'

export function LoadingBar() {
  const [visible, setVisible] = useState(false)
  const [progress, setProgress] = useState(0)

  useEffect(() => {
    let hideTimer: ReturnType<typeof setTimeout>

    const onStart = () => {
      clearTimeout(hideTimer)
      setVisible(true)
      setProgress(0)
      setTimeout(() => setProgress(80), 16)
    }

    const onDone = () => {
      setProgress(100)
      hideTimer = setTimeout(() => {
        setVisible(false)
        setProgress(0)
      }, 400)
    }

    loadingEmitter.on('start', onStart)
    loadingEmitter.on('done', onDone)
    return () => {
      clearTimeout(hideTimer)
      loadingEmitter.off('start', onStart)
      loadingEmitter.off('done', onDone)
    }
  }, [])

  if (!visible) return null

  return (
    <div
      aria-hidden="true"
      className="pointer-events-none fixed top-0 left-0 z-[100] h-0.5 bg-blue-500"
      style={{
        width: `${progress}%`,
        transition:
          progress < 100
            ? 'width 8s cubic-bezier(0.05, 0.5, 0.5, 1)'
            : 'width 200ms ease-out',
      }}
    />
  )
}

export function useLoadingBar() {
  const start = () => loadingEmitter.emit('start')
  const done = () => loadingEmitter.emit('done')
  return { start, done }
}

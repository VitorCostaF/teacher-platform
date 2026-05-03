import { LoadingBar } from './LoadingBar'

export function LoadingBarProvider({ children }: { children: React.ReactNode }) {
  return (
    <>
      <LoadingBar />
      {children}
    </>
  )
}

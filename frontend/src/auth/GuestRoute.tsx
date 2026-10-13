import type { ReactNode } from 'react'
import { useEffect } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from './AuthContext'
import { SessionGate } from './SessionGate'

export function GuestRoute({ children }: { children: ReactNode }) {
  const { status, verify } = useAuth()

  useEffect(() => {
    void verify({ silent: true })
  }, [verify])

  if (status === 'checking') {
    return <SessionGate />
  }

  if (status === 'authenticated') {
    return <Navigate to="/dashboard" replace />
  }

  return children
}

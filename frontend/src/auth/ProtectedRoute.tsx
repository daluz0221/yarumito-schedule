import { useEffect } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'
import { SessionGate } from './SessionGate'

export function ProtectedRoute() {
  const { status, verify } = useAuth()
  const location = useLocation()

  useEffect(() => {
    void verify({ silent: true })
  }, [location.pathname, verify])

  if (status === 'checking') {
    return <SessionGate />
  }

  if (status !== 'authenticated') {
    return <Navigate to="/admin" replace />
  }

  return <Outlet />
}

import { Navigate } from 'react-router-dom'
import { useAuth } from './AuthContext'
import { SessionGate } from './SessionGate'

export function HomeRedirect() {
  const { status } = useAuth()

  if (status === 'checking') {
    return <SessionGate />
  }

  return (
    <Navigate
      to={status === 'authenticated' ? '/dashboard' : '/admin'}
      replace
    />
  )
}

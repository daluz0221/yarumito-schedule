import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { logout as clearAuthSession, verifySession } from '../api'
import type { AuthUser } from '../api'

export type AuthStatus = 'checking' | 'authenticated' | 'anonymous'

type AuthContextValue = {
  status: AuthStatus
  user: AuthUser | null
  verify: (options?: { silent?: boolean }) => Promise<AuthUser | null>
  acceptSession: (user: AuthUser) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>('checking')
  const [user, setUser] = useState<AuthUser | null>(null)
  const requestId = useRef(0)

  const verify = useCallback(async (options?: { silent?: boolean }) => {
    const currentRequest = ++requestId.current

    if (!options?.silent) {
      setStatus('checking')
    }

    const nextUser = await verifySession()

    if (currentRequest !== requestId.current) {
      return nextUser
    }

    if (nextUser) {
      setUser(nextUser)
      setStatus('authenticated')
      return nextUser
    }

    setUser(null)
    setStatus('anonymous')
    return null
  }, [])

  const acceptSession = useCallback((nextUser: AuthUser) => {
    requestId.current += 1
    setUser(nextUser)
    setStatus('authenticated')
  }, [])

  const logout = useCallback(() => {
    requestId.current += 1
    clearAuthSession()
    setUser(null)
    setStatus('anonymous')
  }, [])

  useEffect(() => {
    void verify()
  }, [verify])

  useEffect(() => {
    const onFocus = () => {
      void verify({ silent: true })
    }

    window.addEventListener('focus', onFocus)
    return () => window.removeEventListener('focus', onFocus)
  }, [verify])

  const value = useMemo(
    () => ({ status, user, verify, acceptSession, logout }),
    [status, user, verify, acceptSession, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }

  return context
}

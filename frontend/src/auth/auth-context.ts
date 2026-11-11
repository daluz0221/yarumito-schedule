import { createContext } from 'react'
import type { AuthUser } from '../api'

export type AuthStatus = 'checking' | 'authenticated' | 'anonymous'

export type AuthContextValue = {
  status: AuthStatus
  user: AuthUser | null
  verify: (options?: { silent?: boolean }) => Promise<AuthUser | null>
  acceptSession: (user: AuthUser) => void
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

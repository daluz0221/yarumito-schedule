import type { AuthUser } from './types'

const TOKEN_KEY = 'yarumito.auth.token'
const USER_KEY = 'yarumito.auth.user'

function read(key: string) {
  return localStorage.getItem(key) ?? sessionStorage.getItem(key)
}

// Guarda token y usuario. "Recordarme" usa localStorage; si no, sessionStorage.
export function saveSession(
  token: string,
  user: AuthUser,
  persist: boolean,
) {
  clearSession()
  const store = persist ? localStorage : sessionStorage
  store.setItem(TOKEN_KEY, token)
  store.setItem(USER_KEY, JSON.stringify(user))
}

export function getToken() {
  return read(TOKEN_KEY)
}

export function getUser(): AuthUser | null {
  const raw = read(USER_KEY)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

export function updateStoredUser(user: AuthUser) {
  const persist = Boolean(localStorage.getItem(TOKEN_KEY))
  const store = persist ? localStorage : sessionStorage
  store.setItem(USER_KEY, JSON.stringify(user))
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
}

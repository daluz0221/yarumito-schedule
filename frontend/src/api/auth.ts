import { api } from './client'
import { clearSession, getToken, saveSession, updateStoredUser } from './session'
import type { AuthUser, LoginPayload, LoginResult } from './types'

export function login(payload: LoginPayload) {
  return api
    .post<LoginResult, LoginPayload>('/api/v1/auth/login', payload, {
      auth: false,
    })
    .then((result) => {
      saveSession(result.token, result.usuario, payload.remember)
      return result
    })
}

export function getAuthenticatedUser() {
  return api.get<LoginResult['usuario']>('/api/v1/auth/me')
}

export async function verifySession(): Promise<AuthUser | null> {
  if (!getToken()) {
    clearSession()
    return null
  }

  try {
    const user = await getAuthenticatedUser()
    updateStoredUser(user)
    return user
  } catch {
    clearSession()
    return null
  }
}

export function logout() {
  clearSession()
}

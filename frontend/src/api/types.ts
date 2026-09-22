export type AuthUser = {
  id: string
  correo: string
  rol: 'RECTOR' | 'DOCENTE'
  activo: boolean
}

export type LoginPayload = {
  username: string
  password: string
  remember: boolean
}

export type LoginResult = {
  token: string
  tokenType: string
  expiresIn: number
  usuario: AuthUser
}

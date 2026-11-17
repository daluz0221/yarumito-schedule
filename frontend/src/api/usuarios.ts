import { api } from './client'

export type UsuarioListado = {
  id: string
  correo: string
  rol: 'RECTOR' | 'DOCENTE'
  activo: boolean
}

export function listarUsuarios(signal?: AbortSignal) {
  return api.get<UsuarioListado[]>('/api/v1/usuarios', { signal })
}

export function etiquetaUsuario(usuario: Pick<UsuarioListado, 'correo' | 'rol' | 'activo'>) {
  const rol = usuario.rol === 'RECTOR' ? 'Rector' : 'Docente'
  return usuario.activo ? `${usuario.correo} · ${rol}` : `${usuario.correo} · ${rol} · Inactivo`
}

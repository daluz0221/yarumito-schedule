import { useCallback, useState } from 'react'
import { anioEscolarVisible } from '../api/aniosEscolares'
import type { AcademicYear } from './academicYear'

const CLAVE = 'yarumito.anio.trabajo'

export function leerAnioDeTrabajo() {
  try {
    return sessionStorage.getItem(CLAVE) ?? ''
  } catch {
    return ''
  }
}

export function guardarAnioDeTrabajo(id: string) {
  try {
    if (id) {
      sessionStorage.setItem(CLAVE, id)
    }
  } catch {
    // Si el navegador bloquea el almacenamiento, la elección vive solo en esta vista.
  }
}

// El año elegido se conserva mientras siga en el catálogo.
// Si no hay elección, se usa el año actual y, en su defecto, el último.
export function conservarAnio(
  anios: readonly AcademicYear[] | null | undefined,
  elegido: string,
) {
  if (!anios?.length) {
    return elegido
  }

  if (elegido && anios.some((year) => year.id === elegido)) {
    return elegido
  }

  return anioEscolarVisible([...anios])?.id ?? ''
}

export function useAnioDeTrabajo() {
  const [elegido, setElegido] = useState(leerAnioDeTrabajo)

  const elegir = useCallback((id: string) => {
    setElegido(id)

    if (id) {
      guardarAnioDeTrabajo(id)
    }
  }, [])

  return [elegido, elegir] as const
}

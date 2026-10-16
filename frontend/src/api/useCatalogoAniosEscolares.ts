import { useEffect, useState } from 'react'
import type { AcademicYear } from '../content/academicYear'
import { listarCatalogoAniosEscolares } from './aniosEscolares'
import { ApiError } from './client'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeCatalogo(cause: unknown) {
  if (
    cause instanceof ApiError &&
    cause.message &&
    cause.message !== 'No se pudo completar la solicitud'
  ) {
    return cause.message
  }

  return 'No se pudo cargar el catálogo de años escolares.'
}

export function useCatalogoAniosEscolares(enabled = true) {
  const [anios, setAnios] = useState<AcademicYear[] | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!enabled) {
      return
    }

    const controller = new AbortController()

    listarCatalogoAniosEscolares(controller.signal)
      .then((items) => {
        setAnios(items)
        setError('')
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setAnios([])
        setError(mensajeCatalogo(cause))
      })

    return () => controller.abort()
  }, [enabled])

  return {
    anios: enabled ? anios : null,
    error: enabled ? error : '',
    loading: enabled && anios === null && !error,
  }
}

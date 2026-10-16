import { useEffect, useState } from 'react'
import { ApiError } from './client'
import { listarCatalogoSedes, type SedeResponse } from './sedes'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export function useCatalogoSedes(enabled = true) {
  const [sedes, setSedes] = useState<SedeResponse[] | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!enabled) {
      return
    }

    const controller = new AbortController()

    listarCatalogoSedes(controller.signal)
      .then((items) => {
        setSedes(items)
        setError('')
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setSedes([])
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el catálogo de sedes.',
        )
      })

    return () => controller.abort()
  }, [enabled])

  return {
    sedes: enabled ? sedes : null,
    error: enabled ? error : '',
    loading: enabled && sedes === null && !error,
  }
}

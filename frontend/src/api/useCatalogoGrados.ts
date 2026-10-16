import { useEffect, useState } from 'react'
import { ApiError } from './client'
import { listarCatalogoGrados, type GradoResponse } from './grados'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export function useCatalogoGrados(enabled = true) {
  const [grados, setGrados] = useState<GradoResponse[] | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!enabled) {
      return
    }

    const controller = new AbortController()

    listarCatalogoGrados(controller.signal)
      .then((items) => {
        setGrados(items)
        setError('')
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setGrados([])
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el catálogo de grados.',
        )
      })

    return () => controller.abort()
  }, [enabled])

  return {
    grados: enabled ? grados : null,
    error: enabled ? error : '',
    loading: enabled && grados === null && !error,
  }
}

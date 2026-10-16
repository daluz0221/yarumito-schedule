import { useEffect, useState } from 'react'
import { ApiError } from './client'
import { listarCatalogoGrupos, type GrupoResponse } from './grupos'

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

  return 'No se pudo cargar el listado de grupos.'
}

export function useCatalogoGrupos(enabled = true) {
  const [grupos, setGrupos] = useState<GrupoResponse[] | null>(null)
  const [error, setError] = useState('')
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    if (!enabled) {
      return
    }

    const controller = new AbortController()

    listarCatalogoGrupos(controller.signal)
      .then((items) => {
        setGrupos(items)
        setError('')
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setGrupos([])
        setError(mensajeCatalogo(cause))
      })

    return () => controller.abort()
  }, [enabled, revision])

  return {
    grupos: enabled ? grupos : null,
    error: enabled ? error : '',
    loading: enabled && grupos === null && !error,
    recargar: () => setRevision((current) => current + 1),
  }
}

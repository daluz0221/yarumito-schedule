import { clearSession, getToken } from './session'

const DEFAULT_BASE_URL = 'http://localhost:8080'

export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export type ApiRequestOptions<TBody = unknown> = {
  method?: HttpMethod
  body?: TBody
  auth?: boolean
  headers?: Record<string, string>
  signal?: AbortSignal
}

function getBaseUrl() {
  const configured = import.meta.env.VITE_API_BASE_URL
  return configured?.trim() || DEFAULT_BASE_URL
}

function joinUrl(path: string) {
  const base = getBaseUrl().replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${base}${normalizedPath}`
}

function readErrorMessage(payload: unknown, status: number) {
  if (payload && typeof payload === 'object') {
    const record = payload as { mensaje?: unknown; message?: unknown }

    if (typeof record.mensaje === 'string' && record.mensaje.trim()) {
      return record.mensaje
    }

    if (typeof record.message === 'string' && record.message.trim()) {
      return record.message
    }
  }

  if (status === 401) {
    return 'No autenticado'
  }

  if (status === 403) {
    return 'No tienes permiso para realizar esta acción'
  }

  return 'No se pudo completar la solicitud'
}

async function parseBody(response: Response) {
  const text = await response.text()

  if (!text) {
    return null
  }

  try {
    return JSON.parse(text) as unknown
  } catch {
    return text
  }
}

// Cliente HTTP único de la app. Todas las llamadas a la API deben pasar por aquí.
export async function apiRequest<TResponse, TBody = unknown>(
  path: string,
  options: ApiRequestOptions<TBody> = {},
): Promise<TResponse> {
  const { method = 'GET', body, auth = true, headers = {}, signal } = options
  const token = auth ? getToken() : null
  const requestHeaders = new Headers(headers)

  if (token) {
    requestHeaders.set('Authorization', `Bearer ${token}`)
  }

  let requestBody: BodyInit | undefined

  if (body !== undefined) {
    if (body instanceof FormData) {
      requestBody = body
    } else {
      requestHeaders.set('Content-Type', 'application/json')
      requestBody = JSON.stringify(body)
    }
  }

  let response: Response

  try {
    response = await fetch(joinUrl(path), {
      method,
      headers: requestHeaders,
      body: requestBody,
      signal,
    })
  } catch (error) {
    if (isAbortError(error)) {
      throw error
    }

    throw new ApiError(
      'No se pudo conectar con el servidor. Inténtalo de nuevo.',
      0,
    )
  }

  const payload = await parseBody(response)

  if (!response.ok) {
    if (auth && response.status === 401) {
      clearSession()
    }

    throw new ApiError(readErrorMessage(payload, response.status), response.status)
  }

  return payload as TResponse
}

export const api = {
  get<TResponse>(path: string, options?: Omit<ApiRequestOptions, 'method' | 'body'>) {
    return apiRequest<TResponse>(path, { ...options, method: 'GET' })
  },

  post<TResponse, TBody = unknown>(
    path: string,
    body?: TBody,
    options?: Omit<ApiRequestOptions<TBody>, 'method' | 'body'>,
  ) {
    return apiRequest<TResponse, TBody>(path, { ...options, method: 'POST', body })
  },

  put<TResponse, TBody = unknown>(
    path: string,
    body?: TBody,
    options?: Omit<ApiRequestOptions<TBody>, 'method' | 'body'>,
  ) {
    return apiRequest<TResponse, TBody>(path, { ...options, method: 'PUT', body })
  },

  patch<TResponse, TBody = unknown>(
    path: string,
    body?: TBody,
    options?: Omit<ApiRequestOptions<TBody>, 'method' | 'body'>,
  ) {
    return apiRequest<TResponse, TBody>(path, { ...options, method: 'PATCH', body })
  },

  delete<TResponse>(path: string, options?: Omit<ApiRequestOptions, 'method' | 'body'>) {
    return apiRequest<TResponse>(path, { ...options, method: 'DELETE' })
  },
}

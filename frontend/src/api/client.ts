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
  if (typeof configured === 'string' && configured.trim()) {
    return configured.trim()
  }
  if (import.meta.env.PROD) {
    return ''
  }
  return DEFAULT_BASE_URL
}

function joinUrl(path: string) {
  const base = getBaseUrl().replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${base}${normalizedPath}`
}

function readErrorMessage(payload: unknown, status: number) {
  if (payload && typeof payload === 'object') {
    const record = payload as { mensaje?: unknown; message?: unknown; detail?: unknown }

    if (typeof record.mensaje === 'string' && record.mensaje.trim()) {
      return record.mensaje
    }

    if (typeof record.message === 'string' && record.message.trim()) {
      return record.message
    }

    if (typeof record.detail === 'string' && record.detail.trim()) {
      return record.detail
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

export type DescargaArchivo = {
  blob: Blob
  filename: string
}

function nombreDesdeContentDisposition(header: string | null, fallback: string) {
  if (!header) {
    return fallback
  }

  const utf8 = /filename\*=UTF-8''([^;]+)/i.exec(header)

  if (utf8?.[1]) {
    try {
      return decodeURIComponent(utf8[1].trim())
    } catch {
      return utf8[1].trim()
    }
  }

  const simple = /filename="([^"]+)"/i.exec(header) ?? /filename=([^;]+)/i.exec(header)
  return simple?.[1]?.trim() || fallback
}

// Descarga binaria (PDF o Excel). El nombre de respaldo se usa si el navegador
// no puede leer Content-Disposition por CORS.
export async function apiDownload<TBody = unknown>(
  path: string,
  options: ApiRequestOptions<TBody> & { filename: string },
): Promise<DescargaArchivo> {
  const { method = 'POST', body, auth = true, headers = {}, signal, filename } = options
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

  if (!response.ok) {
    const payload = await parseBody(response)

    if (auth && response.status === 401) {
      clearSession()
    }

    throw new ApiError(readErrorMessage(payload, response.status), response.status)
  }

  return {
    blob: await response.blob(),
    filename: nombreDesdeContentDisposition(
      response.headers.get('Content-Disposition'),
      filename,
    ),
  }
}

export function guardarArchivo(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
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

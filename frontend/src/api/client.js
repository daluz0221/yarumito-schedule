import { clearSession, getToken } from './session';
const DEFAULT_BASE_URL = 'http://localhost:8080';
export class ApiError extends Error {
    status;
    constructor(message, status) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function getBaseUrl() {
    const configured = import.meta.env.VITE_API_BASE_URL;
    return configured?.trim() || DEFAULT_BASE_URL;
}
function joinUrl(path) {
    const base = getBaseUrl().replace(/\/$/, '');
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    return `${base}${normalizedPath}`;
}
function readErrorMessage(payload, status) {
    if (payload && typeof payload === 'object') {
        const record = payload;
        if (typeof record.mensaje === 'string' && record.mensaje.trim()) {
            return record.mensaje;
        }
        if (typeof record.message === 'string' && record.message.trim()) {
            return record.message;
        }
        if (typeof record.detail === 'string' && record.detail.trim()) {
            return record.detail;
        }
    }
    if (status === 401) {
        return 'No autenticado';
    }
    if (status === 403) {
        return 'No tienes permiso para realizar esta acción';
    }
    return 'No se pudo completar la solicitud';
}
async function parseBody(response) {
    const text = await response.text();
    if (!text) {
        return null;
    }
    try {
        return JSON.parse(text);
    }
    catch {
        return text;
    }
}
// Cliente HTTP único de la app. Todas las llamadas a la API deben pasar por aquí.
export async function apiRequest(path, options = {}) {
    const { method = 'GET', body, auth = true, headers = {}, signal } = options;
    const token = auth ? getToken() : null;
    const requestHeaders = new Headers(headers);
    if (token) {
        requestHeaders.set('Authorization', `Bearer ${token}`);
    }
    let requestBody;
    if (body !== undefined) {
        if (body instanceof FormData) {
            requestBody = body;
        }
        else {
            requestHeaders.set('Content-Type', 'application/json');
            requestBody = JSON.stringify(body);
        }
    }
    let response;
    try {
        response = await fetch(joinUrl(path), {
            method,
            headers: requestHeaders,
            body: requestBody,
            signal,
        });
    }
    catch (error) {
        if (isAbortError(error)) {
            throw error;
        }
        throw new ApiError('No se pudo conectar con el servidor. Inténtalo de nuevo.', 0);
    }
    const payload = await parseBody(response);
    if (!response.ok) {
        if (auth && response.status === 401) {
            clearSession();
        }
        throw new ApiError(readErrorMessage(payload, response.status), response.status);
    }
    return payload;
}
function nombreDesdeContentDisposition(header, fallback) {
    if (!header) {
        return fallback;
    }
    const utf8 = /filename\*=UTF-8''([^;]+)/i.exec(header);
    if (utf8?.[1]) {
        try {
            return decodeURIComponent(utf8[1].trim());
        }
        catch {
            return utf8[1].trim();
        }
    }
    const simple = /filename="([^"]+)"/i.exec(header) ?? /filename=([^;]+)/i.exec(header);
    return simple?.[1]?.trim() || fallback;
}
export async function apiDownload(path, options) {
    const { method = 'POST', body, auth = true, headers = {}, signal, filename } = options;
    const token = auth ? getToken() : null;
    const requestHeaders = new Headers(headers);
    if (token) {
        requestHeaders.set('Authorization', `Bearer ${token}`);
    }
    let requestBody;
    if (body !== undefined) {
        if (body instanceof FormData) {
            requestBody = body;
        }
        else {
            requestHeaders.set('Content-Type', 'application/json');
            requestBody = JSON.stringify(body);
        }
    }
    let response;
    try {
        response = await fetch(joinUrl(path), {
            method,
            headers: requestHeaders,
            body: requestBody,
            signal,
        });
    }
    catch (error) {
        if (isAbortError(error)) {
            throw error;
        }
        throw new ApiError('No se pudo conectar con el servidor. Inténtalo de nuevo.', 0);
    }
    if (!response.ok) {
        const payload = await parseBody(response);
        if (auth && response.status === 401) {
            clearSession();
        }
        throw new ApiError(readErrorMessage(payload, response.status), response.status);
    }
    return {
        blob: await response.blob(),
        filename: nombreDesdeContentDisposition(response.headers.get('Content-Disposition'), filename),
    };
}
export function guardarArchivo(blob, filename) {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
}
export const api = {
    get(path, options) {
        return apiRequest(path, { ...options, method: 'GET' });
    },
    post(path, body, options) {
        return apiRequest(path, { ...options, method: 'POST', body });
    },
    put(path, body, options) {
        return apiRequest(path, { ...options, method: 'PUT', body });
    },
    patch(path, body, options) {
        return apiRequest(path, { ...options, method: 'PATCH', body });
    },
    delete(path, options) {
        return apiRequest(path, { ...options, method: 'DELETE' });
    },
};

import { api } from './client';
import { clearSession, getToken, saveSession, updateStoredUser } from './session';
export function login(payload) {
    return api
        .post('/api/v1/auth/login', payload, {
        auth: false,
    })
        .then((result) => {
        saveSession(result.token, result.usuario, payload.remember);
        return result;
    });
}
export function getAuthenticatedUser() {
    return api.get('/api/v1/auth/me');
}
export async function verifySession() {
    if (!getToken()) {
        clearSession();
        return null;
    }
    try {
        const user = await getAuthenticatedUser();
        updateStoredUser(user);
        return user;
    }
    catch {
        clearSession();
        return null;
    }
}
export function logout() {
    clearSession();
}

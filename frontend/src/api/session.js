const TOKEN_KEY = 'yarumito.auth.token';
const USER_KEY = 'yarumito.auth.user';
function read(key) {
    return localStorage.getItem(key) ?? sessionStorage.getItem(key);
}
// Guarda token y usuario. "Recordarme" usa localStorage; si no, sessionStorage.
export function saveSession(token, user, persist) {
    clearSession();
    const store = persist ? localStorage : sessionStorage;
    store.setItem(TOKEN_KEY, token);
    store.setItem(USER_KEY, JSON.stringify(user));
}
export function getToken() {
    return read(TOKEN_KEY);
}
export function getUser() {
    const raw = read(USER_KEY);
    if (!raw) {
        return null;
    }
    try {
        return JSON.parse(raw);
    }
    catch {
        return null;
    }
}
export function updateStoredUser(user) {
    const persist = Boolean(localStorage.getItem(TOKEN_KEY));
    const store = persist ? localStorage : sessionStorage;
    store.setItem(USER_KEY, JSON.stringify(user));
}
export function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
}

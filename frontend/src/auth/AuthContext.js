import { jsx as _jsx } from "react/jsx-runtime";
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, } from 'react';
import { logout as clearAuthSession, verifySession } from '../api';
const AuthContext = createContext(null);
export function AuthProvider({ children }) {
    const [status, setStatus] = useState('checking');
    const [user, setUser] = useState(null);
    const requestId = useRef(0);
    const verify = useCallback(async (options) => {
        const currentRequest = ++requestId.current;
        if (!options?.silent) {
            setStatus('checking');
        }
        const nextUser = await verifySession();
        if (currentRequest !== requestId.current) {
            return nextUser;
        }
        if (nextUser) {
            setUser(nextUser);
            setStatus('authenticated');
            return nextUser;
        }
        setUser(null);
        setStatus('anonymous');
        return null;
    }, []);
    const acceptSession = useCallback((nextUser) => {
        requestId.current += 1;
        setUser(nextUser);
        setStatus('authenticated');
    }, []);
    const logout = useCallback(() => {
        requestId.current += 1;
        clearAuthSession();
        setUser(null);
        setStatus('anonymous');
    }, []);
    useEffect(() => {
        void verify();
    }, [verify]);
    useEffect(() => {
        const onFocus = () => {
            void verify({ silent: true });
        };
        window.addEventListener('focus', onFocus);
        return () => window.removeEventListener('focus', onFocus);
    }, [verify]);
    const value = useMemo(() => ({ status, user, verify, acceptSession, logout }), [status, user, verify, acceptSession, logout]);
    return _jsx(AuthContext.Provider, { value: value, children: children });
}
export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth debe usarse dentro de AuthProvider');
    }
    return context;
}

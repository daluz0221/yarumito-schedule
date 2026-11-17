import { jsx as _jsx } from "react/jsx-runtime";
import { useEffect } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { SessionGate } from './SessionGate';
export function ProtectedRoute() {
    const { status, verify } = useAuth();
    const location = useLocation();
    useEffect(() => {
        void verify({ silent: true });
    }, [location.pathname, verify]);
    if (status === 'checking') {
        return _jsx(SessionGate, {});
    }
    if (status !== 'authenticated') {
        return _jsx(Navigate, { to: "/admin", replace: true });
    }
    return _jsx(Outlet, {});
}

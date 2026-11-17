import { jsx as _jsx } from "react/jsx-runtime";
import { useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { SessionGate } from './SessionGate';
export function GuestRoute({ children }) {
    const { status, verify } = useAuth();
    useEffect(() => {
        void verify({ silent: true });
    }, [verify]);
    if (status === 'checking') {
        return _jsx(SessionGate, {});
    }
    if (status === 'authenticated') {
        return _jsx(Navigate, { to: "/dashboard", replace: true });
    }
    return children;
}

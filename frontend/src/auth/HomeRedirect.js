import { jsx as _jsx } from "react/jsx-runtime";
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { SessionGate } from './SessionGate';
export function HomeRedirect() {
    const { status } = useAuth();
    if (status === 'checking') {
        return _jsx(SessionGate, {});
    }
    return (_jsx(Navigate, { to: status === 'authenticated' ? '/dashboard' : '/admin', replace: true }));
}

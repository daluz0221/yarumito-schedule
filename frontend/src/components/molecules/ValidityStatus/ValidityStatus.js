import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import styles from './ValidityStatus.module.css';
export function ValidityStatus({ current, since }) {
    return (_jsxs("span", { className: styles.status, children: [_jsx("span", { className: current ? styles.current : styles.closed, children: current ? 'Vigente' : 'Finalizado' }), _jsx("span", { className: styles.since, children: current && !since.includes('–') ? `Desde ${since}` : since })] }));
}

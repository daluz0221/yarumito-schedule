import { jsx as _jsx } from "react/jsx-runtime";
import styles from './SessionGate.module.css';
export function SessionGate() {
    return (_jsx("main", { className: styles.gate, "aria-busy": "true", "aria-live": "polite", children: _jsx("p", { className: styles.copy, children: "Verificando sesi\u00F3n..." }) }));
}

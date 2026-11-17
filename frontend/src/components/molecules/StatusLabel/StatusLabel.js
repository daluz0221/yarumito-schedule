import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { StatusDot } from '../../atoms/StatusDot';
import styles from './StatusLabel.module.css';
export function StatusLabel({ label, tone }) {
    return (_jsxs("span", { className: styles.status, children: [_jsx(StatusDot, { tone: tone }), label] }));
}

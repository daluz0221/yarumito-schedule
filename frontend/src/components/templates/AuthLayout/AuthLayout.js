import { jsx as _jsx } from "react/jsx-runtime";
import styles from './AuthLayout.module.css';
export function AuthLayout({ children }) {
    return (_jsx("main", { className: styles.layout, children: _jsx("div", { className: styles.content, children: children }) }));
}

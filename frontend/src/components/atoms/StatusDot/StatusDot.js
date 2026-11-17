import { jsx as _jsx } from "react/jsx-runtime";
import styles from './StatusDot.module.css';
export function StatusDot({ tone, label }) {
    return _jsx("span", { className: `${styles.dot} ${styles[tone]}`, "aria-label": label });
}

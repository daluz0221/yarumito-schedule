import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Table.module.css';
export function Table({ children, minWidth = 760, className }) {
    const classes = [styles.scroll, className ?? ''].filter(Boolean).join(' ');
    return (_jsx("div", { className: classes, children: _jsx("table", { className: styles.table, style: { minWidth }, children: children }) }));
}

import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Label.module.css';
export function Label({ variant = 'default', className, children, ...props }) {
    const classes = [styles.label, styles[variant], className ?? '']
        .filter(Boolean)
        .join(' ');
    return (_jsx("label", { className: classes, ...props, children: children }));
}

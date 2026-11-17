import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Button.module.css';
export function Button({ variant = 'primary', size = 'md', fullWidth = false, className, type = 'button', children, ...props }) {
    const classes = [
        styles.button,
        styles[variant],
        styles[size],
        fullWidth ? styles.fullWidth : '',
        className ?? '',
    ]
        .filter(Boolean)
        .join(' ');
    return (_jsx("button", { type: type, className: classes, ...props, children: children }));
}

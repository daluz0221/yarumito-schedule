import { jsx as _jsx } from "react/jsx-runtime";
import styles from './IconButton.module.css';
export function IconButton({ label, className, type = 'button', children, ...props }) {
    const classes = [styles.button, className ?? ''].filter(Boolean).join(' ');
    return (_jsx("button", { type: type, className: classes, "aria-label": label, ...props, children: children }));
}

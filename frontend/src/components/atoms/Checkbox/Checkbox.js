import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Checkbox.module.css';
export function Checkbox({ className, ...props }) {
    const classes = [styles.checkbox, className ?? ''].filter(Boolean).join(' ');
    return _jsx("input", { type: "checkbox", className: classes, ...props });
}

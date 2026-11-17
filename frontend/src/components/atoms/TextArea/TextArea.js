import { jsx as _jsx } from "react/jsx-runtime";
import styles from './TextArea.module.css';
export function TextArea({ className, ...props }) {
    const classes = [styles.input, className ?? ''].filter(Boolean).join(' ');
    return _jsx("textarea", { className: classes, ...props });
}

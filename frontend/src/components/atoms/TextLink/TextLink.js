import { jsx as _jsx } from "react/jsx-runtime";
import { Link } from 'react-router-dom';
import styles from './TextLink.module.css';
export function TextLink({ children, to, onClick, disabled = false, className, type = 'button', }) {
    const classes = [styles.link, className ?? ''].filter(Boolean).join(' ');
    if (to && !disabled) {
        return (_jsx(Link, { to: to, className: classes, onClick: onClick, children: children }));
    }
    return (_jsx("button", { type: type, className: classes, onClick: onClick, disabled: disabled, children: children }));
}

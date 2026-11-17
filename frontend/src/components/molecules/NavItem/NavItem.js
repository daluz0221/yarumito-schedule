import { jsx as _jsx, Fragment as _Fragment, jsxs as _jsxs } from "react/jsx-runtime";
import { NavLink } from 'react-router-dom';
import { Text } from '../../atoms/Text';
import styles from './NavItem.module.css';
export function NavItem({ icon, label, to, end = false, disabled = false, indented = false, onClick, onNavigate, }) {
    const classes = [
        styles.item,
        indented ? styles.indented : '',
        disabled ? styles.disabled : '',
    ]
        .filter(Boolean)
        .join(' ');
    const content = (_jsxs(_Fragment, { children: [_jsx("span", { className: styles.icon, children: icon }), _jsx(Text, { variant: "nav", children: label })] }));
    if (disabled) {
        return (_jsx("span", { className: classes, "aria-disabled": "true", children: content }));
    }
    if (onClick && !to) {
        return (_jsx("button", { type: "button", className: `${classes} ${styles.action}`, onClick: () => {
                onClick();
                onNavigate?.();
            }, children: content }));
    }
    if (!to) {
        return (_jsx("span", { className: classes, "aria-disabled": "true", children: content }));
    }
    return (_jsx(NavLink, { to: to, end: end, onClick: onNavigate, className: ({ isActive }) => [classes, isActive ? styles.active : ''].filter(Boolean).join(' '), children: content }));
}

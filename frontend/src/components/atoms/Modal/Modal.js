import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect } from 'react';
import { createPortal } from 'react-dom';
import { Text } from '../Text';
import styles from './Modal.module.css';
export function Modal({ open, title, description, children, footer, size = 'md', onClose, }) {
    useEffect(() => {
        if (!open) {
            return;
        }
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        const onKeyDown = (event) => {
            if (event.key === 'Escape') {
                onClose();
            }
        };
        window.addEventListener('keydown', onKeyDown);
        return () => {
            document.body.style.overflow = previousOverflow;
            window.removeEventListener('keydown', onKeyDown);
        };
    }, [open, onClose]);
    if (!open) {
        return null;
    }
    return createPortal(_jsx("div", { className: styles.overlay, onClick: onClose, role: "presentation", children: _jsxs("div", { className: [
                styles.dialog,
                size === 'sm' ? styles.sm : '',
                size === 'lg' ? styles.lg : '',
            ]
                .filter(Boolean)
                .join(' '), role: "dialog", "aria-modal": "true", "aria-label": title, onClick: (event) => event.stopPropagation(), children: [_jsxs("header", { className: styles.header, children: [_jsx(Text, { variant: "sectionTitle", as: "h2", className: styles.title, children: title }), description ? _jsx(Text, { variant: "body", children: description }) : null] }), children ? _jsx("div", { className: styles.body, children: children }) : null, footer ? _jsx("div", { className: styles.footer, children: footer }) : null] }) }), document.body);
}

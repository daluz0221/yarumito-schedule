import { jsx as _jsx } from "react/jsx-runtime";
import { PencilIcon, TrashIcon } from '../icons';
import styles from './IconAction.module.css';
export function IconActions({ children }) {
    return _jsx("span", { className: styles.group, children: children });
}
export function IconAction({ label, tone = 'edit', onClick }) {
    const danger = tone === 'danger';
    return (_jsx("button", { type: "button", className: danger ? `${styles.action} ${styles.danger}` : styles.action, onClick: onClick, "aria-label": label, title: label, children: danger ? _jsx(TrashIcon, {}) : _jsx(PencilIcon, {}) }));
}

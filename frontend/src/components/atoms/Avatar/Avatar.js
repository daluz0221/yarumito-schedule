import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Avatar.module.css';
export function Avatar({ initials, size = 'md' }) {
    const classes = [styles.avatar, styles[size]].join(' ');
    return _jsx("span", { className: classes, children: initials });
}

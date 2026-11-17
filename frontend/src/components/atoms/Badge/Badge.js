import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Badge.module.css';
export function Badge({ children, tone = 'neutral' }) {
    const classes = [styles.badge, styles[tone]].join(' ');
    return _jsx("span", { className: classes, children: children });
}

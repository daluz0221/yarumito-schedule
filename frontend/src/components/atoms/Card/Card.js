import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Card.module.css';
export function Card({ as: Component = 'section', children, padding = 'md', highlighted = false, className, }) {
    const classes = [
        styles.card,
        styles[padding],
        highlighted ? styles.highlighted : '',
        className ?? '',
    ]
        .filter(Boolean)
        .join(' ');
    return _jsx(Component, { className: classes, children: children });
}

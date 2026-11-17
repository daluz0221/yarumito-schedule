import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Select.module.css';
export function Select({ options, className, selectSize = 'md', ...props }) {
    const classes = [styles.select, styles[selectSize], className ?? '']
        .filter(Boolean)
        .join(' ');
    return (_jsx("select", { className: classes, ...props, children: options.map((option) => (_jsx("option", { value: option.value, children: option.label }, option.value))) }));
}

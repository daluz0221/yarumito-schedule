import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import styles from './TextInput.module.css';
export function TextInput({ className, rightSlot, inputSize = 'md', ...props }) {
    const classes = [
        styles.input,
        styles[inputSize],
        rightSlot ? styles.hasSlot : '',
        className ?? '',
    ]
        .filter(Boolean)
        .join(' ');
    return (_jsxs("div", { className: styles.wrapper, children: [_jsx("input", { className: classes, ...props }), rightSlot ? _jsx("div", { className: styles.slot, children: rightSlot }) : null] }));
}

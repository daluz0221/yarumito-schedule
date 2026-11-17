import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useRef } from 'react';
import { FormField } from '../FormField';
import styles from './FileField.module.css';
export function FileField({ id, label, hint, fileName, placeholder = 'Adjuntar archivo', onFileChange, }) {
    const inputRef = useRef(null);
    return (_jsxs(FormField, { id: id, label: label, hint: hint, children: [_jsx("button", { type: "button", className: [styles.trigger, fileName ? styles.hasFile : '']
                    .filter(Boolean)
                    .join(' '), onClick: () => inputRef.current?.click(), children: fileName || placeholder }), _jsx("input", { ref: inputRef, id: id, type: "file", className: styles.input, onChange: (event) => {
                    const file = event.target.files?.[0];
                    onFileChange?.(file?.name ?? '');
                } })] }));
}

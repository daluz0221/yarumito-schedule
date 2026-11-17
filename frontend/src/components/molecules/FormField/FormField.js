import { jsxs as _jsxs, jsx as _jsx } from "react/jsx-runtime";
import { Label } from '../../atoms/Label';
import styles from './FormField.module.css';
export function FormField({ id, label, required = false, hint, error, children, }) {
    return (_jsxs("div", { className: styles.field, children: [_jsxs(Label, { htmlFor: id, children: [label, required ? (_jsxs("span", { className: styles.required, "aria-hidden": "true", children: [' ', "*"] })) : null] }), children, error ? (_jsx("p", { id: `${id}-error`, className: styles.error, role: "alert", children: error })) : hint ? (_jsx("p", { className: styles.hint, children: hint })) : null] }));
}

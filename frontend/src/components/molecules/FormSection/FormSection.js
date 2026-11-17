import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Text } from '../../atoms/Text';
import styles from './FormSection.module.css';
export function FormSection({ title, description, children }) {
    return (_jsxs("section", { className: styles.section, children: [_jsxs("header", { className: styles.header, children: [_jsx(Text, { variant: "nav", as: "h3", className: styles.title, children: title }), _jsx(Text, { variant: "body", children: description })] }), _jsx("div", { className: styles.grid, children: children })] }));
}

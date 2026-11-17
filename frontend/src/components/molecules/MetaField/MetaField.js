import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Text } from '../../atoms/Text';
import styles from './MetaField.module.css';
export function MetaField({ label, value }) {
    return (_jsxs("div", { className: styles.field, children: [_jsx(Text, { variant: "caption", children: label }), _jsx("p", { className: styles.value, children: value })] }));
}

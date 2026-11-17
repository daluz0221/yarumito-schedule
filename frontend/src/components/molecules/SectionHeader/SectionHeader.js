import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Text } from '../../atoms/Text';
import styles from './SectionHeader.module.css';
export function SectionHeader({ title, description, action, }) {
    return (_jsxs("div", { className: styles.header, children: [_jsxs("div", { className: styles.copy, children: [_jsx(Text, { variant: "sectionTitle", children: title }), _jsx(Text, { variant: "body", children: description })] }), action ? _jsx("div", { className: styles.action, children: action }) : null] }));
}

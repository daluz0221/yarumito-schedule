import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { StatusDot } from '../../atoms/StatusDot';
import { Text } from '../../atoms/Text';
import styles from './AlertItem.module.css';
export function AlertItem({ message, tone }) {
    return (_jsxs("li", { className: styles.item, children: [_jsx(StatusDot, { tone: tone }), _jsx(Text, { variant: "body", children: message })] }));
}

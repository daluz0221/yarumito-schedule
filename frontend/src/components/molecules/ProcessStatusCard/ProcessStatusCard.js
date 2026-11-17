import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Badge } from '../../atoms/Badge';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './ProcessStatusCard.module.css';
export function ProcessStatusCard({ year, status }) {
    return (_jsxs(Card, { className: styles.card, children: [_jsx(Text, { variant: "sectionTitle", children: "Estado del proceso" }), _jsxs("div", { className: styles.meta, children: [_jsxs(Text, { variant: "body", children: ["A\u00F1o lectivo: ", year] }), _jsx(Badge, { children: status })] }), _jsx(Button, { variant: "muted", size: "sm", fullWidth: true, disabled: true, children: "Pr\u00F3ximamente" })] }));
}

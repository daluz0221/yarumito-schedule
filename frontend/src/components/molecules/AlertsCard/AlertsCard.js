import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { AlertItem } from '../AlertItem';
import styles from './AlertsCard.module.css';
export function AlertsCard({ items }) {
    return (_jsxs(Card, { className: styles.card, children: [_jsx(Text, { variant: "sectionTitle", children: "Alertas" }), _jsx("ul", { className: styles.list, children: items.map((item) => (_jsx(AlertItem, { message: item.message, tone: item.tone }, item.id))) })] }));
}

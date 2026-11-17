import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './StatCard.module.css';
const washClass = {
    total: styles.washTotal,
    active: styles.washActive,
    license: styles.washLicense,
    retired: styles.washRetired,
    danger: styles.washDanger,
    warning: styles.washLicense,
    success: styles.washActive,
    neutral: styles.washRetired,
};
export function StatCard({ value, label, tone = 'default', wash }) {
    const figureClass = [tone === 'default' ? '' : styles[tone], wash ? styles.figure : '']
        .filter(Boolean)
        .join(' ');
    return (_jsxs(Card, { as: "article", padding: "sm", className: [styles.card, wash ? washClass[wash] : ''].filter(Boolean).join(' '), children: [_jsx(Text, { variant: "stat", className: figureClass || undefined, children: value }), _jsx(Text, { variant: "caption", children: label })] }));
}

import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { publicationCopy } from '../../../content/publication';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './PublicationChecks.module.css';
export function PublicationChecks({ checks }) {
    return (_jsxs(Card, { as: "section", padding: "lg", className: styles.card, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: publicationCopy.checksTitle }), _jsx("ul", { className: styles.list, children: checks.map((check) => (_jsxs("li", { className: styles.item, children: [_jsx("span", { className: check.passed ? styles.pass : styles.fail, "aria-hidden": "true", children: check.passed ? '✓' : '!' }), _jsx("span", { children: check.label })] }, check.id))) })] }));
}

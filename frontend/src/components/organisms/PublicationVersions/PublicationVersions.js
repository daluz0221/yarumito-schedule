import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { publicationCopy } from '../../../content/publication';
import { Badge } from '../../atoms/Badge';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './PublicationVersions.module.css';
export function PublicationVersions({ view }) {
    return (_jsxs(Card, { as: "section", padding: "lg", className: styles.card, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: publicationCopy.versionsTitle }), _jsxs("div", { className: styles.grid, children: [_jsxs("article", { className: styles.current, children: [_jsx("p", { className: styles.version, children: publicationCopy.currentVersion }), _jsx(Badge, { tone: view.status === 'PUBLICADO' ? 'success' : 'warning', children: view.status }), _jsx(Text, { variant: "caption", children: view.versionCaption })] }), _jsxs("article", { className: styles.previous, children: [_jsx("p", { className: styles.version, children: publicationCopy.previousTitle }), _jsx(Text, { variant: "caption", children: publicationCopy.previousEmpty })] })] })] }));
}

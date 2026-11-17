import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { publicationCopy } from '../../../content/publication';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Table } from '../../atoms/Table';
import { Text } from '../../atoms/Text';
import styles from './PublicationLog.module.css';
export function PublicationLog({ record, onPublish }) {
    const published = Boolean(record);
    return (_jsxs(Card, { as: "section", padding: "lg", className: styles.card, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: publicationCopy.logTitle }), record ? (_jsxs(Table, { minWidth: 560, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "Fecha" }), _jsx("th", { children: "Acci\u00F3n" }), _jsx("th", { children: "Versi\u00F3n" }), _jsx("th", { children: "Responsable" })] }) }), _jsx("tbody", { children: _jsxs("tr", { children: [_jsx("td", { children: record.at }), _jsx("td", { children: record.action }), _jsxs("td", { children: ["v", record.version] }), _jsx("td", { children: record.actor })] }) })] })) : (_jsx("p", { className: styles.empty, children: publicationCopy.logEmpty })), _jsxs("div", { className: styles.footer, children: [_jsx("p", { className: styles.note, children: publicationCopy.logNote }), _jsx(Button, { variant: "primary", size: "sm", disabled: published, onClick: onPublish, children: published ? publicationCopy.published : publicationCopy.publish })] })] }));
}

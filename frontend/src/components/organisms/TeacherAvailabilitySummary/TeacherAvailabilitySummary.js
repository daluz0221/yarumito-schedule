import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { restrictionCountLabel } from '../../../content/teacherRestrictions';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './TeacherAvailabilitySummary.module.css';
export function TeacherAvailabilitySummary({ name, areaName, statusLabel, year, restrictionCount, }) {
    return (_jsxs(Card, { className: styles.card, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: name }), _jsxs("p", { className: styles.meta, children: [_jsxs("span", { children: ["\u00C1rea de nombramiento: ", areaName] }), _jsxs("span", { children: ["Estado: ", statusLabel] }), _jsxs("span", { children: ["A\u00F1o: ", year] }), _jsx("span", { children: restrictionCountLabel(restrictionCount) })] })] }));
}

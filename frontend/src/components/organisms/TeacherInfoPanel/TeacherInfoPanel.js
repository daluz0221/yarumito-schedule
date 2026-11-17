import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Badge } from '../../atoms/Badge';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './TeacherInfoPanel.module.css';
const associatedTopics = [
    'Datos generales',
    'Títulos profesionales',
    'Idoneidad profesional',
    'Asignación académica',
];
export function TeacherInfoPanel() {
    return (_jsxs(Card, { className: styles.panel, children: [_jsx(Text, { variant: "sectionTitle", children: "Informaci\u00F3n asociada al docente" }), _jsxs("div", { className: styles.content, children: [_jsx("div", { className: styles.chips, children: associatedTopics.map((topic) => (_jsx(Badge, { tone: "soft", children: topic }, topic))) }), _jsx(Text, { variant: "body", children: "La informaci\u00F3n del docente ser\u00E1 utilizada posteriormente en los procesos de asignaci\u00F3n acad\u00E9mica, validaci\u00F3n de idoneidad y construcci\u00F3n del horario." })] })] }));
}

import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './PendingAssignments.module.css';
export function PendingAssignments({ items, readOnly, onProgram, }) {
    return (_jsxs(Card, { className: styles.card, children: [_jsxs("div", { className: styles.heading, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: "Asignaciones pendientes" }), _jsx(Text, { variant: "body", children: "Horas a\u00FAn no ubicadas en la rejilla." })] }), items.length === 0 ? (_jsx("p", { className: styles.empty, children: "No hay horas pendientes por ubicar en este grupo." })) : (_jsx("ul", { className: styles.list, children: items.map((item) => (_jsxs("li", { className: styles.item, children: [_jsx("p", { className: styles.subject, children: item.subject }), _jsx("p", { className: styles.teacher, children: item.teacherName }), _jsxs("p", { className: styles.hours, children: ["Pendiente: ", item.pendingHours, " h"] }), _jsx(Button, { variant: "primary", size: "sm", fullWidth: true, disabled: readOnly, onClick: () => onProgram(item.key), children: "Programar" })] }, item.key))) })), _jsx(Text, { variant: "caption", children: "Las asignaciones provienen de la asignaci\u00F3n acad\u00E9mica. Aqu\u00ED solo se ubican en d\u00EDa, franja y aula." })] }));
}

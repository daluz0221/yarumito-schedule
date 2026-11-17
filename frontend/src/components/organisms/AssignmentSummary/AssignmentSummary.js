import { jsxs as _jsxs } from "react/jsx-runtime";
import { labelAssignmentStatus, } from '../../../content/academicAssignment';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './AssignmentSummary.module.css';
export function AssignmentSummary({ groupCode, gradeLabel, requiredHours, assignedHours, status, subjectCount, year, }) {
    return (_jsxs(Card, { className: styles.card, children: [_jsxs(Text, { as: "p", variant: "sectionTitle", className: styles.title, children: ["Grupo ", groupCode, " \u00B7 Grado ", gradeLabel, " \u00B7 Requeridas ", requiredHours, " h \u00B7 Asignadas ", assignedHours, " h \u00B7 Estado ", labelAssignmentStatus(status)] }), _jsxs(Text, { variant: "caption", children: ["Resumen de las ", subjectCount, " asignaturas del plan de estudios de", ' ', gradeLabel, " \u00B7 A\u00F1o ", year, "."] })] }));
}

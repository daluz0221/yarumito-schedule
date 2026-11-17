import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Text } from '../../atoms/Text';
import { RegisterTeacherForm } from '../RegisterTeacherForm';
import styles from './RegisterTeacherWorkspace.module.css';
export function RegisterTeacherWorkspace() {
    return (_jsxs("section", { className: styles.workspace, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { variant: "sectionTitle", children: "Nuevo docente" }), _jsx(Text, { variant: "body", children: "Ingrese la informaci\u00F3n necesaria para registrar al docente en el sistema." })] }), _jsx(RegisterTeacherForm, {})] }));
}

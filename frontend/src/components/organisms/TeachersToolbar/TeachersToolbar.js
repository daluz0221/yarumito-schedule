import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useNavigate } from 'react-router-dom';
import { Button } from '../../atoms/Button';
import { Text } from '../../atoms/Text';
import styles from './TeachersToolbar.module.css';
export function TeachersToolbar({ onRefresh }) {
    const navigate = useNavigate();
    return (_jsxs("section", { className: styles.toolbar, children: [_jsxs("div", { className: styles.copy, children: [_jsx(Text, { variant: "sectionTitle", children: "Docentes registrados" }), _jsx(Text, { variant: "body", children: "Consulte, busque y administre la informaci\u00F3n de los docentes de la instituci\u00F3n." })] }), _jsxs("div", { className: styles.actions, children: [_jsx(Button, { variant: "muted", size: "sm", onClick: onRefresh, children: "Actualizar listado" }), _jsx(Button, { variant: "primary", size: "sm", onClick: () => navigate('/dashboard/docentes/registrar'), children: "Registrar docente" })] })] }));
}

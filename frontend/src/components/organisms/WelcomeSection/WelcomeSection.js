import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Text } from '../../atoms/Text';
import styles from './WelcomeSection.module.css';
export function WelcomeSection() {
    return (_jsxs("section", { className: styles.section, children: [_jsx(Text, { variant: "display", children: "Bienvenido al Sistema de Gesti\u00F3n de Horarios" }), _jsx(Text, { variant: "body", children: "Administre la informaci\u00F3n acad\u00E9mica y construya los horarios de la instituci\u00F3n teniendo en cuenta las reglas de negocio y las condiciones de cada per\u00EDodo lectivo." })] }));
}

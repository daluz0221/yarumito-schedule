import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import escudoYarumito from '../../../assets/escudo-yarumito.png';
import { Text } from '../../atoms/Text';
import styles from './SidebarBrand.module.css';
export function SidebarBrand({ title, school, campus }) {
    return (_jsxs("div", { className: styles.brand, children: [_jsx("img", { className: styles.crest, src: escudoYarumito, alt: "Escudo de la Instituci\u00F3n Educativa Yarumito" }), _jsxs("div", { className: styles.copy, children: [_jsx(Text, { variant: "brand", children: title }), _jsx(Text, { variant: "caption", children: school }), _jsx(Text, { variant: "caption", className: styles.campus, children: campus })] })] }));
}

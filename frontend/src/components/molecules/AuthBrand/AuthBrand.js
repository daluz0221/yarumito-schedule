import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import escudoYarumito from '../../../assets/escudo-yarumito.png';
import { Logo } from '../../atoms/Logo';
import { Text } from '../../atoms/Text';
import styles from './AuthBrand.module.css';
export function AuthBrand({ title, subtitle, logoSrc = escudoYarumito, logoAlt = 'Escudo de la Institución Educativa Yarumito', }) {
    return (_jsxs("header", { className: styles.brand, children: [_jsx(Logo, { src: logoSrc, alt: logoAlt }), _jsxs("div", { className: styles.copy, children: [_jsx(Text, { variant: "title", children: title }), _jsx(Text, { variant: "subtitle", children: subtitle })] })] }));
}

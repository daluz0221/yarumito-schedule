import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Logo.module.css';
export function Logo({ src, alt, width = 92 }) {
    return _jsx("img", { className: styles.logo, src: src, alt: alt, width: width });
}

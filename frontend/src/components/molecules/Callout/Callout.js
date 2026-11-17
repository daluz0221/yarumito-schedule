import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Callout.module.css';
export function Callout({ children }) {
    return _jsx("div", { className: styles.callout, children: children });
}

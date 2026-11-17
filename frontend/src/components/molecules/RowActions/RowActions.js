import { jsx as _jsx } from "react/jsx-runtime";
import styles from './RowActions.module.css';
export function RowActions({ children }) {
    return _jsx("div", { className: styles.actions, children: children });
}

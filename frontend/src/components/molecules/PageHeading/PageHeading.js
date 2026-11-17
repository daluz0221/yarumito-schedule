import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Text } from '../../atoms/Text';
import styles from './PageHeading.module.css';
export function PageHeading({ title, subtitle }) {
    return (_jsxs("div", { className: styles.heading, children: [_jsx(Text, { variant: "pageTitle", children: title }), _jsx(Text, { variant: "caption", children: subtitle })] }));
}

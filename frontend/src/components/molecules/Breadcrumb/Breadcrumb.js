import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { TextLink } from '../../atoms/TextLink';
import styles from './Breadcrumb.module.css';
export function Breadcrumb({ items }) {
    return (_jsx("nav", { className: styles.nav, "aria-label": "Migas de pan", children: _jsx("ol", { className: styles.list, children: items.map((item, index) => {
                const isLast = index === items.length - 1;
                return (_jsxs("li", { className: styles.item, children: [item.to && !isLast ? (_jsx(TextLink, { to: item.to, children: item.label })) : (_jsx("span", { className: isLast ? styles.current : undefined, children: item.label })), isLast ? null : _jsx("span", { className: styles.separator, children: "/" })] }, `${item.label}-${index}`));
            }) }) }));
}

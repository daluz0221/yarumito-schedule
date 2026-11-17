import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { IconButton } from '../../atoms/IconButton';
import { MenuIcon } from '../../atoms/icons';
import { PageHeading } from '../../molecules/PageHeading';
import { UserChip } from '../../molecules/UserChip';
import styles from './DashboardHeader.module.css';
export function DashboardHeader({ title, subtitle, onMenuClick, }) {
    return (_jsxs("header", { className: styles.header, children: [_jsxs("div", { className: styles.left, children: [_jsx("span", { className: styles.menuButton, children: _jsx(IconButton, { label: "Abrir men\u00FA", onClick: onMenuClick, children: _jsx(MenuIcon, {}) }) }), _jsx(PageHeading, { title: title, subtitle: subtitle })] }), _jsx(UserChip, { initials: "R", role: "Rector / Administrador", location: "Sede Principal" })] }));
}

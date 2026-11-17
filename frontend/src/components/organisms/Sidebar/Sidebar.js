import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../auth';
import { sidebarAccountNav, sidebarPrimaryNav, sidebarSecondaryNav, } from '../../../content/dashboard';
import { SidebarBrand } from '../../molecules/SidebarBrand';
import { SidebarNavGroup } from '../../molecules/SidebarNavGroup';
import styles from './Sidebar.module.css';
export function Sidebar({ onNavigate }) {
    const { logout } = useAuth();
    const navigate = useNavigate();
    const accountItems = sidebarAccountNav.map((item) => item.id === 'logout'
        ? {
            ...item,
            onClick: () => {
                logout();
                navigate('/admin', { replace: true });
            },
        }
        : item);
    return (_jsxs("aside", { className: styles.sidebar, children: [_jsx(SidebarBrand, { title: "Gesti\u00F3n de Horarios", school: "I.E. Rural Yarumito", campus: "Sede Principal" }), _jsxs("div", { className: styles.nav, children: [_jsx(SidebarNavGroup, { label: "M\u00F3dulos principales", items: sidebarPrimaryNav, onNavigate: onNavigate }), _jsx("div", { className: styles.divider }), _jsx(SidebarNavGroup, { label: "Planificaci\u00F3n", items: sidebarSecondaryNav, onNavigate: onNavigate })] }), _jsx("div", { className: styles.account, children: _jsx(SidebarNavGroup, { label: "Cuenta", items: accountItems, onNavigate: onNavigate }) })] }));
}

import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { CloseIcon } from '../../atoms/icons';
import { IconButton } from '../../atoms/IconButton';
import styles from './DashboardLayout.module.css';
export function DashboardLayout({ sidebar, header, children, }) {
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);
    const closeSidebar = () => setIsSidebarOpen(false);
    const openSidebar = () => setIsSidebarOpen(true);
    useEffect(() => {
        if (!isSidebarOpen) {
            return;
        }
        const onKeyDown = (event) => {
            if (event.key === 'Escape') {
                closeSidebar();
            }
        };
        document.body.style.overflow = 'hidden';
        window.addEventListener('keydown', onKeyDown);
        return () => {
            document.body.style.overflow = '';
            window.removeEventListener('keydown', onKeyDown);
        };
    }, [isSidebarOpen]);
    return (_jsxs("div", { className: styles.shell, children: [_jsxs("div", { className: [styles.sidebarSlot, isSidebarOpen ? styles.open : '']
                    .filter(Boolean)
                    .join(' '), children: [_jsx("div", { className: styles.sidebarClose, children: _jsx(IconButton, { label: "Cerrar men\u00FA", onClick: closeSidebar, children: _jsx(CloseIcon, {}) }) }), sidebar({ onNavigate: closeSidebar })] }), isSidebarOpen ? (_jsx("button", { type: "button", className: styles.backdrop, "aria-label": "Cerrar men\u00FA", onClick: closeSidebar })) : null, _jsxs("div", { className: styles.main, children: [header({ onMenuClick: openSidebar }), _jsx("div", { className: styles.body, children: children })] })] }));
}

import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { AulasWorkspace } from '../../components/organisms/AulasWorkspace';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function AulasPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Aulas", subtitle: "Espacios f\u00EDsicos de cada sede", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [{ label: 'Configuración académica' }, { label: 'Aulas' }] }), _jsx(AulasWorkspace, {})] }));
}

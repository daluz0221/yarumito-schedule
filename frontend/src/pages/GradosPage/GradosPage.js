import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { GradosWorkspace } from '../../components/organisms/GradosWorkspace';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function GradosPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Grados", subtitle: "Administraci\u00F3n de los grados de la instituci\u00F3n", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [{ label: 'Configuración académica' }, { label: 'Grados' }] }), _jsx(GradosWorkspace, {})] }));
}

import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { RestrictionsWorkspace } from '../../components/organisms/RestrictionsWorkspace';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function RestrictionsPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Restricciones docentes", subtitle: "Disponibilidad para la planificaci\u00F3n del a\u00F1o lectivo", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Docentes', to: '/dashboard/docentes' },
                    { label: 'Restricciones y disponibilidad' },
                ] }), _jsx(RestrictionsWorkspace, {})] }));
}

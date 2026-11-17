import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { AreasWorkspace } from '../../components/organisms/AreasWorkspace';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function AreasPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "\u00C1reas y asignaturas", subtitle: "Administraci\u00F3n del cat\u00E1logo acad\u00E9mico institucional", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Gestión académica' },
                    { label: 'Áreas y asignaturas' },
                ] }), _jsx(AreasWorkspace, {})] }));
}

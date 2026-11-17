import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { RegisterTeacherWorkspace } from '../../components/organisms/RegisterTeacherWorkspace';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function RegisterTeacherPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Registrar docente", subtitle: "Registre la informaci\u00F3n b\u00E1sica e institucional del docente.", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Inicio', to: '/dashboard' },
                    { label: 'Gestión de Docentes', to: '/dashboard/docentes' },
                    { label: 'Registrar docente' },
                ] }), _jsx(RegisterTeacherWorkspace, {})] }));
}

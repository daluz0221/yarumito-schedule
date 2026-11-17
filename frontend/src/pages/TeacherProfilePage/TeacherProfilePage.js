import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { TeacherProfileWorkspace } from '../../components/organisms/TeacherProfileWorkspace';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function TeacherProfilePage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Perfil acad\u00E9mico del docente", subtitle: "Gestione las \u00E1reas o asignaturas para las que el docente se encuentra habilitado.", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Volver a docentes', to: '/dashboard/docentes' },
                    { label: 'Perfil académico' },
                ] }), _jsx(TeacherProfileWorkspace, {})] }));
}

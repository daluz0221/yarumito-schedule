import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { TeacherInfoPanel } from '../../components/organisms/TeacherInfoPanel';
import { TeachersWorkspace } from '../../components/organisms/TeachersWorkspace';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function TeachersPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Gesti\u00F3n de Docentes", subtitle: "Administre la informaci\u00F3n general, acad\u00E9mica y profesional de los docentes.", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Inicio', to: '/dashboard' },
                    { label: 'Gestión de Docentes' },
                ] }), _jsx(TeachersWorkspace, {}), _jsx(TeacherInfoPanel, {})] }));
}

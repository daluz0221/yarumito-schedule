import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { StudyPlanWorkspace } from '../../components/organisms/StudyPlanWorkspace';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function StudyPlanPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Plan de estudios", subtitle: "Configuraci\u00F3n curricular por a\u00F1o lectivo y grado", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Gestión académica' },
                    { label: 'Plan de estudios' },
                ] }), _jsx(StudyPlanWorkspace, {})] }));
}

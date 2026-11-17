import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { AssignmentWorkspace } from '../../components/organisms/AssignmentWorkspace';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function AssignmentPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Asignaci\u00F3n acad\u00E9mica", subtitle: "Docentes, asignaturas y grupos del a\u00F1o lectivo", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Planeación académica' },
                    { label: 'Asignación académica' },
                ] }), _jsx(AssignmentWorkspace, {})] }));
}

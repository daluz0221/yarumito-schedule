import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { ScheduleWorkspace } from '../../components/organisms/ScheduleWorkspace';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function SchedulePage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Construcci\u00F3n de horario", subtitle: "Programa asignaciones por d\u00EDa, franja horaria y aula", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Planeación académica' },
                    { label: 'Construcción de horario' },
                ] }), _jsx(ScheduleWorkspace, {})] }));
}

import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { AcademicYearWorkspace } from '../../components/organisms/AcademicYearWorkspace';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function AcademicYearPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "A\u00F1o lectivo y grupos", subtitle: "Configuraci\u00F3n del per\u00EDodo acad\u00E9mico y sus grupos activos", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Configuración académica' },
                    { label: 'Año lectivo y grupos' },
                ] }), _jsx(AcademicYearWorkspace, {})] }));
}

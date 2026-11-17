import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { ValidationsWorkspace } from '../../components/organisms/ValidationsWorkspace';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function ValidationsPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Validaciones y conflictos", subtitle: "Revisa los errores y advertencias detectados antes de publicar el horario", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Planeación académica' },
                    { label: 'Validaciones y conflictos' },
                ] }), _jsx(ValidationsWorkspace, {})] }));
}

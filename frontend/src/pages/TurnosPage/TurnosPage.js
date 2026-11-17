import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { TurnosWorkspace } from '../../components/organisms/TurnosWorkspace';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function TurnosPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Turnos", subtitle: "Jornadas y estructura del horario", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [{ label: 'Configuración académica' }, { label: 'Turnos' }] }), _jsx(TurnosWorkspace, {})] }));
}

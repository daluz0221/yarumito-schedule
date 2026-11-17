import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { SedesWorkspace } from '../../components/organisms/SedesWorkspace';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function SedesPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Sedes", subtitle: "Administraci\u00F3n de las sedes de la instituci\u00F3n", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [{ label: 'Configuración académica' }, { label: 'Sedes' }] }), _jsx(SedesWorkspace, {})] }));
}

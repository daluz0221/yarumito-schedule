import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { FranjasWorkspace } from '../../components/organisms/FranjasWorkspace';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function FranjasPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Franjas horarias", subtitle: "Clases y descansos de cada turno", onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [{ label: 'Configuración académica' }, { label: 'Franjas horarias' }] }), _jsx(FranjasWorkspace, {})] }));
}

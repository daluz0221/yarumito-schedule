import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { dashboardStats } from '../../content/dashboard';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { InsightsSection } from '../../components/organisms/InsightsSection';
import { ModulesSection } from '../../components/organisms/ModulesSection';
import { Sidebar } from '../../components/organisms/Sidebar';
import { StatsSection } from '../../components/organisms/StatsSection';
import { WelcomeSection } from '../../components/organisms/WelcomeSection';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
export function DashboardPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: "Panel principal", subtitle: "Gesti\u00F3n acad\u00E9mica y planificaci\u00F3n de horarios", onMenuClick: onMenuClick })), children: [_jsx(WelcomeSection, {}), _jsx(StatsSection, { items: dashboardStats }), _jsx(ModulesSection, {}), _jsx(InsightsSection, {})] }));
}

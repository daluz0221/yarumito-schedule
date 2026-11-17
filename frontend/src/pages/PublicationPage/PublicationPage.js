import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Breadcrumb } from '../../components/molecules/Breadcrumb';
import { DashboardHeader } from '../../components/organisms/DashboardHeader';
import { PublicationWorkspace } from '../../components/organisms/PublicationWorkspace';
import { Sidebar } from '../../components/organisms/Sidebar';
import { DashboardLayout } from '../../components/templates/DashboardLayout';
import { publicationCopy } from '../../content/publication';
export function PublicationPage() {
    return (_jsxs(DashboardLayout, { sidebar: ({ onNavigate }) => _jsx(Sidebar, { onNavigate: onNavigate }), header: ({ onMenuClick }) => (_jsx(DashboardHeader, { title: publicationCopy.pageTitle, subtitle: publicationCopy.pageSubtitle, onMenuClick: onMenuClick })), children: [_jsx(Breadcrumb, { items: [
                    { label: 'Planeación académica' },
                    { label: publicationCopy.pageTitle },
                ] }), _jsx(PublicationWorkspace, {})] }));
}

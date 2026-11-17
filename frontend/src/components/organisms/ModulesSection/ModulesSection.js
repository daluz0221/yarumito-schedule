import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { dashboardModules, } from '../../../content/dashboard';
import { CalendarIcon, ClipboardIcon, ClockIcon, GridIcon, LayersIcon, PublishIcon, SearchIcon, ShieldIcon, UsersIcon, } from '../../atoms/icons';
import { Text } from '../../atoms/Text';
import { ModuleCard } from '../../molecules/ModuleCard';
import styles from './ModulesSection.module.css';
const moduleIcons = {
    users: _jsx(UsersIcon, {}),
    layers: _jsx(LayersIcon, {}),
    calendar: _jsx(CalendarIcon, {}),
    clock: _jsx(ClockIcon, {}),
    clipboard: _jsx(ClipboardIcon, {}),
    grid: _jsx(GridIcon, {}),
    shield: _jsx(ShieldIcon, {}),
    publish: _jsx(PublishIcon, {}),
    search: _jsx(SearchIcon, {}),
};
export function ModulesSection() {
    return (_jsxs("section", { className: styles.section, children: [_jsx(Text, { variant: "sectionTitle", children: "M\u00F3dulos principales" }), _jsx("div", { className: styles.grid, children: dashboardModules.map((module) => (_jsx(ModuleCard, { icon: moduleIcons[module.icon], title: module.title, description: module.description, available: module.available, highlighted: module.highlighted, to: module.to }, module.id))) })] }));
}

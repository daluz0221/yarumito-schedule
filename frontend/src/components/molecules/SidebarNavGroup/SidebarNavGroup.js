import { jsx as _jsx } from "react/jsx-runtime";
import { BookIcon, BookOpenIcon, CalendarIcon, ClipboardIcon, ClockIcon, GridIcon, HomeIcon, ListIcon, LogoutIcon, PublishIcon, SearchIcon, ShieldIcon, UserIcon, UsersIcon, } from '../../atoms/icons';
import { NavItem } from '../NavItem';
import styles from './SidebarNavGroup.module.css';
const navIcons = {
    home: _jsx(HomeIcon, {}),
    users: _jsx(UsersIcon, {}),
    bookOpen: _jsx(BookOpenIcon, {}),
    calendar: _jsx(CalendarIcon, {}),
    book: _jsx(BookIcon, {}),
    list: _jsx(ListIcon, {}),
    clock: _jsx(ClockIcon, {}),
    clipboard: _jsx(ClipboardIcon, {}),
    grid: _jsx(GridIcon, {}),
    shield: _jsx(ShieldIcon, {}),
    publish: _jsx(PublishIcon, {}),
    search: _jsx(SearchIcon, {}),
    user: _jsx(UserIcon, {}),
    logout: _jsx(LogoutIcon, {}),
};
export function SidebarNavGroup({ items, label, onNavigate, }) {
    return (_jsx("nav", { className: styles.group, "aria-label": label, children: items.map((item) => (_jsx(NavItem, { icon: navIcons[item.icon], label: item.label, to: item.to, end: item.end, disabled: item.disabled, indented: item.indented, onClick: item.onClick, onNavigate: onNavigate }, item.id))) }));
}

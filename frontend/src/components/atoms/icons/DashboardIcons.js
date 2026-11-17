import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { IconFrame } from './IconFrame';
const stroke = {
    stroke: 'currentColor',
    strokeWidth: 1.7,
    strokeLinecap: 'round',
    strokeLinejoin: 'round',
};
export function HomeIcon() {
    return (_jsx(IconFrame, { children: _jsx("path", { d: "M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1v-9.5Z", ...stroke }) }));
}
export function UsersIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("path", { d: "M16 20v-1.2A3.8 3.8 0 0 0 12.2 15H7.8A3.8 3.8 0 0 0 4 18.8V20", ...stroke }), _jsx("circle", { cx: "10", cy: "8.5", r: "3", ...stroke }), _jsx("path", { d: "M20 20v-1.1A3.2 3.2 0 0 0 17.4 16", ...stroke }), _jsx("path", { d: "M16.2 5.4a3 3 0 0 1 0 6.1", ...stroke })] }));
}
export function BookOpenIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("path", { d: "M12 6.5c-2-1.4-4.6-2-7-2v13c2.4 0 5 .6 7 2 2-1.4 4.6-2 7-2v-13c-2.4 0-5 .6-7 2Z", ...stroke }), _jsx("path", { d: "M12 6.5v13", ...stroke })] }));
}
export function CalendarIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("rect", { x: "4", y: "5.5", width: "16", height: "14.5", rx: "2", ...stroke }), _jsx("path", { d: "M4 10h16M8 4v3M16 4v3", ...stroke })] }));
}
export function BookIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("path", { d: "M5 5.5A2.5 2.5 0 0 1 7.5 3H19v16.5H7.5A2.5 2.5 0 0 0 5 22V5.5Z", ...stroke }), _jsx("path", { d: "M5 18.5h14", ...stroke })] }));
}
export function ListIcon() {
    return (_jsx(IconFrame, { children: _jsx("path", { d: "M9 7h11M9 12h11M9 17h11M5 7h.01M5 12h.01M5 17h.01", ...stroke }) }));
}
export function ClockIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("circle", { cx: "12", cy: "12", r: "8", ...stroke }), _jsx("path", { d: "M12 8v4.2L15 15", ...stroke })] }));
}
export function ClipboardIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("rect", { x: "6", y: "5", width: "12", height: "15", rx: "2", ...stroke }), _jsx("path", { d: "M9 5.2V4h6v1.2", ...stroke }), _jsx("path", { d: "M9 11h6M9 15h4", ...stroke })] }));
}
export function GridIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("rect", { x: "4", y: "4", width: "7", height: "7", rx: "1.2", ...stroke }), _jsx("rect", { x: "13", y: "4", width: "7", height: "7", rx: "1.2", ...stroke }), _jsx("rect", { x: "4", y: "13", width: "7", height: "7", rx: "1.2", ...stroke }), _jsx("rect", { x: "13", y: "13", width: "7", height: "7", rx: "1.2", ...stroke })] }));
}
export function ShieldIcon() {
    return (_jsx(IconFrame, { children: _jsx("path", { d: "M12 3.5 19 6.5v6.2c0 4.3-2.9 6.7-7 8.3-4.1-1.6-7-4-7-8.3V6.5L12 3.5Z", ...stroke }) }));
}
export function SearchIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("circle", { cx: "11", cy: "11", r: "6.2", ...stroke }), _jsx("path", { d: "M16 16.2 20 20", ...stroke })] }));
}
export function UserIcon({ size = 20 } = {}) {
    return (_jsxs(IconFrame, { size: size, children: [_jsx("circle", { cx: "12", cy: "8.2", r: "3.2", ...stroke }), _jsx("path", { d: "M5.5 19.2c.8-3 3.2-4.6 6.5-4.6s5.7 1.6 6.5 4.6", ...stroke })] }));
}
export function TrashIcon({ size = 18 } = {}) {
    return (_jsxs(IconFrame, { size: size, children: [_jsx("path", { d: "M4.5 7.5h15", ...stroke }), _jsx("path", { d: "M9.2 7.4V5.8A1.3 1.3 0 0 1 10.5 4.5h3a1.3 1.3 0 0 1 1.3 1.3v1.6", ...stroke }), _jsx("path", { d: "M7.2 7.5 8 18.2a1.4 1.4 0 0 0 1.4 1.3h5.2a1.4 1.4 0 0 0 1.4-1.3l.8-10.7", ...stroke }), _jsx("path", { d: "M10.2 11.2v5.2M13.8 11.2v5.2", ...stroke })] }));
}
export function PencilIcon({ size = 18 } = {}) {
    return (_jsxs(IconFrame, { size: size, children: [_jsx("path", { d: "M4 20h4.2L19.1 9.1a1.7 1.7 0 0 0 0-2.4l-1.8-1.8a1.7 1.7 0 0 0-2.4 0L4 15.8V20Z", ...stroke }), _jsx("path", { d: "m12.6 6.7 4.7 4.7", ...stroke })] }));
}
export function FilterOffIcon({ size = 18 } = {}) {
    return (_jsxs(IconFrame, { size: size, children: [_jsx("path", { d: "M3.5 5.2h11.6L11 10.2v4.1l-2.8 1.2v-5.3L3.5 5.2Z", ...stroke }), _jsx("path", { d: "m15.6 14.6 4.4 4.4M20 14.6l-4.4 4.4", ...stroke })] }));
}
export function BanIcon({ size = 18 } = {}) {
    return (_jsxs(IconFrame, { size: size, children: [_jsx("circle", { cx: "12", cy: "12", r: "7.2", ...stroke }), _jsx("path", { d: "m7.2 16.8 9.6-9.6", ...stroke })] }));
}
export function CheckIcon({ size = 18 } = {}) {
    return (_jsxs(IconFrame, { size: size, children: [_jsx("circle", { cx: "12", cy: "12", r: "7.2", ...stroke }), _jsx("path", { d: "m8.4 12.2 2.3 2.3 4.9-5", ...stroke })] }));
}
export function LogoutIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("path", { d: "M15 7.5V6a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2v-1.5", ...stroke }), _jsx("path", { d: "M10 12h11M18 8.5 21.5 12 18 15.5", ...stroke })] }));
}
export function ChevronRightIcon() {
    return (_jsx(IconFrame, { size: 18, children: _jsx("path", { d: "m9 6 6 6-6 6", ...stroke }) }));
}
export function MenuIcon() {
    return (_jsx(IconFrame, { children: _jsx("path", { d: "M4 7h16M4 12h16M4 17h16", ...stroke }) }));
}
export function CloseIcon() {
    return (_jsx(IconFrame, { children: _jsx("path", { d: "m6 6 12 12M18 6 6 18", ...stroke }) }));
}
export function PlusIcon() {
    return (_jsx(IconFrame, { size: 18, children: _jsx("path", { d: "M12 5v14M5 12h14", ...stroke }) }));
}
export function PublishIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("path", { d: "M12 15.5V5.5M8.2 9.2 12 5.5l3.8 3.7", ...stroke }), _jsx("path", { d: "M5 15v3.2A1.3 1.3 0 0 0 6.3 19.5h11.4a1.3 1.3 0 0 0 1.3-1.3V15", ...stroke })] }));
}
export function LayersIcon() {
    return (_jsxs(IconFrame, { children: [_jsx("path", { d: "m12 4 8 4-8 4-8-4 8-4Z", ...stroke }), _jsx("path", { d: "m4 12 8 4 8-4M4 16l8 4 8-4", ...stroke })] }));
}

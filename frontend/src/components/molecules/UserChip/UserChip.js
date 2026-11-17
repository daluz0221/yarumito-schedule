import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Avatar } from '../../atoms/Avatar';
import { ChevronRightIcon } from '../../atoms/icons';
import { Text } from '../../atoms/Text';
import styles from './UserChip.module.css';
export function UserChip({ initials, role, location }) {
    return (_jsxs("button", { type: "button", className: styles.chip, children: [_jsx(Avatar, { initials: initials }), _jsxs("span", { className: styles.copy, children: [_jsx(Text, { variant: "nav", children: role }), _jsx(Text, { variant: "caption", children: location })] }), _jsx("span", { className: styles.chevron, children: _jsx(ChevronRightIcon, {}) })] }));
}

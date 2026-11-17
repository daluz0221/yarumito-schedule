import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useNavigate } from 'react-router-dom';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './ModuleCard.module.css';
export function ModuleCard({ icon, title, description, available = true, highlighted = false, to, }) {
    const navigate = useNavigate();
    return (_jsxs(Card, { as: "article", highlighted: highlighted, className: styles.card, children: [_jsx("span", { className: styles.icon, children: icon }), _jsxs("div", { className: styles.copy, children: [_jsx(Text, { variant: "nav", as: "h3", children: title }), _jsx(Text, { variant: "body", children: description })] }), _jsx(Button, { variant: available ? 'outline' : 'muted', size: "sm", fullWidth: true, disabled: !available, onClick: () => {
                    if (available && to) {
                        navigate(to);
                    }
                }, children: available ? 'Abrir módulo' : 'Próximamente' })] }));
}

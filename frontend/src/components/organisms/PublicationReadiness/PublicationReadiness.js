import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { publicationCopy } from '../../../content/publication';
import { Badge } from '../../atoms/Badge';
import { Button } from '../../atoms/Button';
import styles from './PublicationReadiness.module.css';
export function PublicationReadiness({ view, onSeeWarnings, onSeeBlocked, }) {
    const blocked = view.readiness === 'blocked';
    return (_jsxs("div", { className: blocked ? styles.blocked : styles.clear, role: "status", children: [_jsx("strong", { children: blocked ? publicationCopy.blocking : publicationCopy.noBlocking }), _jsxs("div", { className: styles.actions, children: [view.warnings > 0 ? (_jsxs(Badge, { tone: "warning", children: [view.warnings, " ", view.warnings === 1 ? 'advertencia pendiente' : 'advertencias pendientes'] })) : null, view.warnings > 0 ? (_jsx(Button, { variant: "outline", size: "sm", onClick: onSeeWarnings, children: publicationCopy.seeWarnings })) : null, blocked ? (_jsx(Button, { variant: "outline", size: "sm", onClick: onSeeBlocked, children: publicationCopy.seeValidations })) : null] }), _jsx("p", { children: blocked
                    ? publicationCopy.blockingNote
                    : view.warnings > 0
                        ? publicationCopy.warningsNote
                        : publicationCopy.readyNote })] }));
}

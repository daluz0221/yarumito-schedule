import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { conflictDetailFacts, labelConflictKind, labelConflictSeverity, labelConflictStatus, } from '../../../content/validations';
import { Badge } from '../../atoms/Badge';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './ConflictDetailModal.module.css';
export function ConflictDetailModal({ open, conflict, scheduleLabel, detectedAt, resolving = false, resolveError = '', onClose, onOpenSchedule, onResolve, }) {
    if (!conflict) {
        return null;
    }
    const warning = conflict.severity === 'ADVERTENCIA';
    return (_jsx(Modal, { open: open, title: labelConflictKind(conflict.kind), onClose: onClose, footer: _jsxs("div", { className: styles.actions, children: [_jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: "Cerrar" }), conflict.status === 'PENDIENTE' && onResolve ? (_jsx(Button, { variant: "outline", size: "sm", disabled: resolving, onClick: onResolve, children: resolving ? 'Guardando…' : 'Marcar como resuelto' })) : null, _jsx(Button, { variant: "primary", size: "sm", onClick: onOpenSchedule, children: "Corregir en horario" })] }), children: _jsxs("div", { className: styles.body, children: [_jsxs("div", { className: styles.pills, children: [_jsx(Badge, { tone: warning ? 'warning' : 'danger', children: labelConflictSeverity(conflict.severity) }), _jsx(Badge, { children: labelConflictStatus(conflict.status) })] }), _jsx("p", { className: warning ? styles.warning : styles.error, children: conflict.message }), _jsx("dl", { className: styles.facts, children: conflictDetailFacts(conflict, scheduleLabel, detectedAt).map((fact) => (_jsxs("div", { className: styles.fact, children: [_jsx("dt", { children: fact.label }), _jsx("dd", { children: fact.value })] }, fact.label))) }), resolveError ? (_jsx("p", { className: styles.resolveError, role: "alert", children: resolveError })) : null] }) }));
}

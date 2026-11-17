import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { labelAvailability, restrictionEffect } from '../../../content/teacherRestrictions';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './RestrictionDetailModal.module.css';
export function RestrictionDetailModal({ open, year, teacherName, bandLabel, restriction, loading = false, error = '', readOnly = false, yearClosed = false, onEdit, onClose, }) {
    const canEdit = !readOnly && Boolean(restriction) && !loading && !error;
    return (_jsx(Modal, { open: open, title: "Detalle de restricci\u00F3n", description: yearClosed
            ? `${teacherName} · Año lectivo ${year} — Cerrado · Solo lectura`
            : `${teacherName} · Año lectivo ${year}`, onClose: onClose, footer: canEdit ? (_jsxs("div", { className: styles.actions, children: [_jsx(Button, { variant: "primary", size: "sm", onClick: onEdit, children: "Editar" }), _jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: "Cerrar" })] })) : (_jsx("div", { className: styles.readOnlyActions, children: _jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: "Cerrar" }) })), children: _jsxs("div", { className: styles.body, children: [loading ? _jsx("p", { className: styles.effect, children: "Cargando restricci\u00F3n..." }) : null, error ? (_jsx("p", { className: styles.error, role: "alert", children: error })) : null, restriction ? (_jsxs(_Fragment, { children: [_jsxs("p", { className: styles.slot, children: [restriction.day, " \u00B7 ", bandLabel] }), _jsxs("p", { className: styles.type, children: ["Tipo: ", labelAvailability(restriction.status)] }), _jsx(Detail, { label: "Motivo", value: restriction.motivo || '—' }), _jsx(Detail, { label: "Documento soporte", value: restriction.documentoSoporte ? 'Documento adjunto' : 'Sin adjunto' }), _jsx(Detail, { label: "Aprobada por:", value: restriction.aprobadaPorId
                                ? 'Aprobación registrada'
                                : 'Sin aprobación registrada' }), _jsx("p", { className: styles.effect, children: restrictionEffect(restriction.status) })] })) : null] }) }));
}
function Detail({ label, value }) {
    return (_jsxs("div", { className: styles.detail, children: [_jsx("p", { children: label }), _jsx("strong", { children: value })] }));
}

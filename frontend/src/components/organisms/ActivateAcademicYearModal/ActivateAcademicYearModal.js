import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { actualizarAnioEscolar, ApiError } from '../../../api';
import { Modal } from '../../atoms/Modal';
import { Callout } from '../../molecules/Callout';
import { ModalActions } from '../../molecules/ModalActions';
import styles from './ActivateAcademicYearModal.module.css';
function fechaIso(value) {
    const local = /^(\d{2})\/(\d{2})\/(\d{4})/.exec(value ?? '');
    if (local) {
        return `${local[3]}-${local[2]}-${local[1]}`;
    }
    return value;
}
export function ActivateAcademicYearModal({ open, year, otroActual = false, onClose, onActivated, }) {
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const aviso = otroActual
        ? 'Ya existe otro año escolar marcado como actual'
        : year && year.status !== 'PLANEACION'
            ? `Transición de estado no permitida: ${year.status} -> ACTIVO`
            : '';
    const handleClose = () => {
        if (submitting) {
            return;
        }
        setError('');
        onClose();
    };
    const confirmar = async () => {
        if (!year || submitting || aviso) {
            return;
        }
        setSubmitting(true);
        setError('');
        try {
            const updated = await actualizarAnioEscolar(year.id, {
                anio: year.year,
                fechaInicio: fechaIso(year.startDate),
                fechaFin: fechaIso(year.endDate),
                estado: 'ACTIVO',
                esActual: true,
            });
            onActivated?.(updated);
            setError('');
            onClose();
        }
        catch (cause) {
            setError(cause instanceof ApiError
                ? cause.message
                : 'No se pudo activar el año escolar. Inténtalo de nuevo.');
        }
        finally {
            setSubmitting(false);
        }
    };
    return (_jsxs(Modal, { open: open, title: `Activar año lectivo ${year?.year ?? ''}`, description: "Al activar este a\u00F1o lectivo, ser\u00E1 utilizado como per\u00EDodo acad\u00E9mico actual del sistema.", onClose: handleClose, footer: _jsx(ModalActions, { confirmLabel: submitting ? 'Guardando...' : 'Activar año', disabled: submitting, onCancel: handleClose, onConfirm: confirmar }), children: [_jsx(Callout, { children: _jsx("p", { children: "Los grupos configurados como activos estar\u00E1n disponibles para la planificaci\u00F3n acad\u00E9mica." }) }), aviso || error ? (_jsx("p", { className: styles.error, role: "alert", children: aviso || error })) : null] }));
}

import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { actualizarAnioEscolar, ApiError } from '../../../api';
import { Modal } from '../../atoms/Modal';
import { Callout } from '../../molecules/Callout';
import { ModalActions } from '../../molecules/ModalActions';
import styles from './CloseAcademicYearModal.module.css';
function fechaIso(value) {
    const local = /^(\d{2})\/(\d{2})\/(\d{4})/.exec(value ?? '');
    if (local) {
        return `${local[3]}-${local[2]}-${local[1]}`;
    }
    return value;
}
export function CloseAcademicYearModal({ open, year, onClose, onClosed, }) {
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const aviso = year && year.status !== 'ACTIVO'
        ? `Transición de estado no permitida: ${year.status} -> CERRADO`
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
                estado: 'CERRADO',
                esActual: false,
            });
            onClosed?.(updated);
            setError('');
            onClose();
        }
        catch (cause) {
            setError(cause instanceof ApiError
                ? cause.message
                : 'No se pudo cerrar el año escolar. Inténtalo de nuevo.');
        }
        finally {
            setSubmitting(false);
        }
    };
    return (_jsxs(Modal, { open: open, title: "Cerrar a\u00F1o lectivo", description: `¿Deseas cerrar el año lectivo ${year?.year ?? ''}?`, onClose: handleClose, footer: _jsx(ModalActions, { confirmLabel: submitting ? 'Guardando...' : 'Cerrar año', disabled: submitting, onCancel: handleClose, onConfirm: confirmar }), children: [_jsx(Callout, { children: _jsx("p", { children: "La informaci\u00F3n se conservar\u00E1 para consulta hist\u00F3rica y no se utilizar\u00E1 en nuevas configuraciones acad\u00E9micas." }) }), aviso || error ? (_jsx("p", { className: styles.error, role: "alert", children: aviso || error })) : null] }));
}

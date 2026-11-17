import { jsxs as _jsxs, jsx as _jsx } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { formatVigencia, labelTipoIdoneidad, } from '../../../content/teacherProfile';
import { Modal } from '../../atoms/Modal';
import { Text } from '../../atoms/Text';
import { Callout } from '../../molecules/Callout';
import { ModalActions } from '../../molecules/ModalActions';
import { TextField } from '../../molecules/TextField';
import styles from './EndSuitabilityModal.module.css';
export function EndSuitabilityModal({ open, item, areaNombre, submitting = false, error = '', onClose, onSubmit, }) {
    const [vigenteHasta, setVigenteHasta] = useState('');
    const [localError, setLocalError] = useState('');
    useEffect(() => {
        if (open) {
            setVigenteHasta('');
            setLocalError('');
        }
    }, [open]);
    const handleSubmit = (event) => {
        event.preventDefault();
        if (submitting) {
            return;
        }
        if (!vigenteHasta) {
            setLocalError('La fecha de finalización es obligatoria');
            return;
        }
        setLocalError('');
        onSubmit(vigenteHasta);
    };
    return (_jsx(Modal, { open: open, title: "Finalizar vigencia de idoneidad", onClose: onClose, footer: _jsx(ModalActions, { formId: "end-suitability-form", confirmLabel: submitting ? 'Guardando...' : 'Finalizar vigencia', confirmType: "submit", disabled: submitting, onCancel: onClose }), children: _jsxs("form", { id: "end-suitability-form", className: styles.form, onSubmit: handleSubmit, children: [_jsxs(Callout, { children: [_jsxs("p", { children: ["\u00C1rea: ", areaNombre || '—'] }), _jsxs("p", { children: ["Tipo: ", item ? labelTipoIdoneidad(item.tipo) : '—'] }), _jsxs("p", { children: ["Vigencia: ", item ? formatVigencia(item) : '—'] })] }), _jsx(TextField, { id: "end-date", name: "vigenteHasta", label: "Fecha de finalizaci\u00F3n", type: "date", required: true, value: vigenteHasta, onChange: (event) => setVigenteHasta(event.target.value) }), error || localError ? (_jsx("p", { className: styles.error, role: "alert", children: error || localError })) : null, _jsx(Text, { variant: "body", children: "La idoneidad permanece vigente hasta la fecha indicada, inclusive. Al d\u00EDa siguiente queda como hist\u00F3rico." })] }) }));
}

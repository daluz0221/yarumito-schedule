import { jsx as _jsx } from "react/jsx-runtime";
import { Modal } from '../../atoms/Modal';
import { Callout } from '../../molecules/Callout';
import { ModalActions } from '../../molecules/ModalActions';
export function RemovePlanSubjectModal({ open, subjectName, gradeLabel, year, onClose, }) {
    return (_jsx(Modal, { open: open, title: "Quitar asignatura del plan", description: `¿Quitar ${subjectName} del plan de ${gradeLabel} del año ${year}?`, onClose: onClose, footer: _jsx(ModalActions, { confirmLabel: "Quitar del plan", onCancel: onClose, onConfirm: onClose }), children: _jsx(Callout, { children: _jsx("p", { children: "La asignatura continuar\u00E1 existiendo en \u00C1reas y asignaturas (M-07). Solo se quitar\u00E1 su relaci\u00F3n con este plan." }) }) }));
}

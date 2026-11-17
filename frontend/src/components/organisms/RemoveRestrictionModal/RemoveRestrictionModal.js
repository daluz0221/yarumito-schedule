import { jsx as _jsx } from "react/jsx-runtime";
import { timeBandLabel } from '../../../content/teacherRestrictions';
import { Modal } from '../../atoms/Modal';
import { ModalActions } from '../../molecules/ModalActions';
import styles from './RemoveRestrictionModal.module.css';
export function RemoveRestrictionModal({ open, teacherName, year, day, bandId, onConfirm, onClose, }) {
    return (_jsx(Modal, { open: open, title: "Quitar restricci\u00F3n", description: `${teacherName} · ${year} · ${day} · ${timeBandLabel(bandId)}`, onClose: onClose, footer: _jsx(ModalActions, { confirmLabel: "Quitar restricci\u00F3n", onCancel: onClose, onConfirm: onConfirm }), children: _jsx("p", { className: styles.copy, children: "Esta franja volver\u00E1 a considerarse disponible para la planificaci\u00F3n del docente." }) }));
}

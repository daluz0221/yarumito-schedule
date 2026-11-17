import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { publicationCopy } from '../../../content/publication';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './PublishScheduleModal.module.css';
export function PublishScheduleModal({ open, view, onClose, onConfirm, }) {
    return (_jsx(Modal, { open: open, title: publicationCopy.confirmTitle, description: publicationCopy.confirmLead(view.version, view.scheduleName, view.campusFull), onClose: onClose, footer: _jsxs("div", { className: styles.actions, children: [_jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: publicationCopy.confirmCancel }), _jsx(Button, { variant: "primary", size: "sm", onClick: onConfirm, children: publicationCopy.confirmAction })] }), children: _jsx("ul", { className: styles.effects, children: publicationCopy.confirmEffects.map((item) => (_jsx("li", { children: item }, item))) }) }));
}

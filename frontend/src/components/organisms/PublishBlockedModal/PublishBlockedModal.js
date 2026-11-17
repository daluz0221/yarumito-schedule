import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { publicationCopy } from '../../../content/publication';
import { Badge } from '../../atoms/Badge';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './PublishBlockedModal.module.css';
export function PublishBlockedModal({ open, errors, onClose, onReview, }) {
    return (_jsx(Modal, { open: open, title: publicationCopy.blockedTitle, onClose: onClose, footer: _jsx("div", { className: styles.actions, children: _jsx(Button, { variant: "primary", size: "sm", onClick: onReview, children: publicationCopy.blockedAction }) }), children: _jsxs("div", { className: styles.body, children: [_jsx("div", { className: styles.pills, children: _jsx(Badge, { tone: "danger", children: errors === 1 ? 'ERROR pendiente' : `${errors} ERRORES pendientes` }) }), _jsx("p", { className: styles.lead, children: publicationCopy.blockedLead }), _jsx("p", { className: styles.hint, children: publicationCopy.blockedHint })] }) }));
}

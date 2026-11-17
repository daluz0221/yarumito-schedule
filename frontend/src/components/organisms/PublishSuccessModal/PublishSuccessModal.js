import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { publicationCopy } from '../../../content/publication';
import { Badge } from '../../atoms/Badge';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './PublishSuccessModal.module.css';
export function PublishSuccessModal({ open, view, record, onClose, onSeeSchedule, }) {
    return (_jsx(Modal, { open: open, title: publicationCopy.successTitle, onClose: onClose, footer: _jsxs("div", { className: styles.actions, children: [_jsx(Button, { variant: "muted", size: "sm", onClick: onSeeSchedule, children: publicationCopy.successSeeSchedule }), _jsx(Button, { variant: "primary", size: "sm", onClick: onClose, children: publicationCopy.successClose })] }), children: _jsxs("div", { className: styles.body, children: [_jsx("div", { className: styles.pills, children: _jsx(Badge, { tone: "success", children: "PUBLICADO" }) }), _jsx("p", { className: styles.lead, children: publicationCopy.successLead(view.version, view.scheduleName) }), _jsxs("dl", { className: styles.facts, children: [_jsx(Fact, { label: "Estado", value: "PUBLICADO" }), _jsx(Fact, { label: "Fecha de publicaci\u00F3n", value: record?.at ?? '—' }), _jsx(Fact, { label: "Publicado por", value: record?.actor ?? view.author })] })] }) }));
}
function Fact({ label, value }) {
    return (_jsxs("div", { className: styles.fact, children: [_jsx("dt", { children: label }), _jsx("dd", { children: value })] }));
}

import { jsxs as _jsxs, jsx as _jsx } from "react/jsx-runtime";
import { publicationCopy, } from '../../../content/publication';
import { Badge } from '../../atoms/Badge';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './PublishWarningsModal.module.css';
export function PublishWarningsModal({ open, warnings, onClose, onReview, onContinue, }) {
    const count = warnings.length;
    return (_jsx(Modal, { open: open, title: publicationCopy.warningsTitle, onClose: onClose, footer: _jsxs("div", { className: styles.actions, children: [_jsx(Button, { variant: "muted", size: "sm", onClick: onReview, children: publicationCopy.warningsReview }), _jsx(Button, { variant: "primary", size: "sm", onClick: onContinue, children: publicationCopy.warningsPublish })] }), children: _jsxs("div", { className: styles.body, children: [_jsx("div", { className: styles.pills, children: _jsxs(Badge, { tone: "warning", children: [count, " ", count === 1 ? 'ADVERTENCIA' : 'ADVERTENCIAS'] }) }), _jsx("p", { className: styles.lead, children: publicationCopy.warningsLead(count) }), _jsxs("div", { className: styles.panel, children: [_jsx("ul", { className: styles.list, children: warnings.map((item) => (_jsx("li", { children: item.title }, item.id))) }), _jsx("p", { className: styles.note, children: publicationCopy.warningsDisclaimer })] })] }) }));
}

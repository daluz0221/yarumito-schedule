import { jsx as _jsx } from "react/jsx-runtime";
import { Modal } from '../../atoms/Modal';
import { Text } from '../../atoms/Text';
import { Callout } from '../../molecules/Callout';
import { ModalActions } from '../../molecules/ModalActions';
export function DeleteTitleModal({ open, onClose }) {
    return (_jsx(Modal, { open: open, title: "Eliminar t\u00EDtulo profesional", description: "\u00BFDeseas eliminar este t\u00EDtulo profesional?", onClose: onClose, footer: _jsx(ModalActions, { onCancel: onClose }), children: _jsx(Callout, { children: _jsx(Text, { variant: "body", children: "Esta acci\u00F3n solo estar\u00E1 disponible cuando el t\u00EDtulo no est\u00E9 siendo utilizado como soporte de una idoneidad." }) }) }));
}

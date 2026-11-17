import { jsx as _jsx, Fragment as _Fragment, jsxs as _jsxs } from "react/jsx-runtime";
import { Button } from '../../atoms/Button';
export function ModalActions({ cancelLabel = 'Cancelar', confirmLabel, confirmType = 'button', formId, disabled = false, onCancel, onConfirm, }) {
    return (_jsxs(_Fragment, { children: [_jsx(Button, { variant: "muted", size: "sm", disabled: disabled, onClick: onCancel, children: cancelLabel }), confirmLabel ? (_jsx(Button, { type: confirmType, form: formId, variant: "primary", size: "sm", disabled: disabled, onClick: onConfirm, children: confirmLabel })) : null] }));
}

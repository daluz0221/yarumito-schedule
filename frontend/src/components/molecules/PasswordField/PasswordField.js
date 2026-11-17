import { jsx as _jsx } from "react/jsx-runtime";
import { useState } from 'react';
import { IconButton } from '../../atoms/IconButton';
import { EyeIcon, EyeOffIcon } from '../../atoms/icons';
import { TextInput } from '../../atoms/TextInput';
import { FormField } from '../FormField';
export function PasswordField({ id, label, ...inputProps }) {
    const [visible, setVisible] = useState(false);
    return (_jsx(FormField, { id: id, label: label, children: _jsx(TextInput, { id: id, type: visible ? 'text' : 'password', autoComplete: "current-password", rightSlot: _jsx(IconButton, { label: visible ? 'Ocultar contraseña' : 'Mostrar contraseña', onClick: () => setVisible((current) => !current), children: visible ? _jsx(EyeOffIcon, {}) : _jsx(EyeIcon, {}) }), ...inputProps }) }));
}

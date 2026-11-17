import { jsx as _jsx } from "react/jsx-runtime";
import { TextInput } from '../../atoms/TextInput';
import { FormField } from '../FormField';
export function TextField({ id, label, required = false, hint, error, ...inputProps }) {
    return (_jsx(FormField, { id: id, label: label, required: required, hint: hint, error: error, children: _jsx(TextInput, { id: id, inputSize: "sm", required: required, "aria-invalid": Boolean(error), "aria-describedby": error ? `${id}-error` : undefined, ...inputProps }) }));
}

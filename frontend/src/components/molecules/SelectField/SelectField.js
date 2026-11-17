import { jsx as _jsx } from "react/jsx-runtime";
import { Select } from '../../atoms/Select';
import { FormField } from '../FormField';
export function SelectField({ id, label, name, value, options, required = false, hint, error, disabled = false, onChange, }) {
    return (_jsx(FormField, { id: id, label: label, required: required, hint: hint, error: error, children: _jsx(Select, { id: id, name: name, value: value, options: options, selectSize: "sm", required: required, disabled: disabled, "aria-invalid": Boolean(error), "aria-describedby": error ? `${id}-error` : undefined, onChange: (event) => onChange(event.target.value) }) }));
}

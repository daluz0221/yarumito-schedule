import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Checkbox } from '../../atoms/Checkbox';
import { Label } from '../../atoms/Label';
import styles from './CheckboxField.module.css';
export function CheckboxField({ id, label, checked, onChange, name, }) {
    return (_jsxs("div", { className: styles.row, children: [_jsx(Checkbox, { id: id, name: name, checked: checked, onChange: (event) => onChange(event.target.checked) }), _jsx(Label, { htmlFor: id, variant: "inline", children: label })] }));
}

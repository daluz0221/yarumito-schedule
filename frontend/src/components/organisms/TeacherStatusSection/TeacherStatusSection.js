import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { registerStatusOptions } from '../../../content/teachers';
import { Text } from '../../atoms/Text';
import { FormSection } from '../../molecules/FormSection';
import { SelectField } from '../../molecules/SelectField';
import styles from './TeacherStatusSection.module.css';
export function TeacherStatusSection({ value, options = registerStatusOptions, hint, error, disabled = false, onChange, }) {
    return (_jsxs(FormSection, { title: "Estado", description: "Estado administrativo del docente: activo, en licencia o retirado.", children: [_jsx(SelectField, { id: "estado", name: "estado", label: "Estado", required: true, value: value, options: options, hint: hint, error: error, disabled: disabled, onChange: (next) => onChange(next) }), _jsx("div", { className: styles.hint, children: _jsx(Text, { variant: "body", children: "Determina si el docente se encuentra activo, en licencia o retirado dentro de los procesos acad\u00E9micos del sistema." }) })] }));
}

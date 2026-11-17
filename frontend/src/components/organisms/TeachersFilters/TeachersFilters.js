import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { teacherContractOptions, teacherStatusOptions, } from '../../../content/teachers';
import { TextInput } from '../../atoms/TextInput';
import { FilterOffIcon } from '../../atoms/icons';
import { FormField } from '../../molecules/FormField';
import { SelectField } from '../../molecules/SelectField';
import styles from './TeachersFilters.module.css';
export function TeachersFilters({ query, area, status, contract, areaOptions, areasLoading = false, areasError, onQueryChange, onAreaChange, onStatusChange, onContractChange, onClear, }) {
    return (_jsxs("form", { className: styles.filters, onSubmit: (event) => event.preventDefault(), children: [_jsx(FormField, { id: "teacher-search", label: "Buscar docente", children: _jsx(TextInput, { id: "teacher-search", name: "query", inputSize: "sm", placeholder: "Buscar por nombre, apellido o documento", value: query, onChange: (event) => onQueryChange(event.target.value) }) }), _jsx(SelectField, { id: "teacher-area", label: "\u00C1rea", name: "area", value: area, options: areaOptions, hint: areasError || (areasLoading ? 'Cargando áreas...' : undefined), disabled: areasLoading || Boolean(areasError), onChange: onAreaChange }), _jsx(SelectField, { id: "teacher-status", label: "Estado", name: "status", value: status, options: teacherStatusOptions, onChange: onStatusChange }), _jsx(SelectField, { id: "teacher-contract", label: "Tipo de vinculaci\u00F3n", name: "contract", value: contract, options: teacherContractOptions, onChange: onContractChange }), _jsx("div", { className: styles.clear, children: _jsxs("button", { type: "button", className: styles.clearButton, onClick: onClear, children: [_jsx(FilterOffIcon, {}), "Limpiar filtros"] }) })] }));
}

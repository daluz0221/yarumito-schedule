import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { opcionesFiltroGrados, useCatalogoGrados } from '../../../api';
import { groupStatusFilterOptions } from '../../../content/academicYear';
import { TextInput } from '../../atoms/TextInput';
import { FormField } from '../../molecules/FormField';
import { SelectField } from '../../molecules/SelectField';
import styles from './SchoolGroupsFilters.module.css';
export function SchoolGroupsFilters({ grade, status, query, onGradeChange, onStatusChange, onQueryChange, }) {
    const { grados, error, loading } = useCatalogoGrados();
    const gradeOptions = opcionesFiltroGrados(grados, error);
    const gradeLista = !loading && !error;
    return (_jsxs("form", { className: styles.filters, onSubmit: (event) => event.preventDefault(), children: [_jsx(SelectField, { id: "group-grade", label: "Grado", name: "grade", value: gradeLista ? grade : '', options: gradeOptions, disabled: !gradeLista, error: error || undefined, hint: !error && grados?.length === 0 ? 'Sin grados registrados' : undefined, onChange: onGradeChange }), _jsx(SelectField, { id: "group-status", label: "Estado", name: "status", value: status, options: groupStatusFilterOptions, onChange: onStatusChange }), _jsx(FormField, { id: "group-search", label: "Buscar grupo", children: _jsx(TextInput, { id: "group-search", name: "query", inputSize: "sm", placeholder: "Buscar por c\u00F3digo...", value: query, onChange: (event) => onQueryChange(event.target.value) }) })] }));
}

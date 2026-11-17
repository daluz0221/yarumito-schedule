import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Card } from '../../atoms/Card';
import { FormField } from '../../molecules/FormField';
import { SelectField } from '../../molecules/SelectField';
import styles from './AssignmentSelector.module.css';
export function AssignmentSelector({ yearId, groupId, gradeLabel, yearStatus, yearOptions, groupOptions, yearsDisabled = false, groupsDisabled = false, onYearChange, onGroupChange, }) {
    const resolvedGroups = groupOptions.length > 0 ? groupOptions : [{ value: '', label: 'Sin grupos activos' }];
    const emptyGroups = resolvedGroups.every((option) => option.value === '');
    return (_jsx(Card, { className: styles.card, children: _jsxs("form", { className: styles.filters, onSubmit: (event) => event.preventDefault(), children: [_jsx(SelectField, { id: "assignment-year", label: "A\u00F1o lectivo", name: "year", value: yearId, options: yearOptions, disabled: yearsDisabled, onChange: onYearChange }), _jsx(SelectField, { id: "assignment-group", label: "Grupo", name: "group", value: emptyGroups ? '' : groupId, options: resolvedGroups, disabled: groupsDisabled || emptyGroups || yearStatus === 'CERRADO', onChange: onGroupChange }), _jsx(FormField, { id: "assignment-grade", label: "Grado", children: _jsx("div", { className: styles.derived, id: "assignment-grade", children: gradeLabel ? `${gradeLabel} · Derivado del grupo` : 'Derivado del grupo' }) })] }) }));
}

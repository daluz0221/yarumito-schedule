import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useRef, useState } from 'react';
import { ApiError, consultarPlanEstudios } from '../../../api';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { TextInput } from '../../atoms/TextInput';
import { FormField } from '../../molecules/FormField';
import { SectionHeader } from '../../molecules/SectionHeader';
import { SelectField } from '../../molecules/SelectField';
import { GradeSubjectsTable } from '../GradeSubjectsTable';
import { PlanSubjectFormModal } from '../PlanSubjectFormModal';
import { RemovePlanSubjectModal } from '../RemovePlanSubjectModal';
import styles from './GradeSubjectsSection.module.css';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function detalleError(cause) {
    if (cause instanceof ApiError) {
        if (cause.status === 404) {
            return 'Este registro del plan ya no está disponible. Cierra el formulario y revisa el listado.';
        }
        return cause.message;
    }
    return 'No se pudo cargar el registro del plan de estudios.';
}
export function GradeSubjectsSection({ year, yearId, gradeId, gradeLabel, areaNames, area, query, rows, planes, emptyPlanMessage = 'Este grado todavía no tiene asignaturas en el plan.', onAreaChange, onQueryChange, onSaved, onMissing, }) {
    const [dialog, setDialog] = useState(null);
    const [editing, setEditing] = useState(null);
    const [editLoading, setEditLoading] = useState(false);
    const [editError, setEditError] = useState('');
    const detailRequest = useRef(null);
    useEffect(() => {
        return () => detailRequest.current?.abort();
    }, []);
    const closeDialog = () => {
        detailRequest.current?.abort();
        detailRequest.current = null;
        setDialog(null);
        setEditing(null);
        setEditLoading(false);
        setEditError('');
    };
    const loadPlan = (planId) => {
        detailRequest.current?.abort();
        const controller = new AbortController();
        detailRequest.current = controller;
        setDialog({ name: 'edit', id: planId });
        setEditing(null);
        setEditError('');
        setEditLoading(true);
        consultarPlanEstudios(planId, controller.signal)
            .then((plan) => {
            if (controller.signal.aborted) {
                return;
            }
            setEditing(plan);
            onSaved(plan);
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setEditing(null);
            setEditError(detalleError(cause));
            if (cause instanceof ApiError && cause.status === 404) {
                onMissing?.(planId);
            }
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setEditLoading(false);
            }
        });
    };
    const areaOptions = [
        { value: '', label: 'Todas' },
        ...areaNames.map((name) => ({ value: name, label: name })),
    ];
    return (_jsxs(Card, { className: styles.section, children: [_jsx(SectionHeader, { title: "Asignaturas del grado", description: `Estructura curricular configurada para ${year} · ${gradeLabel}.`, action: _jsx(Button, { variant: "primary", size: "sm", onClick: () => setDialog({ name: 'create' }), children: "+ Agregar asignatura" }) }), _jsxs("form", { className: styles.filters, onSubmit: (event) => event.preventDefault(), children: [_jsx(FormField, { id: "plan-search", label: "Buscar asignatura", children: _jsx(TextInput, { id: "plan-search", name: "query", inputSize: "sm", placeholder: "Buscar por nombre...", value: query, onChange: (event) => onQueryChange(event.target.value) }) }), _jsx(SelectField, { id: "plan-area", label: "\u00C1rea", name: "area", value: area, options: areaOptions, onChange: onAreaChange })] }), _jsx("div", { className: styles.tableHost, children: _jsx(GradeSubjectsTable, { rows: rows, emptyMessage: query || area
                        ? 'No se encontraron asignaturas con esos filtros.'
                        : emptyPlanMessage, onEdit: (entry) => loadPlan(entry.id), onRemove: (entry) => setDialog({ name: 'remove', entry }) }) }), _jsx(Text, { variant: "caption", children: "Configuraci\u00F3n por grado, no por grupo. Una asignatura no puede duplicarse para el mismo A\u00F1o lectivo + Grado." }), dialog?.name === 'create' || dialog?.name === 'edit' ? (_jsx(PlanSubjectFormModal, { open: true, mode: dialog.name, year: year, yearId: yearId, gradeId: gradeId, gradeLabel: gradeLabel, planes: planes, plan: editing ?? undefined, loading: dialog.name === 'edit' && editLoading, loadError: dialog.name === 'edit' ? editError : '', onRetry: dialog.name === 'edit' ? () => loadPlan(dialog.id) : undefined, onClose: closeDialog, onCreated: onSaved, onUpdated: onSaved })) : null, dialog?.name === 'remove' ? (_jsx(RemovePlanSubjectModal, { open: true, subjectName: dialog.entry.subject, gradeLabel: gradeLabel, year: year, onClose: closeDialog })) : null] }));
}

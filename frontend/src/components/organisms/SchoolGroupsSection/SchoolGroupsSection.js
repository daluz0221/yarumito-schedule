import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useRef, useState } from 'react';
import { ApiError, consultarGrupo } from '../../../api';
import { StatCard } from '../../molecules/StatCard';
import { SectionHeader } from '../../molecules/SectionHeader';
import { Button } from '../../atoms/Button';
import { GroupFormModal } from '../GroupFormModal';
import { SchoolGroupsFilters } from '../SchoolGroupsFilters';
import { SchoolGroupsTable } from '../SchoolGroupsTable';
import styles from './SchoolGroupsSection.module.css';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function detalleError(cause) {
    if (cause instanceof ApiError) {
        if (cause.status === 404) {
            return 'Este grupo ya no está disponible. Cierra el formulario y revisa el listado.';
        }
        return cause.message;
    }
    return 'No se pudo cargar el grupo.';
}
export function SchoolGroupsSection({ year, groups, stats, grade, status, query, rows, emptyMessage, onGradeChange, onStatusChange, onQueryChange, onGroupCreated, }) {
    const [modal, setModal] = useState({ name: 'closed' });
    const [editing, setEditing] = useState(null);
    const [editLoading, setEditLoading] = useState(false);
    const [editError, setEditError] = useState('');
    const detailRequest = useRef(null);
    useEffect(() => {
        return () => detailRequest.current?.abort();
    }, []);
    const closeModal = () => {
        detailRequest.current?.abort();
        detailRequest.current = null;
        setModal({ name: 'closed' });
        setEditing(null);
        setEditLoading(false);
        setEditError('');
    };
    const loadGrupo = (groupId) => {
        detailRequest.current?.abort();
        const controller = new AbortController();
        detailRequest.current = controller;
        setModal({ name: 'edit', id: groupId });
        setEditing(null);
        setEditError('');
        setEditLoading(true);
        consultarGrupo(groupId, controller.signal)
            .then((grupo) => {
            if (controller.signal.aborted) {
                return;
            }
            setEditing(grupo);
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setEditing(null);
            setEditError(detalleError(cause));
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setEditLoading(false);
            }
        });
    };
    return (_jsxs("section", { className: styles.section, children: [_jsx(SectionHeader, { title: "Grupos del a\u00F1o lectivo", description: "Define los grupos existentes para cada grado y selecciona cu\u00E1les estar\u00E1n activos durante el per\u00EDodo.", action: _jsx(Button, { variant: "primary", size: "sm", onClick: () => setModal({ name: 'create' }), children: "+ Agregar grupo" }) }), _jsx("div", { className: styles.stats, "aria-label": "Resumen de grupos", children: stats.map((stat) => (_jsx(StatCard, { value: stat.value, label: stat.label }, stat.id))) }), _jsx(SchoolGroupsFilters, { grade: grade, status: status, query: query, onGradeChange: onGradeChange, onStatusChange: onStatusChange, onQueryChange: onQueryChange }), _jsx(SchoolGroupsTable, { rows: rows, emptyMessage: emptyMessage, onEdit: (group) => loadGrupo(group.id) }), _jsx(GroupFormModal, { open: modal.name === 'create', mode: "create", year: year, groups: groups, onClose: closeModal, onCreated: () => {
                    closeModal();
                    onGroupCreated?.();
                } }), _jsx(GroupFormModal, { open: modal.name === 'edit', mode: "edit", year: year, groups: groups, grupo: editing ?? undefined, loading: editLoading, loadError: editError, onRetry: modal.name === 'edit' ? () => loadGrupo(modal.id) : undefined, onClose: closeModal, onUpdated: () => onGroupCreated?.() })] }));
}

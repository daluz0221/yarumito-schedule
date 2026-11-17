import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useRef, useState } from 'react';
import { ApiError, consultarAsignatura } from '../../../api';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { TextInput } from '../../atoms/TextInput';
import { BanIcon, CheckIcon, PencilIcon } from '../../atoms/icons';
import { FormField } from '../../molecules/FormField';
import { SectionHeader } from '../../molecules/SectionHeader';
import { SelectField } from '../../molecules/SelectField';
import { DeactivateSubjectModal } from '../DeactivateSubjectModal';
import { SubjectFormModal, } from '../SubjectFormModal';
import styles from './SubjectsSection.module.css';
const statusOptions = [
    { value: '', label: 'Todas' },
    { value: 'activa', label: 'Activas' },
    { value: 'inactiva', label: 'Inactivas' },
];
const typeOptions = [
    { value: '', label: 'Todas' },
    { value: 'regular', label: 'Regular' },
    { value: 'media', label: 'Media técnica' },
];
function subjectDetailErrorMessage(cause) {
    if (cause instanceof ApiError) {
        if (cause.status === 404) {
            return 'Esta asignatura ya no está disponible. Cierra el formulario y revisa el listado.';
        }
        return cause.message;
    }
    return 'No se pudo cargar la asignatura.';
}
export function SubjectsSection({ areas, selectedAreaId, areaName, subjects = [], subjectsLoading = false, subjectsError = '', onSubjectCreated, onSubjectUpdated, onSubjectMissing, }) {
    const [query, setQuery] = useState('');
    const [status, setStatus] = useState('');
    const [type, setType] = useState('');
    const [modal, setModal] = useState({ name: 'closed' });
    const [editingSubject, setEditingSubject] = useState(null);
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
        setEditingSubject(null);
        setEditLoading(false);
        setEditError('');
    };
    const loadSubjectDetail = (subjectId) => {
        detailRequest.current?.abort();
        const controller = new AbortController();
        detailRequest.current = controller;
        setModal({ name: 'edit', id: subjectId });
        setEditingSubject(null);
        setEditError('');
        setEditLoading(true);
        consultarAsignatura(subjectId, controller.signal)
            .then((subject) => {
            if (controller.signal.aborted) {
                return;
            }
            setEditingSubject(subject);
            onSubjectUpdated?.(subject);
        })
            .catch((cause) => {
            if (controller.signal.aborted ||
                (cause instanceof DOMException && cause.name === 'AbortError')) {
                return;
            }
            setEditingSubject(null);
            setEditError(subjectDetailErrorMessage(cause));
            if (cause instanceof ApiError && cause.status === 404) {
                onSubjectMissing?.(subjectId);
            }
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setEditLoading(false);
            }
        });
    };
    const normalizedQuery = query.trim().toLowerCase();
    const visibleSubjects = subjects.filter((subject) => {
        if (status === 'activa' && subject.activa === false) {
            return false;
        }
        if (status === 'inactiva' && subject.activa !== false) {
            return false;
        }
        if (type === 'media' && !subject.esMediaTecnica) {
            return false;
        }
        if (type === 'regular' && subject.esMediaTecnica) {
            return false;
        }
        if (!normalizedQuery) {
            return true;
        }
        return (subject.codigo.toLowerCase().includes(normalizedQuery) ||
            subject.nombre.toLowerCase().includes(normalizedQuery) ||
            (subject.abreviatura ?? '').toLowerCase().includes(normalizedQuery));
    });
    const emptyMessage = subjectsError
        ? subjectsError
        : subjectsLoading
            ? 'Cargando asignaturas...'
            : !areaName
                ? 'Selecciona un área para ver sus asignaturas.'
                : subjects.length === 0
                    ? 'Esta área no tiene asignaturas registradas.'
                    : 'No se encontraron asignaturas con esos filtros.';
    return (_jsxs(Card, { className: styles.section, children: [_jsx(SectionHeader, { title: "Asignaturas", description: areaName
                    ? `Asignaturas del área ${areaName}`
                    : 'Selecciona un área para consultar sus asignaturas.', action: _jsx(Button, { variant: "primary", size: "sm", onClick: () => setModal({ name: 'create' }), children: "+ Nueva asignatura" }) }), _jsxs("form", { className: styles.filters, onSubmit: (event) => event.preventDefault(), children: [_jsx(FormField, { id: "subject-search", label: "Buscar asignatura", children: _jsx(TextInput, { id: "subject-search", name: "query", inputSize: "sm", placeholder: "Buscar por c\u00F3digo o nombre...", value: query, onChange: (event) => setQuery(event.target.value) }) }), _jsx(SelectField, { id: "subject-status", label: "Estado", name: "status", value: status, options: statusOptions, onChange: setStatus }), _jsx(SelectField, { id: "subject-type", label: "Tipo", name: "type", value: type, options: typeOptions, onChange: setType })] }), _jsxs("div", { className: styles.tableHost, children: [_jsx("div", { className: styles.scroll, children: _jsxs("table", { className: styles.table, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "C\u00F3digo" }), _jsx("th", { children: "Asignatura" }), _jsx("th", { children: "Idoneidad" }), _jsx("th", { children: "Media T\u00E9cnica" }), _jsx("th", { children: "M\u00E1x. consecutivas" }), _jsx("th", { children: "Estado" }), _jsx("th", { children: "Acciones" })] }) }), _jsx("tbody", { children: visibleSubjects.map((subject) => (_jsxs("tr", { children: [_jsx("td", { children: subject.codigo }), _jsx("td", { children: subject.nombre }), _jsx("td", { className: subject.exigeIdoneidadEstricta ? styles.positive : undefined, children: subject.exigeIdoneidadEstricta ? 'Estricto' : 'No' }), _jsx("td", { children: subject.esMediaTecnica ? 'Sí' : 'No' }), _jsx("td", { children: subject.maxClasesConsecutivas }), _jsx("td", { className: subject.activa ? styles.positive : undefined, children: subject.activa ? 'Activa' : 'Inactiva' }), _jsx("td", { children: _jsx(SubjectActions, { nombre: subject.nombre, activa: subject.activa, onEdit: () => loadSubjectDetail(subject.id), onDeactivate: () => setModal({ name: 'deactivate', subject }) }) })] }, subject.id))) })] }) }), visibleSubjects.length > 0 ? (_jsx("ul", { className: styles.cards, children: visibleSubjects.map((subject) => (_jsxs("li", { className: styles.card, children: [_jsxs("div", { className: styles.cardHeader, children: [_jsx("strong", { children: subject.nombre }), _jsx("span", { className: subject.activa ? styles.positive : undefined, children: subject.activa ? 'Activa' : 'Inactiva' })] }), _jsxs("p", { children: [subject.codigo, ' · ', _jsx("span", { className: subject.exigeIdoneidadEstricta ? styles.positive : undefined, children: subject.exigeIdoneidadEstricta ? 'Estricto' : 'No' }), ' · ', subject.maxClasesConsecutivas, " consecutivas"] }), _jsx(SubjectActions, { nombre: subject.nombre, activa: subject.activa, onEdit: () => loadSubjectDetail(subject.id), onDeactivate: () => setModal({ name: 'deactivate', subject }) })] }, subject.id))) })) : null, visibleSubjects.length === 0 ? (_jsx("p", { className: styles.empty, children: emptyMessage })) : null] }), modal.name === 'create' || modal.name === 'edit' ? (_jsx(SubjectFormModal, { open: true, mode: modal.name, areas: areas, selectedAreaId: selectedAreaId, subject: modal.name === 'edit' ? editingSubject ?? undefined : undefined, loading: modal.name === 'edit' && editLoading, loadError: modal.name === 'edit' ? editError : '', onRetry: modal.name === 'edit' ? () => loadSubjectDetail(modal.id) : undefined, onClose: closeModal, onCreated: onSubjectCreated, onUpdated: onSubjectUpdated }, modal.name === 'edit' ? modal.id : selectedAreaId || 'create')) : null, modal.name === 'deactivate' ? (_jsx(DeactivateSubjectModal, { open: true, subject: modal.subject, onClose: closeModal, onUpdated: onSubjectUpdated })) : null] }));
}
function SubjectActions({ nombre, activa, onEdit, onDeactivate, }) {
    const inactive = activa === false;
    const toggleLabel = inactive ? 'Activar' : 'Inactivar';
    return (_jsxs("span", { className: styles.actions, children: [_jsx("button", { type: "button", className: styles.iconAction, onClick: onEdit, "aria-label": `Editar ${nombre}`, title: "Editar", children: _jsx(PencilIcon, {}) }), _jsx("button", { type: "button", className: `${styles.iconAction} ${inactive ? styles.iconActionOn : styles.iconActionOff}`, onClick: onDeactivate, "aria-label": `${toggleLabel} ${nombre}`, title: toggleLabel, children: inactive ? _jsx(CheckIcon, {}) : _jsx(BanIcon, {}) })] }));
}

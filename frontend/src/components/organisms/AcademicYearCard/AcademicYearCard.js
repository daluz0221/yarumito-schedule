import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useRef, useState } from 'react';
import { aAcademicYear, ApiError, consultarAnioEscolar, } from '../../../api';
import { academicYearOptions, formatYearTimeline, labelYearStatus, nextPlannedYear, } from '../../../content/academicYear';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { SelectField } from '../../molecules/SelectField';
import { AcademicYearFormModal } from '../AcademicYearFormModal';
import { ActivateAcademicYearModal } from '../ActivateAcademicYearModal';
import { CloseAcademicYearModal } from '../CloseAcademicYearModal';
import styles from './AcademicYearCard.module.css';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function detalleError(cause) {
    if (cause instanceof ApiError) {
        if (cause.status === 404) {
            return 'Este año escolar ya no está disponible. Cierra el formulario y revisa el listado.';
        }
        return cause.message;
    }
    return 'No se pudo cargar el año escolar.';
}
export function AcademicYearCard({ years, year, onYearChange, onSaved, onMissing, }) {
    const [modal, setModal] = useState({ name: 'closed' });
    const [editing, setEditing] = useState(null);
    const [editLoading, setEditLoading] = useState(false);
    const [editError, setEditError] = useState('');
    const detailRequest = useRef(null);
    const plannedYear = year.status === 'ACTIVO' && year.current
        ? nextPlannedYear(years, year)
        : null;
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
    const publicar = (anio) => {
        const mapped = aAcademicYear(anio);
        if (mapped) {
            onSaved(mapped);
        }
    };
    const loadAnio = (anioId) => {
        detailRequest.current?.abort();
        const controller = new AbortController();
        detailRequest.current = controller;
        setModal({ name: 'edit' });
        setEditing(null);
        setEditError('');
        setEditLoading(true);
        consultarAnioEscolar(anioId, controller.signal)
            .then((anio) => {
            if (controller.signal.aborted) {
                return;
            }
            setEditing(anio);
            const mapped = aAcademicYear(anio);
            if (mapped) {
                onSaved(mapped);
            }
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setEditing(null);
            setEditError(detalleError(cause));
            if (cause instanceof ApiError && cause.status === 404) {
                onMissing?.(anioId);
            }
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setEditLoading(false);
            }
        });
    };
    return (_jsxs(_Fragment, { children: [_jsxs(Card, { as: "section", padding: "lg", className: styles.card, children: [_jsxs("div", { className: styles.top, children: [_jsxs("div", { className: styles.copy, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: "A\u00F1o lectivo" }), _jsx(Text, { variant: "body", children: "Consulta el per\u00EDodo actual y administra su ciclo de vida." })] }), _jsxs("div", { className: styles.yearControls, children: [_jsx("div", { className: styles.yearSelect, children: _jsx(SelectField, { id: "academic-year", label: "A\u00F1o lectivo", name: "academicYear", value: year.id, options: academicYearOptions(years), onChange: onYearChange }) }), _jsx(Button, { variant: "primary", size: "sm", onClick: () => setModal({ name: 'create' }), children: "+ Nuevo a\u00F1o lectivo" })] })] }), _jsxs("dl", { className: styles.facts, children: [_jsx(Fact, { label: "A\u00F1o", value: String(year.year) }), _jsx(Fact, { label: "Fecha de inicio", value: year.startDate }), _jsx(Fact, { label: "Fecha de finalizaci\u00F3n", value: year.endDate }), _jsx(Fact, { label: "Estado", value: labelYearStatus(year.status), tone: statusTone(year.status) }), _jsx(Fact, { label: "A\u00F1o actual", value: year.current ? 'Sí' : 'No' })] }), _jsxs("div", { className: styles.row, children: [_jsxs(Text, { variant: "body", className: styles.note, children: ["Acciones disponibles para ", year.year, " \u2014 ", labelYearStatus(year.status)] }), _jsxs("div", { className: styles.actions, children: [_jsx(Button, { variant: "outline", size: "sm", onClick: () => loadAnio(year.id), children: "Editar fechas" }), year.status === 'ACTIVO' ? (_jsx(Button, { variant: "primary", size: "sm", onClick: () => setModal({ name: 'close' }), children: "Cerrar a\u00F1o" })) : null, year.status === 'PLANEACION' ? (_jsxs(Button, { variant: "primary", size: "sm", onClick: () => setModal({ name: 'activate', year }), children: ["Activar ", year.year] })) : null] })] }), _jsxs("div", { className: styles.row, children: [_jsx(Text, { variant: "caption", className: styles.note, children: formatYearTimeline(years) }), plannedYear ? (_jsx("div", { className: styles.actions, children: _jsxs(Button, { variant: "primary", size: "sm", onClick: () => setModal({ name: 'activate', year: plannedYear }), children: ["Activar ", plannedYear.year] }) })) : null] })] }), _jsx(AcademicYearFormModal, { open: modal.name === 'create', mode: "create", years: years, onClose: closeModal, onCreated: publicar }), _jsx(AcademicYearFormModal, { open: modal.name === 'edit', mode: "edit", years: years, anio: editing ?? undefined, loading: editLoading, loadError: editError, onRetry: () => loadAnio(year.id), onClose: closeModal, onUpdated: publicar }), _jsx(ActivateAcademicYearModal, { open: modal.name === 'activate', year: modal.name === 'activate' ? modal.year : undefined, otroActual: years.some((item) => item.current &&
                    item.id !== (modal.name === 'activate' ? modal.year.id : '')), onClose: closeModal, onActivated: publicar }), _jsx(CloseAcademicYearModal, { open: modal.name === 'close', year: year, onClose: closeModal, onClosed: publicar })] }));
}
function Fact({ label, value, tone, }) {
    return (_jsxs("div", { className: styles.fact, children: [_jsx(Text, { as: "dt", variant: "caption", children: label }), _jsx("dd", { className: [styles.value, tone ? styles[tone] : ''].filter(Boolean).join(' '), children: value })] }));
}
function statusTone(status) {
    if (status === 'ACTIVO') {
        return 'success';
    }
    if (status === 'PLANEACION') {
        return 'warning';
    }
    return undefined;
}

import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { actualizarRestriccion, ApiError, consultarRestriccion, diaApi, aRestriccionVista, registrarRestriccion, tipoApi, } from '../../../api';
import { availabilityDays, labelAvailability, } from '../../../content/teacherRestrictions';
import { Select } from '../../atoms/Select';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { RestrictionDetailModal } from '../RestrictionDetailModal';
import { RestrictionFormModal } from '../RestrictionFormModal';
import { RestrictionTypeModal } from '../RestrictionTypeModal';
import styles from './WeeklyAvailability.module.css';
const legend = [
    { status: 'DISPONIBLE', detail: 'Sin restricción' },
    { status: 'NO_DISPONIBLE', detail: 'Bloqueante' },
    { status: 'PREFERENCIA', detail: 'Advertencia' },
];
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function mensajeError(cause, fallback) {
    return cause instanceof ApiError && cause.message ? cause.message : fallback;
}
function findRestriction(restrictions, bandId, day) {
    return restrictions.find((item) => item.bandId === bandId && item.day === day);
}
export function WeeklyAvailability({ yearId, year, teacherId, teacherName, readOnly, yearClosed = false, shiftLabel, turnos, turnoId, onTurnoChange, bands, bandsLoading, bandsError, restrictions, loading, error, onSaved, }) {
    const [dialog, setDialog] = useState(null);
    const [consulted, setConsulted] = useState(null);
    const [consultKey, setConsultKey] = useState('');
    const [consulting, setConsulting] = useState(false);
    const [consultError, setConsultError] = useState('');
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState('');
    const closeDialog = () => {
        setDialog(null);
        setSaveError('');
    };
    const detailKey = dialog && dialog.name !== 'create' && dialog.name !== 'type' && dialog.day
        ? `${dialog.day}-${dialog.bandId}`
        : '';
    const listed = dialog && dialog.name !== 'create' && dialog.name !== 'type' && dialog.day
        ? findRestriction(restrictions, dialog.bandId, dialog.day)
        : undefined;
    const restriction = consultKey === detailKey && consulted ? consulted : listed;
    const classBands = bands.filter((band) => band.kind === 'class');
    const bandLabel = (bandId) => bands.find((band) => band.id === bandId)?.label ?? '';
    useEffect(() => {
        if (dialog?.name !== 'detail' || !dialog.day) {
            return;
        }
        const current = findRestriction(restrictions, dialog.bandId, dialog.day);
        if (!current) {
            return;
        }
        const controller = new AbortController();
        const key = `${dialog.day}-${dialog.bandId}`;
        setConsulting(true);
        setConsultError('');
        setConsulted(null);
        setConsultKey(key);
        consultarRestriccion(current.id, controller.signal)
            .then((dto) => {
            if (controller.signal.aborted) {
                return;
            }
            const vista = aRestriccionVista(dto);
            if (!vista) {
                setConsultError('No se pudo leer la restricción.');
                return;
            }
            setConsulted(vista);
            setConsultKey(key);
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setConsultError(mensajeError(cause, 'No se pudo consultar la restricción.'));
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setConsulting(false);
            }
        });
        return () => controller.abort();
    }, [dialog, restrictions]);
    const statusAt = (bandId, day) => findRestriction(restrictions, bandId, day)?.status ?? 'DISPONIBLE';
    const openSlot = (day, bandId) => {
        const status = statusAt(bandId, day);
        if (readOnly) {
            if (status !== 'DISPONIBLE') {
                setDialog({ name: 'detail', day, bandId });
            }
            return;
        }
        setDialog(status === 'DISPONIBLE'
            ? { name: 'type', day, bandId }
            : { name: 'detail', day, bandId });
    };
    const saveRestriction = async (values) => {
        setSaving(true);
        setSaveError('');
        try {
            if (dialog?.name === 'edit') {
                if (!restriction) {
                    setSaveError('No se pudo identificar la restricción.');
                    return;
                }
                await actualizarRestriccion(restriction.id, {
                    tipo: tipoApi(values.tipo),
                    motivo: values.motivo,
                    documentoSoporte: values.documentoSoporte,
                });
            }
            else {
                await registrarRestriccion({
                    docenteId: teacherId,
                    anioEscolarId: yearId,
                    dia: diaApi(values.day),
                    franjaHorariaId: values.bandId,
                    tipo: tipoApi(values.tipo),
                    motivo: values.motivo,
                    documentoSoporte: values.documentoSoporte,
                });
            }
            closeDialog();
            onSaved();
        }
        catch (cause) {
            setSaveError(mensajeError(cause, 'No se pudo guardar la restricción.'));
        }
        finally {
            setSaving(false);
        }
    };
    return (_jsxs(Card, { className: styles.card, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: "Disponibilidad semanal" }), turnos.length > 1 ? (_jsxs("div", { className: styles.shift, children: [_jsx("label", { className: styles.shiftLabel, htmlFor: "restriction-shift", children: "Turno" }), _jsx(Select, { id: "restriction-shift", selectSize: "sm", className: styles.shiftSelect, value: turnoId, options: turnos.map((turno) => ({ value: turno.id, label: turno.nombre })), onChange: (event) => onTurnoChange(event.target.value) })] })) : (_jsxs(Text, { variant: "body", children: ["Turno: ", shiftLabel] })), _jsx(Text, { variant: "body", children: "Selecciona una franja para consultar o agregar una restricci\u00F3n." })] }), _jsx(Button, { variant: "primary", size: "sm", className: styles.action, disabled: readOnly || classBands.length === 0, onClick: () => setDialog({ name: 'type', day: '', bandId: '' }), children: "+ Agregar restricci\u00F3n" }), _jsx("ul", { className: styles.legend, children: legend.map((item) => (_jsxs("li", { children: [_jsx(StatusMark, { status: item.status }), _jsxs("span", { children: [labelAvailability(item.status), " \u00B7 ", item.detail] })] }, item.status))) }), _jsx("p", { className: styles.note, children: "Las franjas sin restricciones se consideran disponibles para la planificaci\u00F3n." }), bandsLoading ? _jsx("p", { className: styles.note, children: "Cargando franjas horarias..." }) : null, bandsError ? (_jsx("p", { className: styles.error, role: "alert", children: bandsError })) : null, loading ? _jsx("p", { className: styles.note, children: "Cargando restricciones..." }) : null, error ? (_jsx("p", { className: styles.error, role: "alert", children: error })) : null, !bandsLoading && !bandsError && bands.length === 0 ? (_jsx("p", { className: styles.note, children: "Este turno no tiene franjas horarias." })) : null, bands.length > 0 ? (_jsxs("div", { className: styles.grid, children: [_jsx("span", { className: styles.head, children: "Franja" }), availabilityDays.map((day) => (_jsx("span", { className: styles.head, children: day }, day))), bands.map((band) => band.kind === 'break' ? (_jsx("p", { className: styles.break, children: band.label }, band.id)) : (_jsxs("div", { className: styles.row, children: [_jsx("span", { className: styles.time, children: band.label }), availabilityDays.map((day) => {
                                const status = statusAt(band.id, day);
                                return (_jsxs("button", { type: "button", className: [styles.cell, styles[status]].join(' '), "aria-label": `${day} ${band.label}: ${labelAvailability(status)}`, onClick: () => openSlot(day, band.id), children: [_jsx("span", { className: styles.day, children: day }), _jsx(StatusMark, { status: status }), labelAvailability(status)] }, day));
                            })] }, band.id)))] })) : null, dialog?.name === 'type' ? (_jsx(RestrictionTypeModal, { open: true, onSelect: (tipo) => setDialog({ name: 'create', day: dialog.day, bandId: dialog.bandId, tipo }), onClose: closeDialog })) : null, dialog?.name === 'create' || dialog?.name === 'edit' ? (_jsx(RestrictionFormModal, { open: true, mode: dialog.name, year: year, teacherName: teacherName, day: dialog.day, bandId: dialog.bandId, bandLabel: bandLabel(dialog.bandId), tipo: dialog.name === 'create' ? dialog.tipo : undefined, restriction: dialog.name === 'edit' ? restriction : undefined, bandOptions: classBands.map((band) => ({ value: band.id, label: band.label })), isTaken: (day, bandId) => restrictions.some((item) => item.day === day && item.bandId === bandId), submitting: saving, error: saveError, onClose: closeDialog, onSubmit: (values) => {
                    void saveRestriction(values);
                } }, `${dialog.name}-${dialog.day}-${dialog.bandId}-${dialog.name === 'create' ? dialog.tipo : restriction?.id ?? 'edit'}`)) : null, dialog?.name === 'detail' ? (_jsx(RestrictionDetailModal, { open: true, year: year, teacherName: teacherName, bandLabel: bandLabel(dialog.bandId), restriction: restriction ?? null, loading: consulting && !restriction, error: restriction ? '' : consultError, readOnly: readOnly, yearClosed: yearClosed, onEdit: () => {
                    setSaveError('');
                    setDialog({ name: 'edit', day: dialog.day, bandId: dialog.bandId });
                }, onClose: closeDialog })) : null] }));
}
function StatusMark({ status }) {
    if (status === 'NO_DISPONIBLE') {
        return (_jsxs("svg", { className: styles.icon, viewBox: "0 0 16 16", "aria-hidden": "true", children: [_jsx("circle", { cx: "8", cy: "8", r: "5.25", fill: "none", stroke: "currentColor", strokeWidth: "1.4" }), _jsx("path", { d: "M4.8 11.2 11.2 4.8", fill: "none", stroke: "currentColor", strokeWidth: "1.4" })] }));
    }
    if (status === 'PREFERENCIA') {
        return (_jsx("svg", { className: styles.icon, viewBox: "0 0 16 16", "aria-hidden": "true", children: _jsx("path", { d: "M8 2.2 13.2 8 8 13.8 2.8 8Z", fill: "none", stroke: "currentColor", strokeWidth: "1.4" }) }));
    }
    return (_jsx("svg", { className: styles.icon, viewBox: "0 0 16 16", "aria-hidden": "true", children: _jsx("path", { d: "M3.2 8.3 6.4 11.4 12.8 4.6", fill: "none", stroke: "currentColor", strokeWidth: "1.6", strokeLinecap: "round", strokeLinejoin: "round" }) }));
}

import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { actualizarFranjaHoraria, ApiError, horaCorta, horaDesdeMinutos, listarFranjasPorTurno, minutosDelDia, registrarFranjaHoraria, } from '../../../api';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { CheckboxField } from '../../molecules/CheckboxField';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
import styles from './FranjaFormModal.module.css';
const rangeMessage = 'La hora de inicio debe ser anterior a la hora de finalización';
const overlapMessage = 'La franja horaria se solapa con otra franja del mismo turno';
const duplicateNumberMessage = 'Ya existe una franja académica con ese número en el turno';
const duplicateOrderMessage = 'Ya existe una franja con ese orden en el turno';
const breakNumberMessage = 'Una franja de descanso debe tener número 0';
const classNumberMessage = 'Una franja académica debe tener un número mayor que cero';
const breakHoursMessage = 'Una franja de descanso debe tener 0 horas académicas equivalentes';
const classHoursMessage = 'Una franja académica debe tener una equivalencia académica mayor que cero';
const breakDurationMessage = 'La duración del descanso debe coincidir con la configurada en el turno';
const classDurationMessage = 'La duración de la franja académica debe coincidir con la configurada en el turno';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function entero(value) {
    return /^\d+$/.test(value) ? Number(value) : Number.NaN;
}
function decimal(value) {
    const normalizado = value.trim().replace(',', '.');
    return /^\d+(\.\d{1,2})?$/.test(normalizado) ? Number(normalizado) : Number.NaN;
}
function filtrarDecimal(value) {
    const limpio = value.replace(',', '.').replace(/[^\d.]/g, '');
    const [enteroParte, ...resto] = limpio.split('.');
    if (resto.length === 0) {
        return enteroParte;
    }
    return `${enteroParte}.${resto.join('').slice(0, 2)}`;
}
function sugerir(turno, franjas) {
    const orden = franjas.reduce((max, item) => Math.max(max, item.orden), 0) + 1;
    const numero = franjas.reduce((max, item) => Math.max(max, item.esDescanso ? 0 : item.numero), 0) + 1;
    const inicioMinutos = franjas.reduce((latest, item) => {
        const fin = minutosDelDia(item.horaFin);
        if (fin == null) {
            return latest;
        }
        return latest == null || fin > latest ? fin : latest;
    }, null);
    const horaInicio = inicioMinutos == null
        ? turno
            ? horaCorta(turno.horaInicio)
            : ''
        : horaDesdeMinutos(inicioMinutos);
    const base = minutosDelDia(horaInicio);
    const horaFin = base != null && turno ? horaDesdeMinutos(base + turno.duracionClaseMinutos) : '';
    return {
        turnoId: turno?.id ?? '',
        numero: String(numero),
        horaInicio,
        horaFin,
        esDescanso: false,
        horas: '1',
        orden: String(orden),
    };
}
function valuesFromFranja(franja) {
    return {
        turnoId: franja.turnoId,
        numero: String(franja.numero),
        horaInicio: horaCorta(franja.horaInicio),
        horaFin: horaCorta(franja.horaFin),
        esDescanso: franja.esDescanso,
        horas: String(franja.horasAcademicasEquivalentes),
        orden: String(franja.orden),
    };
}
function seSolapa(inicio, fin, hermanas, franjaId) {
    return hermanas.some((item) => {
        if (item.id === franjaId) {
            return false;
        }
        const otraInicio = minutosDelDia(item.horaInicio);
        const otraFin = minutosDelDia(item.horaFin);
        if (otraInicio == null || otraFin == null) {
            return false;
        }
        return inicio < otraFin && fin > otraInicio;
    });
}
function FranjaFormFields({ mode, turnos, franjas, turnoInicialId, franja, onClose, onCreated, onUpdated, onSubmittingChange, }) {
    const creating = mode === 'create';
    const turnoInicial = turnos.find((turno) => turno.id === turnoInicialId);
    const [values, setValues] = useState(() => creating ? sugerir(turnoInicial, franjas) : valuesFromFranja(franja));
    const [hermanas, setHermanas] = useState(franjas);
    const [attempted, setAttempted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const formId = `franja-${mode}-form`;
    const turno = turnos.find((item) => item.id === values.turnoId);
    const numero = entero(values.numero);
    const orden = entero(values.orden);
    const horas = values.esDescanso ? 0 : decimal(values.horas);
    const inicio = minutosDelDia(values.horaInicio);
    const fin = minutosDelDia(values.horaFin);
    const rangoInvalido = inicio != null && fin != null && inicio >= fin;
    const duracion = inicio != null && fin != null && fin > inicio ? fin - inicio : null;
    const duracionEsperada = values.esDescanso
        ? turno?.duracionDescansoMinutos
        : turno?.duracionClaseMinutos;
    const duracionInvalida = duracion != null && duracionEsperada != null && duracion !== duracionEsperada;
    const numeroInvalido = values.esDescanso ? numero !== 0 : !(numero > 0);
    const horasInvalidas = values.esDescanso ? horas !== 0 : !(horas > 0);
    const ordenInvalido = !(orden >= 1);
    const numeroDuplicado = numero > 0 &&
        hermanas.some((item) => item.id !== franja?.id && item.numero === numero);
    const ordenDuplicado = hermanas.some((item) => item.id !== franja?.id && item.orden === orden);
    const solapa = inicio != null &&
        fin != null &&
        fin > inicio &&
        seSolapa(inicio, fin, hermanas, franja?.id);
    const setBusy = (next) => {
        setSubmitting(next);
        onSubmittingChange(next);
    };
    useEffect(() => {
        if (!values.turnoId) {
            setHermanas([]);
            return;
        }
        const controller = new AbortController();
        listarFranjasPorTurno(values.turnoId, controller.signal)
            .then((items) => {
            if (!controller.signal.aborted) {
                setHermanas(items ?? []);
            }
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setHermanas([]);
        });
        return () => controller.abort();
    }, [values.turnoId]);
    const cambiarDescanso = (esDescanso) => {
        setValues((current) => {
            const turnoActual = turnos.find((item) => item.id === current.turnoId);
            const base = minutosDelDia(current.horaInicio);
            const minutos = esDescanso
                ? turnoActual?.duracionDescansoMinutos
                : turnoActual?.duracionClaseMinutos;
            const horaFin = base != null && minutos != null ? horaDesdeMinutos(base + minutos) : current.horaFin;
            return {
                ...current,
                esDescanso,
                numero: esDescanso ? '0' : current.numero === '0' ? '' : current.numero,
                horas: esDescanso ? '0' : current.horas === '0' || current.horas === '0.00' ? '1' : current.horas,
                horaFin: horaFin || current.horaFin,
            };
        });
    };
    const handleSubmit = async (event) => {
        event.preventDefault();
        setAttempted(true);
        setSubmitError('');
        if (submitting ||
            !values.turnoId ||
            !values.horaInicio ||
            !values.horaFin ||
            numeroInvalido ||
            horasInvalidas ||
            ordenInvalido ||
            rangoInvalido ||
            duracionInvalida ||
            numeroDuplicado ||
            ordenDuplicado ||
            solapa) {
            return;
        }
        if (!creating && !franja?.id) {
            setSubmitError('No se encontró la franja horaria a actualizar.');
            return;
        }
        const payload = {
            turnoId: values.turnoId,
            numero,
            horaInicio: values.horaInicio,
            horaFin: values.horaFin,
            esDescanso: values.esDescanso,
            horasAcademicasEquivalentes: horas,
            orden,
        };
        setBusy(true);
        try {
            if (creating) {
                onCreated?.(await registrarFranjaHoraria(payload));
            }
            else if (franja) {
                onUpdated?.(await actualizarFranjaHoraria(franja.id, payload));
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : creating
                    ? 'No se pudo registrar la franja horaria.'
                    : 'No se pudo actualizar la franja horaria.');
        }
        finally {
            setBusy(false);
        }
    };
    const duracionTexto = turno
        ? `El turno ${turno.nombre} espera clases de ${turno.duracionClaseMinutos} min y un descanso de ${turno.duracionDescansoMinutos} min.`
        : 'Elige un turno para conocer la duración esperada.';
    return (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsx(SelectField, { id: `${formId}-shift`, label: "Turno", name: "turno", required: true, disabled: submitting || turnos.length === 0, value: values.turnoId, options: [
                    { value: '', label: 'Seleccionar turno' },
                    ...turnos.map((item) => ({ value: item.id, label: item.nombre })),
                ], error: attempted && !values.turnoId ? 'El turno es obligatorio' : undefined, onChange: (turnoId) => setValues((current) => ({ ...current, turnoId })) }), _jsx(CheckboxField, { id: `${formId}-break`, name: "esDescanso", label: "Esta franja es un descanso", checked: values.esDescanso, onChange: cambiarDescanso }), _jsxs("div", { className: styles.grid, children: [_jsx(TextField, { id: `${formId}-number`, label: "N\u00FAmero", inputMode: "numeric", required: true, disabled: submitting || values.esDescanso, value: values.numero, error: numeroDuplicado
                            ? duplicateNumberMessage
                            : numeroInvalido && (attempted || values.esDescanso || values.numero !== '')
                                ? values.esDescanso
                                    ? breakNumberMessage
                                    : classNumberMessage
                                : undefined, onChange: (event) => setValues((current) => ({ ...current, numero: event.target.value.replace(/\D/g, '') })) }), _jsx(TextField, { id: `${formId}-order`, label: "Orden", inputMode: "numeric", required: true, disabled: submitting, value: values.orden, error: ordenDuplicado
                            ? duplicateOrderMessage
                            : attempted && ordenInvalido
                                ? 'El orden debe ser mayor que cero'
                                : undefined, onChange: (event) => setValues((current) => ({ ...current, orden: event.target.value.replace(/\D/g, '') })) }), _jsx(TextField, { id: `${formId}-start`, label: "Hora de inicio", type: "time", required: true, disabled: submitting, value: values.horaInicio, error: rangoInvalido
                            ? rangeMessage
                            : attempted && !values.horaInicio
                                ? 'La hora de inicio es obligatoria'
                                : undefined, onChange: (event) => setValues((current) => ({ ...current, horaInicio: event.target.value })) }), _jsx(TextField, { id: `${formId}-end`, label: "Hora de finalizaci\u00F3n", type: "time", required: true, disabled: submitting, value: values.horaFin, error: duracionInvalida
                            ? values.esDescanso
                                ? breakDurationMessage
                                : classDurationMessage
                            : solapa
                                ? overlapMessage
                                : attempted && !values.horaFin
                                    ? 'La hora de finalización es obligatoria'
                                    : undefined, onChange: (event) => setValues((current) => ({ ...current, horaFin: event.target.value })) }), _jsx(TextField, { id: `${formId}-hours`, label: "Horas acad\u00E9micas equivalentes", inputMode: "decimal", required: true, disabled: submitting || values.esDescanso, value: values.horas, error: horasInvalidas && (attempted || values.esDescanso)
                            ? values.esDescanso
                                ? breakHoursMessage
                                : classHoursMessage
                            : undefined, onChange: (event) => setValues((current) => ({ ...current, horas: filtrarDecimal(event.target.value) })) })] }), submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsxs("p", { className: styles.hint, children: [duracionTexto, " El n\u00FAmero y el orden no se repiten dentro del turno, y las franjas no pueden cruzarse."] })] }));
}
export function FranjaFormModal({ open, mode, turnos, franjas, turnoInicialId, franja, loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const creating = mode === 'create';
    const showForm = !loading && !loadError && (creating || Boolean(franja));
    const formId = `franja-${mode}-form`;
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    return (_jsx(Modal, { open: open, title: creating ? 'Nueva franja horaria' : 'Editar franja horaria', description: creating
            ? 'Agrega un bloque académico o un descanso al turno.'
            : 'Actualiza el horario, el número o el orden de la franja.', onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : creating
                            ? 'Guardar franja'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading || Boolean(loadError), onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando franja horaria..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : showForm ? (_jsx(FranjaFormFields, { mode: mode, turnos: turnos, franjas: franjas, turnoInicialId: turnoInicialId, franja: franja, onClose: onClose, onCreated: onCreated, onUpdated: onUpdated, onSubmittingChange: setSubmitting }, creating ? `create-${turnoInicialId}-${franjas.length}` : franja?.id)) : (_jsx("p", { className: styles.status, children: "Cargando franja horaria..." })) }));
}

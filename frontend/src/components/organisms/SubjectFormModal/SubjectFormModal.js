import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { actualizarAsignatura, ApiError, listarAsignaturasPredefinidas, registrarAsignatura, } from '../../../api';
import { areaSelectOptions, SIN_REQUISITO_AULA, subjectClassroomOptions, subjectColorOptions, subjectFormHint, subjectClassroomFormValue, subjectStatusOptions, subjectYesNoOptions, } from '../../../content/subjects';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
import styles from './SubjectFormModal.module.css';
const requiredChoiceMessage = 'Seleccione una opción.';
function yesNoValue(value) {
    if (value === undefined) {
        return '';
    }
    return value ? 'si' : 'no';
}
function statusValue(activa) {
    if (activa === undefined) {
        return '';
    }
    return activa ? 'activa' : 'inactiva';
}
function createValues(areaId) {
    return {
        areaId,
        nombre: '',
        codigo: '',
        abreviatura: '',
        color: '',
        idoneidad: '',
        media: '',
        exclusivo: '',
        aula: '',
        maxConsecutivas: '2',
        activa: '',
    };
}
function valuesFromSubject(subject) {
    return {
        areaId: subject.areaId,
        nombre: subject.nombre,
        codigo: subject.codigo,
        abreviatura: subject.abreviatura ?? '',
        color: subject.colorUi ?? '',
        idoneidad: yesNoValue(subject.exigeIdoneidadEstricta),
        media: yesNoValue(subject.esMediaTecnica),
        exclusivo: yesNoValue(subject.requiereDocenteExclusivo),
        aula: subject.tipoAulaRequerida ?? SIN_REQUISITO_AULA,
        maxConsecutivas: String(subject.maxClasesConsecutivas ?? 2),
        activa: statusValue(subject.activa),
    };
}
function optionalText(value) {
    const trimmed = value.trim();
    return trimmed ? trimmed : null;
}
function classroomPayload(value) {
    if (!value || value === SIN_REQUISITO_AULA) {
        return null;
    }
    return value;
}
function SubjectFormFields({ mode, areas, selectedAreaId, subject, onClose, onCreated, onUpdated, onSubmittingChange, }) {
    const initialAreaId = areas.some((area) => area.id === selectedAreaId)
        ? selectedAreaId
        : '';
    const [values, setValues] = useState(() => mode === 'edit' && subject ? valuesFromSubject(subject) : createValues(initialAreaId));
    const [attempted, setAttempted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const [catalogo, setCatalogo] = useState([]);
    const [catalogoCodigo, setCatalogoCodigo] = useState('');
    const formId = `subject-${mode}-form`;
    const creating = mode === 'create';
    const maxValue = Number(values.maxConsecutivas);
    const maxInvalid = !values.maxConsecutivas || !Number.isInteger(maxValue) || maxValue < 1;
    const missingChoice = !values.areaId ||
        !values.idoneidad ||
        !values.media ||
        !values.exclusivo ||
        !values.aula ||
        !values.activa;
    const setBusy = (next) => {
        setSubmitting(next);
        onSubmittingChange(next);
    };
    const areaCodigo = areas.find((item) => item.id === values.areaId)?.codigo ?? '';
    useEffect(() => {
        if (!areaCodigo) {
            setCatalogo([]);
            return;
        }
        const controller = new AbortController();
        listarAsignaturasPredefinidas(areaCodigo, controller.signal)
            .then((items) => {
            if (!controller.signal.aborted) {
                setCatalogo(items ?? []);
            }
        })
            .catch(() => {
            if (!controller.signal.aborted) {
                setCatalogo([]);
            }
        });
        return () => controller.abort();
    }, [areaCodigo]);
    const handleSubmit = async (event) => {
        event.preventDefault();
        setAttempted(true);
        setSubmitError('');
        if (submitting) {
            return;
        }
        if (!values.nombre.trim() || !values.codigo.trim() || missingChoice || maxInvalid) {
            return;
        }
        if (!creating && !subject?.id) {
            setSubmitError('No se encontró la asignatura a actualizar.');
            return;
        }
        const payload = {
            areaId: values.areaId,
            nombre: values.nombre.trim(),
            codigo: values.codigo.trim(),
            abreviatura: optionalText(values.abreviatura),
            colorUi: optionalText(values.color),
            exigeIdoneidadEstricta: values.idoneidad === 'si',
            esMediaTecnica: values.media === 'si',
            requiereDocenteExclusivo: values.exclusivo === 'si',
            tipoAulaRequerida: classroomPayload(values.aula),
            maxClasesConsecutivas: maxValue,
            activa: values.activa === 'activa',
        };
        setBusy(true);
        try {
            if (creating) {
                const created = await registrarAsignatura(payload);
                onCreated?.(created);
            }
            else if (subject) {
                const updated = await actualizarAsignatura(subject.id, payload);
                onUpdated?.(updated);
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : creating
                    ? 'No se pudo registrar la asignatura. Inténtalo de nuevo.'
                    : 'No se pudo actualizar la asignatura. Inténtalo de nuevo.');
        }
        finally {
            setBusy(false);
        }
    };
    const patch = (partial) => {
        setValues((current) => ({ ...current, ...partial }));
    };
    return (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsxs("div", { className: styles.grid, children: [_jsx(SelectField, { id: `${formId}-area`, label: "\u00C1rea", name: "areaId", required: true, disabled: submitting || !creating, value: values.areaId, options: areaSelectOptions(areas), error: attempted && !values.areaId ? requiredChoiceMessage : undefined, onChange: (areaId) => {
                            setCatalogoCodigo('');
                            patch({ areaId });
                        } }), _jsx(SelectField, { id: `${formId}-catalog`, label: "Asignatura del cat\u00E1logo", name: "catalogo", disabled: submitting || !areaCodigo || catalogo.length === 0, value: catalogoCodigo, options: [
                            {
                                value: '',
                                label: !areaCodigo
                                    ? 'Elige un área'
                                    : catalogo.length
                                        ? 'Escribir manualmente'
                                        : 'Sin asignaturas en el catálogo',
                            },
                            ...catalogo.map((item) => ({
                                value: item.codigo,
                                label: item.nombre,
                            })),
                        ], hint: "El nombre, el c\u00F3digo y la abreviatura salen del cat\u00E1logo del \u00E1rea.", onChange: (codigo) => {
                            const opcion = catalogo.find((item) => item.codigo === codigo);
                            setCatalogoCodigo(codigo);
                            if (opcion) {
                                patch({
                                    nombre: opcion.nombre,
                                    codigo: opcion.codigo,
                                    abreviatura: opcion.abreviatura,
                                });
                            }
                        } }), _jsx(TextField, { id: `${formId}-name`, label: "Nombre", name: "nombre", required: true, disabled: submitting, placeholder: "Ingresa el nombre", value: values.nombre, onChange: (event) => patch({ nombre: event.target.value }) }), _jsx(TextField, { id: `${formId}-code`, label: "C\u00F3digo", name: "codigo", required: true, disabled: submitting, placeholder: "Ingresa el c\u00F3digo", value: values.codigo, onChange: (event) => patch({ codigo: event.target.value.toUpperCase() }) }), _jsx(TextField, { id: `${formId}-abbreviation`, label: "Abreviatura", name: "abreviatura", disabled: submitting, placeholder: "Ingresa la abreviatura", value: values.abreviatura, onChange: (event) => patch({ abreviatura: event.target.value.toUpperCase() }) }), _jsx(SelectField, { id: `${formId}-color`, label: "Color de identificaci\u00F3n (opcional)", name: "color", disabled: submitting, value: values.color, options: subjectColorOptions, onChange: (color) => patch({ color }) }), _jsx(SelectField, { id: `${formId}-suitability`, label: "Exige idoneidad estricta", name: "idoneidad", disabled: submitting, value: values.idoneidad, options: subjectYesNoOptions, error: attempted && !values.idoneidad ? requiredChoiceMessage : undefined, onChange: (idoneidad) => patch({ idoneidad: idoneidad }) }), _jsx(SelectField, { id: `${formId}-media`, label: "Asignatura de Media T\u00E9cnica", name: "media", disabled: submitting, value: values.media, options: subjectYesNoOptions, error: attempted && !values.media ? requiredChoiceMessage : undefined, onChange: (media) => patch({ media: media }) }), _jsx(SelectField, { id: `${formId}-exclusive`, label: "Requiere docente exclusivo", name: "exclusivo", disabled: submitting, value: values.exclusivo, options: subjectYesNoOptions, error: attempted && !values.exclusivo ? requiredChoiceMessage : undefined, onChange: (exclusivo) => patch({ exclusivo: exclusivo }) }), _jsx(SelectField, { id: `${formId}-classroom`, label: "Tipo de aula requerida", name: "aula", disabled: submitting, value: values.aula, options: subjectClassroomOptions, error: attempted && !values.aula ? requiredChoiceMessage : undefined, onChange: (aula) => patch({ aula: subjectClassroomFormValue(aula) }) }), _jsx(TextField, { id: `${formId}-max`, label: "M\u00E1ximo de clases consecutivas", name: "maxConsecutivas", required: true, disabled: submitting, inputMode: "numeric", value: values.maxConsecutivas, error: attempted && maxInvalid
                            ? 'Ingresa un máximo de al menos 1.'
                            : undefined, onChange: (event) => patch({ maxConsecutivas: event.target.value.replace(/\D/g, '') }) })] }), _jsx(SelectField, { id: `${formId}-status`, label: "Estado", name: "activa", disabled: submitting, value: values.activa, options: subjectStatusOptions, error: attempted && !values.activa ? requiredChoiceMessage : undefined, onChange: (activa) => patch({ activa: activa }) }), submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsx("p", { className: styles.hint, children: subjectFormHint })] }));
}
export function SubjectFormModal({ open, mode, areas, selectedAreaId = '', subject, loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const creating = mode === 'create';
    const showForm = !loading && !loadError && (creating || Boolean(subject));
    const formId = `subject-${mode}-form`;
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    return (_jsx(Modal, { open: open, size: "lg", title: mode === 'create' ? 'Nueva asignatura' : 'Editar asignatura', description: mode === 'create'
            ? 'Agrega una asignatura al área seleccionada del catálogo académico.'
            : subject?.nombre
                ? `Actualiza las propiedades académicas de ${subject.nombre}.`
                : 'Actualiza las propiedades académicas de la asignatura seleccionada.', onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : creating
                            ? 'Guardar asignatura'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading, onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando asignatura..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : showForm ? (_jsx(SubjectFormFields, { mode: mode, areas: areas, selectedAreaId: selectedAreaId, subject: subject, onClose: onClose, onCreated: onCreated, onUpdated: onUpdated, onSubmittingChange: setSubmitting }, mode === 'edit' ? subject?.id : 'create')) : (_jsx("p", { className: styles.status, children: "Cargando asignatura..." })) }));
}

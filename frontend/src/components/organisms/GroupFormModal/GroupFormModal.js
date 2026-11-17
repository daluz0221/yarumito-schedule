import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useRef, useState } from 'react';
import { actualizarGrupo, ApiError, etiquetaGrado, etiquetaTipoAula, gradoPorId, listarAulasPorSede, listarDocentes, opcionesCatalogoSedes, opcionesGradoRequerido, planDesdeGrado, registrarGrupo, sedeVisible, sugerirCodigoGrupo, useCatalogoGrados, useCatalogoSedes, } from '../../../api';
import { groupStatusOptions, isGroupCodeTaken, labelYearStatus, } from '../../../content/academicYear';
import { formatTeacherName } from '../../../content/teachers';
import { Modal } from '../../atoms/Modal';
import { Text } from '../../atoms/Text';
import { TextLink } from '../../atoms/TextLink';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
import styles from './GroupFormModal.module.css';
const createValues = {
    code: '',
    grade: '',
    students: '',
    directorId: '',
    status: '',
    classroomId: '',
    sedeId: '',
};
const statusOptions = [
    { value: '', label: 'Seleccione un estado' },
    ...groupStatusOptions,
];
const duplicateCreateMessage = 'Ya existe un grupo con ese código en el año escolar indicado';
const duplicateEditMessage = 'Ya existe otro grupo con ese código en el año escolar indicado';
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
function esUuid(value) {
    return UUID.test(value);
}
function opcionesDirector(docentes, error) {
    const sinAsignar = { value: '', label: 'Sin asignar' };
    if (error || !docentes) {
        return [sinAsignar];
    }
    return [
        sinAsignar,
        ...docentes.map((teacher) => ({
            value: teacher.id,
            label: formatTeacherName(teacher),
        })),
    ];
}
function mensajeCrearGrupo(cause) {
    if (!(cause instanceof ApiError)) {
        return 'No se pudo crear el grupo. Inténtalo de nuevo.';
    }
    if (cause.message && cause.message !== 'No se pudo completar la solicitud') {
        return cause.message;
    }
    if (cause.status === 409) {
        return duplicateCreateMessage;
    }
    if (cause.status === 404) {
        return 'No se encontró el grado, la sede, el año lectivo o el director indicado.';
    }
    if (cause.status === 400) {
        return 'Revisa los datos del grupo. Hay un campo obligatorio o con formato inválido.';
    }
    if (cause.status === 0) {
        return cause.message;
    }
    return 'No se pudo crear el grupo. Inténtalo de nuevo.';
}
export function GroupFormModal({ open, mode, year, groups, group, grupo, loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, }) {
    const [values, setValues] = useState(createValues);
    const [attempted, setAttempted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const [directores, setDirectores] = useState(null);
    const [directoresError, setDirectoresError] = useState('');
    const [aulas, setAulas] = useState(null);
    const [aulasError, setAulasError] = useState('');
    const [codigoSugeridoEstado, setCodigoSugeridoEstado] = useState('idle');
    const sugeridoAplicado = useRef('');
    const { grados, error: gradosError } = useCatalogoGrados(open);
    const { sedes, error: sedesError } = useCatalogoSedes(open);
    const gradeOptions = opcionesGradoRequerido(grados, gradosError);
    const sedeOptions = opcionesCatalogoSedes(sedes, sedesError);
    const gradoLista = Boolean(grados?.length) && !gradosError;
    const sedeLista = Boolean(sedes?.length) && !sedesError;
    const gradoSeleccionado = gradoPorId(grados, values.grade);
    const formId = `group-${mode}-form`;
    const plan = gradoSeleccionado ? planDesdeGrado(gradoSeleccionado) : null;
    const etiquetaSeleccionada = gradoSeleccionado
        ? etiquetaGrado(gradoSeleccionado.nombre)
        : '';
    const directorOptions = opcionesDirector(directores, directoresError);
    const creating = mode === 'create';
    const codeTaken = isGroupCodeTaken(groups, year.id, values.code, grupo?.id ?? group?.id);
    const duplicateCodeMessage = creating ? duplicateCreateMessage : duplicateEditMessage;
    const aulasDeLaSede = (aulas ?? []).filter((aula) => aula.sedeId === values.sedeId);
    const aulaActual = aulasDeLaSede.find((aula) => aula.id === values.classroomId);
    const classroomOptions = !esUuid(values.sedeId)
        ? [{ value: '', label: 'Sin asignar' }]
        : aulas === null && !aulasError
            ? [
                { value: '', label: 'Cargando aulas…' },
                ...(esUuid(values.classroomId)
                    ? [{ value: values.classroomId, label: 'Aula actual' }]
                    : []),
            ]
            : [
                { value: '', label: 'Sin asignar' },
                ...aulasDeLaSede
                    .filter((aula) => aula.activa || aula.id === values.classroomId)
                    .map((aula) => ({
                    value: aula.id,
                    label: aula.activa
                        ? `${aula.nombre} · ${etiquetaTipoAula(aula.tipo)}`
                        : `${aula.nombre} · inactiva`,
                })),
                ...(esUuid(values.classroomId) && !aulaActual
                    ? [{ value: values.classroomId, label: 'Aula actual' }]
                    : []),
            ];
    const codeHint = !creating
        ? undefined
        : codigoSugeridoEstado === 'loading'
            ? 'Buscando el siguiente código disponible…'
            : codigoSugeridoEstado === 'error'
                ? 'No se pudo sugerir el código. Escríbelo manualmente.'
                : codigoSugeridoEstado === 'ready'
                    ? 'Sugerido para este grado y año. Puedes cambiarlo.'
                    : 'Elige un grado para sugerir el siguiente código.';
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    useEffect(() => {
        if (!open || mode !== 'create') {
            return;
        }
        setValues(createValues);
        setAttempted(false);
        setSubmitError('');
        sugeridoAplicado.current = '';
        setCodigoSugeridoEstado('idle');
    }, [mode, open]);
    useEffect(() => {
        if (!open || mode !== 'create' || !esUuid(values.grade) || !esUuid(year.id)) {
            return;
        }
        const controller = new AbortController();
        const gradoId = values.grade;
        const anioId = year.id;
        setCodigoSugeridoEstado('loading');
        sugerirCodigoGrupo(gradoId, anioId, controller.signal)
            .then((codigo) => {
            setCodigoSugeridoEstado('ready');
            setValues((current) => {
                if (current.grade !== gradoId) {
                    return current;
                }
                const actual = current.code.trim();
                if (actual && actual !== sugeridoAplicado.current) {
                    return current;
                }
                sugeridoAplicado.current = codigo;
                return { ...current, code: codigo };
            });
        })
            .catch((cause) => {
            if (cause instanceof DOMException && cause.name === 'AbortError') {
                return;
            }
            setCodigoSugeridoEstado('error');
        });
        return () => controller.abort();
    }, [mode, open, values.grade, year.id]);
    useEffect(() => {
        if (!open || mode !== 'edit' || !grupo) {
            return;
        }
        setValues({
            code: grupo.codigo,
            grade: grupo.gradoId,
            students: grupo.cantidadEstudiantes == null ? '' : String(grupo.cantidadEstudiantes),
            directorId: grupo.directorGrupoId ?? '',
            status: grupo.activo ? 'ACTIVO' : 'INACTIVO',
            classroomId: grupo.aulaFijaId && esUuid(grupo.aulaFijaId) ? grupo.aulaFijaId : '',
            sedeId: grupo.sedeId,
        });
        setAttempted(false);
        setSubmitError('');
    }, [grupo, mode, open]);
    useEffect(() => {
        if (!open) {
            return;
        }
        const controller = new AbortController();
        setDirectores(null);
        setDirectoresError('');
        const cargar = async () => {
            const size = 100;
            const first = await listarDocentes({ estado: 'ACTIVO', page: 0, size }, controller.signal);
            const docentes = [...first.content];
            const pages = Math.ceil(first.total / size);
            for (let page = 1; page < pages; page += 1) {
                const next = await listarDocentes({ estado: 'ACTIVO', page, size }, controller.signal);
                docentes.push(...next.content);
            }
            return docentes;
        };
        cargar()
            .then((docentes) => {
            setDirectores(docentes);
            setDirectoresError('');
        })
            .catch((cause) => {
            if (cause instanceof DOMException && cause.name === 'AbortError') {
                return;
            }
            setDirectores([]);
            setDirectoresError('No se pudieron cargar los docentes. Puedes crear el grupo sin director.');
        });
        return () => controller.abort();
    }, [open]);
    useEffect(() => {
        if (!open || !sedes?.length) {
            return;
        }
        const sedeId = sedeVisible(sedes)?.id ?? '';
        setValues((current) => sedes.some((sede) => sede.id === current.sedeId) ? current : { ...current, sedeId });
    }, [open, sedes]);
    useEffect(() => {
        if (!open || !esUuid(values.sedeId)) {
            return;
        }
        const controller = new AbortController();
        const sedeId = values.sedeId;
        setAulas(null);
        setAulasError('');
        listarAulasPorSede(sedeId, controller.signal)
            .then((items) => {
            if (controller.signal.aborted) {
                return;
            }
            setAulas(items);
            setAulasError('');
        })
            .catch((cause) => {
            if (cause instanceof DOMException && cause.name === 'AbortError') {
                return;
            }
            setAulas([]);
            setAulasError('No se pudieron cargar las aulas. Puedes guardar el grupo sin aula.');
        });
        return () => controller.abort();
    }, [open, values.sedeId]);
    const handleSubmit = async (event) => {
        event.preventDefault();
        setAttempted(true);
        setSubmitError('');
        if (submitting) {
            return;
        }
        if (!creating && !grupo?.id) {
            setSubmitError('No se encontró el grupo a actualizar.');
            return;
        }
        if (codeTaken ||
            !gradoLista ||
            !sedeLista ||
            !esUuid(creating ? year.id : (grupo?.anioEscolarId ?? year.id)) ||
            !values.code.trim() ||
            !values.grade ||
            !values.sedeId ||
            !values.status) {
            if (!esUuid(creating ? year.id : (grupo?.anioEscolarId ?? year.id))) {
                setSubmitError('El año lectivo seleccionado no está disponible en el servidor. No se puede crear el grupo.');
            }
            return;
        }
        if (values.directorId && !esUuid(values.directorId)) {
            setSubmitError('El director seleccionado no es válido. Déjalo en Sin asignar.');
            return;
        }
        const payload = {
            codigo: values.code.trim(),
            gradoId: values.grade,
            anioEscolarId: creating ? year.id : (grupo?.anioEscolarId ?? year.id),
            sedeId: values.sedeId,
            activo: values.status === 'ACTIVO',
            ...(values.students !== ''
                ? { cantidadEstudiantes: Number(values.students) }
                : {}),
            ...(values.directorId ? { directorGrupoId: values.directorId } : {}),
            ...(esUuid(values.classroomId) ? { aulaFijaId: values.classroomId } : {}),
        };
        setSubmitting(true);
        try {
            if (creating) {
                const created = await registrarGrupo(payload);
                onCreated?.(created);
            }
            else if (grupo) {
                const updated = await actualizarGrupo(grupo.id, payload);
                onUpdated?.(updated);
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(creating
                ? mensajeCrearGrupo(cause)
                : cause instanceof ApiError
                    ? cause.message
                    : 'No se pudo actualizar el grupo. Inténtalo de nuevo.');
        }
        finally {
            setSubmitting(false);
        }
    };
    return (_jsx(Modal, { open: open, size: "lg", title: mode === 'create' ? 'Agregar grupo' : `Editar grupo ${grupo?.codigo ?? group?.code ?? values.code}`, description: mode === 'create'
            ? 'Configura un grupo para el año lectivo seleccionado.'
            : 'Actualiza la configuración anual del grupo.', onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : creating
                            ? 'Guardar grupo'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading || Boolean(loadError), onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando grupo..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsxs("div", { className: styles.grid, children: [_jsx(TextField, { id: `${formId}-code`, label: "C\u00F3digo del grupo", required: true, disabled: submitting, value: values.code, hint: codeHint, error: codeTaken ? duplicateCodeMessage : undefined, onChange: (event) => setValues((current) => ({ ...current, code: event.target.value })) }), _jsx(SelectField, { id: `${formId}-grade`, label: "Grado", required: true, value: gradoLista ? values.grade : '', options: gradeOptions, disabled: !gradoLista || submitting, error: gradosError ||
                                (attempted && gradoLista && !values.grade ? 'Seleccione un grado.' : undefined), onChange: (grade) => setValues((current) => ({ ...current, grade })) }), _jsx(TextField, { id: `${formId}-year`, label: "A\u00F1o lectivo", disabled: true, className: styles.locked, value: `${year.year} — ${labelYearStatus(year.status)}`, onChange: () => undefined }), _jsx(SelectField, { id: `${formId}-campus`, label: "Sede", required: creating, value: sedeLista ? values.sedeId : '', options: sedeOptions, disabled: !sedeLista || submitting, error: sedesError ||
                                (attempted && sedeLista && !values.sedeId ? 'Seleccione una sede.' : undefined), onChange: (sedeId) => setValues((current) => ({
                                ...current,
                                sedeId,
                                classroomId: grupo && sedeId === grupo.sedeId && grupo.aulaFijaId && esUuid(grupo.aulaFijaId)
                                    ? grupo.aulaFijaId
                                    : '',
                            })) }), _jsx(TextField, { id: `${formId}-students`, label: "Cantidad de estudiantes", inputMode: "numeric", disabled: submitting, value: values.students, onChange: (event) => setValues((current) => ({
                                ...current,
                                students: event.target.value.replace(/\D/g, ''),
                            })) }), _jsx(SelectField, { id: `${formId}-director`, label: "Director de grupo (opcional)", value: values.directorId, options: directorOptions, disabled: submitting || directores === null, error: directoresError || undefined, onChange: (directorId) => setValues((current) => ({ ...current, directorId })) }), _jsx(SelectField, { id: `${formId}-status`, label: "Estado", required: true, disabled: submitting, value: values.status, options: statusOptions, error: attempted && !values.status ? 'Seleccione un estado.' : undefined, onChange: (status) => setValues((current) => ({
                                ...current,
                                status: status,
                            })) }), _jsx(SelectField, { id: `${formId}-classroom`, label: "Aula habitual (opcional)", disabled: submitting || !esUuid(values.sedeId) || (aulas === null && !aulasError), value: values.classroomId, options: classroomOptions, error: aulasError || undefined, onChange: (classroomId) => setValues((current) => ({ ...current, classroomId })) })] }), _jsxs("div", { className: styles.derived, "aria-live": "polite", children: [_jsxs("div", { children: [_jsx(Text, { variant: "caption", children: "Tipo derivado del grado" }), _jsx("p", { className: styles.derivedValue, children: plan?.typeLabel ?? '—' })] }), _jsxs("div", { children: [_jsx(Text, { variant: "caption", children: "Intensidad semanal autom\u00E1tica" }), _jsx("p", { className: styles.derivedValue, children: plan ? `${plan.weeklyHours} h` : '—' })] })] }), submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsx("p", { className: styles.hint, children: mode === 'edit'
                        ? 'Tipo e intensidad derivados del grado. Director y aula son opcionales.'
                        : gradosError
                            ? 'No se pudieron cargar los grados. El grupo no se puede guardar hasta elegir un grado del catálogo.'
                            : grados === null
                                ? 'Cargando grados del catálogo…'
                                : grados.length === 0
                                    ? 'No hay grados registrados. El grupo no se puede guardar hasta que exista un grado en el catálogo.'
                                    : plan
                                        ? `${etiquetaSeleccionada} seleccionado → ${plan.typeLabel} · ${plan.weeklyHours} h. Estado: Activo o Inactivo. Director y aula son opcionales.`
                                        : 'Seleccione un grado para ver el tipo y la intensidad que vienen del catálogo. Director y aula son opcionales.' })] })) }));
}

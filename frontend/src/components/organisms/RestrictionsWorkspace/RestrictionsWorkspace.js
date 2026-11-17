import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { anioEscolarVisible, ApiError, horaCorta, listarCatalogoAreas, listarCatalogoTurnos, listarDocentes, listarFranjasPorTurno, listarRestriccionesPorDocenteYAnio, useCatalogoAniosEscolares, } from '../../../api';
import { resolveAreaName } from '../../../content/teacherProfile';
import { formatTeacherName, labelEstado } from '../../../content/teachers';
import { Text } from '../../atoms/Text';
import { RestrictionSelector } from '../RestrictionSelector';
import { TeacherAvailabilitySummary } from '../TeacherAvailabilitySummary';
import { WeeklyAvailability } from '../WeeklyAvailability';
import styles from './RestrictionsWorkspace.module.css';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function mensajeError(cause, fallback) {
    return cause instanceof ApiError && cause.message ? cause.message : fallback;
}
function horaVisible(value) {
    return horaCorta(value).replace(/^0(?=\d:)/, '');
}
function turnoInicial(turnos) {
    return (turnos.find((turno) => turno.nombre.trim().toLowerCase() === 'mañana') ?? turnos[0] ?? null);
}
function aBand(franja) {
    const horario = `${horaVisible(franja.horaInicio)} – ${horaVisible(franja.horaFin)}`;
    return {
        id: franja.id,
        kind: franja.esDescanso ? 'break' : 'class',
        label: franja.esDescanso ? `Descanso · ${horario} · No editable` : horario,
    };
}
export function RestrictionsWorkspace() {
    const [searchParams] = useSearchParams();
    const requestedTeacherId = searchParams.get('docente') ?? '';
    const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares();
    const [yearId, setYearId] = useState('');
    const [teacherId, setTeacherId] = useState(requestedTeacherId);
    const [catalog, setCatalog] = useState(null);
    const [turnos, setTurnos] = useState([]);
    const [turnosError, setTurnosError] = useState('');
    const [turnoId, setTurnoId] = useState('');
    const [franjas, setFranjas] = useState(null);
    const [franjasError, setFranjasError] = useState('');
    const [restrictions, setRestrictions] = useState([]);
    const [restrictionsLoading, setRestrictionsLoading] = useState(false);
    const [restrictionsError, setRestrictionsError] = useState('');
    const [revision, setRevision] = useState(0);
    useEffect(() => {
        if (!anios?.length) {
            return;
        }
        setYearId((current) => anios.some((year) => year.id === current) ? current : (anioEscolarVisible(anios)?.id ?? ''));
    }, [anios]);
    useEffect(() => {
        const controller = new AbortController();
        Promise.all([
            listarDocentes({ page: 0, size: 100 }, controller.signal),
            listarCatalogoAreas(controller.signal),
        ])
            .then(([teachersPage, areas]) => {
            const teachers = [...teachersPage.content].sort((left, right) => formatTeacherName(left).localeCompare(formatTeacherName(right), 'es'));
            setCatalog({ teachers, areas, error: '' });
            setTeacherId((current) => {
                if (requestedTeacherId && teachers.some((teacher) => teacher.id === requestedTeacherId)) {
                    return requestedTeacherId;
                }
                if (current && teachers.some((teacher) => teacher.id === current)) {
                    return current;
                }
                return teachers[0]?.id ?? '';
            });
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setCatalog({
                teachers: [],
                areas: [],
                error: mensajeError(cause, 'No se pudo cargar la disponibilidad docente.'),
            });
        });
        return () => controller.abort();
    }, [requestedTeacherId]);
    useEffect(() => {
        const controller = new AbortController();
        listarCatalogoTurnos(controller.signal)
            .then((items) => {
            if (controller.signal.aborted) {
                return;
            }
            setTurnos(items);
            setTurnosError('');
            setTurnoId((current) => items.some((turno) => turno.id === current) ? current : (turnoInicial(items)?.id ?? ''));
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setTurnos([]);
            setTurnosError(mensajeError(cause, 'No se pudo cargar el catálogo de turnos.'));
        });
        return () => controller.abort();
    }, []);
    useEffect(() => {
        if (!turnoId) {
            setFranjas([]);
            return;
        }
        const controller = new AbortController();
        setFranjas(null);
        setFranjasError('');
        listarFranjasPorTurno(turnoId, controller.signal)
            .then((items) => {
            if (!controller.signal.aborted) {
                setFranjas([...(items ?? [])].sort((left, right) => left.orden - right.orden || left.numero - right.numero));
            }
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setFranjas([]);
            setFranjasError(mensajeError(cause, 'No se pudieron cargar las franjas del turno.'));
        });
        return () => controller.abort();
    }, [turnoId]);
    useEffect(() => {
        if (!teacherId || !yearId) {
            setRestrictions([]);
            setRestrictionsLoading(false);
            return;
        }
        const controller = new AbortController();
        setRestrictionsLoading(true);
        setRestrictionsError('');
        listarRestriccionesPorDocenteYAnio(teacherId, yearId, controller.signal)
            .then((items) => {
            if (!controller.signal.aborted) {
                setRestrictions(items);
            }
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setRestrictions([]);
            setRestrictionsError(mensajeError(cause, 'No se pudieron cargar las restricciones.'));
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setRestrictionsLoading(false);
            }
        });
        return () => controller.abort();
    }, [teacherId, yearId, revision]);
    const year = anios?.find((item) => item.id === yearId) ?? null;
    const teachers = catalog?.teachers ?? [];
    const teacher = teachers.find((item) => item.id === teacherId);
    const teacherName = teacher ? formatTeacherName(teacher) : '';
    const turno = turnos.find((item) => item.id === turnoId) ?? null;
    const readOnly = !year || year.status === 'CERRADO' || teacher?.estado !== 'ACTIVO';
    const bands = (franjas ?? []).map(aBand);
    return (_jsxs("section", { className: styles.workspace, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { as: "h2", variant: "pageTitle", children: "Restricciones y disponibilidad docente" }), _jsx(Text, { variant: "body", children: "Registra las franjas en las que cada docente presenta restricciones o condiciones de disponibilidad durante el a\u00F1o lectivo." })] }), aniosLoading || catalog === null ? (_jsx("p", { className: styles.message, children: "Cargando docentes..." })) : null, aniosError ? _jsx("p", { className: styles.message, children: aniosError }) : null, catalog?.error ? _jsx("p", { className: styles.message, children: catalog.error }) : null, turnosError ? _jsx("p", { className: styles.message, children: turnosError }) : null, catalog && !catalog.error && year ? (_jsxs(_Fragment, { children: [_jsx(RestrictionSelector, { status: year.status, year: year.year, years: anios ?? [], teacherName: teacherName, teachers: teachers.map((item) => ({
                            id: item.id,
                            name: formatTeacherName(item),
                            areaName: resolveAreaName(item.areaNombramientoId, catalog.areas),
                            active: item.estado === 'ACTIVO',
                        })), onYearChange: setYearId, onTeacherChange: setTeacherId }), teachers.length === 0 ? (_jsx("p", { className: styles.message, children: "No hay docentes registrados." })) : null, teacher ? (_jsxs(_Fragment, { children: [_jsx(TeacherAvailabilitySummary, { name: teacherName, areaName: resolveAreaName(teacher.areaNombramientoId, catalog.areas), statusLabel: labelEstado(teacher.estado), year: year.year, restrictionCount: restrictions.length }), teacher.estado !== 'ACTIVO' ? (_jsx("p", { className: styles.message, children: "Solo un docente activo puede registrar o editar restricciones." })) : null, _jsx(WeeklyAvailability, { yearId: year.id, year: year.year, teacherId: teacher.id, teacherName: teacherName, readOnly: readOnly, yearClosed: year.status === 'CERRADO', shiftLabel: turno?.nombre ?? 'Sin turno', turnos: turnos.map((item) => ({ id: item.id, nombre: item.nombre })), turnoId: turnoId, onTurnoChange: setTurnoId, bands: bands, bandsLoading: franjas === null && !franjasError, bandsError: franjasError, restrictions: restrictions, loading: restrictionsLoading, error: restrictionsError, onSaved: () => setRevision((current) => current + 1) })] })) : null] })) : null, catalog && !catalog.error && !aniosLoading && !aniosError && !year ? (_jsx("p", { className: styles.message, children: "No hay a\u00F1os lectivos registrados." })) : null] }));
}

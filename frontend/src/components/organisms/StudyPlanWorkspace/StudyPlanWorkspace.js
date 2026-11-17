import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { anioEscolarVisible, ApiError, etiquetaGrado, listarAreas, listarAsignaturas, listarCatalogoTurnos, listarPlanesEstudio, opcionesAniosEscolares, opcionesSelectorPlan, planDesdeGrado, useCatalogoAniosEscolares, useCatalogoGrados, } from '../../../api';
import { academicYears } from '../../../content/academicYear';
import { configuredWeeklyHours, filterStudyPlanEntries, formatAvailableYears, } from '../../../content/studyPlan';
import { Text } from '../../atoms/Text';
import { GradePlanSummary } from '../GradePlanSummary';
import { GradeSubjectsSection } from '../GradeSubjectsSection';
import { StudyPlanSelector } from '../StudyPlanSelector';
import styles from './StudyPlanWorkspace.module.css';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function mensajeError(cause, fallback) {
    return cause instanceof ApiError ? cause.message : fallback;
}
function entradaDesdePlan(plan, areaPorAsignatura, turnoPorId) {
    const area = areaPorAsignatura.get(plan.asignaturaId)?.trim();
    const turno = plan.turnoId ? turnoPorId.get(plan.turnoId)?.trim() : '';
    return {
        id: plan.id,
        yearId: plan.anioEscolarId,
        gradeId: plan.gradoId,
        area: area || '—',
        subject: plan.asignaturaNombre?.trim() || '—',
        weeklyHours: plan.horasSemanales,
        shift: turno || '—',
        note: plan.observacion?.trim() || null,
    };
}
function vistaGrado(grado) {
    const plan = planDesdeGrado(grado);
    return {
        id: grado.id,
        label: etiquetaGrado(grado.nombre),
        level: plan.typeLabel,
        weeklyHours: plan.weeklyHours,
    };
}
export function StudyPlanWorkspace() {
    const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares();
    const catalogo = aniosError ? academicYears : anios;
    const [yearId, setYearId] = useState('');
    const [gradeId, setGradeId] = useState('');
    const [area, setArea] = useState('');
    const [query, setQuery] = useState('');
    const [areas, setAreas] = useState([]);
    const [areaPorAsignatura, setAreaPorAsignatura] = useState(() => new Map());
    const [turnoPorId, setTurnoPorId] = useState(() => new Map());
    const [planes, setPlanes] = useState(null);
    const [planesError, setPlanesError] = useState('');
    const { grados, error: gradosError, loading: gradosLoading } = useCatalogoGrados();
    useEffect(() => {
        const controller = new AbortController();
        listarCatalogoTurnos(controller.signal)
            .then((items) => {
            if (controller.signal.aborted) {
                return;
            }
            setTurnoPorId(new Map(items.map((turno) => [turno.id, turno.nombre])));
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setTurnoPorId(new Map());
        });
        return () => controller.abort();
    }, []);
    useEffect(() => {
        const controller = new AbortController();
        listarAreas(controller.signal)
            .then(async (loadedAreas) => {
            if (controller.signal.aborted) {
                return;
            }
            setAreas(loadedAreas);
            try {
                const groups = await Promise.all(loadedAreas.map((item) => listarAsignaturas(item.id, controller.signal).then((subjects) => subjects.map((subject) => [subject.id, item.nombre]))));
                if (!controller.signal.aborted) {
                    setAreaPorAsignatura(new Map(groups.flat()));
                }
            }
            catch (cause) {
                if (isAbortError(cause) || controller.signal.aborted) {
                    return;
                }
                setAreaPorAsignatura(new Map());
            }
        })
            .catch((cause) => {
            if (isAbortError(cause)) {
                return;
            }
            setAreas([]);
            setAreaPorAsignatura(new Map());
        });
        return () => controller.abort();
    }, []);
    useEffect(() => {
        const controller = new AbortController();
        listarPlanesEstudio(controller.signal)
            .then((items) => {
            setPlanes(items);
            setPlanesError('');
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setPlanes([]);
            setPlanesError(mensajeError(cause, 'No se pudo cargar el plan de estudios. Las horas semanales no están disponibles en este momento.'));
        });
        return () => controller.abort();
    }, []);
    useEffect(() => {
        if (!catalogo?.length) {
            return;
        }
        setYearId((current) => catalogo.some((year) => year.id === current)
            ? current
            : (anioEscolarVisible(catalogo)?.id ?? ''));
    }, [catalogo]);
    useEffect(() => {
        if (!grados?.length) {
            return;
        }
        setGradeId((current) => grados.some((grado) => grado.id === current) ? current : grados[0].id);
    }, [grados]);
    const year = catalogo?.find((item) => item.id === yearId) ?? null;
    const grado = grados?.find((item) => item.id === gradeId) ?? null;
    const grade = grado ? vistaGrado(grado) : null;
    const planesLoading = planes === null && !planesError;
    const entriesDelAnio = (planes ?? [])
        .filter((plan) => (year ? plan.anioEscolarId === year.id : false))
        .map((plan) => entradaDesdePlan(plan, areaPorAsignatura, turnoPorId));
    const planEntries = entriesDelAnio.filter((entry) => entry.gradeId === grado?.id);
    const rows = filterStudyPlanEntries(entriesDelAnio, {
        yearId: year?.id ?? '',
        gradeId: grado?.id ?? '',
        area,
        query,
    });
    const areaNames = areas.length
        ? areas.map((item) => item.nombre)
        : [...new Set(planEntries.map((entry) => entry.area))];
    const selectYear = (nextYearId) => {
        setYearId(nextYearId);
        setArea('');
        setQuery('');
    };
    const selectGrade = (nextGradeId) => {
        setGradeId(nextGradeId);
        setArea('');
        setQuery('');
    };
    return (_jsxs("section", { className: styles.workspace, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { as: "h2", variant: "pageTitle", children: "Plan de estudios" }), _jsx(Text, { variant: "body", children: "Configura las asignaturas y la intensidad horaria semanal correspondiente a cada grado durante el a\u00F1o lectivo." })] }), aniosLoading ? _jsx("p", { className: styles.notice, children: "Cargando a\u00F1os escolares\u2026" }) : null, aniosError ? (_jsxs("p", { className: styles.alert, role: "alert", children: [aniosError, " Se muestran los a\u00F1os de referencia locales para que puedas seguir revisando el plan."] })) : null, planesError ? (_jsxs("p", { className: styles.alert, role: "alert", children: [planesError, " Puedes seguir eligiendo a\u00F1o y grado; la intensidad se mostrar\u00E1 cuando el listado vuelva a estar disponible."] })) : null, _jsx(StudyPlanSelector, { yearId: year?.id ?? '', yearOptions: catalogo
                    ? opcionesAniosEscolares(catalogo, '')
                    : opcionesAniosEscolares(null, ''), yearsSummary: catalogo?.length
                    ? formatAvailableYears(catalogo)
                    : aniosLoading
                        ? 'Cargando años lectivos…'
                        : 'Sin años escolares registrados', yearDisabled: !catalogo?.length, gradeId: grade?.id ?? '', gradeOptions: opcionesSelectorPlan(grados, gradosError), gradeError: gradosError, gradeDisabled: gradosLoading || Boolean(gradosError) || !grados?.length, status: year?.status ?? null, onYearChange: selectYear, onGradeChange: selectGrade }), grade && year ? (planesLoading ? (_jsx("p", { className: styles.notice, children: "Cargando la intensidad horaria del plan de estudios\u2026" })) : (_jsxs(_Fragment, { children: [_jsx(GradePlanSummary, { grade: grade, configuredHours: configuredWeeklyHours(planEntries) }), _jsx(GradeSubjectsSection, { year: year.year, yearId: year.id, gradeId: grade.id, gradeLabel: grade.label, areaNames: areaNames, area: area, query: query, rows: rows, planes: (planes ?? []).filter((plan) => plan.anioEscolarId === year.id && plan.gradoId === grade.id), onSaved: (plan) => {
                            setPlanes((current) => {
                                const list = current ?? [];
                                return list.some((item) => item.id === plan.id)
                                    ? list.map((item) => (item.id === plan.id ? plan : item))
                                    : [...list, plan];
                            });
                            setPlanesError('');
                        }, onMissing: (planId) => setPlanes((current) => (current ?? []).filter((item) => item.id !== planId)), emptyPlanMessage: planesError
                            ? 'La intensidad de este grado no está disponible mientras falle la consulta del plan.'
                            : undefined, onAreaChange: setArea, onQueryChange: setQuery })] }))) : (_jsx(Text, { variant: "body", children: !year
                    ? aniosLoading
                        ? 'Cargando el año lectivo del plan de estudios…'
                        : 'No hay años escolares registrados. El plan de estudios se configura sobre un año lectivo.'
                    : gradosError
                        ? 'El plan de estudios necesita un grado del catálogo. Cuando el listado vuelva a estar disponible, podrás continuar.'
                        : gradosLoading
                            ? 'Cargando grados del catálogo…'
                            : 'No hay grados registrados. El plan de estudios no se puede configurar hasta que exista al menos un grado.' }))] }));
}

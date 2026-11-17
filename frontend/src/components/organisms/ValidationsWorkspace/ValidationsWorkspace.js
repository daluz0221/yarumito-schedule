import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { aConflictoRevisado, anioEscolarVisible, ApiError, consultarHorarioGrupo, listarConflictosPendientes, listarConflictosPorHorario, listarConflictosPorSeveridad, marcarConflictoResuelto, opcionesAniosEscolares, useCatalogoAniosEscolares, useCatalogoGrupos, } from '../../../api';
import { filterConflicts, summarizeConflicts, } from '../../../content/validations';
import { Text } from '../../atoms/Text';
import { ConflictDetailModal } from '../ConflictDetailModal';
import { ConflictsTable } from '../ConflictsTable';
import { ValidationReadyModal } from '../ValidationReadyModal';
import { ValidationFilters } from '../ValidationFilters';
import { ValidationMetrics } from '../ValidationMetrics';
import { ValidationScheduleCard } from '../ValidationScheduleCard';
import styles from './ValidationsWorkspace.module.css';
const tiposConflicto = [
    { value: '', label: 'Todos' },
    { value: 'teacher-busy', label: 'Docente ocupado' },
    { value: 'classroom-busy', label: 'Aula ocupada' },
    { value: 'capacity', label: 'Sobreaforo' },
];
const filtroDocente = [{ value: '', label: 'Todos' }];
const filtroAula = [{ value: '', label: 'Todas' }];
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function mensajeError(cause, fallback) {
    return cause instanceof ApiError && cause.message ? cause.message : fallback;
}
function tonoEstado(estado) {
    if (estado === 'PUBLICADO') {
        return 'success';
    }
    if (estado === 'ARCHIVADO') {
        return 'neutral';
    }
    if (estado === 'BORRADOR') {
        return 'warning';
    }
    return 'muted';
}
function etiquetaEstado(estado) {
    if (estado === 'PUBLICADO') {
        return 'Publicado';
    }
    if (estado === 'ARCHIVADO') {
        return 'Archivado';
    }
    if (estado === 'BORRADOR') {
        return 'Borrador';
    }
    return 'Sin horario';
}
function consultaDe(filters) {
    if (filters.severity === 'ERROR' || filters.severity === 'ADVERTENCIA') {
        return 'severidad';
    }
    if (filters.status === 'PENDIENTE') {
        return 'pendientes';
    }
    return 'todos';
}
function revisar(items, yearId) {
    return items.flatMap((item) => {
        const conflict = aConflictoRevisado(item, yearId);
        return conflict ? [conflict] : [];
    });
}
export function ValidationsWorkspace() {
    const navigate = useNavigate();
    const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares();
    const { grupos, error: gruposError, loading: gruposLoading } = useCatalogoGrupos();
    const [yearIdState, setYearIdState] = useState('');
    const [groupIdState, setGroupIdState] = useState('');
    const [tablero, setTablero] = useState(null);
    const [horarioCargando, setHorarioCargando] = useState(false);
    const [horarioError, setHorarioError] = useState('');
    const [conflictoError, setConflictoError] = useState('');
    const [cargando, setCargando] = useState(false);
    const [todos, setTodos] = useState([]);
    const [visibles, setVisibles] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [readyOpen, setReadyOpen] = useState(false);
    const [resolviendo, setResolviendo] = useState(false);
    const [resolverError, setResolverError] = useState('');
    const [revision, setRevision] = useState(0);
    const [filters, setFilters] = useState({
        severity: '',
        status: 'PENDIENTE',
        kind: '',
        teacherName: '',
        classroom: '',
    });
    const yearId = anios?.some((year) => year.id === yearIdState)
        ? yearIdState
        : (anioEscolarVisible(anios ?? [])?.id ?? '');
    const year = anios?.find((item) => item.id === yearId) ?? null;
    const gruposDelAnio = useMemo(() => (grupos ?? []).filter((group) => group.anioEscolarId === yearId && group.activo), [grupos, yearId]);
    const groupId = gruposDelAnio.some((group) => group.id === groupIdState)
        ? groupIdState
        : (gruposDelAnio[0]?.id ?? '');
    const grupo = gruposDelAnio.find((group) => group.id === groupId) ?? null;
    const horarioId = tablero?.grupoId === groupId ? (tablero.horarioId ?? '') : '';
    useEffect(() => {
        const controller = new AbortController();
        if (!groupId) {
            setTablero(null);
            setHorarioCargando(false);
            setHorarioError('');
            return () => controller.abort();
        }
        setHorarioCargando(true);
        setHorarioError('');
        consultarHorarioGrupo(groupId, controller.signal)
            .then((siguiente) => {
            setTablero(siguiente);
            setHorarioError('');
        })
            .catch((cause) => {
            if (isAbortError(cause)) {
                return;
            }
            setTablero(null);
            setHorarioError(mensajeError(cause, 'No se pudo cargar el horario de este grupo.'));
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setHorarioCargando(false);
            }
        });
        return () => controller.abort();
    }, [groupId, revision]);
    useEffect(() => {
        const controller = new AbortController();
        const modo = consultaDe(filters);
        if (!horarioId) {
            setTodos([]);
            setVisibles([]);
            setCargando(false);
            setConflictoError('');
            return () => controller.abort();
        }
        setCargando(true);
        setVisibles([]);
        setConflictoError('');
        const especificos = modo === 'pendientes'
            ? listarConflictosPendientes(horarioId, controller.signal)
            : modo === 'severidad'
                ? listarConflictosPorSeveridad(horarioId, filters.severity, controller.signal)
                : null;
        Promise.all([
            listarConflictosPorHorario(horarioId, controller.signal),
            especificos ?? Promise.resolve(null),
        ])
            .then(([completos, filtrados]) => {
            setTodos(revisar(completos ?? [], yearId));
            setVisibles(revisar(filtrados ?? completos ?? [], yearId));
            setConflictoError('');
        })
            .catch((cause) => {
            if (isAbortError(cause)) {
                return;
            }
            setTodos([]);
            setVisibles([]);
            setConflictoError(mensajeError(cause, 'No se pudieron cargar los conflictos de este horario.'));
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setCargando(false);
            }
        });
        return () => controller.abort();
    }, [filters.severity, filters.status, horarioId, revision, yearId]);
    useEffect(() => {
        setSelectedId(null);
        setResolverError('');
    }, [groupId, yearId]);
    const summary = summarizeConflicts(todos);
    const rows = filterConflicts(visibles, {
        severity: '',
        status: consultaDe(filters) === 'pendientes' ? '' : filters.status,
        kind: filters.kind,
        groupId: '',
        teacherName: '',
        classroom: '',
    });
    const selected = todos.find((item) => item.id === selectedId) ?? null;
    const scheduleLabel = tablero
        ? `Horario ${tablero.anioEscolar} - ${tablero.sedeNombre} · v${tablero.versionHorario || 1}`
        : 'Sin horario';
    const groupOptions = gruposLoading
        ? [{ value: '', label: 'Cargando grupos…' }]
        : gruposDelAnio.length === 0
            ? [{ value: '', label: 'Sin grupos activos' }]
            : gruposDelAnio.map((group) => ({
                value: group.id,
                label: `${group.codigo} · ${group.gradoNombre}`,
            }));
  const emptyMessage = horarioCargando
    ? 'Cargando el horario…'
    : cargando
    ? 'Cargando conflictos…'
        : !yearId
            ? 'No hay un año lectivo disponible para consultar conflictos.'
            : !groupId
                ? 'Este año lectivo no tiene grupos activos.'
                : horarioError
                    ? 'No se pudo consultar el horario de este grupo.'
                    : !horarioId
                        ? 'Este grupo todavía no tiene un horario, así que no hay conflictos registrados.'
                        : todos.length === 0
                            ? 'Este horario no tiene conflictos registrados.'
                            : 'No hay conflictos que coincidan con esos filtros.';
    const marcarResuelto = () => {
        if (!selected || selected.status === 'RESUELTO') {
            return;
        }
        setResolviendo(true);
        setResolverError('');
        marcarConflictoResuelto(selected.id)
            .then(() => {
            setRevision((current) => current + 1);
        })
            .catch((cause) => {
            setResolverError(mensajeError(cause, 'No se pudo marcar el conflicto como resuelto.'));
        })
            .finally(() => {
            setResolviendo(false);
        });
    };
    return (_jsxs("section", { className: styles.workspace, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { as: "h2", variant: "pageTitle", children: "Validaciones y conflictos" }), _jsx(Text, { variant: "body", children: "Revisa los errores y advertencias detectados antes de publicar el horario." })] }), _jsx(ValidationScheduleCard, { year: year, yearOptions: opcionesAniosEscolares(anios, aniosError), yearDisabled: aniosLoading || Boolean(aniosError), versionLabel: horarioId ? String(tablero?.versionHorario || 1) : '—', scheduleName: tablero?.sedeNombre
                    ? `Horario ${tablero.anioEscolar} - ${tablero.sedeNombre}`
                    : '', campusLabel: tablero?.sedeNombre || grupo?.sedeNombre || '—', statusLabel: etiquetaEstado(horarioId ? tablero?.estadoHorario : null), statusTone: tonoEstado(horarioId ? tablero?.estadoHorario : null), onYearChange: setYearIdState }), _jsx(ValidationMetrics, { ...summary }), horarioId ? (_jsx(PublishBanner, { readiness: summary.readiness, onOpenReady: () => setReadyOpen(true) })) : null, aniosError ? (_jsx("p", { className: styles.alert, role: "alert", children: aniosError })) : null, gruposError ? (_jsx("p", { className: styles.alert, role: "alert", children: gruposError })) : null, horarioError ? (_jsx("p", { className: styles.alert, role: "alert", children: horarioError })) : null, conflictoError ? (_jsx("p", { className: styles.alert, role: "alert", children: conflictoError })) : null, _jsx(ValidationFilters, { values: { ...filters, groupId }, groupOptions: groupOptions, teacherOptions: filtroDocente, classroomOptions: filtroAula, kindOptions: tiposConflicto, validating: cargando || horarioCargando, onChange: (patch) => {
                    if (patch.groupId !== undefined) {
                        setGroupIdState(patch.groupId);
                    }
                    setFilters((current) => ({
                        ...current,
                        ...(patch.severity !== undefined ? { severity: patch.severity } : {}),
                        ...(patch.status !== undefined ? { status: patch.status } : {}),
                        ...(patch.kind !== undefined ? { kind: patch.kind } : {}),
                        ...(patch.teacherName !== undefined ? { teacherName: patch.teacherName } : {}),
                        ...(patch.classroom !== undefined ? { classroom: patch.classroom } : {}),
                    }));
                }, onRevalidate: () => setRevision((current) => current + 1) }), _jsx(ConflictsTable, { rows: rows, emptyMessage: emptyMessage, onDetail: (conflict) => {
                    setResolverError('');
                    setSelectedId(conflict.id);
                } }), _jsx(ConflictDetailModal, { open: Boolean(selected), conflict: selected, scheduleLabel: scheduleLabel, detectedAt: selected?.detectedAt ?? '—', resolving: resolviendo, resolveError: resolverError, onClose: () => {
                    setSelectedId(null);
                    setResolverError('');
                }, onOpenSchedule: () => navigate('/dashboard/horario'), onResolve: marcarResuelto }), _jsx(ValidationReadyModal, { open: readyOpen, hasWarnings: summary.readiness === 'warnings', onClose: () => setReadyOpen(false), onOpenSchedule: () => navigate('/dashboard/horario'), onPreparePublish: () => navigate('/dashboard/publicacion') })] }));
}
function PublishBanner({ readiness, onOpenReady, }) {
    if (readiness === 'blocked') {
        return (_jsxs("div", { className: styles.blocked, role: "status", children: [_jsx("strong", { children: "No apto para publicaci\u00F3n" }), _jsx("p", { children: "Existen errores pendientes. Corrige el horario y vuelve a validar antes de continuar." })] }));
    }
    if (readiness === 'warnings') {
        return (_jsxs("button", { type: "button", className: styles.caution, onClick: onOpenReady, children: [_jsx("strong", { children: "Apto con advertencias" }), _jsx("p", { children: "No hay errores pendientes. Revisa las advertencias antes de publicar el horario." })] }));
    }
    return (_jsxs("button", { type: "button", className: styles.ready, onClick: onOpenReady, children: [_jsx("strong", { children: "Apto para publicaci\u00F3n" }), _jsx("p", { children: "No hay errores ni advertencias pendientes en el horario validado." })] }));
}

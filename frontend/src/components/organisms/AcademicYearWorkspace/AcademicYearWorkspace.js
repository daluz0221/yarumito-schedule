import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { aAcademicYear, anioEscolarVisible, gruposVisibles, useCatalogoAniosEscolares, useCatalogoGrados, useCatalogoGrupos, } from '../../../api';
import { academicYears, filterSchoolGroups, matchesAcademicYear, schoolGroupStats, schoolGroups, } from '../../../content/academicYear';
import { Button } from '../../atoms/Button';
import { Text } from '../../atoms/Text';
import { AcademicYearCard } from '../AcademicYearCard';
import { AcademicYearFormModal } from '../AcademicYearFormModal';
import { ParametrosInstitucionalesSection } from '../ParametrosInstitucionalesSection';
import { SchoolGroupsSection } from '../SchoolGroupsSection';
import styles from './AcademicYearWorkspace.module.css';
export function AcademicYearWorkspace() {
    const { anios, error, loading, incorporar, retirar } = useCatalogoAniosEscolares();
    const { grupos, error: gruposError, loading: gruposLoading, recargar: recargarGrupos, } = useCatalogoGrupos();
    const { grados } = useCatalogoGrados();
    const catalogo = error ? academicYears : anios;
    const [yearId, setYearId] = useState('');
    const [grade, setGrade] = useState('');
    const [status, setStatus] = useState('');
    const [query, setQuery] = useState('');
    const [crear, setCrear] = useState(false);
    useEffect(() => {
        if (!catalogo?.length) {
            return;
        }
        setYearId((current) => catalogo.some((year) => year.id === current)
            ? current
            : (anioEscolarVisible(catalogo)?.id ?? ''));
    }, [catalogo]);
    const year = catalogo?.find((item) => item.id === yearId) ?? null;
    const fuenteGrupos = gruposError
        ? schoolGroups
        : grupos === null
            ? null
            : gruposVisibles(grupos, grados);
    const yearGroups = year && fuenteGrupos
        ? fuenteGrupos.filter((item) => matchesAcademicYear(item, year))
        : [];
    const rows = filterSchoolGroups(yearGroups, { grade, status, query });
    const selectYear = (nextYearId) => {
        setYearId(nextYearId);
        setGrade('');
        setStatus('');
        setQuery('');
    };
    const guardarAnio = (anio) => {
        incorporar(anio);
        setYearId(anio.id);
    };
    return (_jsxs("section", { className: styles.workspace, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { as: "h2", variant: "pageTitle", children: "A\u00F1o lectivo y grupos" }), _jsx(Text, { variant: "body", children: "Configura el per\u00EDodo acad\u00E9mico y los grupos activos de la instituci\u00F3n para cada a\u00F1o lectivo." })] }), loading ? _jsx("p", { className: styles.notice, children: "Cargando a\u00F1os escolares\u2026" }) : null, gruposLoading ? _jsx("p", { className: styles.notice, children: "Cargando grupos\u2026" }) : null, error ? (_jsxs("p", { className: styles.alert, role: "alert", children: [error, " Se muestran los a\u00F1os de referencia locales para que puedas seguir revisando los grupos."] })) : null, gruposError ? (_jsxs("p", { className: styles.alert, role: "alert", children: [gruposError, " Se muestran los grupos de referencia locales para que puedas seguir configurando el a\u00F1o lectivo."] })) : null, year ? (_jsxs(_Fragment, { children: [_jsx(AcademicYearCard, { years: catalogo ?? [], year: year, onYearChange: selectYear, onSaved: guardarAnio, onMissing: retirar }), _jsx(ParametrosInstitucionalesSection, { yearId: year.id, yearLabel: year.year }, year.id), fuenteGrupos ? (_jsx(SchoolGroupsSection, { year: year, groups: yearGroups, stats: schoolGroupStats(yearGroups), grade: grade, status: status, query: query, rows: rows, emptyMessage: yearGroups.length === 0
                            ? 'Este año lectivo aún no tiene grupos registrados.'
                            : 'No se encontraron grupos con esos filtros.', onGradeChange: setGrade, onStatusChange: setStatus, onQueryChange: setQuery, onGroupCreated: recargarGrupos })) : null] })) : loading ? null : (_jsxs("div", { className: styles.intro, children: [_jsx(Text, { variant: "body", children: "No hay a\u00F1os escolares registrados. Los grupos se configuran sobre un a\u00F1o lectivo." }), _jsx(Button, { variant: "primary", size: "sm", onClick: () => setCrear(true), children: "+ Nuevo a\u00F1o lectivo" }), _jsx(AcademicYearFormModal, { open: crear, mode: "create", years: catalogo ?? [], onClose: () => setCrear(false), onCreated: (anio) => {
                            const mapped = aAcademicYear(anio);
                            if (mapped) {
                                guardarAnio(mapped);
                            }
                        } })] }))] }));
}

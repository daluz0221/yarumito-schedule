import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../auth';
import { ApiError, etiquetaCatalogoSedes, listarDocentes, useCatalogoSedes } from '../../../api';
import { academicYears } from '../../../content/academicYear';
import { buildPublicationView, pendingPublicationWarnings, publicationAuthor, publicationCopy, publicationVersion, } from '../../../content/publication';
import { seedPlacements } from '../../../content/scheduleConstruction';
import { formatTeacherName } from '../../../content/teachers';
import { applyConflictStatus, defaultValidationYearId, detectScheduleConflicts, formatValidationTimestamp, } from '../../../content/validations';
import { Text } from '../../atoms/Text';
import { PublicationChecks } from '../PublicationChecks';
import { PublicationLog } from '../PublicationLog';
import { PublicationMetrics } from '../PublicationMetrics';
import { PublicationReadiness } from '../PublicationReadiness';
import { PublicationSummary } from '../PublicationSummary';
import { PublicationVersions } from '../PublicationVersions';
import { PublishBlockedModal } from '../PublishBlockedModal';
import { PublishScheduleModal } from '../PublishScheduleModal';
import { PublishSuccessModal } from '../PublishSuccessModal';
import { PublishWarningsModal } from '../PublishWarningsModal';
import styles from './PublicationWorkspace.module.css';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
export function PublicationWorkspace() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [yearId, setYearId] = useState(defaultValidationYearId);
    const [teachers, setTeachers] = useState([]);
    const [catalogError, setCatalogError] = useState('');
    const { sedes, error: sedesError } = useCatalogoSedes();
    const [record, setRecord] = useState(null);
    const [modal, setModal] = useState('none');
    useEffect(() => {
        const controller = new AbortController();
        listarDocentes({ page: 0, size: 100, estado: 'ACTIVO' }, controller.signal)
            .then((page) => {
            setTeachers([...page.content].sort((left, right) => formatTeacherName(left).localeCompare(formatTeacherName(right), 'es')));
            setCatalogError('');
        })
            .catch((cause) => {
            if (isAbortError(cause)) {
                return;
            }
            setTeachers([]);
            setCatalogError(cause instanceof ApiError
                ? cause.message
                : 'No se pudieron cargar los docentes. La publicación usa los nombres del horario local.');
        });
        return () => controller.abort();
    }, []);
    const year = academicYears.find((item) => item.id === yearId) ?? academicYears[0];
    const author = publicationAuthor(user);
    const detected = useMemo(() => detectScheduleConflicts(year.id, seedPlacements, teachers), [year.id, teachers]);
    const reviewed = useMemo(() => applyConflictStatus(detected, []), [detected]);
    const warnings = useMemo(() => pendingPublicationWarnings(reviewed), [reviewed]);
    const view = buildPublicationView({
        year,
        conflicts: reviewed,
        author,
        published: Boolean(record),
        campusName: etiquetaCatalogoSedes(sedes, sedesError),
    });
    const closeModal = () => setModal('none');
    const openValidations = () => {
        closeModal();
        navigate('/dashboard/validaciones');
    };
    const openSchedule = () => {
        closeModal();
        navigate('/dashboard/horario');
    };
    const selectYear = (nextYearId) => {
        setYearId(nextYearId);
        setRecord(null);
        setModal('none');
    };
    const startPublish = () => {
        if (record) {
            setModal('success');
            return;
        }
        if (view.readiness === 'blocked') {
            setModal('blocked');
            return;
        }
        if (view.readiness === 'warnings') {
            setModal('warnings');
            return;
        }
        setModal('confirm');
    };
    const publish = () => {
        setRecord({
            version: publicationVersion,
            action: 'Publicado',
            actor: author,
            at: formatValidationTimestamp(new Date()),
        });
        setModal('success');
    };
    return (_jsxs("section", { className: styles.workspace, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { as: "h2", variant: "pageTitle", children: publicationCopy.pageTitle }), _jsx(Text, { variant: "body", children: publicationCopy.pageSubtitle })] }), _jsx(PublicationSummary, { year: year, view: view, onYearChange: selectYear }), _jsx(PublicationMetrics, { view: view }), _jsx(PublicationReadiness, { view: view, onSeeWarnings: () => setModal('warnings'), onSeeBlocked: () => setModal('blocked') }), catalogError ? _jsx("p", { className: styles.notice, children: catalogError }) : null, sedesError ? (_jsxs("p", { className: styles.alert, role: "alert", children: [sedesError, " El resumen de publicaci\u00F3n sigue disponible."] })) : null, _jsx(PublicationChecks, { checks: view.checks }), _jsx(PublicationVersions, { view: view }), _jsx(PublicationLog, { record: record, onPublish: startPublish }), _jsx(PublishWarningsModal, { open: modal === 'warnings', warnings: warnings, onClose: closeModal, onReview: openValidations, onContinue: () => setModal('confirm') }), _jsx(PublishScheduleModal, { open: modal === 'confirm', view: view, onClose: closeModal, onConfirm: publish }), _jsx(PublishBlockedModal, { open: modal === 'blocked', errors: view.errors, onClose: closeModal, onReview: openValidations }), _jsx(PublishSuccessModal, { open: modal === 'success', view: view, record: record, onClose: closeModal, onSeeSchedule: openSchedule })] }));
}

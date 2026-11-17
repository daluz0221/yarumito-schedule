import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { ApiError, consultarDocente } from '../../../api';
import { teacherToForm } from '../../../content/teachers';
import { Text } from '../../atoms/Text';
import { TextLink } from '../../atoms/TextLink';
import { RegisterTeacherForm } from '../RegisterTeacherForm';
import styles from './EditTeacherWorkspace.module.css';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
export function EditTeacherWorkspace() {
    const { teacherId } = useParams();
    const [teacher, setTeacher] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    useEffect(() => {
        if (!teacherId) {
            setTeacher(null);
            setError('No se indicó el docente a editar.');
            setLoading(false);
            return;
        }
        const controller = new AbortController();
        setLoading(true);
        setError('');
        consultarDocente(teacherId, controller.signal)
            .then(setTeacher)
            .catch((cause) => {
            if (isAbortError(cause)) {
                return;
            }
            setTeacher(null);
            setError(cause instanceof ApiError
                ? cause.message
                : 'No se pudo cargar el docente.');
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setLoading(false);
            }
        });
        return () => controller.abort();
    }, [teacherId]);
    return (_jsxs("section", { className: styles.workspace, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { variant: "sectionTitle", children: "Editar docente" }), _jsx(Text, { variant: "body", children: "Actualice la informaci\u00F3n b\u00E1sica e institucional del docente." })] }), loading ? (_jsx("p", { className: styles.status, children: "Cargando docente..." })) : error || !teacher ? (_jsxs("p", { className: styles.error, role: "alert", children: [error || 'El docente no existe.', ' ', _jsx(TextLink, { to: "/dashboard/docentes", children: "Volver al listado" })] })) : (_jsx(RegisterTeacherForm, { mode: "edit", teacherId: teacher.id, initialValues: teacherToForm(teacher) }))] }));
}

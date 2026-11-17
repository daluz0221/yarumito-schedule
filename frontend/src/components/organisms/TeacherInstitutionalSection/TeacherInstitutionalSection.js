import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { listarEscalafones, listarRegimenesDocentes, regimenDesdeEscalafon, } from '../../../api';
import { registerContractOptions, sanitizeDigits, } from '../../../content/teachers';
import { FormSection } from '../../molecules/FormSection';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
export function TeacherInstitutionalSection({ values, areaOptions, areaHint, areasDisabled = false, errors = {}, onChange, }) {
    const [regimen, setRegimen] = useState(() => regimenDesdeEscalafon(values.escalafon));
    const [regimenes, setRegimenes] = useState([]);
    const [escalafones, setEscalafones] = useState([]);
    const [escalafonesError, setEscalafonesError] = useState('');
    const sinEscalafon = regimen === 'ETNO_AFRO' || regimen === 'NO_APLICA';
    useEffect(() => {
        const inferido = regimenDesdeEscalafon(values.escalafon);
        if (inferido) {
            setRegimen((current) => current || inferido);
        }
    }, [values.escalafon]);
    useEffect(() => {
        const controller = new AbortController();
        listarRegimenesDocentes(controller.signal)
            .then((items) => {
            if (!controller.signal.aborted) {
                setRegimenes(items ?? []);
            }
        })
            .catch(() => {
            if (!controller.signal.aborted) {
                setRegimenes([]);
            }
        });
        return () => controller.abort();
    }, []);
    useEffect(() => {
        if (!regimen) {
            setEscalafones([]);
            setEscalafonesError('');
            return;
        }
        const controller = new AbortController();
        setEscalafonesError('');
        if (regimen === 'ETNO_AFRO' || regimen === 'NO_APLICA') {
            setEscalafones([]);
        }
        listarEscalafones(regimen, controller.signal)
            .then((items) => {
            if (!controller.signal.aborted) {
                setEscalafones(items ?? []);
            }
        })
            .catch(() => {
            if (!controller.signal.aborted) {
                setEscalafones([]);
                setEscalafonesError('No se pudieron cargar los escalafones.');
            }
        });
        return () => controller.abort();
    }, [regimen]);
    const escalafonActual = values.escalafon.trim();
    const escalafonConocido = escalafones.some((item) => item.codigo === escalafonActual);
    const escalafonOptions = [
        { value: '', label: sinEscalafon ? 'Sin escalafón' : 'Seleccionar escalafón' },
        ...(escalafonActual && !escalafonConocido && !sinEscalafon
            ? [{ value: escalafonActual, label: `${escalafonActual} (valor actual)` }]
            : []),
        ...(sinEscalafon
            ? []
            : escalafones.map((item) => ({ value: item.codigo, label: item.nombre }))),
    ];
    return (_jsxs(FormSection, { title: "Informaci\u00F3n institucional", description: "Informaci\u00F3n necesaria para identificar la vinculaci\u00F3n acad\u00E9mica del docente dentro de la instituci\u00F3n.", children: [_jsx(SelectField, { id: "tipoVinculacion", name: "tipoVinculacion", label: "Tipo de vinculaci\u00F3n", required: true, value: values.tipoVinculacion, options: registerContractOptions, error: errors.tipoVinculacion, onChange: (value) => onChange('tipoVinculacion', value) }), _jsx(SelectField, { id: "areaNombramientoId", name: "areaNombramientoId", label: "\u00C1rea de nombramiento", required: true, value: values.areaNombramientoId, options: areaOptions, hint: areaHint, error: errors.areaNombramientoId, disabled: areasDisabled, onChange: (value) => onChange('areaNombramientoId', value) }), _jsx(TextField, { id: "numeroDecreto", name: "numeroDecreto", label: "N\u00FAmero de decreto", placeholder: "Ej. 12344", inputMode: "numeric", value: values.numeroDecreto, error: errors.numeroDecreto, onChange: (event) => onChange('numeroDecreto', sanitizeDigits(event.target.value)) }), _jsx(TextField, { id: "fechaDecreto", name: "fechaDecreto", type: "date", label: "Fecha de decreto", value: values.fechaDecreto, error: errors.fechaDecreto, onChange: (event) => onChange('fechaDecreto', event.target.value) }), _jsx(SelectField, { id: "regimen", name: "regimen", label: "R\u00E9gimen docente", value: regimen, options: [
                    { value: '', label: regimenes.length ? 'Seleccionar régimen' : 'Cargando regímenes...' },
                    ...regimenes.map((item) => ({ value: item.codigo, label: item.nombre })),
                ], hint: "El r\u00E9gimen define las opciones de escalaf\u00F3n. No se guarda aparte del escalaf\u00F3n.", onChange: (codigo) => {
                    setRegimen(codigo);
                    onChange('escalafon', '');
                } }), _jsx(SelectField, { id: "escalafon", name: "escalafon", label: "Escalaf\u00F3n", value: sinEscalafon ? '' : values.escalafon, options: escalafonOptions, disabled: !regimen || sinEscalafon, hint: sinEscalafon
                    ? 'Este régimen no asigna un escalafón.'
                    : escalafonesError
                        ? escalafonesError
                        : !regimen
                            ? 'Elige primero el régimen docente.'
                            : undefined, error: errors.escalafon, onChange: (codigo) => onChange('escalafon', codigo) }), _jsx(TextField, { id: "fechaVinculacion", name: "fechaVinculacion", type: "date", label: "Fecha de vinculaci\u00F3n", value: values.fechaVinculacion, error: errors.fechaVinculacion, onChange: (event) => onChange('fechaVinculacion', event.target.value) })] }));
}

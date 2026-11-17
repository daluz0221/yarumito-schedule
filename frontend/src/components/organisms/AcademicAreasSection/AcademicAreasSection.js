import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useRef, useState } from 'react';
import { ApiError, consultarArea } from '../../../api';
import { areaStatusOptions, } from '../../../content/areas';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { TextInput } from '../../atoms/TextInput';
import { FormField } from '../../molecules/FormField';
import { SectionHeader } from '../../molecules/SectionHeader';
import { SelectField } from '../../molecules/SelectField';
import { AcademicAreasTable } from '../AcademicAreasTable';
import { AreaFormModal } from '../AreaFormModal';
import { DeactivateAreaModal } from '../DeactivateAreaModal';
import styles from './AcademicAreasSection.module.css';
function areaDetailErrorMessage(cause) {
    if (cause instanceof ApiError) {
        if (cause.status === 404) {
            return 'Esta área ya no está disponible. Cierra el formulario y revisa el listado.';
        }
        return cause.message;
    }
    return 'No se pudo cargar el área.';
}
export function AcademicAreasSection({ areas, rows, selectedId, selectedName, query, status, loading, onQueryChange, onStatusChange, onSelect, onAreaCreated, onAreaUpdated, onAreaMissing, }) {
    const [modal, setModal] = useState({ name: 'closed' });
    const [editingArea, setEditingArea] = useState(null);
    const [editLoading, setEditLoading] = useState(false);
    const [editError, setEditError] = useState('');
    const detailRequest = useRef(null);
    useEffect(() => {
        return () => detailRequest.current?.abort();
    }, []);
    const closeModal = () => {
        detailRequest.current?.abort();
        detailRequest.current = null;
        setModal({ name: 'closed' });
        setEditingArea(null);
        setEditLoading(false);
        setEditError('');
    };
    const loadAreaDetail = (areaId) => {
        detailRequest.current?.abort();
        const controller = new AbortController();
        detailRequest.current = controller;
        setModal({ name: 'edit', id: areaId });
        setEditingArea(null);
        setEditError('');
        setEditLoading(true);
        consultarArea(areaId, controller.signal)
            .then((area) => {
            if (controller.signal.aborted) {
                return;
            }
            setEditingArea(area);
            onAreaUpdated(area);
        })
            .catch((cause) => {
            if (controller.signal.aborted ||
                (cause instanceof DOMException && cause.name === 'AbortError')) {
                return;
            }
            setEditingArea(null);
            setEditError(areaDetailErrorMessage(cause));
            if (cause instanceof ApiError && cause.status === 404) {
                onAreaMissing?.(areaId);
            }
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setEditLoading(false);
            }
        });
    };
    return (_jsxs(Card, { className: styles.section, children: [_jsx(SectionHeader, { title: "\u00C1reas acad\u00E9micas", description: "Selecciona un \u00E1rea para consultar las asignaturas que pertenecen a ella.", action: _jsx(Button, { variant: "primary", size: "sm", onClick: () => setModal({ name: 'create' }), children: "+ Nueva \u00E1rea" }) }), _jsxs("form", { className: styles.filters, onSubmit: (event) => event.preventDefault(), children: [_jsx(FormField, { id: "area-search", label: "Buscar \u00E1rea", children: _jsx(TextInput, { id: "area-search", name: "query", inputSize: "sm", placeholder: "Buscar por c\u00F3digo o nombre...", value: query, onChange: (event) => onQueryChange(event.target.value) }) }), _jsx(SelectField, { id: "area-status", label: "Estado", name: "status", value: status, options: areaStatusOptions, onChange: (value) => onStatusChange(value) })] }), _jsx("div", { className: styles.tableHost, children: _jsx(AcademicAreasTable, { rows: rows, selectedId: selectedId, emptyMessage: loading
                        ? 'Cargando áreas...'
                        : 'No se encontraron áreas con esos filtros.', onSelect: onSelect, onEdit: (area) => {
                        onSelect(area.id);
                        loadAreaDetail(area.id);
                    }, onDeactivate: (area) => {
                        onSelect(area.id);
                        setModal({ name: 'deactivate', area });
                    } }) }), selectedName ? (_jsxs(Text, { variant: "caption", children: ["\u00C1rea seleccionada: ", selectedName, ". Las asignaturas mostradas abajo pertenecen \u00FAnicamente a esta \u00E1rea."] })) : null, modal.name === 'create' || modal.name === 'edit' ? (_jsx(AreaFormModal, { open: true, mode: modal.name, areas: areas, area: modal.name === 'edit' ? editingArea ?? undefined : undefined, loading: modal.name === 'edit' && editLoading, loadError: modal.name === 'edit' ? editError : '', onRetry: modal.name === 'edit' ? () => loadAreaDetail(modal.id) : undefined, onClose: closeModal, onCreated: onAreaCreated, onUpdated: onAreaUpdated }, modal.name === 'edit' ? modal.id : 'create')) : null, modal.name === 'deactivate' ? (_jsx(DeactivateAreaModal, { open: true, area: modal.area, onClose: closeModal, onUpdated: onAreaUpdated })) : null] }));
}

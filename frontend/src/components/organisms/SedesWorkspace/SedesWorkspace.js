import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useRef, useState } from 'react';
import { ApiError, consultarSede, listarCatalogoSedes } from '../../../api';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { TextInput } from '../../atoms/TextInput';
import { FormField } from '../../molecules/FormField';
import { SectionHeader } from '../../molecules/SectionHeader';
import { SedeFormModal } from '../SedeFormModal';
import { SedesTable } from '../SedesTable';
import styles from './SedesWorkspace.module.css';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function detalleError(cause) {
    if (cause instanceof ApiError) {
        if (cause.status === 404) {
            return 'Esta sede ya no está disponible. Cierra el formulario y revisa el listado.';
        }
        return cause.message;
    }
    return 'No se pudo cargar la sede.';
}
function coincide(sede, query) {
    const texto = query.trim().toLowerCase();
    if (!texto) {
        return true;
    }
    return [sede.nombre, sede.codigo, sede.direccion]
        .filter(Boolean)
        .some((valor) => valor.toLowerCase().includes(texto));
}
export function SedesWorkspace() {
    const [sedes, setSedes] = useState([]);
    const [query, setQuery] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [modal, setModal] = useState({ name: 'closed' });
    const [editing, setEditing] = useState(null);
    const [editLoading, setEditLoading] = useState(false);
    const [editError, setEditError] = useState('');
    const detailRequest = useRef(null);
    useEffect(() => {
        const controller = new AbortController();
        listarCatalogoSedes(controller.signal)
            .then((items) => {
            setSedes(items);
        })
            .catch((cause) => {
            if (isAbortError(cause)) {
                return;
            }
            setSedes([]);
            setError(cause instanceof ApiError
                ? cause.message
                : 'No se pudo cargar el catálogo de sedes.');
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setLoading(false);
            }
        });
        return () => controller.abort();
    }, []);
    useEffect(() => {
        return () => detailRequest.current?.abort();
    }, []);
    const closeModal = () => {
        detailRequest.current?.abort();
        detailRequest.current = null;
        setModal({ name: 'closed' });
        setEditing(null);
        setEditLoading(false);
        setEditError('');
    };
    const loadSede = (sedeId) => {
        detailRequest.current?.abort();
        const controller = new AbortController();
        detailRequest.current = controller;
        setModal({ name: 'edit', id: sedeId });
        setEditing(null);
        setEditError('');
        setEditLoading(true);
        consultarSede(sedeId, controller.signal)
            .then((sede) => {
            if (controller.signal.aborted) {
                return;
            }
            setEditing(sede);
            setSedes((current) => current.map((item) => (item.id === sede.id ? sede : item)));
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setEditing(null);
            setEditError(detalleError(cause));
            if (cause instanceof ApiError && cause.status === 404) {
                setSedes((current) => current.filter((item) => item.id !== sedeId));
            }
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setEditLoading(false);
            }
        });
    };
    const rows = sedes
        .filter((sede) => coincide(sede, query))
        .sort((left, right) => left.nombre.localeCompare(right.nombre, 'es'));
    return (_jsxs("section", { className: styles.workspace, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { as: "h2", variant: "pageTitle", children: "Sedes" }), _jsx(Text, { variant: "body", children: "Registra y actualiza las sedes de la instituci\u00F3n. La sede principal se usa como referencia en horarios, validaciones y publicaci\u00F3n." })] }), error ? (_jsx("p", { className: styles.error, role: "alert", children: error })) : null, _jsxs(Card, { className: styles.section, children: [_jsx(SectionHeader, { title: "Cat\u00E1logo de sedes", description: "Consulta una sede para editar su nombre, c\u00F3digo, direcci\u00F3n o si es la principal.", action: _jsx(Button, { variant: "primary", size: "sm", onClick: () => setModal({ name: 'create' }), children: "+ Nueva sede" }) }), _jsx("div", { className: styles.search, children: _jsx(FormField, { id: "sede-search", label: "Buscar sede", children: _jsx(TextInput, { id: "sede-search", name: "query", inputSize: "sm", placeholder: "Buscar por c\u00F3digo, nombre o direcci\u00F3n...", value: query, onChange: (event) => setQuery(event.target.value) }) }) }), _jsx("div", { className: styles.tableHost, children: _jsx(SedesTable, { rows: rows, emptyMessage: loading
                                ? 'Cargando sedes...'
                                : sedes.length === 0
                                    ? 'Aún no hay sedes registradas.'
                                    : 'No se encontraron sedes con esa búsqueda.', onEdit: (sede) => loadSede(sede.id) }) })] }), _jsx(SedeFormModal, { open: modal.name !== 'closed', mode: modal.name === 'edit' ? 'edit' : 'create', sedes: sedes, sede: editing ?? undefined, loading: modal.name === 'edit' && editLoading, loadError: modal.name === 'edit' ? editError : '', onRetry: modal.name === 'edit' ? () => loadSede(modal.id) : undefined, onClose: closeModal, onCreated: (sede) => {
                    setSedes((current) => current.some((item) => item.id === sede.id) ? current : [...current, sede]);
                    setError('');
                }, onUpdated: (sede) => {
                    setSedes((current) => current.map((item) => (item.id === sede.id ? sede : item)));
                    setError('');
                } })] }));
}

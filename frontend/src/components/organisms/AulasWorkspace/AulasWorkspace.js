import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useRef, useState } from 'react';
import { ApiError, consultarAula, eliminarAula, listarAulasPorSede, opcionesCatalogoSedes, useCatalogoSedes, } from '../../../api';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { Modal } from '../../atoms/Modal';
import { Text } from '../../atoms/Text';
import { TextInput } from '../../atoms/TextInput';
import { FormField } from '../../molecules/FormField';
import { ModalActions } from '../../molecules/ModalActions';
import { SectionHeader } from '../../molecules/SectionHeader';
import { SelectField } from '../../molecules/SelectField';
import { AulaFormModal } from '../AulaFormModal';
import { AulasTable } from '../AulasTable';
import styles from './AulasWorkspace.module.css';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function detalleError(cause) {
    if (cause instanceof ApiError) {
        if (cause.status === 404) {
            return 'Esta aula ya no está disponible. Cierra el formulario y revisa el listado.';
        }
        return cause.message;
    }
    return 'No se pudo cargar el aula.';
}
function coincide(aula, query) {
    const texto = query.trim().toLowerCase();
    if (!texto) {
        return true;
    }
    return aula.nombre.toLowerCase().includes(texto);
}
export function AulasWorkspace() {
    const { sedes, error: sedesError } = useCatalogoSedes();
    const [sedeId, setSedeId] = useState('');
    const [aulas, setAulas] = useState([]);
    const [query, setQuery] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [modal, setModal] = useState({ name: 'closed' });
    const [editing, setEditing] = useState(null);
    const [editLoading, setEditLoading] = useState(false);
    const [editError, setEditError] = useState('');
    const [removing, setRemoving] = useState(null);
    const [removeError, setRemoveError] = useState('');
    const [removingBusy, setRemovingBusy] = useState(false);
    const detailRequest = useRef(null);
    useEffect(() => {
        if (sedeId || !sedes?.length) {
            return;
        }
        const principal = sedes.find((sede) => sede.esPrincipal);
        setSedeId(principal?.id ?? sedes[0].id);
    }, [sedes, sedeId]);
    useEffect(() => {
        if (!sedeId) {
            return;
        }
        const controller = new AbortController();
        setLoading(true);
        setError('');
        listarAulasPorSede(sedeId, controller.signal)
            .then((items) => {
            setAulas(items);
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setAulas([]);
            setError(cause instanceof ApiError
                ? cause.message
                : 'No se pudo cargar el catálogo de aulas.');
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setLoading(false);
            }
        });
        return () => controller.abort();
    }, [sedeId]);
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
    const loadAula = (aulaId, mode) => {
        detailRequest.current?.abort();
        const controller = new AbortController();
        detailRequest.current = controller;
        setModal({ name: mode, id: aulaId });
        setEditing(null);
        setEditError('');
        setEditLoading(true);
        consultarAula(aulaId, controller.signal)
            .then((aula) => {
            if (controller.signal.aborted) {
                return;
            }
            setEditing(aula);
            setAulas((current) => current.map((item) => (item.id === aula.id ? aula : item)));
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setEditing(null);
            setEditError(detalleError(cause));
            if (cause instanceof ApiError && cause.status === 404) {
                setAulas((current) => current.filter((item) => item.id !== aulaId));
            }
        })
            .finally(() => {
            if (!controller.signal.aborted) {
                setEditLoading(false);
            }
        });
    };
    const confirmDelete = async () => {
        if (!removing || removingBusy) {
            return;
        }
        setRemovingBusy(true);
        setRemoveError('');
        try {
            await eliminarAula(removing.id);
            setAulas((current) => current.filter((item) => item.id !== removing.id));
            setRemoving(null);
        }
        catch (cause) {
            setRemoveError(cause instanceof ApiError
                ? cause.message
                : 'No se pudo eliminar el aula.');
        }
        finally {
            setRemovingBusy(false);
        }
    };
    const rows = aulas
        .filter((aula) => coincide(aula, query))
        .sort((left, right) => left.nombre.localeCompare(right.nombre, 'es'));
    const formMode = modal.name === 'closed' ? 'create' : modal.name;
    const sedeOptions = opcionesCatalogoSedes(sedes, sedesError);
    return (_jsxs("section", { className: styles.workspace, children: [_jsxs("div", { className: styles.intro, children: [_jsx(Text, { as: "h2", variant: "pageTitle", children: "Aulas" }), _jsx(Text, { variant: "body", children: "Registra los espacios de cada sede. Una nueva versi\u00F3n conserva el aula anterior como inactiva cuando cambia su nombre, su tipo o su sede." })] }), error ? (_jsx("p", { className: styles.error, role: "alert", children: error })) : null, _jsxs(Card, { className: styles.section, children: [_jsx(SectionHeader, { title: "Cat\u00E1logo de aulas", description: "Elige una sede para ver sus aulas, corregirlas o registrar una nueva versi\u00F3n.", action: _jsx(Button, { variant: "primary", size: "sm", disabled: !sedeId, onClick: () => setModal({ name: 'create' }), children: "+ Nueva aula" }) }), _jsxs("div", { className: styles.filters, children: [_jsx(SelectField, { id: "aula-sede", label: "Sede", value: sedes?.length ? sedeId : '', options: sedeOptions, disabled: !sedes?.length, error: sedesError || undefined, onChange: setSedeId }), _jsx(FormField, { id: "aula-search", label: "Buscar aula", children: _jsx(TextInput, { id: "aula-search", name: "query", inputSize: "sm", placeholder: "Buscar por nombre...", value: query, onChange: (event) => setQuery(event.target.value) }) })] }), _jsx("div", { className: styles.tableHost, children: _jsx(AulasTable, { rows: rows, emptyMessage: !sedeId
                                ? 'Selecciona una sede para ver sus aulas.'
                                : loading
                                    ? 'Cargando aulas...'
                                    : aulas.length === 0
                                        ? 'Esta sede aún no tiene aulas registradas.'
                                        : 'No se encontraron aulas con esa búsqueda.', onEdit: (aula) => loadAula(aula.id, 'edit'), onVersion: (aula) => loadAula(aula.id, 'version'), onDelete: (aula) => {
                                setRemoveError('');
                                setRemoving(aula);
                            } }) })] }), _jsx(AulaFormModal, { open: modal.name !== 'closed', mode: formMode, sedes: sedes, sedesError: sedesError, aulas: aulas, aula: editing ?? undefined, sedeInicial: sedeId, loading: modal.name !== 'closed' && modal.name !== 'create' && editLoading, loadError: modal.name === 'edit' || modal.name === 'version' ? editError : '', onRetry: modal.name === 'edit' || modal.name === 'version'
                    ? () => loadAula(modal.id, modal.name)
                    : undefined, onClose: closeModal, onCreated: (aula) => {
                    if (aula.sedeId === sedeId) {
                        setAulas((current) => current.some((item) => item.id === aula.id) ? current : [...current, aula]);
                    }
                    setError('');
                }, onUpdated: (aula) => {
                    setAulas((current) => aula.sedeId === sedeId
                        ? current.map((item) => (item.id === aula.id ? aula : item))
                        : current.filter((item) => item.id !== aula.id));
                    setError('');
                }, onVersioned: (anteriorId, aula) => {
                    setAulas((current) => {
                        const actualizadas = current.map((item) => item.id === anteriorId ? { ...item, activa: false } : item);
                        if (aula.sedeId !== sedeId || actualizadas.some((item) => item.id === aula.id)) {
                            return actualizadas;
                        }
                        return [...actualizadas, aula];
                    });
                    setError('');
                } }), _jsxs(Modal, { open: Boolean(removing), title: "Eliminar aula", description: removing ? removing.nombre : undefined, onClose: () => {
                    if (!removingBusy) {
                        setRemoving(null);
                    }
                }, footer: _jsx(ModalActions, { confirmLabel: removingBusy ? 'Eliminando...' : 'Eliminar', disabled: removingBusy, onCancel: () => setRemoving(null), onConfirm: confirmDelete }), children: [_jsx("p", { className: styles.deleteCopy, children: "El sistema solo elimina un aula si puede garantizar que nunca se us\u00F3. Si no puede demostrarlo, el aula se conserva." }), removeError ? (_jsx("p", { className: styles.error, role: "alert", children: removeError })) : null] })] }));
}

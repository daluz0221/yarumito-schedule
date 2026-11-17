import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ApiError, login } from '../../../api';
import { useAuth } from '../../../auth';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import { TextInput } from '../../atoms/TextInput';
import { CheckboxField } from '../../molecules/CheckboxField';
import { FormField } from '../../molecules/FormField';
import { PasswordField } from '../../molecules/PasswordField';
import styles from './LoginForm.module.css';
export function LoginForm({ onSubmit }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [remember, setRemember] = useState(false);
    const [error, setError] = useState('');
    const [authError, setAuthError] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const navigate = useNavigate();
    const { acceptSession } = useAuth();
    const closeAuthError = () => setAuthError('');
    const handleSubmit = async (event) => {
        event.preventDefault();
        const values = {
            username: username.trim(),
            password,
            remember,
        };
        if (!values.username || !values.password) {
            setError('Ingresa tu correo y contraseña.');
            return;
        }
        setError('');
        setAuthError('');
        setSubmitting(true);
        try {
            const result = await login(values);
            acceptSession(result.usuario);
            onSubmit?.(values);
            navigate('/dashboard');
        }
        catch (cause) {
            setAuthError(cause instanceof ApiError
                ? cause.message
                : 'No se pudo iniciar sesión. Inténtalo de nuevo.');
        }
        finally {
            setSubmitting(false);
        }
    };
    return (_jsxs("form", { className: styles.form, onSubmit: handleSubmit, noValidate: true, children: [_jsxs("div", { className: styles.fields, children: [_jsx(FormField, { id: "username", label: "Usuario", children: _jsx(TextInput, { id: "username", name: "username", type: "email", autoComplete: "username", placeholder: "Ingresa tu correo electr\u00F3nico", value: username, disabled: submitting, onChange: (event) => setUsername(event.target.value) }) }), _jsx(PasswordField, { id: "password", name: "password", label: "Contrase\u00F1a", placeholder: "Ingresa tu contrase\u00F1a", value: password, disabled: submitting, onChange: (event) => setPassword(event.target.value) }), _jsx(CheckboxField, { id: "remember", name: "remember", label: "Recordarme", checked: remember, onChange: setRemember })] }), error ? (_jsx("p", { className: styles.error, role: "alert", children: error })) : null, _jsx(Button, { type: "submit", fullWidth: true, disabled: submitting, children: submitting ? 'Iniciando sesión...' : 'Iniciar sesión' }), _jsx(Modal, { open: Boolean(authError), title: "Error de autenticaci\u00F3n", description: authError, size: "sm", onClose: closeAuthError, footer: _jsx(Button, { size: "sm", onClick: closeAuthError, children: "Aceptar" }) })] }));
}

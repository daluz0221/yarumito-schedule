import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { AuthBrand } from '../../components/molecules/AuthBrand';
import { LoginForm } from '../../components/organisms/LoginForm';
import { AuthLayout } from '../../components/templates/AuthLayout';
export function AdminLoginPage() {
    return (_jsxs(AuthLayout, { children: [_jsx(AuthBrand, { title: "Bienvenidos", subtitle: "Gesti\u00F3n de Horarios" }), _jsx(LoginForm, {})] }));
}

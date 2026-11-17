import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider, GuestRoute, HomeRedirect, ProtectedRoute, } from './auth';
import { AcademicYearPage } from './pages/AcademicYearPage';
import { AuditoriaPage } from './pages/AuditoriaPage';
import { AulasPage } from './pages/AulasPage';
import { AreasPage } from './pages/AreasPage';
import { AssignmentPage } from './pages/AssignmentPage';
import { AdminLoginPage } from './pages/AdminLoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { GradosPage } from './pages/GradosPage';
import { EditTeacherPage } from './pages/EditTeacherPage';
import { FranjasPage } from './pages/FranjasPage';
import { RegisterTeacherPage } from './pages/RegisterTeacherPage';
import { PublicationPage } from './pages/PublicationPage';
import { RestrictionsPage } from './pages/RestrictionsPage';
import { SchedulePage } from './pages/SchedulePage';
import { SedesPage } from './pages/SedesPage';
import { StudyPlanPage } from './pages/StudyPlanPage';
import { TeacherProfilePage } from './pages/TeacherProfilePage';
import { TeachersPage } from './pages/TeachersPage';
import { TurnosPage } from './pages/TurnosPage';
import { ValidationsPage } from './pages/ValidationsPage';
export default function App() {
    return (_jsx(BrowserRouter, { children: _jsx(AuthProvider, { children: _jsxs(Routes, { children: [_jsx(Route, { path: "/admin", element: _jsx(GuestRoute, { children: _jsx(AdminLoginPage, {}) }) }), _jsxs(Route, { element: _jsx(ProtectedRoute, {}), children: [_jsx(Route, { path: "/dashboard", element: _jsx(DashboardPage, {}) }), _jsx(Route, { path: "/dashboard/anio-lectivo", element: _jsx(AcademicYearPage, {}) }), _jsx(Route, { path: "/dashboard/sedes", element: _jsx(SedesPage, {}) }), _jsx(Route, { path: "/dashboard/aulas", element: _jsx(AulasPage, {}) }), _jsx(Route, { path: "/dashboard/turnos", element: _jsx(TurnosPage, {}) }), _jsx(Route, { path: "/dashboard/franjas", element: _jsx(FranjasPage, {}) }), _jsx(Route, { path: "/dashboard/grados", element: _jsx(GradosPage, {}) }), _jsx(Route, { path: "/dashboard/areas", element: _jsx(AreasPage, {}) }), _jsx(Route, { path: "/dashboard/plan-estudios", element: _jsx(StudyPlanPage, {}) }), _jsx(Route, { path: "/dashboard/restricciones", element: _jsx(RestrictionsPage, {}) }), _jsx(Route, { path: "/dashboard/asignacion", element: _jsx(AssignmentPage, {}) }), _jsx(Route, { path: "/dashboard/horario", element: _jsx(SchedulePage, {}) }), _jsx(Route, { path: "/dashboard/validaciones", element: _jsx(ValidationsPage, {}) }), _jsx(Route, { path: "/dashboard/publicacion", element: _jsx(PublicationPage, {}) }), _jsx(Route, { path: "/dashboard/auditoria", element: _jsx(AuditoriaPage, {}) }), _jsx(Route, { path: "/dashboard/docentes", element: _jsx(TeachersPage, {}) }), _jsx(Route, { path: "/dashboard/docentes/registrar", element: _jsx(RegisterTeacherPage, {}) }), _jsx(Route, { path: "/dashboard/docentes/:teacherId/editar", element: _jsx(EditTeacherPage, {}) }), _jsx(Route, { path: "/dashboard/docentes/:teacherId", element: _jsx(TeacherProfilePage, {}) })] }), _jsx(Route, { path: "/", element: _jsx(HomeRedirect, {}) })] }) }) }));
}

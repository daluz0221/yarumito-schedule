import { BrowserRouter, Route, Routes } from 'react-router-dom'
import {
  AuthProvider,
  GuestRoute,
  HomeRedirect,
  ProtectedRoute,
} from './auth'
import { AcademicYearPage } from './pages/AcademicYearPage'
import { AreasPage } from './pages/AreasPage'
import { AssignmentPage } from './pages/AssignmentPage'
import { AdminLoginPage } from './pages/AdminLoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { EditTeacherPage } from './pages/EditTeacherPage'
import { RegisterTeacherPage } from './pages/RegisterTeacherPage'
import { PublicationPage } from './pages/PublicationPage'
import { RestrictionsPage } from './pages/RestrictionsPage'
import { SchedulePage } from './pages/SchedulePage'
import { StudyPlanPage } from './pages/StudyPlanPage'
import { TeacherProfilePage } from './pages/TeacherProfilePage'
import { TeachersPage } from './pages/TeachersPage'
import { ValidationsPage } from './pages/ValidationsPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route
            path="/admin"
            element={
              <GuestRoute>
                <AdminLoginPage />
              </GuestRoute>
            }
          />
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route
              path="/dashboard/anio-lectivo"
              element={<AcademicYearPage />}
            />
            <Route path="/dashboard/areas" element={<AreasPage />} />
            <Route path="/dashboard/plan-estudios" element={<StudyPlanPage />} />
            <Route path="/dashboard/restricciones" element={<RestrictionsPage />} />
            <Route path="/dashboard/asignacion" element={<AssignmentPage />} />
            <Route path="/dashboard/horario" element={<SchedulePage />} />
            <Route path="/dashboard/validaciones" element={<ValidationsPage />} />
            <Route path="/dashboard/publicacion" element={<PublicationPage />} />
            <Route path="/dashboard/docentes" element={<TeachersPage />} />
            <Route
              path="/dashboard/docentes/registrar"
              element={<RegisterTeacherPage />}
            />
            <Route
              path="/dashboard/docentes/:teacherId/editar"
              element={<EditTeacherPage />}
            />
            <Route
              path="/dashboard/docentes/:teacherId"
              element={<TeacherProfilePage />}
            />
          </Route>
          <Route path="/" element={<HomeRedirect />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

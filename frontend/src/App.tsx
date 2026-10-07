import { BrowserRouter, Route, Routes } from 'react-router-dom'
import {
  AuthProvider,
  GuestRoute,
  HomeRedirect,
  ProtectedRoute,
} from './auth'
import { AdminLoginPage } from './pages/AdminLoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { EditTeacherPage } from './pages/EditTeacherPage'
import { RegisterTeacherPage } from './pages/RegisterTeacherPage'
import { TeacherProfilePage } from './pages/TeacherProfilePage'
import { TeachersPage } from './pages/TeachersPage'

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

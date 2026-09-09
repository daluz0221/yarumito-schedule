import { AuthBrand } from '../../components/molecules/AuthBrand'
import { LoginForm } from '../../components/organisms/LoginForm'
import { AuthLayout } from '../../components/templates/AuthLayout'

export function AdminLoginPage() {
  return (
    <AuthLayout>
      <AuthBrand title="Bienvenidos" subtitle="Gestión de Horarios" />
      <LoginForm />
    </AuthLayout>
  )
}

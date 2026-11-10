import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError, login } from '../../../api'
import { useAuth } from '../../../auth'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import { TextInput } from '../../atoms/TextInput'
import { CheckboxField } from '../../molecules/CheckboxField'
import { FormField } from '../../molecules/FormField'
import { PasswordField } from '../../molecules/PasswordField'
import styles from './LoginForm.module.css'

export type LoginFormValues = {
  username: string
  password: string
  remember: boolean
}

export type LoginFormProps = {
  onSubmit?: (values: LoginFormValues) => void
}

export function LoginForm({ onSubmit }: LoginFormProps) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)
  const [error, setError] = useState('')
  const [authError, setAuthError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const navigate = useNavigate()
  const { acceptSession } = useAuth()

  const closeAuthError = () => setAuthError('')

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const values = {
      username: username.trim(),
      password,
      remember,
    }

    if (!values.username || !values.password) {
      setError('Ingresa tu correo y contraseña.')
      return
    }

    setError('')
    setAuthError('')
    setSubmitting(true)

    try {
      const result = await login(values)
      acceptSession(result.usuario)
      onSubmit?.(values)
      navigate('/dashboard')
    } catch (cause) {
      setAuthError(
        cause instanceof ApiError
          ? cause.message
          : 'No se pudo iniciar sesión. Inténtalo de nuevo.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <div className={styles.fields}>
        <FormField id="username" label="Usuario">
          <TextInput
            id="username"
            name="username"
            type="email"
            autoComplete="username"
            placeholder="Ingresa tu correo electrónico"
            value={username}
            disabled={submitting}
            onChange={(event) => setUsername(event.target.value)}
          />
        </FormField>

        <PasswordField
          id="password"
          name="password"
          label="Contraseña"
          placeholder="Ingresa tu contraseña"
          value={password}
          disabled={submitting}
          onChange={(event) => setPassword(event.target.value)}
        />

        <CheckboxField
          id="remember"
          name="remember"
          label="Recordarme"
          checked={remember}
          onChange={setRemember}
        />
      </div>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      <Button type="submit" fullWidth disabled={submitting}>
        {submitting ? 'Iniciando sesión...' : 'Iniciar sesión'}
      </Button>

      <Modal
        open={Boolean(authError)}
        title="Error de autenticación"
        description={authError}
        size="sm"
        onClose={closeAuthError}
        footer={
          <Button size="sm" onClick={closeAuthError}>
            Aceptar
          </Button>
        }
      />
    </form>
  )
}

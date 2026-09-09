import { useState, type FormEvent } from 'react'
import { Button } from '../../atoms/Button'
import { TextInput } from '../../atoms/TextInput'
import { CheckboxField } from '../../molecules/CheckboxField'
import { FormField } from '../../molecules/FormField'
import { PasswordField } from '../../molecules/PasswordField'
import styles from './LoginForm.module.css'
import { useNavigate } from 'react-router-dom'

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
  const navigate = useNavigate()

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onSubmit?.({ username, password, remember })
    navigate('/dashboard')
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
            onChange={(event) => setUsername(event.target.value)}
          />
        </FormField>

        <PasswordField
          id="password"
          name="password"
          label="Contraseña"
          placeholder="Ingresa tu contraseña"
          value={password}
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

      <Button type="submit" fullWidth>
        Iniciar sesión
      </Button>
    </form>
  )
}

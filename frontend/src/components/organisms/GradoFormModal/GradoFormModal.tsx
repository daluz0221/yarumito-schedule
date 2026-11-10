import { useState, type FormEvent } from 'react'
import { actualizarGrado, ApiError, registrarGrado } from '../../../api'
import type { GradoResponse } from '../../../api/grados'
import { Modal } from '../../atoms/Modal'
import { TextLink } from '../../atoms/TextLink'
import { ModalActions } from '../../molecules/ModalActions'
import { SelectField } from '../../molecules/SelectField'
import styles from './GradoFormModal.module.css'

export type GradoFormMode = 'create' | 'edit'

const NIVELES = [
  { nivel: 6, nombre: 'Sexto', esMedia: false, prioridad: 5, horas: 30 },
  { nivel: 7, nombre: 'Séptimo', esMedia: false, prioridad: 4, horas: 30 },
  { nivel: 8, nombre: 'Octavo', esMedia: false, prioridad: 3, horas: 30 },
  { nivel: 9, nombre: 'Noveno', esMedia: false, prioridad: 2, horas: 30 },
  { nivel: 10, nombre: 'Décimo', esMedia: true, prioridad: 1, horas: 37 },
  { nivel: 11, nombre: 'Undécimo', esMedia: true, prioridad: 1, horas: 37 },
] as const

const NIVEL_OPTIONS = [
  { value: '', label: 'Seleccione un grado' },
  ...NIVELES.map((item) => ({
    value: String(item.nivel),
    label: `${item.nivel}° ${item.nombre}`,
  })),
]

export type GradoFormModalProps = {
  open: boolean
  mode: GradoFormMode
  grados: GradoResponse[]
  grado?: GradoResponse
  loading?: boolean
  loadError?: string
  onRetry?: () => void
  onClose: () => void
  onCreated?: (grado: GradoResponse) => void
  onUpdated?: (grado: GradoResponse) => void
}

function nivelOcupado(grados: GradoResponse[], nivel: number, gradoId?: string) {
  return grados.some((grado) => grado.id !== gradoId && grado.nivel === nivel)
}

type GradoFormFieldsProps = {
  mode: GradoFormMode
  grados: GradoResponse[]
  grado?: GradoResponse
  onClose: () => void
  onCreated?: (grado: GradoResponse) => void
  onUpdated?: (grado: GradoResponse) => void
  onSubmittingChange: (submitting: boolean) => void
}

function GradoFormFields({
  mode,
  grados,
  grado,
  onClose,
  onCreated,
  onUpdated,
  onSubmittingChange,
}: GradoFormFieldsProps) {
  const [nivel, setNivel] = useState(mode === 'edit' && grado ? String(grado.nivel) : '')
  const [attempted, setAttempted] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const formId = `grado-${mode}-form`
  const creating = mode === 'create'
  const nivelNumero = Number(nivel)
  const perfil = NIVELES.find((item) => item.nivel === nivelNumero)
  const ocupado = perfil ? nivelOcupado(grados, perfil.nivel, grado?.id) : false

  const setBusy = (next: boolean) => {
    setSubmitting(next)
    onSubmittingChange(next)
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAttempted(true)
    setSubmitError('')

    if (submitting || !perfil || ocupado) {
      return
    }

    if (!creating && !grado?.id) {
      setSubmitError('No se encontró el grado a actualizar.')
      return
    }

    setBusy(true)

    try {
      if (creating) {
        const created = await registrarGrado({ nivel: perfil.nivel })
        onCreated?.(created)
      } else if (grado) {
        const updated = await actualizarGrado(grado.id, { nivel: perfil.nivel })
        onUpdated?.(updated)
      }

      onClose()
    } catch (cause) {
      setSubmitError(
        cause instanceof ApiError
          ? cause.message
          : creating
            ? 'No se pudo registrar el grado. Inténtalo de nuevo.'
            : 'No se pudo actualizar el grado. Inténtalo de nuevo.',
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <form id={formId} className={styles.form} onSubmit={handleSubmit}>
      <SelectField
        id={`${formId}-nivel`}
        label="Grado"
        name="nivel"
        required
        value={nivel}
        options={NIVEL_OPTIONS}
        disabled={submitting}
        error={
          attempted && !perfil
            ? 'Seleccione un grado.'
            : ocupado
              ? 'Ya existe un grado de ese nivel'
              : undefined
        }
        onChange={setNivel}
      />
      {perfil ? (
        <dl className={styles.preview}>
          <div>
            <dt>Nombre</dt>
            <dd>{perfil.nombre}</dd>
          </div>
          <div>
            <dt>Tipo</dt>
            <dd>{perfil.esMedia ? 'Media' : 'Básica secundaria'}</dd>
          </div>
          <div>
            <dt>Prioridad de asignación</dt>
            <dd>{perfil.prioridad}</dd>
          </div>
          <div>
            <dt>Horas semanales</dt>
            <dd>{perfil.horas}</dd>
          </div>
        </dl>
      ) : null}
      {submitError ? (
        <p className={styles.error} role="alert">
          {submitError}
        </p>
      ) : null}
      <p className={styles.hint}>
        El nombre, si es media, la prioridad y las horas semanales los calcula
        el servidor a partir del nivel.
      </p>
    </form>
  )
}

export function GradoFormModal({
  open,
  mode,
  grados,
  grado,
  loading = false,
  loadError = '',
  onRetry,
  onClose,
  onCreated,
  onUpdated,
}: GradoFormModalProps) {
  const [submitting, setSubmitting] = useState(false)
  const creating = mode === 'create'
  const showForm = !loading && !loadError && (creating || Boolean(grado))
  const formId = `grado-${mode}-form`

  const handleClose = () => {
    if (submitting) {
      return
    }

    onClose()
  }

  return (
    <Modal
      open={open}
      title={creating ? 'Nuevo grado' : 'Editar grado'}
      description={
        creating
          ? 'Registra un grado de 6° a 11°. El resto de datos se calcula solo.'
          : 'Cambia el nivel del grado. El servidor recalcula nombre, tipo, prioridad y horas.'
      }
      onClose={handleClose}
      footer={
        <ModalActions
          formId={formId}
          confirmLabel={
            loadError
              ? undefined
              : loading
                ? 'Cargando...'
                : submitting
                  ? 'Guardando...'
                  : creating
                    ? 'Guardar grado'
                    : 'Guardar cambios'
          }
          confirmType="submit"
          disabled={submitting || loading || Boolean(loadError)}
          onCancel={handleClose}
        />
      }
    >
      {loading ? (
        <p className={styles.status}>Cargando grado...</p>
      ) : loadError ? (
        <div className={styles.feedback}>
          <p className={styles.error} role="alert">
            {loadError}
          </p>
          {onRetry ? <TextLink onClick={onRetry}>Reintentar</TextLink> : null}
        </div>
      ) : showForm ? (
        <GradoFormFields
          key={mode === 'edit' ? grado?.id : 'create'}
          mode={mode}
          grados={grados}
          grado={grado}
          onClose={onClose}
          onCreated={onCreated}
          onUpdated={onUpdated}
          onSubmittingChange={setSubmitting}
        />
      ) : (
        <p className={styles.status}>Cargando grado...</p>
      )}
    </Modal>
  )
}

import { useCallback, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import {
  actualizarIdoneidad,
  ApiError,
  consultarDocente,
  finalizarVigenciaIdoneidad,
  listarAreas,
  listarIdoneidadesPorDocente,
  registrarIdoneidad,
} from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import type { Teacher } from '../../../content/teachers'
import {
  formatDocumentNumber,
  resolveAreaName,
  toActualizarIdoneidadRequest,
  toCrearIdoneidadRequest,
  toFinalizarVigenciaRequest,
  type CrearIdoneidadForm,
  type Idoneidad,
} from '../../../content/teacherProfile'
import { Text } from '../../atoms/Text'
import { TextLink } from '../../atoms/TextLink'
import { EndSuitabilityModal } from '../EndSuitabilityModal'
import { SuitabilityFormModal } from '../SuitabilityFormModal'
import { TeacherSuitabilitySection } from '../TeacherSuitabilitySection'
import { TeacherSummaryCard } from '../TeacherSummaryCard'
import { TeacherTitlesSection } from '../TeacherTitlesSection'
import styles from './TeacherProfileWorkspace.module.css'

type ProfileModal =
  | { name: 'closed' }
  | { name: 'addSuitability' }
  | { name: 'editSuitability'; item: Idoneidad }
  | { name: 'endSuitability'; item: Idoneidad }

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export function TeacherProfileWorkspace() {
  const { teacherId } = useParams()
  const [teacher, setTeacher] = useState<Teacher | null>(null)
  const [items, setItems] = useState<Idoneidad[]>([])
  const [areas, setAreas] = useState<AreaResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modal, setModal] = useState<ProfileModal>({ name: 'closed' })
  const [modalError, setModalError] = useState('')
  const [modalSubmitting, setModalSubmitting] = useState(false)

  const closeModal = useCallback(() => {
    if (modalSubmitting) {
      return
    }

    setModal({ name: 'closed' })
    setModalError('')
  }, [modalSubmitting])

  const loadProfile = useCallback(
    async (id: string, signal?: AbortSignal) => {
      const [nextTeacher, nextItems, nextAreas] = await Promise.all([
        consultarDocente(id, signal),
        listarIdoneidadesPorDocente(id, signal),
        listarAreas(signal),
      ])

      setTeacher(nextTeacher)
      setItems(nextItems)
      setAreas(nextAreas)
    },
    [],
  )

  useEffect(() => {
    if (!teacherId) {
      setTeacher(null)
      setItems([])
      setError('No se indicó el docente.')
      setLoading(false)
      return
    }

    const controller = new AbortController()

    setLoading(true)
    setError('')

    loadProfile(teacherId, controller.signal)
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setTeacher(null)
        setItems([])
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudo cargar el perfil del docente.',
        )
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })

    return () => controller.abort()
  }, [loadProfile, teacherId])

  const refreshIdoneidades = async () => {
    if (!teacherId) {
      return
    }

    setItems(await listarIdoneidadesPorDocente(teacherId))
  }

  const handleSaveIdoneidad = async (values: CrearIdoneidadForm) => {
    if (!teacherId) {
      return
    }

    setModalSubmitting(true)
    setModalError('')

    try {
      if (modal.name === 'editSuitability') {
        await actualizarIdoneidad(
          modal.item.id,
          toActualizarIdoneidadRequest(values),
        )
      } else {
        await registrarIdoneidad(toCrearIdoneidadRequest(teacherId, values))
      }

      await refreshIdoneidades()
      setModal({ name: 'closed' })
    } catch (cause) {
      setModalError(
        cause instanceof ApiError
          ? cause.message
          : 'No se pudo guardar la idoneidad.',
      )
    } finally {
      setModalSubmitting(false)
    }
  }

  const handleEndIdoneidad = async (vigenteHasta: string) => {
    if (modal.name !== 'endSuitability') {
      return
    }

    setModalSubmitting(true)
    setModalError('')

    try {
      await finalizarVigenciaIdoneidad(
        modal.item.id,
        toFinalizarVigenciaRequest(vigenteHasta),
      )
      await refreshIdoneidades()
      setModal({ name: 'closed' })
    } catch (cause) {
      setModalError(
        cause instanceof ApiError
          ? cause.message
          : 'No se pudo finalizar la vigencia.',
      )
    } finally {
      setModalSubmitting(false)
    }
  }

  const documentLabel = teacher
    ? `${teacher.tipoDocumento} ${formatDocumentNumber(teacher.numeroDocumento)}`
    : ''
  const areaOptions = areas.map((area) => ({
    value: area.id,
    label: area.nombre,
  }))

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text variant="sectionTitle">Perfil académico del docente</Text>
        <Text variant="body">
          Consulte los datos del docente y gestione las idoneidades académicas.
          Los títulos profesionales quedan pendientes.
        </Text>
      </div>

      {loading ? (
        <p className={styles.status}>Cargando perfil...</p>
      ) : error || !teacher ? (
        <p className={styles.error} role="alert">
          {error || 'El docente no existe.'}{' '}
          <TextLink to="/dashboard/docentes">Volver al listado</TextLink>
        </p>
      ) : (
        <>
          <TeacherSummaryCard
            teacher={teacher}
            documentLabel={documentLabel}
            areaNombre={resolveAreaName(teacher.areaNombramientoId, areas)}
          />
          <TeacherTitlesSection />
          <TeacherSuitabilitySection
            items={items}
            areaNameById={(areaId) => resolveAreaName(areaId, areas)}
            onAdd={() => {
              setModalError('')
              setModal({ name: 'addSuitability' })
            }}
            onEdit={(item) => {
              setModalError('')
              setModal({ name: 'editSuitability', item })
            }}
            onEnd={(item) => {
              setModalError('')
              setModal({ name: 'endSuitability', item })
            }}
          />
        </>
      )}

      <SuitabilityFormModal
        open={modal.name === 'addSuitability' || modal.name === 'editSuitability'}
        mode={modal.name === 'editSuitability' ? 'edit' : 'add'}
        item={modal.name === 'editSuitability' ? modal.item : undefined}
        areaOptions={areaOptions}
        submitting={modalSubmitting}
        error={modalError}
        onClose={closeModal}
        onSubmit={handleSaveIdoneidad}
      />
      <EndSuitabilityModal
        open={modal.name === 'endSuitability'}
        item={modal.name === 'endSuitability' ? modal.item : undefined}
        areaNombre={
          modal.name === 'endSuitability'
            ? resolveAreaName(modal.item.areaId, areas)
            : undefined
        }
        submitting={modalSubmitting}
        error={modalError}
        onClose={closeModal}
        onSubmit={handleEndIdoneidad}
      />
    </section>
  )
}

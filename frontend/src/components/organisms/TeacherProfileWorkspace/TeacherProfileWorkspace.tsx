import { useCallback, useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import {
  actualizarIdoneidad,
  ApiError,
  consultarAsignatura,
  consultarDocente,
  consultarIdoneidad,
  finalizarVigenciaIdoneidad,
  listarAreas,
  listarIdoneidadesPorDocente,
  actualizarTituloProfesional,
  consultarTituloProfesional,
  listarTitulosPorDocente,
  registrarIdoneidad,
  registrarTituloProfesional,
} from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import type { Teacher } from '../../../content/teachers'
import {
  formatDocumentNumber,
  resolveAreaName,
  toActualizarIdoneidadRequest,
  toCrearIdoneidadRequest,
  toActualizarTituloProfesionalRequest,
  toCrearTituloProfesionalRequest,
  toFinalizarVigenciaRequest,
  type CrearIdoneidadForm,
  type CrearTituloForm,
  type Idoneidad,
  type ProfessionalTitle,
} from '../../../content/teacherProfile'
import { Text } from '../../atoms/Text'
import { TextLink } from '../../atoms/TextLink'
import { EndSuitabilityModal } from '../EndSuitabilityModal'
import { SuitabilityFormModal } from '../SuitabilityFormModal'
import { TitleFormModal } from '../TitleFormModal'
import { TeacherSuitabilitySection } from '../TeacherSuitabilitySection'
import { TeacherSummaryCard } from '../TeacherSummaryCard'
import { TeacherTitlesSection } from '../TeacherTitlesSection'
import styles from './TeacherProfileWorkspace.module.css'

type ProfileModal =
  | { name: 'closed' }
  | { name: 'addTitle' }
  | { name: 'editTitle'; id: string }
  | { name: 'addSuitability' }
  | { name: 'editSuitability'; id: string }
  | { name: 'endSuitability'; item: Idoneidad }

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function titlesErrorMessage(cause: unknown) {
  return cause instanceof ApiError
    ? cause.message
    : 'No se pudieron cargar los títulos profesionales.'
}

function titleDetailErrorMessage(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Este título ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  if (cause instanceof Error && cause.message) {
    return cause.message
  }

  return 'No se pudo cargar el título profesional.'
}

function titleSaveErrorMessage(
  cause: unknown,
  action: 'registrar' | 'actualizar',
) {
  if (cause instanceof ApiError) {
    if (cause.status === 404 && action === 'actualizar') {
      return 'Este título ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    if (
      cause.status === 400 &&
      cause.message === 'No se pudo completar la solicitud'
    ) {
      return `No se pudo ${action} el título. Revisa el nivel, el nombre y el año de graduación.`
    }

    return cause.message
  }

  if (cause instanceof Error && cause.message) {
    return cause.message
  }

  return 'No se pudo guardar el título profesional.'
}

function idoneidadDetailErrorMessage(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Esta idoneidad ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  if (cause instanceof Error && cause.message) {
    return cause.message
  }

  return 'No se pudo cargar la idoneidad.'
}

function idoneidadSaveErrorMessage(
  cause: unknown,
  action: 'registrar' | 'actualizar',
) {
  if (cause instanceof ApiError) {
    if (cause.status === 404 && action === 'actualizar') {
      return 'Esta idoneidad ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  if (cause instanceof Error && cause.message) {
    return cause.message
  }

  return 'No se pudo guardar la idoneidad.'
}

export function TeacherProfileWorkspace() {
  const { teacherId } = useParams()
  const [teacher, setTeacher] = useState<Teacher | null>(null)
  const [items, setItems] = useState<Idoneidad[]>([])
  const [titles, setTitles] = useState<ProfessionalTitle[]>([])
  const [titlesError, setTitlesError] = useState('')
  const [titlesLoading, setTitlesLoading] = useState(false)
  const [areas, setAreas] = useState<AreaResponse[]>([])
  const [subjectNames, setSubjectNames] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modal, setModal] = useState<ProfileModal>({ name: 'closed' })
  const [editingTitle, setEditingTitle] = useState<ProfessionalTitle | null>(null)
  const [titleDetailLoading, setTitleDetailLoading] = useState(false)
  const [titleDetailError, setTitleDetailError] = useState('')
  const [editingIdoneidad, setEditingIdoneidad] = useState<Idoneidad | null>(null)
  const [idoneidadDetailLoading, setIdoneidadDetailLoading] = useState(false)
  const [idoneidadDetailError, setIdoneidadDetailError] = useState('')
  const [modalError, setModalError] = useState('')
  const [modalSubmitting, setModalSubmitting] = useState(false)
  const titleDetailRequest = useRef<AbortController | null>(null)
  const idoneidadDetailRequest = useRef<AbortController | null>(null)

  const closeModal = useCallback(() => {
    if (modalSubmitting) {
      return
    }

    titleDetailRequest.current?.abort()
    titleDetailRequest.current = null
    idoneidadDetailRequest.current?.abort()
    idoneidadDetailRequest.current = null
    setModal({ name: 'closed' })
    setEditingTitle(null)
    setTitleDetailLoading(false)
    setTitleDetailError('')
    setEditingIdoneidad(null)
    setIdoneidadDetailLoading(false)
    setIdoneidadDetailError('')
    setModalError('')
  }, [modalSubmitting])

  const loadProfile = useCallback(
    async (id: string, signal?: AbortSignal) => {
      const [nextTeacher, nextItems, nextAreas, nextTitles] = await Promise.all([
        consultarDocente(id, signal),
        listarIdoneidadesPorDocente(id, signal),
        listarAreas(signal),
        listarTitulosPorDocente(id, signal).then(
          (loaded) => ({ items: loaded, error: '' }),
          (cause: unknown) => {
            if (isAbortError(cause)) {
              throw cause
            }

            return {
              items: [] as ProfessionalTitle[],
              error: titlesErrorMessage(cause),
            }
          },
        ),
      ])

      setTeacher(nextTeacher)
      setItems(nextItems)
      setAreas(nextAreas)
      setTitles(nextTitles.items)
      setTitlesError(nextTitles.error)
    },
    [],
  )

  useEffect(() => {
    if (!teacherId) {
      setTeacher(null)
      setItems([])
      setTitles([])
      setTitlesError('')
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
        setTitles([])
        setTitlesError('')
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

  useEffect(() => {
    const ids = [
      ...new Set(items.map((item) => item.asignaturaId).filter(Boolean)),
    ]

    if (ids.length === 0) {
      return
    }

    const controller = new AbortController()

    void Promise.all(
      ids.map(async (id) => {
        try {
          const subject = await consultarAsignatura(id, controller.signal)
          return [id, subject.nombre.trim() || 'Asignatura sin nombre'] as const
        } catch (cause) {
          if (isAbortError(cause)) {
            throw cause
          }

          return [id, 'Asignatura no disponible'] as const
        }
      }),
    )
      .then((entries) => {
        if (controller.signal.aborted) {
          return
        }

        setSubjectNames((current) => {
          const next = { ...current }

          for (const [id, name] of entries) {
            next[id] = name
          }

          return next
        })
      })
      .catch((cause: unknown) => {
        if (isAbortError(cause)) {
          return
        }
      })

    return () => controller.abort()
  }, [items])

  const loadTitleDetail = useCallback(
    async (id: string) => {
      titleDetailRequest.current?.abort()
      const controller = new AbortController()
      titleDetailRequest.current = controller

      setTitleDetailLoading(true)
      setTitleDetailError('')
      setEditingTitle(null)
      setModalError('')

      try {
        const title = await consultarTituloProfesional(id, controller.signal)

        if (controller.signal.aborted) {
          return
        }

        setEditingTitle(title)
        setTitles((current) =>
          current.map((item) => (item.id === title.id ? title : item)),
        )
      } catch (cause) {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditingTitle(null)
        setTitleDetailError(titleDetailErrorMessage(cause))

        if (cause instanceof ApiError && cause.status === 404 && teacherId) {
          try {
            setTitles(await listarTitulosPorDocente(teacherId))
            setTitlesError('')
          } catch (refreshCause) {
            if (!isAbortError(refreshCause)) {
              setTitlesError(titlesErrorMessage(refreshCause))
            }
          }
        }
      } finally {
        if (!controller.signal.aborted) {
          setTitleDetailLoading(false)
        }
      }
    },
    [teacherId],
  )

  const loadIdoneidadDetail = useCallback(
    async (id: string) => {
      idoneidadDetailRequest.current?.abort()
      const controller = new AbortController()
      idoneidadDetailRequest.current = controller

      setIdoneidadDetailLoading(true)
      setIdoneidadDetailError('')
      setEditingIdoneidad(null)
      setModalError('')

      try {
        const idoneidad = await consultarIdoneidad(id, controller.signal)

        if (controller.signal.aborted) {
          return
        }

        setEditingIdoneidad(idoneidad)
        setItems((current) =>
          current.map((item) => (item.id === idoneidad.id ? idoneidad : item)),
        )
      } catch (cause) {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditingIdoneidad(null)
        setIdoneidadDetailError(idoneidadDetailErrorMessage(cause))

        if (cause instanceof ApiError && cause.status === 404 && teacherId) {
          try {
            setItems(await listarIdoneidadesPorDocente(teacherId))
          } catch (refreshCause) {
            if (!isAbortError(refreshCause)) {
              setIdoneidadDetailError(
                `${idoneidadDetailErrorMessage(cause)} No se pudo actualizar el listado.`,
              )
            }
          }
        }
      } finally {
        if (!controller.signal.aborted) {
          setIdoneidadDetailLoading(false)
        }
      }
    },
    [teacherId],
  )

  useEffect(() => {
    return () => {
      titleDetailRequest.current?.abort()
      idoneidadDetailRequest.current?.abort()
    }
  }, [])

  const retryTitles = async () => {
    if (!teacherId || titlesLoading) {
      return
    }

    setTitlesLoading(true)
    setTitlesError('')

    try {
      setTitles(await listarTitulosPorDocente(teacherId))
    } catch (cause) {
      setTitles([])
      setTitlesError(titlesErrorMessage(cause))
    } finally {
      setTitlesLoading(false)
    }
  }

  const handleSaveTitulo = async (values: CrearTituloForm) => {
    if (!teacherId || (modal.name !== 'addTitle' && modal.name !== 'editTitle')) {
      return
    }

    const editing = modal.name === 'editTitle' ? editingTitle : null

    if (modal.name === 'editTitle' && !editing) {
      return
    }

    setModalSubmitting(true)
    setModalError('')

    try {
      const saved = editing
        ? await actualizarTituloProfesional(
            editing.id,
            toActualizarTituloProfesionalRequest(values),
          )
        : await registrarTituloProfesional(
            toCrearTituloProfesionalRequest(teacherId, values),
          )

      titleDetailRequest.current = null
      setEditingTitle(null)
      setTitleDetailLoading(false)
      setTitleDetailError('')
      setModal({ name: 'closed' })

      try {
        setTitles(await listarTitulosPorDocente(teacherId))
        setTitlesError('')
      } catch (cause) {
        setTitles((current) => {
          const exists = current.some((item) => item.id === saved.id)

          if (!exists) {
            return [...current, saved]
          }

          return current.map((item) => (item.id === saved.id ? saved : item))
        })
        setTitlesError(
          `${titlesErrorMessage(cause)} El título se guardó y puedes reintentar la carga del listado.`,
        )
      }
    } catch (cause) {
      setModalError(
        titleSaveErrorMessage(cause, editing ? 'actualizar' : 'registrar'),
      )

      if (
        editing &&
        cause instanceof ApiError &&
        cause.status === 404
      ) {
        try {
          setTitles(await listarTitulosPorDocente(teacherId))
          setTitlesError('')
        } catch (refreshCause) {
          setTitlesError(titlesErrorMessage(refreshCause))
        }
      }
    } finally {
      setModalSubmitting(false)
    }
  }

  const refreshIdoneidades = async () => {
    if (!teacherId) {
      return
    }

    setItems(await listarIdoneidadesPorDocente(teacherId))
  }

  const handleSaveIdoneidad = async (values: CrearIdoneidadForm) => {
    if (!teacherId || (modal.name !== 'addSuitability' && modal.name !== 'editSuitability')) {
      return
    }

    const editing = modal.name === 'editSuitability' ? editingIdoneidad : null

    if (modal.name === 'editSuitability' && !editing) {
      return
    }

    setModalSubmitting(true)
    setModalError('')

    try {
      const saved = editing
        ? await actualizarIdoneidad(
            editing.id,
            toActualizarIdoneidadRequest(values),
          )
        : await registrarIdoneidad(toCrearIdoneidadRequest(teacherId, values))

      idoneidadDetailRequest.current = null
      setEditingIdoneidad(null)
      setIdoneidadDetailLoading(false)
      setIdoneidadDetailError('')
      setModal({ name: 'closed' })

      try {
        await refreshIdoneidades()
      } catch {
        setItems((current) => {
          const exists = current.some((item) => item.id === saved.id)

          if (!exists) {
            return [...current, saved]
          }

          return current.map((item) => (item.id === saved.id ? saved : item))
        })
      }
    } catch (cause) {
      setModalError(
        idoneidadSaveErrorMessage(cause, editing ? 'actualizar' : 'registrar'),
      )

      if (editing && cause instanceof ApiError && cause.status === 404) {
        try {
          setItems(await listarIdoneidadesPorDocente(teacherId))
        } catch {
          // El mensaje del modal ya explica que la idoneidad no está disponible.
        }
      }
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
          Consulte los datos del docente, sus títulos profesionales y gestione
          las idoneidades académicas.
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
          <TeacherTitlesSection
            items={titles}
            loading={titlesLoading}
            error={titlesError}
            onRetry={retryTitles}
            onAdd={() => {
              setModalError('')
              setModal({ name: 'addTitle' })
            }}
            onEdit={(item) => {
              setModalError('')
              setModal({ name: 'editTitle', id: item.id })
              void loadTitleDetail(item.id)
            }}
          />
          <TeacherSuitabilitySection
            items={items}
            titles={titles}
            titlesUnavailable={Boolean(titlesError)}
            areaNameById={(areaId) => resolveAreaName(areaId, areas)}
            subjectNameById={(asignaturaId) => subjectNames[asignaturaId] ?? ''}
            onAdd={() => {
              setModalError('')
              setModal({ name: 'addSuitability' })
            }}
            onEdit={(item) => {
              setModalError('')
              setModal({ name: 'editSuitability', id: item.id })
              void loadIdoneidadDetail(item.id)
            }}
            onEnd={(item) => {
              setModalError('')
              setModal({ name: 'endSuitability', item })
            }}
          />
        </>
      )}

      <TitleFormModal
        open={modal.name === 'addTitle' || modal.name === 'editTitle'}
        mode={modal.name === 'editTitle' ? 'edit' : 'add'}
        title={modal.name === 'editTitle' ? editingTitle ?? undefined : undefined}
        loading={modal.name === 'editTitle' && titleDetailLoading}
        loadError={modal.name === 'editTitle' ? titleDetailError : ''}
        submitting={modalSubmitting}
        error={modalError}
        onClose={closeModal}
        onRetry={
          modal.name === 'editTitle'
            ? () => {
                void loadTitleDetail(modal.id)
              }
            : undefined
        }
        onSubmit={handleSaveTitulo}
      />
      <SuitabilityFormModal
        open={modal.name === 'addSuitability' || modal.name === 'editSuitability'}
        mode={modal.name === 'editSuitability' ? 'edit' : 'add'}
        item={modal.name === 'editSuitability' ? editingIdoneidad ?? undefined : undefined}
        areaOptions={areaOptions}
        titles={titles}
        titlesError={titlesError}
        loading={modal.name === 'editSuitability' && idoneidadDetailLoading}
        loadError={modal.name === 'editSuitability' ? idoneidadDetailError : ''}
        submitting={modalSubmitting}
        error={modalError}
        onClose={closeModal}
        onRetry={
          modal.name === 'editSuitability'
            ? () => {
                void loadIdoneidadDetail(modal.id)
              }
            : undefined
        }
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

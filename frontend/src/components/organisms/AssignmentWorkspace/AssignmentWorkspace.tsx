import { useEffect, useMemo, useState } from 'react'
import {
  anioEscolarVisible,
  ApiError,
  confirmarAsignacion,
  consultarAsignacion,
  etiquetaGrado,
  listarAreas,
  listarAsignaturas,
  listarBorradores,
  listarAsignacionesPorGrupo,
  asignarActividadDocente,
  listarActividadesDocente,
  listarDocentes,
  listarPlanesEstudio,
  listarRestriccionesPorAnio,
  opcionesAniosEscolares,
  actualizarAsignacion,
  registrarAsignacion,
  regresarAsignacionABorrador,
  retirarAsignacion,
  useCatalogoAniosEscolares,
  useCatalogoGrupos,
} from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import type { AsignacionActividadResponse } from '../../../api/actividadesDocente'
import type { AsignacionAcademicaResponse } from '../../../api/asignaciones'
import type { PlanEstudiosResponse } from '../../../api/planesEstudio'
import {
  assignmentPriorityNote,
  assignmentPrioritySteps,
  assignmentTabs,
  filasActividadesInstitucionales,
  mergeCurricularAssignments,
  summarizeRows,
  type AssignmentRow,
  type AssignmentTab,
  type CurricularAssignmentSource,
} from '../../../content/academicAssignment'
import type { AcademicYearStatus } from '../../../content/academicYear'
import { useAnioDeTrabajo } from '../../../content/anioDeTrabajo'
import { idDisponible } from '../../../content/idDisponible'
import { formatTeacherName, type Teacher } from '../../../content/teachers'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { ActivityAssignmentFormModal, type ActivityAssignmentFormValues } from '../ActivityAssignmentFormModal'
import { AssignmentFormModal, type AssignmentFormValues } from '../AssignmentFormModal'
import { ConfirmActionModal } from '../ConfirmActionModal'
import { AssignmentSection } from '../AssignmentSection'
import { AssignmentSelector } from '../AssignmentSelector'
import { AssignmentSummary } from '../AssignmentSummary'
import { PlanSubjectPickerModal } from '../PlanSubjectPickerModal'
import styles from './AssignmentWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeError(cause: unknown, fallback: string) {
  return cause instanceof ApiError && cause.message ? cause.message : fallback
}

type Dialog =
  | { name: 'pick-subject' }
  | { name: 'create'; row?: AssignmentRow }
  | { name: 'create-activity'; row?: AssignmentRow }
  | { name: 'edit'; row: AssignmentRow }
  | { name: 'remove'; row: AssignmentRow }
  | { name: 'revert'; row: AssignmentRow }

type Catalog = {
  teachers: Teacher[]
  areas: AreaResponse[]
  error: string
}

function materiasDelGrado(
  planes: PlanEstudiosResponse[],
  yearId: string,
  gradoId: string,
  areaPorAsignatura: ReadonlyMap<string, string>,
) {
  const grouped = new Map<
    string,
    { key: string; area: string; subject: string; requiredHours: number }
  >()

  planes
    .filter((plan) => plan.anioEscolarId === yearId && plan.gradoId === gradoId)
    .forEach((plan) => {
      const current = grouped.get(plan.asignaturaId)

      if (!current) {
        grouped.set(plan.asignaturaId, {
          key: plan.asignaturaId,
          area: areaPorAsignatura.get(plan.asignaturaId)?.trim() || '—',
          subject: plan.asignaturaNombre?.trim() || 'Asignatura',
          requiredHours: plan.horasSemanales,
        })
        return
      }

      current.requiredHours += plan.horasSemanales
    })

  return [...grouped.values()]
}

function fuenteAsignacion(item: AsignacionAcademicaResponse): CurricularAssignmentSource {
  return {
    id: item.id,
    asignaturaId: item.asignaturaId,
    asignaturaNombre: item.asignaturaNombre,
    docenteId: item.docenteId,
    docenteNombre: item.docenteNombre,
    horasSemanales: item.horasSemanales,
    observacion: item.observacion,
    estado: item.estado,
  }
}

export function AssignmentWorkspace() {
  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares()
  const { grupos, error: gruposError, loading: gruposLoading } = useCatalogoGrupos()
  const [yearElegido, elegirAnio] = useAnioDeTrabajo()
  const [groupElegido, setGroupElegido] = useState('')
  const [tab, setTab] = useState<AssignmentTab>('curricular')
  const [actividadesCarga, setActividadesCarga] = useState<{
    clave: string
    items: AsignacionActividadResponse[]
    error: string
  } | null>(null)
  const [asignacionesCarga, setAsignacionesCarga] = useState<{
    clave: string
    items: AsignacionAcademicaResponse[]
    error: string
  } | null>(null)
  const [borradores, setBorradores] = useState<AsignacionAcademicaResponse[] | null>(null)
  const [borradoresError, setBorradoresError] = useState('')
  const [planes, setPlanes] = useState<PlanEstudiosResponse[] | null>(null)
  const [planesError, setPlanesError] = useState('')
  const [areaPorAsignatura, setAreaPorAsignatura] = useState<ReadonlyMap<string, string>>(
    () => new Map(),
  )
  const [catalog, setCatalog] = useState<Catalog | null>(null)
  const [dialog, setDialog] = useState<Dialog | null>(null)
  const [saveError, setSaveError] = useState('')
  const [saving, setSaving] = useState(false)
  const [confirmingId, setConfirmingId] = useState<string | null>(null)
  const [actionError, setActionError] = useState('')
  const [revision, setRevision] = useState(0)
  const [conteoCarga, setConteoCarga] = useState<{
    yearId: string
    counts: Record<string, number>
  } | null>(null)
  const yearId = idDisponible(
    yearElegido,
    anios,
    Boolean(anios?.length),
    anioEscolarVisible(anios ?? [])?.id ?? '',
  )
  const conteoRestricciones = conteoCarga?.yearId === yearId ? conteoCarga.counts : {}

  useEffect(() => {
    if (!yearId) {
      return
    }

    const controller = new AbortController()
    const solicitado = yearId

    listarRestriccionesPorAnio(solicitado, controller.signal)
      .then((items) => {
        if (controller.signal.aborted) {
          return
        }

        const counts: Record<string, number> = {}
        items.forEach((item) => {
          counts[item.docenteId] = (counts[item.docenteId] ?? 0) + 1
        })
        setConteoCarga({ yearId: solicitado, counts })
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setConteoCarga({ yearId: solicitado, counts: {} })
      })

    return () => controller.abort()
  }, [yearId])

  const yearGroups = useMemo(() => {
    return (grupos ?? [])
      .filter((group) => group.anioEscolarId === yearId && group.activo)
      .sort(
        (left, right) =>
          left.gradoNivel - right.gradoNivel || left.codigo.localeCompare(right.codigo, 'es'),
      )
  }, [grupos, yearId])
  const groupId = idDisponible(
    groupElegido,
    yearGroups,
    Boolean(yearId) && grupos !== null,
    yearGroups[0]?.id ?? '',
  )

  useEffect(() => {
    const controller = new AbortController()

    listarPlanesEstudio(controller.signal)
      .then((items) => {
        setPlanes(items)
        setPlanesError('')
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setPlanes([])
        setPlanesError(
          mensajeError(cause, 'No se pudo cargar el plan de estudios de este grado.'),
        )
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()

    Promise.all([
      listarDocentes({ page: 0, size: 100, estado: 'ACTIVO' }, controller.signal),
      listarAreas(controller.signal),
    ])
      .then(async ([teachersPage, areas]) => {
        if (controller.signal.aborted) {
          return
        }

        const teachers = [...teachersPage.content].sort((left, right) =>
          formatTeacherName(left).localeCompare(formatTeacherName(right), 'es'),
        )
        setCatalog({ teachers, areas, error: '' })

        try {
          const groups = await Promise.all(
            areas.map((area) =>
              listarAsignaturas(area.id, controller.signal).then((subjects) =>
                subjects.map((subject) => [subject.id, area.nombre] as const),
              ),
            ),
          )

          if (!controller.signal.aborted) {
            setAreaPorAsignatura(new Map(groups.flat()))
          }
        } catch (cause) {
          if (!isAbortError(cause) && !controller.signal.aborted) {
            setAreaPorAsignatura(new Map())
          }
        }
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setCatalog({
          teachers: [],
          areas: [],
          error: mensajeError(cause, 'No se pudieron cargar los docentes.'),
        })
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()

    listarBorradores(controller.signal)
      .then((items) => {
        setBorradores(items)
        setBorradoresError('')
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setBorradores([])
        setBorradoresError(
          mensajeError(cause, 'No se pudieron cargar los borradores de asignación.'),
        )
      })

    return () => controller.abort()
  }, [revision])

  const asignacionesClave = groupId ? `${groupId}:${revision}` : ''

  useEffect(() => {
    if (!groupId) {
      return
    }

    const controller = new AbortController()
    const clave = `${groupId}:${revision}`

    listarAsignacionesPorGrupo(groupId, controller.signal)
      .then((items) => {
        setAsignacionesCarga({ clave, items, error: '' })
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setAsignacionesCarga({
          clave,
          items: [],
          error: mensajeError(cause, 'No se pudieron cargar las asignaciones de este grupo.'),
        })
      })

    return () => controller.abort()
  }, [groupId, revision])

  const actividadesClave =
    catalog && catalog.teachers.length > 0
      ? `${catalog.teachers.map((teacher) => teacher.id).join(',')}:${revision}`
      : ''

  useEffect(() => {
    if (!catalog || catalog.teachers.length === 0) {
      return
    }

    const controller = new AbortController()
    const clave = `${catalog.teachers.map((teacher) => teacher.id).join(',')}:${revision}`

    Promise.all(
      catalog.teachers.map(async (teacher) => {
        try {
          return await listarActividadesDocente(teacher.id, controller.signal)
        } catch (cause) {
          if (isAbortError(cause) || controller.signal.aborted) {
            throw cause
          }

          return null
        }
      }),
    )
      .then((groups) => {
        if (controller.signal.aborted) {
          return
        }

        const failed = groups.some((groupItems) => groupItems === null)
        setActividadesCarga({
          clave,
          items: groups.flatMap((groupItems) => groupItems ?? []),
          error: failed ? 'No se pudieron cargar las actividades de todos los docentes.' : '',
        })
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setActividadesCarga({
          clave,
          items: [],
          error: mensajeError(cause, 'No se pudieron cargar las actividades de los docentes.'),
        })
      })

    return () => controller.abort()
  }, [catalog, revision])

  const year = anios?.find((item) => item.id === yearId) ?? null
  const group = yearGroups.find((item) => item.id === groupId) ?? null
  const yearStatus: AcademicYearStatus = year?.status ?? 'PLANEACION'
  const gradeLabel = group ? etiquetaGrado(group.gradoNombre) : ''
  const readOnly = yearStatus === 'CERRADO'
  const teachers = catalog?.teachers ?? []
  const areas = catalog?.areas ?? []
  const asignacionesGrupo = !groupId
    ? []
    : asignacionesCarga?.clave === asignacionesClave
      ? asignacionesCarga.items
      : null
  const asignacionesError =
    groupId && asignacionesCarga?.clave === asignacionesClave ? asignacionesCarga.error : ''
  const actividadesDocentes = !catalog
    ? null
    : catalog.teachers.length === 0
      ? []
      : actividadesCarga?.clave === actividadesClave
        ? actividadesCarga.items
        : null
  const actividadesError =
    catalog && actividadesCarga?.clave === actividadesClave ? actividadesCarga.error : ''
  const subjects = group
    ? materiasDelGrado(planes ?? [], yearId, group.gradoId, areaPorAsignatura)
    : []
  const curricularRows =
    planes !== null && asignacionesGrupo !== null
      ? mergeCurricularAssignments(subjects, asignacionesGrupo.map(fuenteAsignacion))
      : []
  const activityOptions = filasActividadesInstitucionales([])
  const activityRows =
    actividadesDocentes === null
      ? []
      : filasActividadesInstitucionales(actividadesDocentes)
  const visibleRows = tab === 'curricular' ? curricularRows : activityRows
  const summary = summarizeRows(curricularRows)
  const pendingSubjects = curricularRows.filter((row) => !row.assignmentId)
  const borradoresGrupo = (borradores ?? []).filter((item) => item.grupoId === group?.id)
  const curricularLista = planes !== null && asignacionesGrupo !== null
  const canCreate =
    !readOnly &&
    (tab === 'curricular'
      ? Boolean(group) && curricularLista && pendingSubjects.length > 0 && !planesError
      : teachers.length > 0 && actividadesDocentes !== null)
  const yearOptions = opcionesAniosEscolares(anios, aniosError)
  const groupOptions = gruposLoading
    ? [{ value: '', label: 'Cargando grupos…' }]
    : gruposError
      ? [{ value: '', label: 'Grupos no disponibles' }]
      : yearGroups.map((item) => ({ value: item.id, label: item.codigo }))

  const closeDialog = () => {
    if (saving) {
      return
    }

    setDialog(null)
    setSaveError('')
  }

  const selectYear = (nextYearId: string) => {
    elegirAnio(nextYearId)
    setDialog(null)
    setActionError('')
  }

  const upsertAssignment = async (values: AssignmentFormValues) => {
    if (!group || !year) {
      return
    }

    if (!values.teacherId) {
      setSaveError('Selecciona un docente registrado.')
      return
    }

    setSaving(true)
    setSaveError('')

    try {
      await registrarAsignacion({
        anioEscolarId: year.id,
        docenteId: values.teacherId,
        grupoId: group.id,
        asignaturaId: values.subjectKey,
        horasSemanales: values.assignedHours,
        observacion: values.note.trim() || null,
      })
      setRevision((current) => current + 1)
      setDialog(null)
      setSaveError('')
      setActionError('')
    } catch (cause) {
      setSaveError(mensajeError(cause, 'No se pudo guardar la asignación académica.'))
    } finally {
      setSaving(false)
    }
  }

  // H-04: corrige una asignación en BORRADOR (docente, horas y observación).
  const updateAssignment = async (values: AssignmentFormValues) => {
    if (dialog?.name !== 'edit' || !dialog.row.assignmentId) {
      return
    }

    if (!values.teacherId) {
      setSaveError('Selecciona un docente registrado.')
      return
    }

    setSaving(true)
    setSaveError('')

    try {
      await actualizarAsignacion(dialog.row.assignmentId, {
        docenteId: values.teacherId,
        horasSemanales: values.assignedHours,
        observacion: values.note.trim() || null,
      })
      setRevision((current) => current + 1)
      setDialog(null)
      setActionError('')
    } catch (cause) {
      setSaveError(mensajeError(cause, 'No se pudo actualizar la asignación académica.'))
    } finally {
      setSaving(false)
    }
  }

  // H-04: retira una asignación en BORRADOR sin clases programadas.
  const removeAssignment = async () => {
    if (dialog?.name !== 'remove' || !dialog.row.assignmentId) {
      return
    }

    setSaving(true)
    setSaveError('')

    try {
      await retirarAsignacion(dialog.row.assignmentId)
      setRevision((current) => current + 1)
      setDialog(null)
      setActionError('')
    } catch (cause) {
      setSaveError(mensajeError(cause, 'No se pudo quitar la asignación académica.'))
    } finally {
      setSaving(false)
    }
  }

  const revertAssignment = async () => {
    if (dialog?.name !== 'revert' || !dialog.row.assignmentId) {
      return
    }

    setSaving(true)
    setSaveError('')

    try {
      await regresarAsignacionABorrador(dialog.row.assignmentId)
      setRevision((current) => current + 1)
      setDialog(null)
      setActionError('')
    } catch (cause) {
      setSaveError(mensajeError(cause, 'No se pudo devolver la asignación a borrador.'))
    } finally {
      setSaving(false)
    }
  }

  const confirmAssignment = async (row: AssignmentRow) => {
    if (!row.assignmentId || confirmingId) {
      return
    }

    setConfirmingId(row.assignmentId)
    setActionError('')

    try {
      const actual = await consultarAsignacion(row.assignmentId)

      if (actual.estado !== 'BORRADOR') {
        setActionError('La asignación ya no está en borrador.')
        setRevision((current) => current + 1)
        return
      }

      await confirmarAsignacion(row.assignmentId)
      setRevision((current) => current + 1)
    } catch (cause) {
      setActionError(mensajeError(cause, 'No se pudo confirmar la asignación.'))
    } finally {
      setConfirmingId(null)
    }
  }

  const upsertActivity = async (values: ActivityAssignmentFormValues) => {
    if (!values.teacherId) {
      setSaveError('Selecciona un docente registrado.')
      return
    }

    const justificacion = values.note.trim()

    if (!justificacion) {
      setSaveError('Ingresa la justificación de la actividad.')
      return
    }

    setSaving(true)
    setSaveError('')

    try {
      await asignarActividadDocente(values.teacherId, {
        actividadId: values.subjectKey,
        horasSemanales: values.assignedHours,
        justificacion,
      })
      setRevision((current) => current + 1)
      setDialog(null)
      setSaveError('')
      setActionError('')
    } catch (cause) {
      setSaveError(mensajeError(cause, 'No se pudo guardar la actividad del docente.'))
    } finally {
      setSaving(false)
    }
  }

  const openSubject = (row: AssignmentRow) => {
    if (row.assignmentId) {
      setDialog(null)
      return
    }

    setSaveError('')
    setDialog({ name: 'create', row })
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Asignación académica
        </Text>
        <Text variant="body">
          Asigna docentes a las asignaturas y grupos de acuerdo con el plan de
          estudios y el perfil académico disponible.
        </Text>
      </div>

      <div className={styles.tabs} role="tablist" aria-label="Tipo de asignación">
        {assignmentTabs.map((item) => (
          <button
            key={item.id}
            type="button"
            role="tab"
            aria-selected={tab === item.id}
            className={tab === item.id ? styles.tabActive : styles.tab}
            onClick={() => {
              setTab(item.id)
              setDialog(null)
            }}
          >
            {item.label}
          </button>
        ))}
      </div>

      <Card className={styles.priority}>
        <Text as="h3" variant="sectionTitle">
          Prioridad para completar la carga
        </Text>
        <Text variant="body">{assignmentPrioritySteps}</Text>
        <Text variant="caption">{assignmentPriorityNote}</Text>
      </Card>

      <AssignmentSelector
        yearId={year?.id ?? ''}
        groupId={group?.id ?? ''}
        gradeLabel={gradeLabel}
        yearStatus={yearStatus}
        yearOptions={yearOptions}
        groupOptions={groupOptions}
        yearsDisabled={aniosLoading || Boolean(aniosError) || !anios?.length}
        groupsDisabled={gruposLoading || Boolean(gruposError)}
        onYearChange={selectYear}
        onGroupChange={(nextGroupId) => {
          setGroupElegido(nextGroupId)
          setDialog(null)
          setActionError('')
        }}
      />

      {aniosError ? <p className={styles.error}>{aniosError}</p> : null}
      {gruposError ? <p className={styles.error}>{gruposError}</p> : null}

      {group && year && curricularLista ? (
        <AssignmentSummary
          groupCode={group.codigo}
          gradeLabel={gradeLabel}
          requiredHours={summary.requiredHours}
          assignedHours={summary.assignedHours}
          status={summary.status}
          subjectCount={subjects.length}
          year={year.year}
        />
      ) : gruposLoading || aniosLoading ? (
        <p className={styles.message}>Cargando año lectivo y grupos...</p>
      ) : (
        <p className={styles.message}>
          Este año lectivo aún no tiene grupos activos para asignar carga.
        </p>
      )}

      {catalog === null ? <p className={styles.message}>Cargando docentes...</p> : null}
      {catalog?.error ? <p className={styles.error}>{catalog.error}</p> : null}
      {planesError ? <p className={styles.error}>{planesError}</p> : null}
      {asignacionesError ? <p className={styles.error}>{asignacionesError}</p> : null}
      {borradoresError ? <p className={styles.error}>{borradoresError}</p> : null}
      {actividadesError ? <p className={styles.error}>{actividadesError}</p> : null}
      {actionError ? <p className={styles.error}>{actionError}</p> : null}

      {tab === 'curricular' && group && borradores && !borradoresError ? (
        <p className={styles.message}>
          Borradores pendientes de confirmar: {borradoresGrupo.length} en este grupo y{' '}
          {borradores.length} en total.
        </p>
      ) : null}

      {group && tab === 'curricular' && !curricularLista && !planesError && !asignacionesError ? (
        <p className={styles.message}>Cargando asignaciones del grupo...</p>
      ) : null}

      {tab === 'activity' && catalog && actividadesDocentes === null ? (
        <p className={styles.message}>Cargando actividades de los docentes...</p>
      ) : null}

      {(tab === 'activity'
        ? catalog !== null && actividadesDocentes !== null
        : Boolean(group) && curricularLista) ? (
        <AssignmentSection
          title={tab === 'curricular' ? 'Asignaciones curriculares' : 'Actividades y proyectos'}
          description={
            tab === 'curricular'
              ? 'Las horas requeridas provienen del plan de estudios; las asignadas corresponden a la asignación académica.'
              : 'Complementa la carga con actividades y proyectos institucionales cuando el plan curricular ya está cubierto.'
          }
          caption={
            tab === 'curricular'
              ? 'La asignación queda en borrador hasta que se confirma. Una confirmada puede volver a borrador si aún no tiene clases ni horario publicado.'
              : 'La asignación queda guardada en el docente. No depende del grupo seleccionado ni reemplaza el plan de estudios.'
          }
          rows={visibleRows}
          emptyMessage={
            tab === 'curricular'
              ? 'Este grado todavía no tiene asignaturas en el plan de estudios.'
              : 'No hay actividades institucionales configuradas.'
          }
          canCreate={canCreate}
          createLabel={tab === 'curricular' ? '+ Asignar docente' : '+ Asignar actividad'}
          readOnly={readOnly}
          confirmingId={confirmingId}
          editableAssigned={tab !== 'activity'}
          groupId={tab === 'curricular' ? group?.id : undefined}
          onCreate={() => {
            setSaveError('')
            setDialog(tab === 'curricular' ? { name: 'pick-subject' } : { name: 'create-activity' })
          }}
          onAssign={(row) => {
            setSaveError('')
            setDialog(
              tab === 'curricular' ? { name: 'create', row } : { name: 'create-activity', row },
            )
          }}
          onEdit={(row) => {
            setSaveError('')
            setDialog({ name: 'edit', row })
          }}
          onRemove={(row) => {
            setSaveError('')
            setDialog({ name: 'remove', row })
          }}
          onConfirm={(row) => {
            void confirmAssignment(row)
          }}
          onRevertToDraft={
            tab === 'curricular' && !readOnly
              ? (row) => {
                  setSaveError('')
                  setDialog({ name: 'revert', row })
                }
              : undefined
          }
        />
      ) : null}

      {dialog?.name === 'pick-subject' && group && year ? (
        <PlanSubjectPickerModal
          open
          year={year.year}
          groupCode={group.codigo}
          gradeLabel={gradeLabel}
          subjects={pendingSubjects}
          onClose={closeDialog}
          onSelect={openSubject}
        />
      ) : null}

      {dialog?.name === 'create' && group && year ? (
        <AssignmentFormModal
          key={`create-${dialog.row?.key ?? 'new'}`}
          open
          mode="create"
          year={year.year}
          restrictionCounts={conteoRestricciones}
          groupCode={group.codigo}
          gradeLabel={gradeLabel}
          subjects={pendingSubjects.length > 0 ? pendingSubjects : curricularRows}
          teachers={teachers}
          areas={areas}
          row={dialog.row}
          saving={saving}
          error={saveError}
          onClose={closeDialog}
          onSubmit={(values) => {
            void upsertAssignment(values)
          }}
        />
      ) : null}

      {dialog?.name === 'edit' && group && year ? (
        <AssignmentFormModal
          key={`edit-${dialog.row.key}`}
          open
          mode="edit"
          year={year.year}
          restrictionCounts={conteoRestricciones}
          groupCode={group.codigo}
          gradeLabel={gradeLabel}
          subjects={curricularRows}
          teachers={teachers}
          areas={areas}
          row={dialog.row}
          saving={saving}
          error={saveError}
          onClose={closeDialog}
          onSubmit={(values) => {
            void updateAssignment(values)
          }}
        />
      ) : null}

      {dialog?.name === 'revert' ? (
        <ConfirmActionModal
          open
          title="Volver a borrador"
          description={`¿Devolver a borrador la asignación de ${dialog.row.subject}${dialog.row.teacherName ? ` a ${dialog.row.teacherName}` : ''}?`}
          note="Solo se puede si la asignación está confirmada, no tiene clases programadas y la sede no tiene el horario publicado. Después se puede corregir y volver a confirmar."
          confirmLabel="Volver a borrador"
          busyLabel="Devolviendo..."
          submitting={saving}
          error={saveError}
          onClose={closeDialog}
          onConfirm={() => {
            void revertAssignment()
          }}
        />
      ) : null}

      {dialog?.name === 'remove' ? (
        <ConfirmActionModal
          open
          title="Quitar asignación"
          description={`¿Quitar la asignación de ${dialog.row.subject}${dialog.row.teacherName ? ` a ${dialog.row.teacherName}` : ''}?`}
          note="Solo se pueden quitar asignaciones en borrador y sin clases programadas."
          confirmLabel="Quitar asignación"
          busyLabel="Quitando..."
          submitting={saving}
          error={saveError}
          onClose={closeDialog}
          onConfirm={() => {
            void removeAssignment()
          }}
        />
      ) : null}

      {dialog?.name === 'create-activity' ? (
        <ActivityAssignmentFormModal
          key={`create-activity-${dialog.row?.key ?? 'new'}`}
          open
          mode="create"
          year={year?.year ?? 0}
          restrictionCounts={conteoRestricciones}
          activities={activityOptions}
          teachers={teachers}
          row={dialog.row}
          saving={saving}
          error={saveError}
          onClose={closeDialog}
          onSubmit={(values) => {
            void upsertActivity(values)
          }}
        />
      ) : null}
    </section>
  )
}

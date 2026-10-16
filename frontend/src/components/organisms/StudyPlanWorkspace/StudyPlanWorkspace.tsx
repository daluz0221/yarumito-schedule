import { useEffect, useState } from 'react'
import {
  anioEscolarVisible,
  ApiError,
  etiquetaGrado,
  listarAreas,
  listarAsignaturas,
  listarPlanesEstudio,
  opcionesAniosEscolares,
  opcionesSelectorPlan,
  planDesdeGrado,
  useCatalogoAniosEscolares,
  useCatalogoGrados,
} from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import type { GradoResponse } from '../../../api/grados'
import type { PlanEstudiosResponse } from '../../../api/planesEstudio'
import { academicYears } from '../../../content/academicYear'
import {
  configuredWeeklyHours,
  filterStudyPlanEntries,
  formatAvailableYears,
  type StudyPlanEntry,
  type StudyPlanGrade,
} from '../../../content/studyPlan'
import { Text } from '../../atoms/Text'
import { GradePlanSummary } from '../GradePlanSummary'
import { GradeSubjectsSection } from '../GradeSubjectsSection'
import { StudyPlanSelector } from '../StudyPlanSelector'
import styles from './StudyPlanWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeError(cause: unknown, fallback: string) {
  return cause instanceof ApiError ? cause.message : fallback
}

function entradaDesdePlan(
  plan: PlanEstudiosResponse,
  areaPorAsignatura: ReadonlyMap<string, string>,
): StudyPlanEntry {
  const area = areaPorAsignatura.get(plan.asignaturaId)?.trim()

  return {
    id: plan.id,
    yearId: plan.anioEscolarId,
    gradeId: plan.gradoId,
    area: area || '—',
    subject: plan.asignaturaNombre?.trim() || '—',
    weeklyHours: plan.horasSemanales,
    shift: '—',
    note: plan.observacion?.trim() || null,
  }
}

function vistaGrado(grado: GradoResponse): StudyPlanGrade {
  const plan = planDesdeGrado(grado)

  return {
    id: grado.id,
    label: etiquetaGrado(grado.nombre),
    level: plan.typeLabel,
    weeklyHours: plan.weeklyHours,
  }
}

export function StudyPlanWorkspace() {
  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares()
  const catalogo = aniosError ? academicYears : anios
  const [yearId, setYearId] = useState('')
  const [gradeId, setGradeId] = useState('')
  const [area, setArea] = useState('')
  const [query, setQuery] = useState('')
  const [areas, setAreas] = useState<AreaResponse[]>([])
  const [areaPorAsignatura, setAreaPorAsignatura] = useState<ReadonlyMap<string, string>>(
    () => new Map(),
  )
  const [planes, setPlanes] = useState<PlanEstudiosResponse[] | null>(null)
  const [planesError, setPlanesError] = useState('')
  const { grados, error: gradosError, loading: gradosLoading } = useCatalogoGrados()

  useEffect(() => {
    const controller = new AbortController()

    listarAreas(controller.signal)
      .then(async (loadedAreas) => {
        if (controller.signal.aborted) {
          return
        }

        setAreas(loadedAreas)

        try {
          const groups = await Promise.all(
            loadedAreas.map((item) =>
              listarAsignaturas(item.id, controller.signal).then((subjects) =>
                subjects.map((subject) => [subject.id, item.nombre] as const),
              ),
            ),
          )

          if (!controller.signal.aborted) {
            setAreaPorAsignatura(new Map(groups.flat()))
          }
        } catch (cause) {
          if (isAbortError(cause) || controller.signal.aborted) {
            return
          }

          setAreaPorAsignatura(new Map())
        }
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setAreas([])
        setAreaPorAsignatura(new Map())
      })

    return () => controller.abort()
  }, [])

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
          mensajeError(
            cause,
            'No se pudo cargar el plan de estudios. Las horas semanales no están disponibles en este momento.',
          ),
        )
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!catalogo?.length) {
      return
    }

    setYearId((current) =>
      catalogo.some((year) => year.id === current)
        ? current
        : (anioEscolarVisible(catalogo)?.id ?? ''),
    )
  }, [catalogo])

  useEffect(() => {
    if (!grados?.length) {
      return
    }

    setGradeId((current) =>
      grados.some((grado) => grado.id === current) ? current : grados[0].id,
    )
  }, [grados])

  const year = catalogo?.find((item) => item.id === yearId) ?? null
  const grado = grados?.find((item) => item.id === gradeId) ?? null
  const grade = grado ? vistaGrado(grado) : null
  const planesLoading = planes === null && !planesError
  const entriesDelAnio = (planes ?? [])
    .filter((plan) => (year ? plan.anioEscolarId === year.id : false))
    .map((plan) => entradaDesdePlan(plan, areaPorAsignatura))
  const planEntries = entriesDelAnio.filter((entry) => entry.gradeId === grado?.id)
  const rows = filterStudyPlanEntries(entriesDelAnio, {
    yearId: year?.id ?? '',
    gradeId: grado?.id ?? '',
    area,
    query,
  })
  const areaNames = areas.length
    ? areas.map((item) => item.nombre)
    : [...new Set(planEntries.map((entry) => entry.area))]

  const selectYear = (nextYearId: string) => {
    setYearId(nextYearId)
    setArea('')
    setQuery('')
  }

  const selectGrade = (nextGradeId: string) => {
    setGradeId(nextGradeId)
    setArea('')
    setQuery('')
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Plan de estudios
        </Text>
        <Text variant="body">
          Configura las asignaturas y la intensidad horaria semanal
          correspondiente a cada grado durante el año lectivo.
        </Text>
      </div>

      {aniosLoading ? <p className={styles.notice}>Cargando años escolares…</p> : null}
      {aniosError ? (
        <p className={styles.alert} role="alert">
          {aniosError} Se muestran los años de referencia locales para que puedas seguir
          revisando el plan.
        </p>
      ) : null}
      {planesError ? (
        <p className={styles.alert} role="alert">
          {planesError} Puedes seguir eligiendo año y grado; la intensidad se mostrará cuando
          el listado vuelva a estar disponible.
        </p>
      ) : null}

      <StudyPlanSelector
        yearId={year?.id ?? ''}
        yearOptions={
          catalogo
            ? opcionesAniosEscolares(catalogo, '')
            : opcionesAniosEscolares(null, '')
        }
        yearsSummary={
          catalogo?.length
            ? formatAvailableYears(catalogo)
            : aniosLoading
              ? 'Cargando años lectivos…'
              : 'Sin años escolares registrados'
        }
        yearDisabled={!catalogo?.length}
        gradeId={grade?.id ?? ''}
        gradeOptions={opcionesSelectorPlan(grados, gradosError)}
        gradeError={gradosError}
        gradeDisabled={gradosLoading || Boolean(gradosError) || !grados?.length}
        status={year?.status ?? null}
        onYearChange={selectYear}
        onGradeChange={selectGrade}
      />

      {grade && year ? (
        planesLoading ? (
          <p className={styles.notice}>Cargando la intensidad horaria del plan de estudios…</p>
        ) : (
          <>
            <GradePlanSummary
              grade={grade}
              configuredHours={configuredWeeklyHours(planEntries)}
            />

            <GradeSubjectsSection
              year={year.year}
              gradeLabel={grade.label}
              areaNames={areaNames}
              area={area}
              query={query}
              rows={rows}
              planEntries={planEntries}
              emptyPlanMessage={
                planesError
                  ? 'La intensidad de este grado no está disponible mientras falle la consulta del plan.'
                  : undefined
              }
              onAreaChange={setArea}
              onQueryChange={setQuery}
            />
          </>
        )
      ) : (
        <Text variant="body">
          {!year
            ? aniosLoading
              ? 'Cargando el año lectivo del plan de estudios…'
              : 'No hay años escolares registrados. El plan de estudios se configura sobre un año lectivo.'
            : gradosError
              ? 'El plan de estudios necesita un grado del catálogo. Cuando el listado vuelva a estar disponible, podrás continuar.'
              : gradosLoading
                ? 'Cargando grados del catálogo…'
                : 'No hay grados registrados. El plan de estudios no se puede configurar hasta que exista al menos un grado.'}
        </Text>
      )}
    </section>
  )
}

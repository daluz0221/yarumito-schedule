import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { ApiError, listarCatalogoAreas, listarDocentes } from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import { academicYears } from '../../../content/academicYear'
import { currentStudyPlanYear } from '../../../content/studyPlan'
import { resolveAreaName } from '../../../content/teacherProfile'
import {
  availabilityStatus,
  countRestrictions,
  preferredRestrictionTeacher,
} from '../../../content/teacherRestrictions'
import { formatTeacherName, labelEstado, type Teacher } from '../../../content/teachers'
import { Text } from '../../atoms/Text'
import { RestrictionSelector } from '../RestrictionSelector'
import { TeacherAvailabilitySummary } from '../TeacherAvailabilitySummary'
import { WeeklyAvailability } from '../WeeklyAvailability'
import styles from './RestrictionsWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

type Catalog = {
  teachers: Teacher[]
  areas: AreaResponse[]
  error: string
}

export function RestrictionsWorkspace() {
  const [searchParams] = useSearchParams()
  const requestedTeacherId = searchParams.get('docente') ?? ''
  const [yearId, setYearId] = useState(currentStudyPlanYear().id)
  const [teacherId, setTeacherId] = useState(requestedTeacherId)
  const [catalog, setCatalog] = useState<Catalog | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    Promise.all([
      listarDocentes({ page: 0, size: 100 }, controller.signal),
      listarCatalogoAreas(controller.signal),
    ])
      .then(([teachersPage, areas]) => {
        const teachers = [...teachersPage.content].sort((left, right) =>
          formatTeacherName(left).localeCompare(formatTeacherName(right), 'es'),
        )
        setCatalog({ teachers, areas, error: '' })
        setTeacherId((current) => {
          if (requestedTeacherId && teachers.some((teacher) => teacher.id === requestedTeacherId)) {
            return requestedTeacherId
          }

          if (current && teachers.some((teacher) => teacher.id === current)) {
            return current
          }

          const preferred = preferredRestrictionTeacher(teachers.map(formatTeacherName))
          const match = teachers.find((teacher) => formatTeacherName(teacher) === preferred)
          return match?.id ?? teachers[0]?.id ?? ''
        })
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setCatalog({
          teachers: [],
          areas: [],
          error:
            cause instanceof ApiError
              ? cause.message
              : 'No se pudo cargar la disponibilidad docente.',
        })
      })

    return () => controller.abort()
  }, [requestedTeacherId])

  const year = academicYears.find((item) => item.id === yearId) ?? academicYears[0]
  const teachers = catalog?.teachers ?? []
  const teacher = teachers.find((item) => item.id === teacherId)
  const teacherName = teacher ? formatTeacherName(teacher) : ''

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Restricciones y disponibilidad docente
        </Text>
        <Text variant="body">
          Registra las franjas en las que cada docente presenta restricciones o
          condiciones de disponibilidad durante el año lectivo.
        </Text>
      </div>

      {catalog === null ? <p className={styles.message}>Cargando docentes...</p> : null}
      {catalog?.error ? <p className={styles.message}>{catalog.error}</p> : null}

      {catalog && !catalog.error ? (
        <>
          <RestrictionSelector
            status={year.status}
            year={year.year}
            teacherName={teacherName}
            teachers={teachers.map((item) => ({
              id: item.id,
              name: formatTeacherName(item),
              areaName: resolveAreaName(item.areaNombramientoId, catalog.areas),
              active: item.estado === 'ACTIVO',
            }))}
            onYearChange={setYearId}
            onTeacherChange={setTeacherId}
          />

          {teachers.length === 0 ? (
            <p className={styles.message}>No hay docentes registrados.</p>
          ) : null}

          {teacher ? (
            <>
              <TeacherAvailabilitySummary
                name={teacherName}
                areaName={resolveAreaName(teacher.areaNombramientoId, catalog.areas)}
                statusLabel={labelEstado(teacher.estado)}
                year={year.year}
                restrictionCount={countRestrictions(year.id, teacherName)}
              />
              <WeeklyAvailability
                yearId={year.id}
                year={year.year}
                teacherName={teacherName}
                readOnly={year.status === 'CERRADO'}
                statusAt={(bandId, day) => availabilityStatus(year.id, teacherName, bandId, day)}
              />
            </>
          ) : null}
        </>
      ) : null}
    </section>
  )
}

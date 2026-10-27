import { useState } from 'react'
import {
  labelYearStatus,
  type AcademicYearStatus,
} from '../../../content/academicYear'
import { FormField } from '../../molecules/FormField'
import { Card } from '../../atoms/Card'
import { RestrictionTeacherModal, type RestrictionTeacherChoice } from '../RestrictionTeacherModal'
import { RestrictionYearModal } from '../RestrictionYearModal'
import styles from './RestrictionSelector.module.css'

export type RestrictionSelectorProps = {
  status: AcademicYearStatus
  year: number
  teacherName: string
  teachers: RestrictionTeacherChoice[]
  onYearChange: (yearId: string) => void
  onTeacherChange: (teacherId: string) => void
}

type Picker = 'year' | 'teacher' | null

export function RestrictionSelector({
  status,
  year,
  teacherName,
  teachers,
  onYearChange,
  onTeacherChange,
}: RestrictionSelectorProps) {
  const [picker, setPicker] = useState<Picker>(null)
  const hint =
    status === 'ACTIVO'
      ? `${year} — Activo · Administración de restricciones habilitada`
      : status === 'CERRADO'
        ? `${year} — Cerrado · Solo lectura`
        : `${year} — ${labelYearStatus(status)}`

  return (
    <Card className={styles.card}>
      <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
        <FormField id="restriction-year" label="Año lectivo">
          <button
            type="button"
            id="restriction-year"
            className={styles.trigger}
            onClick={() => setPicker('year')}
          >
            {year}
          </button>
        </FormField>
        <FormField id="restriction-teacher" label="Docente">
          <button
            type="button"
            id="restriction-teacher"
            className={styles.trigger}
            disabled={teachers.length === 0}
            onClick={() => setPicker('teacher')}
          >
            {teacherName || 'Sin docentes'}
          </button>
        </FormField>
      </form>
      <p className={styles.hint}>{hint}</p>

      {picker === 'year' ? (
        <RestrictionYearModal
          open
          onSelect={(nextYearId) => {
            onYearChange(nextYearId)
            setPicker(null)
          }}
          onClose={() => setPicker(null)}
        />
      ) : null}

      {picker === 'teacher' ? (
        <RestrictionTeacherModal
          open
          teachers={teachers}
          onSelect={(nextTeacherId) => {
            onTeacherChange(nextTeacherId)
            setPicker(null)
          }}
          onClose={() => setPicker(null)}
        />
      ) : null}
    </Card>
  )
}

export type { RestrictionTeacherChoice }

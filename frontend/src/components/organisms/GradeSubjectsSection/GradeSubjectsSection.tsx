import { useEffect, useRef, useState } from 'react'
import { ApiError, consultarPlanEstudios } from '../../../api'
import type { PlanEstudiosResponse } from '../../../api/planesEstudio'
import type { StudyPlanEntry } from '../../../content/studyPlan'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { TextInput } from '../../atoms/TextInput'
import { FormField } from '../../molecules/FormField'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import { GradeSubjectsTable } from '../GradeSubjectsTable'
import { PlanSubjectFormModal } from '../PlanSubjectFormModal'
import { RemovePlanSubjectModal } from '../RemovePlanSubjectModal'
import styles from './GradeSubjectsSection.module.css'

type SectionDialog =
  | { name: 'create' }
  | { name: 'edit'; id: string }
  | { name: 'remove'; entry: StudyPlanEntry }

export type GradeSubjectsSectionProps = {
  year: number
  yearId: string
  gradeId: string
  gradeLabel: string
  areaNames: string[]
  area: string
  query: string
  rows: StudyPlanEntry[]
  planes: PlanEstudiosResponse[]
  emptyPlanMessage?: string
  onAreaChange: (area: string) => void
  onQueryChange: (query: string) => void
  onSaved: (plan: PlanEstudiosResponse) => void
  onMissing?: (planId: string) => void
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function detalleError(cause: unknown) {
  if (cause instanceof ApiError) {
    if (cause.status === 404) {
      return 'Este registro del plan ya no está disponible. Cierra el formulario y revisa el listado.'
    }

    return cause.message
  }

  return 'No se pudo cargar el registro del plan de estudios.'
}

export function GradeSubjectsSection({
  year,
  yearId,
  gradeId,
  gradeLabel,
  areaNames,
  area,
  query,
  rows,
  planes,
  emptyPlanMessage = 'Este grado todavía no tiene asignaturas en el plan.',
  onAreaChange,
  onQueryChange,
  onSaved,
  onMissing,
}: GradeSubjectsSectionProps) {
  const [dialog, setDialog] = useState<SectionDialog | null>(null)
  const [editing, setEditing] = useState<PlanEstudiosResponse | null>(null)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const detailRequest = useRef<AbortController | null>(null)

  useEffect(() => {
    return () => detailRequest.current?.abort()
  }, [])

  const closeDialog = () => {
    detailRequest.current?.abort()
    detailRequest.current = null
    setDialog(null)
    setEditing(null)
    setEditLoading(false)
    setEditError('')
  }

  const loadPlan = (planId: string) => {
    detailRequest.current?.abort()
    const controller = new AbortController()
    detailRequest.current = controller

    setDialog({ name: 'edit', id: planId })
    setEditing(null)
    setEditError('')
    setEditLoading(true)

    consultarPlanEstudios(planId, controller.signal)
      .then((plan) => {
        if (controller.signal.aborted) {
          return
        }

        setEditing(plan)
        onSaved(plan)
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setEditing(null)
        setEditError(detalleError(cause))

        if (cause instanceof ApiError && cause.status === 404) {
          onMissing?.(planId)
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setEditLoading(false)
        }
      })
  }
  const areaOptions = [
    { value: '', label: 'Todas' },
    ...areaNames.map((name) => ({ value: name, label: name })),
  ]

  return (
    <Card className={styles.section}>
      <SectionHeader
        title="Asignaturas del grado"
        description={`Estructura curricular configurada para ${year} · ${gradeLabel}.`}
        action={
          <Button variant="primary" size="sm" onClick={() => setDialog({ name: 'create' })}>
            + Agregar asignatura
          </Button>
        }
      />

      <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
        <FormField id="plan-search" label="Buscar asignatura">
          <TextInput
            id="plan-search"
            name="query"
            inputSize="sm"
            placeholder="Buscar por nombre..."
            value={query}
            onChange={(event) => onQueryChange(event.target.value)}
          />
        </FormField>
        <SelectField
          id="plan-area"
          label="Área"
          name="area"
          value={area}
          options={areaOptions}
          onChange={onAreaChange}
        />
      </form>

      <div className={styles.tableHost}>
        <GradeSubjectsTable
          rows={rows}
          emptyMessage={
            query || area
              ? 'No se encontraron asignaturas con esos filtros.'
              : emptyPlanMessage
          }
          onEdit={(entry) => loadPlan(entry.id)}
          onRemove={(entry) => setDialog({ name: 'remove', entry })}
        />
      </div>

      <Text variant="caption">
        Configuración por grado, no por grupo. Una asignatura no puede duplicarse
        para el mismo Año lectivo + Grado.
      </Text>

      {dialog?.name === 'create' || dialog?.name === 'edit' ? (
        <PlanSubjectFormModal
          open
          mode={dialog.name}
          year={year}
          yearId={yearId}
          gradeId={gradeId}
          gradeLabel={gradeLabel}
          planes={planes}
          plan={editing ?? undefined}
          loading={dialog.name === 'edit' && editLoading}
          loadError={dialog.name === 'edit' ? editError : ''}
          onRetry={dialog.name === 'edit' ? () => loadPlan(dialog.id) : undefined}
          onClose={closeDialog}
          onCreated={onSaved}
          onUpdated={onSaved}
        />
      ) : null}

      {dialog?.name === 'remove' ? (
        <RemovePlanSubjectModal
          open
          subjectName={dialog.entry.subject}
          gradeLabel={gradeLabel}
          year={year}
          onClose={closeDialog}
        />
      ) : null}
    </Card>
  )
}

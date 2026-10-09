import { useState } from 'react'
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
  | { name: 'edit'; entry: StudyPlanEntry }
  | { name: 'remove'; entry: StudyPlanEntry }

export type GradeSubjectsSectionProps = {
  year: number
  gradeLabel: string
  areaNames: string[]
  area: string
  query: string
  rows: StudyPlanEntry[]
  planEntries: StudyPlanEntry[]
  onAreaChange: (area: string) => void
  onQueryChange: (query: string) => void
}

export function GradeSubjectsSection({
  year,
  gradeLabel,
  areaNames,
  area,
  query,
  rows,
  planEntries,
  onAreaChange,
  onQueryChange,
}: GradeSubjectsSectionProps) {
  const [dialog, setDialog] = useState<SectionDialog | null>(null)
  const closeDialog = () => setDialog(null)
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
              : 'Este grado todavía no tiene asignaturas en el plan.'
          }
          onEdit={(entry) => setDialog({ name: 'edit', entry })}
          onRemove={(entry) => setDialog({ name: 'remove', entry })}
        />
      </div>

      <Text variant="caption">
        Configuración por grado, no por grupo. Una asignatura no puede duplicarse
        para el mismo Año lectivo + Grado.
      </Text>

      {dialog?.name === 'create' || dialog?.name === 'edit' ? (
        <PlanSubjectFormModal
          key={dialog.name === 'edit' ? dialog.entry.id : 'create'}
          open
          mode={dialog.name}
          year={year}
          gradeLabel={gradeLabel}
          subjectsInPlan={planEntries.map((entry) => entry.subject)}
          entry={dialog.name === 'edit' ? dialog.entry : undefined}
          onClose={closeDialog}
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

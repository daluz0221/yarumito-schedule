import { useEffect, useMemo, useState } from 'react'
import { ApiError, listarCatalogoAreas, listarDocentes } from '../../../api'
import type { AreaResponse } from '../../../api/areas'
import {
  activitySubjects,
  assignmentGroupOptions,
  assignmentPriorityNote,
  assignmentPrioritySteps,
  assignmentTabs,
  buildAssignmentRows,
  curricularSubjects,
  gradeLabelFromGroup,
  groupsForYear,
  nextAssignmentId,
  planEntriesForGroup,
  seedAssignments,
  summarizeRows,
  type AssignmentRecord,
  type AssignmentRow,
  type AssignmentTab,
} from '../../../content/academicAssignment'
import { academicYears } from '../../../content/academicYear'
import { currentStudyPlanYear } from '../../../content/studyPlan'
import { formatTeacherName, type Teacher } from '../../../content/teachers'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { ActivityAssignmentFormModal, type ActivityAssignmentFormValues } from '../ActivityAssignmentFormModal'
import { AssignmentFormModal, type AssignmentFormValues } from '../AssignmentFormModal'
import { AssignmentSection } from '../AssignmentSection'
import { AssignmentSelector } from '../AssignmentSelector'
import { AssignmentSummary } from '../AssignmentSummary'
import { PlanSubjectPickerModal } from '../PlanSubjectPickerModal'
import { RemoveAssignmentModal } from '../RemoveAssignmentModal'
import styles from './AssignmentWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

type Dialog =
  | { name: 'pick-subject' }
  | { name: 'create'; row?: AssignmentRow }
  | { name: 'edit'; row: AssignmentRow }
  | { name: 'remove'; row: AssignmentRow }
  | { name: 'create-activity'; row?: AssignmentRow }
  | { name: 'edit-activity'; row: AssignmentRow }
  | { name: 'remove-activity'; row: AssignmentRow }

type Catalog = {
  teachers: Teacher[]
  areas: AreaResponse[]
  error: string
}

export function AssignmentWorkspace() {
  const [yearId, setYearId] = useState(currentStudyPlanYear().id)
  const [groupId, setGroupId] = useState(() => groupsForYear(currentStudyPlanYear().id)[0]?.id ?? '')
  const [tab, setTab] = useState<AssignmentTab>('curricular')
  const [assignments, setAssignments] = useState<AssignmentRecord[]>(seedAssignments)
  const [catalog, setCatalog] = useState<Catalog | null>(null)
  const [dialog, setDialog] = useState<Dialog | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    Promise.all([
      listarDocentes({ page: 0, size: 100, estado: 'ACTIVO' }, controller.signal),
      listarCatalogoAreas(controller.signal),
    ])
      .then(([teachersPage, areas]) => {
        const teachers = [...teachersPage.content].sort((left, right) =>
          formatTeacherName(left).localeCompare(formatTeacherName(right), 'es'),
        )
        setCatalog({ teachers, areas, error: '' })
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
              : 'No se pudieron cargar los docentes. La asignación queda en consulta local.',
        })
      })

    return () => controller.abort()
  }, [])

  const year = academicYears.find((item) => item.id === yearId) ?? academicYears[0]
  const yearGroups = groupsForYear(year.id)
  const group = yearGroups.find((item) => item.id === groupId) ?? yearGroups[0]
  const groupOptions = assignmentGroupOptions(year.id)
  const gradeLabel = group ? gradeLabelFromGroup(group) : ''
  const readOnly = year.status === 'CERRADO'
  const planEntries = group ? planEntriesForGroup(year.id, group) : []
  const teachers = catalog?.teachers ?? []
  const areas = catalog?.areas ?? []
  const scopedAssignments = useMemo(
    () =>
      assignments.filter(
        (item) => item.yearId === year.id && item.groupId === (group?.id ?? ''),
      ),
    [assignments, group?.id, year.id],
  )
  const curricularRows = buildAssignmentRows(
    curricularSubjects(planEntries),
    scopedAssignments.filter((item) => item.tab === 'curricular'),
    teachers,
  )
  const activityRows = buildAssignmentRows(
    activitySubjects(),
    scopedAssignments.filter((item) => item.tab === 'activity'),
    teachers,
  )
  const visibleRows = tab === 'curricular' ? curricularRows : activityRows
  const summary = summarizeRows(curricularRows)
  const closeDialog = () => setDialog(null)
  const canCreate = !readOnly && Boolean(group) && visibleRows.length > 0

  const selectYear = (nextYearId: string) => {
    setYearId(nextYearId)
    setGroupId(groupsForYear(nextYearId)[0]?.id ?? '')
    setDialog(null)
  }

  const saveRecord = (
    values: {
      subjectKey: string
      teacherId: string | null
      teacherName: string
      assignedHours: number
      kind: AssignmentRecord['kind']
      note: string
    },
    nextTab: AssignmentTab,
  ) => {
    if (!group) {
      return
    }

    const record: AssignmentRecord = {
      id: nextAssignmentId(year.id, group.id, values.subjectKey),
      yearId: year.id,
      groupId: group.id,
      subjectKey: values.subjectKey,
      tab: nextTab,
      teacherId: values.teacherId,
      teacherName: values.teacherName || null,
      assignedHours: values.assignedHours,
      kind: values.kind,
      note: values.note,
    }

    setAssignments((current) => {
      const exists = current.some((item) => item.id === record.id)
      return exists
        ? current.map((item) => (item.id === record.id ? record : item))
        : [...current, record]
    })
    closeDialog()
  }

  const upsertAssignment = (values: AssignmentFormValues) => {
    saveRecord(values, 'curricular')
  }

  const upsertActivity = (values: ActivityAssignmentFormValues) => {
    saveRecord({ ...values, kind: 'ACTIVIDAD' }, 'activity')
  }

  const removeAssignment = (row: AssignmentRow) => {
    if (!group) {
      return
    }

    const id = nextAssignmentId(year.id, group.id, row.key)
    setAssignments((current) => current.filter((item) => item.id !== id))
    closeDialog()
  }

  const openSubject = (row: AssignmentRow) => {
    setDialog(row.assigned ? { name: 'edit', row } : { name: 'create', row })
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
        yearId={year.id}
        groupId={group?.id ?? ''}
        gradeLabel={gradeLabel}
        yearStatus={year.status}
        groupOptions={groupOptions}
        onYearChange={selectYear}
        onGroupChange={setGroupId}
      />

      {group ? (
        <AssignmentSummary
          groupCode={group.code}
          gradeLabel={gradeLabel}
          requiredHours={summary.requiredHours}
          assignedHours={summary.assignedHours}
          status={summary.status}
          subjectCount={curricularRows.length}
          year={year.year}
        />
      ) : (
        <p className={styles.message}>
          Este año lectivo aún no tiene grupos activos para asignar carga.
        </p>
      )}

      {catalog === null ? <p className={styles.message}>Cargando docentes...</p> : null}
      {catalog?.error ? <p className={styles.message}>{catalog.error}</p> : null}

      {group ? (
        <AssignmentSection
          title={tab === 'curricular' ? 'Asignaciones curriculares' : 'Actividades y proyectos'}
          description={
            tab === 'curricular'
              ? 'Las horas requeridas provienen del plan de estudios; las asignadas corresponden a la asignación académica.'
              : 'Complementa la carga con actividades y proyectos institucionales cuando el plan curricular ya está cubierto.'
          }
          caption={
            tab === 'curricular'
              ? 'La asignación es por grupo. El grado se deriva del grupo seleccionado y no se edita aquí.'
              : 'Estas actividades no reemplazan el plan de estudios; sirven para completar o equilibrar la carga docente.'
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
          onCreate={() =>
            setDialog(tab === 'curricular' ? { name: 'pick-subject' } : { name: 'create-activity' })
          }
          onAssign={(row) =>
            setDialog(tab === 'curricular' ? { name: 'create', row } : { name: 'create-activity', row })
          }
          onEdit={(row) =>
            setDialog(tab === 'curricular' ? { name: 'edit', row } : { name: 'edit-activity', row })
          }
          onRemove={(row) =>
            setDialog(tab === 'curricular' ? { name: 'remove', row } : { name: 'remove-activity', row })
          }
        />
      ) : null}

      {dialog?.name === 'pick-subject' && group ? (
        <PlanSubjectPickerModal
          open
          year={year.year}
          groupCode={group.code}
          gradeLabel={gradeLabel}
          subjects={curricularRows}
          onClose={closeDialog}
          onSelect={openSubject}
        />
      ) : null}

      {dialog?.name === 'create' || dialog?.name === 'edit' ? (
        <AssignmentFormModal
          key={`${dialog.name}-${dialog.row?.key ?? 'new'}`}
          open
          mode={dialog.name}
          yearId={year.id}
          year={year.year}
          groupCode={group?.code ?? ''}
          gradeLabel={gradeLabel}
          subjects={curricularRows}
          teachers={teachers}
          areas={areas}
          row={dialog.row}
          onClose={closeDialog}
          onSubmit={upsertAssignment}
        />
      ) : null}

      {dialog?.name === 'create-activity' || dialog?.name === 'edit-activity' ? (
        <ActivityAssignmentFormModal
          key={`${dialog.name}-${dialog.row?.key ?? 'new'}`}
          open
          mode={dialog.name === 'edit-activity' ? 'edit' : 'create'}
          yearId={year.id}
          year={year.year}
          activities={activityRows}
          teachers={teachers}
          row={dialog.row}
          onClose={closeDialog}
          onSubmit={upsertActivity}
        />
      ) : null}

      {dialog?.name === 'remove' ? (
        <RemoveAssignmentModal
          open
          year={year.year}
          groupCode={group?.code ?? ''}
          subjectName={dialog.row.subject}
          teacherName={dialog.row.teacherName}
          hours={dialog.row.assignedHours}
          onClose={closeDialog}
          onConfirm={() => removeAssignment(dialog.row)}
        />
      ) : null}

      {dialog?.name === 'remove-activity' ? (
        <RemoveAssignmentModal
          open
          variant="activity"
          subjectName={dialog.row.subject}
          teacherName={dialog.row.teacherName}
          hours={dialog.row.assignedHours}
          onClose={closeDialog}
          onConfirm={() => removeAssignment(dialog.row)}
        />
      ) : null}
    </section>
  )
}

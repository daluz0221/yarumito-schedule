import { useEffect, useState } from 'react'
import { ApiError, listarDocentes } from '../../../api'
import {
  buildAssignmentRows,
  gradeLabelFromGroup,
  groupsForYear,
  seedAssignments,
} from '../../../content/academicAssignment'
import { academicYears } from '../../../content/academicYear'
import {
  blocksForGroup,
  classBands,
  curricularRowsSource,
  defaultClassroom,
  inspectScheduleChange,
  pendingPlacements,
  scheduleCampusOptions,
  scheduleMetrics,
  scheduleSubject,
  scheduleShiftLabel,
  scheduleShiftOptions,
  seedPlacements,
  type ScheduleAlert,
  type SchedulePlacement,
} from '../../../content/scheduleConstruction'
import { currentStudyPlanYear } from '../../../content/studyPlan'
import type { WeekDay } from '../../../content/teacherRestrictions'
import { formatTeacherName, type Teacher } from '../../../content/teachers'
import { Text } from '../../atoms/Text'
import { PendingAssignments } from '../PendingAssignments'
import { RemoveScheduleClassModal } from '../RemoveScheduleClassModal'
import { ScheduleAlertModal } from '../ScheduleAlertModal'
import {
  ScheduleClassFormModal,
  type ScheduleClassFormValues,
} from '../ScheduleClassFormModal'
import { ScheduleGrid } from '../ScheduleGrid'
import { ScheduleMetrics } from '../ScheduleMetrics'
import { ScheduleSelector } from '../ScheduleSelector'
import styles from './ScheduleWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

type ClassDialog =
  | { name: 'create'; day: WeekDay; bandId: string; subjectKey: string }
  | { name: 'edit'; placementId: string }

export function ScheduleWorkspace() {
  const [yearId, setYearId] = useState(currentStudyPlanYear().id)
  const [campusId, setCampusId] = useState(scheduleCampusOptions[0].value)
  const [shiftId, setShiftId] = useState(scheduleShiftOptions[0].value)
  const [groupId, setGroupId] = useState(() => groupsForYear(currentStudyPlanYear().id)[0]?.id ?? '')
  const [placements, setPlacements] = useState<SchedulePlacement[]>(seedPlacements)
  const [teachers, setTeachers] = useState<Teacher[]>([])
  const [catalogError, setCatalogError] = useState('')
  const [classDialog, setClassDialog] = useState<ClassDialog | null>(null)
  const [removeId, setRemoveId] = useState<string | null>(null)
  const [alert, setAlert] = useState<ScheduleAlert | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    listarDocentes({ page: 0, size: 100, estado: 'ACTIVO' }, controller.signal)
      .then((page) => {
        setTeachers(
          [...page.content].sort((left, right) =>
            formatTeacherName(left).localeCompare(formatTeacherName(right), 'es'),
          ),
        )
        setCatalogError('')
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setTeachers([])
        setCatalogError(
          cause instanceof ApiError
            ? cause.message
            : 'No se pudieron cargar los docentes. El horario usa la asignación local.',
        )
      })

    return () => controller.abort()
  }, [])

  const year = academicYears.find((item) => item.id === yearId) ?? academicYears[0]
  const yearGroups = groupsForYear(year.id)
  const group = yearGroups.find((item) => item.id === groupId) ?? yearGroups[0]
  const readOnly = year.status === 'CERRADO'
  const gradeLabel = group ? gradeLabelFromGroup(group) : ''
  const shiftLabel =
    scheduleShiftOptions.find((option) => option.value === shiftId)?.label ?? scheduleShiftLabel

  const rows = group
    ? buildAssignmentRows(
        curricularRowsSource(year.id, group),
        seedAssignments.filter(
          (item) =>
            item.yearId === year.id && item.groupId === group.id && item.tab === 'curricular',
        ),
        teachers,
      )
    : []
  const blocks = blocksForGroup(placements, year.id, group?.id ?? '', group?.students ?? 0)
  const pending = pendingPlacements(rows, placements, year.id, group?.id ?? '')
  const metrics = scheduleMetrics(rows, placements, blocks, year.id, group?.id ?? '')

  const selectYear = (nextYearId: string) => {
    setYearId(nextYearId)
    setGroupId(groupsForYear(nextYearId)[0]?.id ?? '')
    setClassDialog(null)
    setRemoveId(null)
    setAlert(null)
  }

  const selectGroup = (nextGroupId: string) => {
    setGroupId(nextGroupId)
    setClassDialog(null)
    setRemoveId(null)
    setAlert(null)
  }

  const openCreate = (day: WeekDay, bandId: string, subjectKey = pending[0]?.key ?? '') => {
    if (readOnly || !group) {
      return
    }

    const item = pending.find((entry) => entry.key === subjectKey) ?? pending[0]
    setAlert(null)
    setRemoveId(null)
    setClassDialog({
      name: 'create',
      day,
      bandId,
      subjectKey: item?.key ?? subjectKey,
    })
  }

  const saveClass = (values: ScheduleClassFormValues) => {
    if (!group || !classDialog) {
      return
    }

    const current =
      classDialog.name === 'edit'
        ? placements.find((item) => item.id === classDialog.placementId)
        : undefined
    const assignment = pending.find((item) => item.key === values.subjectKey)
    const teacherName = current?.teacherName ?? assignment?.teacherName ?? ''
    const subjectKey = current?.subjectKey ?? values.subjectKey

    if (!teacherName || !subjectKey) {
      return
    }

    const draft = {
      id: current?.id,
      yearId: year.id,
      groupId: group.id,
      day: values.day,
      bandId: values.bandId,
      teacherName,
      classroom: values.classroom,
    }
    const result = inspectScheduleChange(draft, placements, group.students)

    if (result.blocking) {
      setAlert(result.blocking)
      return
    }

    if (current) {
      setPlacements((items) =>
        items.map((item) =>
          item.id === current.id
            ? { ...item, day: values.day, bandId: values.bandId, classroom: values.classroom }
            : item,
        ),
      )
    } else {
      setPlacements((items) => [
        ...items,
        {
          id: crypto.randomUUID(),
          yearId: year.id,
          groupId: group.id,
          day: values.day,
          bandId: values.bandId,
          subjectKey,
          teacherName,
          classroom: values.classroom,
        },
      ])
    }

    setClassDialog(null)
    setAlert(result.capacity ? { kind: 'capacity' } : null)
  }

  const confirmRemove = () => {
    if (!removeId) {
      return
    }

    setPlacements((items) => items.filter((item) => item.id !== removeId))
    setRemoveId(null)
    setClassDialog(null)
    setAlert(null)
  }

  const editing =
    classDialog?.name === 'edit'
      ? placements.find((item) => item.id === classDialog.placementId)
      : undefined
  const creatingItem =
    classDialog?.name === 'create'
      ? pending.find((item) => item.key === classDialog.subjectKey) ?? pending[0]
      : undefined
  const formOpen = Boolean(classDialog) && !removeId && !alert

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Construcción de horario
        </Text>
        <Text variant="body">
          Programa las asignaciones académicas por día, franja horaria y aula.
        </Text>
      </div>

      <ScheduleSelector
        yearId={year.id}
        campusId={campusId}
        shiftId={shiftId}
        groupId={group?.id ?? ''}
        gradeLabel={gradeLabel}
        year={year.year}
        groupCode={group?.code ?? ''}
        groupOptions={yearGroups.map((item) => ({ value: item.id, label: item.code }))}
        onYearChange={selectYear}
        onCampusChange={setCampusId}
        onShiftChange={setShiftId}
        onGroupChange={selectGroup}
      />

      {catalogError ? <p className={styles.notice}>{catalogError}</p> : null}

      {group ? (
        <>
          <ScheduleMetrics {...metrics} />
          <div className={styles.board}>
            <ScheduleGrid
              groupCode={group.code}
              shiftLabel={shiftLabel}
              readOnly={readOnly}
              blocks={blocks}
              onProgram={(day, bandId) => openCreate(day, bandId)}
              onEdit={(placementId) => {
                setAlert(null)
                setRemoveId(null)
                setClassDialog({ name: 'edit', placementId })
              }}
            />
            <PendingAssignments
              items={pending}
              readOnly={readOnly}
              onProgram={(subjectKey) => openCreate('Lunes', classBands()[0]?.id ?? '0630', subjectKey)}
            />
          </div>
        </>
      ) : (
        <p className={styles.notice}>
          Este año lectivo aún no tiene grupos activos para construir el horario.
        </p>
      )}

      {classDialog && group && (classDialog.name === 'create' || editing) ? (
        <ScheduleClassFormModal
          open={formOpen}
          mode={classDialog.name === 'edit' ? 'edit' : 'create'}
          groupCode={group.code}
          subject={
            editing
              ? scheduleSubject(editing.subjectKey)?.subject ?? 'Asignatura'
              : creatingItem?.subject ?? ''
          }
          teacherName={editing?.teacherName ?? creatingItem?.teacherName ?? ''}
          pendingHours={creatingItem?.pendingHours ?? 0}
          assignments={pending}
          day={classDialog.name === 'edit' ? editing?.day ?? 'Lunes' : classDialog.day}
          bandId={classDialog.name === 'edit' ? editing?.bandId ?? '0630' : classDialog.bandId}
          classroom={
            classDialog.name === 'edit'
              ? editing?.classroom ?? 'Aula 201'
              : defaultClassroom(creatingItem?.note ?? null)
          }
          subjectKey={editing?.subjectKey ?? creatingItem?.key ?? ''}
          onClose={() => setClassDialog(null)}
          onRemove={() => {
            if (editing) {
              setRemoveId(editing.id)
            }
          }}
          onSubmit={saveClass}
        />
      ) : null}

      <RemoveScheduleClassModal
        open={Boolean(removeId)}
        onClose={() => setRemoveId(null)}
        onConfirm={confirmRemove}
      />
      <ScheduleAlertModal alert={alert} onClose={() => setAlert(null)} />
    </section>
  )
}

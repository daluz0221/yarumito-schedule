import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../../auth'
import { ApiError, listarDocentes } from '../../../api'
import { academicYears } from '../../../content/academicYear'
import {
  buildPublicationView,
  pendingPublicationWarnings,
  publicationAuthor,
  publicationCopy,
  publicationVersion,
  type PublicationModal,
  type PublicationRecord,
} from '../../../content/publication'
import { seedPlacements } from '../../../content/scheduleConstruction'
import { formatTeacherName, type Teacher } from '../../../content/teachers'
import {
  applyConflictStatus,
  defaultValidationYearId,
  detectScheduleConflicts,
  formatValidationTimestamp,
} from '../../../content/validations'
import { Text } from '../../atoms/Text'
import { PublicationChecks } from '../PublicationChecks'
import { PublicationLog } from '../PublicationLog'
import { PublicationMetrics } from '../PublicationMetrics'
import { PublicationReadiness } from '../PublicationReadiness'
import { PublicationSummary } from '../PublicationSummary'
import { PublicationVersions } from '../PublicationVersions'
import { PublishBlockedModal } from '../PublishBlockedModal'
import { PublishScheduleModal } from '../PublishScheduleModal'
import { PublishSuccessModal } from '../PublishSuccessModal'
import { PublishWarningsModal } from '../PublishWarningsModal'
import styles from './PublicationWorkspace.module.css'

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export function PublicationWorkspace() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [yearId, setYearId] = useState(defaultValidationYearId)
  const [teachers, setTeachers] = useState<Teacher[]>([])
  const [catalogError, setCatalogError] = useState('')
  const [record, setRecord] = useState<PublicationRecord | null>(null)
  const [modal, setModal] = useState<PublicationModal>('none')

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
            : 'No se pudieron cargar los docentes. La publicación usa los nombres del horario local.',
        )
      })

    return () => controller.abort()
  }, [])

  const year = academicYears.find((item) => item.id === yearId) ?? academicYears[0]
  const author = publicationAuthor(user)
  const detected = useMemo(
    () => detectScheduleConflicts(year.id, seedPlacements, teachers),
    [year.id, teachers],
  )
  const reviewed = useMemo(() => applyConflictStatus(detected, []), [detected])
  const warnings = useMemo(() => pendingPublicationWarnings(reviewed), [reviewed])
  const view = buildPublicationView({
    year,
    conflicts: reviewed,
    author,
    published: Boolean(record),
  })

  const closeModal = () => setModal('none')

  const openValidations = () => {
    closeModal()
    navigate('/dashboard/validaciones')
  }

  const openSchedule = () => {
    closeModal()
    navigate('/dashboard/horario')
  }

  const selectYear = (nextYearId: string) => {
    setYearId(nextYearId)
    setRecord(null)
    setModal('none')
  }

  const startPublish = () => {
    if (record) {
      setModal('success')
      return
    }

    if (view.readiness === 'blocked') {
      setModal('blocked')
      return
    }

    if (view.readiness === 'warnings') {
      setModal('warnings')
      return
    }

    setModal('confirm')
  }

  const publish = () => {
    setRecord({
      version: publicationVersion,
      action: 'Publicado',
      actor: author,
      at: formatValidationTimestamp(new Date()),
    })
    setModal('success')
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          {publicationCopy.pageTitle}
        </Text>
        <Text variant="body">{publicationCopy.pageSubtitle}</Text>
      </div>

      <PublicationSummary year={year} view={view} onYearChange={selectYear} />
      <PublicationMetrics view={view} />
      <PublicationReadiness
        view={view}
        onSeeWarnings={() => setModal('warnings')}
        onSeeBlocked={() => setModal('blocked')}
      />
      {catalogError ? <p className={styles.notice}>{catalogError}</p> : null}
      <PublicationChecks checks={view.checks} />
      <PublicationVersions view={view} />
      <PublicationLog record={record} onPublish={startPublish} />

      <PublishWarningsModal
        open={modal === 'warnings'}
        warnings={warnings}
        onClose={closeModal}
        onReview={openValidations}
        onContinue={() => setModal('confirm')}
      />
      <PublishScheduleModal
        open={modal === 'confirm'}
        view={view}
        onClose={closeModal}
        onConfirm={publish}
      />
      <PublishBlockedModal
        open={modal === 'blocked'}
        errors={view.errors}
        onClose={closeModal}
        onReview={openValidations}
      />
      <PublishSuccessModal
        open={modal === 'success'}
        view={view}
        record={record}
        onClose={closeModal}
        onSeeSchedule={openSchedule}
      />
    </section>
  )
}

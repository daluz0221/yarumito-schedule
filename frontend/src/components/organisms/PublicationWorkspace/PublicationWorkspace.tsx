import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  anioEscolarVisible,
  ApiError,
  cargarDatosPublicacion,
  opcionesAniosEscolares,
  useCatalogoAniosEscolares,
  useCatalogoGrupos,
  type DatosPublicacion,
} from '../../../api'
import {
  buildPublicationView,
  pendingPublicationWarnings,
  publicationCopy,
  type PublicationModal,
} from '../../../content/publication'
import { Text } from '../../atoms/Text'
import { PublicationChecks } from '../PublicationChecks'
import { PublicationLog } from '../PublicationLog'
import { PublicationMetrics } from '../PublicationMetrics'
import { PublicationReadiness } from '../PublicationReadiness'
import { PublicationSummary } from '../PublicationSummary'
import { PublicationVersions } from '../PublicationVersions'
import { PublishBlockedModal } from '../PublishBlockedModal'
import { PublishWarningsModal } from '../PublishWarningsModal'
import styles from './PublicationWorkspace.module.css'

const sinDatos: DatosPublicacion = { tableros: [], conflictos: [], fallidos: 0 }

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

export function PublicationWorkspace() {
  const navigate = useNavigate()
  const { anios, error: aniosError } = useCatalogoAniosEscolares()
  const { grupos, error: gruposError } = useCatalogoGrupos()
  const [yearIdState, setYearIdState] = useState('')
  const [datos, setDatos] = useState<DatosPublicacion | null>(null)
  const [datosError, setDatosError] = useState('')
  const [modal, setModal] = useState<PublicationModal>('none')

  const yearId = anios?.some((item) => item.id === yearIdState)
    ? yearIdState
    : (anioEscolarVisible(anios ?? [])?.id ?? '')
  const year = anios?.find((item) => item.id === yearId) ?? null

  const grupoIds = useMemo(
    () =>
      (grupos ?? [])
        .filter((group) => group.anioEscolarId === yearId && group.activo)
        .map((group) => group.id),
    [grupos, yearId],
  )
  const sedes = useMemo(
    () =>
      [...new Set((grupos ?? []).filter((group) => grupoIds.includes(group.id)).map((g) => g.sedeNombre))]
        .filter(Boolean)
        .join(', '),
    [grupos, grupoIds],
  )
  const gruposListos = grupos !== null
  const consultaClave = `${yearId}:${grupoIds.join(',')}:${gruposListos}`
  const [consultaVista, setConsultaVista] = useState(consultaClave)

  if (consultaVista !== consultaClave) {
    setConsultaVista(consultaClave)
    setDatos(null)
    setDatosError('')
    setModal('none')
  }

  useEffect(() => {
    if (!year || !gruposListos) {
      return
    }

    const controller = new AbortController()

    cargarDatosPublicacion(grupoIds, year.id, controller.signal)
      .then((resultado) => {
        setDatos(resultado)
        setDatosError(
          resultado.fallidos > 0
            ? 'No se pudo consultar el horario de algunos grupos. El resumen puede estar incompleto.'
            : '',
        )
      })
      .catch((cause) => {
        if (isAbortError(cause)) {
          return
        }

        setDatos(sinDatos)
        setDatosError(
          cause instanceof ApiError && cause.message
            ? cause.message
            : 'No se pudo cargar el horario de este año.',
        )
      })

    return () => controller.abort()
  }, [year, gruposListos, grupoIds])

  const warnings = useMemo(
    () => pendingPublicationWarnings(datos?.conflictos ?? []),
    [datos],
  )

  const view = useMemo(
    () =>
      year
        ? buildPublicationView({
            year,
            datos: datos ?? sinDatos,
            campusName: sedes,
          })
        : null,
    [year, datos, sedes],
  )

  const closeModal = () => setModal('none')

  const openValidations = () => {
    closeModal()
    navigate('/dashboard/validaciones')
  }

  const catalogoError = aniosError || gruposError
  const cargando = Boolean(year) && datos === null && !datosError && !catalogoError

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          {publicationCopy.pageTitle}
        </Text>
        <Text variant="body">{publicationCopy.pageSubtitle}</Text>
      </div>

      {catalogoError ? (
        <p className={styles.alert} role="alert">
          {catalogoError}
        </p>
      ) : null}
      {datosError ? (
        <p className={styles.alert} role="alert">
          {datosError}
        </p>
      ) : null}

      {view && year ? (
        <>
          <PublicationSummary
            yearId={year.id}
            yearOptions={opcionesAniosEscolares(anios, aniosError)}
            view={view}
            onYearChange={setYearIdState}
          />
          {cargando ? <p className={styles.notice}>Cargando horario…</p> : null}
          <PublicationMetrics view={view} />
          <PublicationReadiness
            view={view}
            onSeeWarnings={() => setModal('warnings')}
            onSeeBlocked={() => setModal('blocked')}
          />
          <PublicationChecks checks={view.checks} />
          <PublicationVersions view={view} />
          <PublicationLog published={view.status === 'PUBLICADO'} />
        </>
      ) : !catalogoError ? (
        <p className={styles.notice}>
          {anios === null ? 'Cargando años lectivos…' : 'No hay años escolares registrados.'}
        </p>
      ) : null}

      <PublishWarningsModal
        open={modal === 'warnings'}
        warnings={warnings}
        onClose={closeModal}
        onReview={openValidations}
      />
      <PublishBlockedModal
        open={modal === 'blocked'}
        errors={view?.errors ?? 0}
        onClose={closeModal}
        onReview={openValidations}
      />
    </section>
  )
}

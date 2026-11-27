import { useEffect, useMemo, useState } from 'react'
import { useAnioDeTrabajo } from '../../../content/anioDeTrabajo'
import { useNavigate } from 'react-router-dom'
import {
  anioEscolarVisible,
  ApiError,
  cargarDatosPublicacion,
  crearBorradoresDeMejora,
  descartarBorradorHorario,
  horariosPublicables,
  listarVersionesHorario,
  opcionesAniosEscolares,
  publicarHorario,
  publicarHorariosEnBorrador,
  useCatalogoAniosEscolares,
  useCatalogoGrupos,
  type DatosPublicacion,
  type HorarioVersionResponse,
} from '../../../api'
import {
  buildPublicationView,
  pendingPublicationWarnings,
  publicationCopy,
  type PublicationModal,
} from '../../../content/publication'
import { Text } from '../../atoms/Text'
import { ConfirmActionModal } from '../ConfirmActionModal'
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
  const [yearIdState, elegirAnio] = useAnioDeTrabajo()
  const [datos, setDatos] = useState<DatosPublicacion | null>(null)
  const [datosError, setDatosError] = useState('')
  const [publicacionError, setPublicacionError] = useState('')
  const [aviso, setAviso] = useState('')
  const [publicando, setPublicando] = useState(false)
  const [confirmarPublicacion, setConfirmarPublicacion] = useState(false)
  const [recarga, setRecarga] = useState(0)
  const [modal, setModal] = useState<PublicationModal>('none')
  const [versiones, setVersiones] = useState<HorarioVersionResponse[] | null>(null)
  const [horarioAPublicar, setHorarioAPublicar] = useState<string | null>(null)
  const [descartarId, setDescartarId] = useState<string | null>(null)
  const [descartando, setDescartando] = useState(false)
  const [creandoBorrador, setCreandoBorrador] = useState(false)

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
    setPublicacionError('')
    setAviso('')
    setModal('none')
    setConfirmarPublicacion(false)
    setHorarioAPublicar(null)
    setDescartarId(null)
    if (!consultaVista.startsWith(`${yearId}:`)) {
      setVersiones(null)
    }
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
  }, [year, gruposListos, grupoIds, recarga])

  useEffect(() => {
    if (!year) {
      return
    }

    const controller = new AbortController()

    listarVersionesHorario(year.id, controller.signal)
      .then((lista) => {
        if (!controller.signal.aborted) {
          setVersiones(lista)
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || controller.signal.aborted) {
          return
        }

        setVersiones([])
        setPublicacionError(
          cause instanceof ApiError && cause.message
            ? cause.message
            : 'No se pudieron cargar las versiones del horario.',
        )
      })

    return () => controller.abort()
  }, [year, recarga])

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

  const closeModal = () => {
    setModal('none')
    setConfirmarPublicacion(false)
    setHorarioAPublicar(null)
  }

  const publicar = async (horarioId: string | null) => {
    if (publicando || (!horarioId && !datos)) {
      return
    }

    const habiaPublicado = (versiones ?? []).some((version) => version.estado === 'PUBLICADO')
    const esMejora = horarioId
      ? (versiones ?? []).some((version) => version.id === horarioId && version.version > 1)
      : (versiones ?? []).some((version) => version.estado === 'BORRADOR' && version.version > 1)

    setPublicando(true)
    setPublicacionError('')
    setAviso('')

    try {
      if (horarioId) {
        await publicarHorario(horarioId)
        setAviso(
          habiaPublicado && esMejora
            ? 'Se publicó la nueva versión. La versión que estaba publicada quedó archivada.'
            : 'Se publicó 1 horario.',
        )
      } else {
        const publicados = await publicarHorariosEnBorrador(datos?.tableros ?? [])
        setAviso(
          habiaPublicado && esMejora
            ? 'Se publicó la nueva versión. La versión que estaba publicada quedó archivada.'
            : publicados === 1
              ? 'Se publicó 1 horario.'
              : `Se publicaron ${publicados} horarios.`,
        )
      }
      setModal('none')
      setConfirmarPublicacion(false)
      setHorarioAPublicar(null)
      setRecarga((valor) => valor + 1)
    } catch (cause) {
      setPublicacionError(
        cause instanceof ApiError && cause.message
          ? cause.message
          : 'No se pudo publicar el horario.',
      )
      setRecarga((valor) => valor + 1)
    } finally {
      setPublicando(false)
    }
  }

  const crearBorradores = async (ids: string[]) => {
    if (creandoBorrador || publicando || ids.length === 0) {
      return
    }

    setCreandoBorrador(true)
    setPublicacionError('')
    setAviso('')

    try {
      const resultados = await crearBorradoresDeMejora(ids)
      const nuevos = resultados.filter((item) => item.nuevo).length
      setAviso(
        nuevos > 0
          ? 'Se creó el borrador de mejora. El horario publicado sigue vigente. Edítalo en Construcción de horario.'
          : 'Ya hay un borrador de mejora. Puedes seguir editándolo en Construcción de horario.',
      )
      setRecarga((valor) => valor + 1)
    } catch (cause) {
      setPublicacionError(
        cause instanceof ApiError && cause.message
          ? cause.message
          : 'No se pudo crear el borrador de mejora.',
      )
    } finally {
      setCreandoBorrador(false)
    }
  }

  const descartarBorrador = async () => {
    if (!descartarId || descartando) {
      return
    }

    setDescartando(true)
    setPublicacionError('')
    setAviso('')

    try {
      await descartarBorradorHorario(descartarId)
      setAviso('Se descartó el borrador. El horario publicado sigue vigente.')
      setDescartarId(null)
      setRecarga((valor) => valor + 1)
    } catch (cause) {
      setPublicacionError(
        cause instanceof ApiError && cause.message
          ? cause.message
          : 'No se pudo descartar el borrador.',
      )
    } finally {
      setDescartando(false)
    }
  }

  const catalogoError = aniosError || gruposError
  const cargando = Boolean(year) && datos === null && !datosError && !catalogoError
  const puedePublicar = horariosPublicables(datos?.tableros ?? []).length > 0 && !cargando
  const sedesConBorrador = new Set(
    (versiones ?? []).filter((version) => version.estado === 'BORRADOR').map((version) => version.sedeId),
  )
  const publicadosSinBorrador = (versiones ?? []).filter(
    (version) => version.estado === 'PUBLICADO' && !sedesConBorrador.has(version.sedeId),
  )
  const hayBorradorMejora = (versiones ?? []).some(
    (version) => version.estado === 'BORRADOR' && version.version > 1,
  )
  const modoCrear = !puedePublicar && publicadosSinBorrador.length > 0 && year?.status !== 'CERRADO'
  const ocupado = publicando || creandoBorrador || descartando

  const pedirPublicacion = (horarioId: string | null = null) => {
    if (!view || ocupado) {
      return
    }

    if (!horarioId && !puedePublicar) {
      return
    }

    setHorarioAPublicar(horarioId)

    if (view.errors > 0) {
      setConfirmarPublicacion(false)
      setModal('blocked')
      return
    }

    if (warnings.length > 0) {
      setConfirmarPublicacion(true)
      setModal('warnings')
      return
    }

    void publicar(horarioId)
  }

  const openValidations = () => {
    closeModal()
    navigate('/dashboard/validaciones')
  }

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
      {publicacionError ? (
        <p className={styles.alert} role="alert">
          {publicacionError}
        </p>
      ) : null}
      {aviso ? <p className={styles.success}>{aviso}</p> : null}

      {view && year ? (
        <>
          <PublicationSummary
            yearId={year.id}
            yearOptions={opcionesAniosEscolares(anios, aniosError)}
            view={view}
            onYearChange={elegirAnio}
          />
          {cargando ? <p className={styles.notice}>Cargando horario…</p> : null}
          <PublicationMetrics view={view} />
          <PublicationReadiness
            view={view}
            onSeeWarnings={() => {
              setConfirmarPublicacion(false)
              setModal('warnings')
            }}
            onSeeBlocked={() => setModal('blocked')}
          />
          <PublicationChecks checks={view.checks} />
          <PublicationVersions
            versions={versiones}
            busy={ocupado}
            onCreateDraft={
              year.status === 'CERRADO'
                ? undefined
                : (horarioId) => void crearBorradores([horarioId])
            }
            onPublishDraft={(horarioId) => pedirPublicacion(horarioId)}
            onDiscardDraft={setDescartarId}
          />
          <PublicationLog
            message={
              !view.hasSchedule
                ? publicationCopy.noScheduleNote
                : hayBorradorMejora
                  ? publicationCopy.logDraft
                  : view.status === 'PUBLICADO'
                    ? publicationCopy.logPublished
                    : publicationCopy.logEmpty
            }
            note={
              hayBorradorMejora
                ? publicationCopy.logDraftNote
                : view.status === 'PUBLICADO'
                  ? publicationCopy.logPublishedNote
                  : publicationCopy.logNote
            }
            actionLabel={
              modoCrear
                ? creandoBorrador
                  ? publicationCopy.creatingDraft
                  : publicationCopy.createDraft
                : publicando
                  ? publicationCopy.publishing
                  : hayBorradorMejora
                    ? publicationCopy.publishDraft
                    : view.status === 'PUBLICADO'
                      ? publicationCopy.published
                      : publicationCopy.publish
            }
            busy={ocupado}
            disabled={modoCrear ? year?.status === 'CERRADO' : !puedePublicar}
            onAction={
              modoCrear
                ? () => void crearBorradores(publicadosSinBorrador.map((version) => version.id))
                : () => pedirPublicacion(null)
            }
          />
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
        onConfirm={confirmarPublicacion ? () => void publicar(horarioAPublicar) : undefined}
        confirming={publicando}
      />
      <PublishBlockedModal
        open={modal === 'blocked'}
        errors={view?.errors ?? 0}
        onClose={closeModal}
        onReview={openValidations}
      />
      <ConfirmActionModal
        open={Boolean(descartarId)}
        title="Descartar borrador"
        description="Se elimina el borrador de mejora y el horario publicado sigue vigente."
        confirmLabel={publicationCopy.discardDraft}
        busyLabel={publicationCopy.discardingDraft}
        submitting={descartando}
        onClose={() => {
          if (!descartando) {
            setDescartarId(null)
          }
        }}
        onConfirm={() => void descartarBorrador()}
      />
    </section>
  )
}

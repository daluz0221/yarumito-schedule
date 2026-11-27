import { useEffect, useMemo, useRef, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useAuth } from '../../../auth'
import {
  anioEscolarVisible,
  ApiError,
  consultarAuditoriaPorEntidad,
  consultarAuditoriaPorUsuario,
  consultarHorarioGrupo,
  etiquetaUsuario,
  listarAsignacionesPorGrupo,
  listarUsuarios,
  opcionesAniosEscolares,
  useCatalogoAniosEscolares,
  useCatalogoGrupos,
} from '../../../api'
import type { AsignacionAcademicaResponse } from '../../../api/asignaciones'
import type { ClaseProgramadaResponse } from '../../../api/horarios'
import type { UsuarioListado } from '../../../api/usuarios'
import { guardarAnioDeTrabajo, leerAnioDeTrabajo } from '../../../content/anioDeTrabajo'
import {
  consultaInicial,
  ENTIDADES_GRUPO,
  esEntidad,
  esEntidadGrupo,
  esEntidadOtra,
  etiquetaEntidad,
  ordenarRegistros,
  UUID,
  type ConsultaAuditoria,
  type EntidadGrupo,
} from '../../../content/auditoria'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import { AuditoriaOtrosRegistros, type SolicitudOtroRegistro } from '../AuditoriaOtrosRegistros'
import { HistorialTabla, ResultadoConsulta } from './AuditoriaTabla'
import { useResolvedorAuditoria } from './useResolvedorAuditoria'
import styles from './AuditoriaWorkspace.module.css'

const DIAS: Record<ClaseProgramadaResponse['dia'], string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
}

const ORDEN_DIA: ClaseProgramadaResponse['dia'][] = [
  'LUNES',
  'MARTES',
  'MIERCOLES',
  'JUEVES',
  'VIERNES',
]

const VACIO_REGISTRO = 'No hay registros de auditoría para este registro.'
const VACIO_GRUPO = 'No hay registros de auditoría para las asignaciones y clases de este grupo.'
const VACIO_GRUPO_SIN_DATOS = 'Este grupo no tiene asignaciones ni clases programadas.'

type Alcance = 'registro' | 'grupo'

function esAbort(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function opcionesDeUsuarios(
  usuarios: UsuarioListado[],
  sesion: { id: string; correo: string; rol: UsuarioListado['rol'] } | null,
  seleccionado: string,
) {
  const opciones = usuarios.map((item) => ({
    value: item.id,
    label: etiquetaUsuario(item),
  }))
  const ids = new Set(opciones.map((item) => item.value))

  if (sesion?.id && !ids.has(sesion.id)) {
    opciones.unshift({
      value: sesion.id,
      label: etiquetaUsuario({
        correo: sesion.correo,
        rol: sesion.rol,
        activo: true,
      }),
    })
    ids.add(sesion.id)
  }

  if (seleccionado && !ids.has(seleccionado)) {
    opciones.push({
      value: seleccionado,
      label: 'Cuenta no listada',
    })
  }

  return opciones
}

function etiquetaEstadoAsignacion(estado: string) {
  if (estado === 'BORRADOR') {
    return 'Borrador'
  }

  if (estado === 'CONFIRMADA') {
    return 'Confirmada'
  }

  return estado
}

function etiquetaAsignacion(item: AsignacionAcademicaResponse) {
  return [
    item.asignaturaNombre,
    item.docenteNombre,
    item.grupoCodigo,
    etiquetaEstadoAsignacion(item.estado),
  ]
    .map((parte) => parte?.trim())
    .filter(Boolean)
    .join(' · ')
}

function etiquetaClase(clase: ClaseProgramadaResponse) {
  const dia = DIAS[clase.dia] ?? clase.dia
  const franja = Number.isFinite(clase.franjaNumero) ? `franja ${clase.franjaNumero}` : ''

  return [dia, franja, clase.asignaturaNombre, clase.aulaNombre]
    .map((parte) => parte?.trim())
    .filter(Boolean)
    .join(' · ')
}

function mensajeError(cause: unknown, respaldo: string) {
  return cause instanceof ApiError ? cause.message : respaldo
}

export function AuditoriaWorkspace() {
  const { user } = useAuth()
  const { resolvedor, docentes, docentesError } = useResolvedorAuditoria(user?.rol === 'RECTOR')
  const [searchParams] = useSearchParams()
  const entidadInicial = searchParams.get('entidad') ?? ''
  const idInicial = searchParams.get('id') ?? ''
  const grupoInicial = searchParams.get('grupo') ?? ''
  const entidadSeccion = useRef<HTMLDivElement>(null)
  const usuarioSeccion = useRef<HTMLDivElement>(null)
  const otrosSeccion = useRef<HTMLDivElement>(null)
  const entidadAbort = useRef<AbortController | null>(null)
  const usuarioAbort = useRef<AbortController | null>(null)
  const grupoElegido = useRef(false)
  const grupoAnterior = useRef<string | null>(null)
  const [yearId, setYearId] = useState(leerAnioDeTrabajo)
  const [groupId, setGroupId] = useState('')
  const [tipo, setTipo] = useState<EntidadGrupo>(
    esEntidadGrupo(entidadInicial) ? entidadInicial : 'AsignacionAcademica',
  )
  const [solicitudOtra, setSolicitudOtra] = useState<SolicitudOtroRegistro | null>(
    esEntidadOtra(entidadInicial) && UUID.test(idInicial)
      ? { entidad: entidadInicial, id: idInicial, n: 1 }
      : null,
  )
  const [registroId, setRegistroId] = useState('')
  const [consultaEntidad, setConsultaEntidad] = useState<ConsultaAuditoria>(consultaInicial)
  const [vacioEntidad, setVacioEntidad] = useState(VACIO_REGISTRO)
  const [tituloEntidad, setTituloEntidad] = useState('')
  const [alcance, setAlcance] = useState<Alcance>('registro')
  const [idConsultado, setIdConsultado] = useState('')
  const [usuarioId, setUsuarioId] = useState(user?.id ?? '')
  const [usuarioIdError, setUsuarioIdError] = useState('')
  const [consultaUsuario, setConsultaUsuario] = useState<ConsultaAuditoria>(consultaInicial)
  const [usuarios, setUsuarios] = useState<UsuarioListado[]>([])
  const [usuariosError, setUsuariosError] = useState('')
  const [usuariosCargando, setUsuariosCargando] = useState(true)
  const [asignaciones, setAsignaciones] = useState<AsignacionAcademicaResponse[] | null>(null)
  const [clases, setClases] = useState<ClaseProgramadaResponse[] | null>(null)
  const [catalogoError, setCatalogoError] = useState('')
  const [catalogoVisto, setCatalogoVisto] = useState('')
  const [yearFijado, setYearFijado] = useState(false)
  const [enlacePreparado, setEnlacePreparado] = useState(false)

  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares(
    user?.rol === 'RECTOR',
  )
  const { grupos, error: gruposError, loading: gruposLoading } = useCatalogoGrupos(
    user?.rol === 'RECTOR',
  )

  const yearGroups = useMemo(() => {
    return (grupos ?? [])
      .filter((group) => group.anioEscolarId === yearId && group.activo)
      .sort(
        (left, right) =>
          left.gradoNivel - right.gradoNivel || left.codigo.localeCompare(right.codigo, 'es'),
      )
  }, [grupos, yearId])

  const grupo = yearGroups.find((item) => item.id === groupId) ?? null
  const catalogoCargando = user?.rol === 'RECTOR' && Boolean(groupId) && catalogoVisto !== groupId

  const etiquetas = useMemo(() => {
    const mapa = new Map<string, string>()

    ;(asignaciones ?? []).forEach((item) => {
      if (item.id) {
        mapa.set(item.id, etiquetaAsignacion(item))
      }
    })
    ;(clases ?? []).forEach((clase) => {
      if (clase.id) {
        mapa.set(clase.id, etiquetaClase(clase))
      }
    })

    return mapa
  }, [asignaciones, clases])

  const opcionesAsignacion = useMemo(() => {
    return [...(asignaciones ?? [])]
      .filter((item) => item.id)
      .sort((left, right) =>
        (left.asignaturaNombre ?? '').localeCompare(right.asignaturaNombre ?? '', 'es'),
      )
      .map((item) => ({
        value: item.id,
        label: etiquetaAsignacion(item),
      }))
  }, [asignaciones])

  const opcionesClase = useMemo(() => {
    return [...(clases ?? [])]
      .filter((clase) => clase.id)
      .sort((left, right) => {
        const dia = ORDEN_DIA.indexOf(left.dia) - ORDEN_DIA.indexOf(right.dia)

        if (dia !== 0) {
          return dia
        }

        return (left.franjaNumero ?? 0) - (right.franjaNumero ?? 0)
      })
      .map((clase) => ({
        value: clase.id,
        label: etiquetaClase(clase),
      }))
  }, [clases])

  const opcionesRegistroBase = tipo === 'ClaseProgramada' ? opcionesClase : opcionesAsignacion
  const opcionesRegistro = useMemo(() => {
    const opciones = [...opcionesRegistroBase]

    if (registroId && !opciones.some((item) => item.value === registroId)) {
      opciones.unshift({
        value: registroId,
        label: etiquetas.get(registroId) ?? 'Registro abierto desde otra consulta',
      })
    }

    return [{ value: '', label: catalogoCargando ? 'Cargando registros…' : 'Elige un registro' }, ...opciones]
  }, [catalogoCargando, etiquetas, opcionesRegistroBase, registroId])

  function abrirOtro(entidad: string, id: string) {
    if (esEntidadOtra(entidad)) {
      setSolicitudOtra((actual) => ({ entidad, id, n: (actual?.n ?? 0) + 1 }))
    }
  }

  async function cargarEntidad(entidadValor: string, idValor: string) {
    if (!esEntidad(entidadValor) || !UUID.test(idValor)) {
      return
    }

    if (!esEntidadGrupo(entidadValor)) {
      abrirOtro(entidadValor, idValor)
      return
    }

    entidadAbort.current?.abort()
    const controller = new AbortController()
    entidadAbort.current = controller
    setAlcance('registro')
    setIdConsultado(idValor)
    setTipo(entidadValor)
    setRegistroId(idValor)
    setVacioEntidad(VACIO_REGISTRO)
    setTituloEntidad(etiquetaEntidad(entidadValor))
    setConsultaEntidad({ estado: 'cargando', registros: [], error: '' })

    try {
      const registros = await consultarAuditoriaPorEntidad(entidadValor, idValor, controller.signal)

      if (controller.signal.aborted) {
        return
      }

      setConsultaEntidad({
        estado: 'listo',
        registros: ordenarRegistros(Array.isArray(registros) ? registros : []),
        error: '',
      })
    } catch (cause) {
      if (esAbort(cause) || controller.signal.aborted) {
        return
      }

      setConsultaEntidad({
        estado: 'error',
        registros: [],
        error: mensajeError(cause, 'No se pudo consultar el historial de la entidad.'),
      })
    }
  }

  useEffect(() => {
    return () => {
      entidadAbort.current?.abort()
      usuarioAbort.current?.abort()
    }
  }, [])

  const aniosListos = Boolean(anios?.length) && !(grupoInicial && grupos === null)

  if (!yearFijado && aniosListos && anios) {
    const grupoEnlace = (grupos ?? []).find((item) => item.id === grupoInicial)
    const recordado = leerAnioDeTrabajo()
    const siguiente =
      grupoEnlace && anios.some((year) => year.id === grupoEnlace.anioEscolarId)
        ? grupoEnlace.anioEscolarId
        : anios.some((year) => year.id === recordado)
          ? recordado
          : (anioEscolarVisible(anios)?.id ?? '')

    setYearFijado(true)
    setYearId(siguiente)
  }

  if (
    !enlacePreparado &&
    user?.rol === 'RECTOR' &&
    esEntidadGrupo(entidadInicial) &&
    UUID.test(idInicial)
  ) {
    setEnlacePreparado(true)
    setAlcance('registro')
    setIdConsultado(idInicial)
    setTipo(entidadInicial)
    setRegistroId(idInicial)
    setVacioEntidad(VACIO_REGISTRO)
    setTituloEntidad(etiquetaEntidad(entidadInicial))
    setConsultaEntidad({ estado: 'cargando', registros: [], error: '' })
  }

  useEffect(() => {
    if (!enlacePreparado || !esEntidadGrupo(entidadInicial) || !UUID.test(idInicial)) {
      return
    }

    const controller = new AbortController()
    entidadAbort.current?.abort()
    entidadAbort.current = controller

    consultarAuditoriaPorEntidad(entidadInicial, idInicial, controller.signal)
      .then((registros) => {
        if (controller.signal.aborted) {
          return
        }

        setConsultaEntidad({
          estado: 'listo',
          registros: ordenarRegistros(Array.isArray(registros) ? registros : []),
          error: '',
        })
      })
      .catch((cause: unknown) => {
        if (esAbort(cause) || controller.signal.aborted) {
          return
        }

        setConsultaEntidad({
          estado: 'error',
          registros: [],
          error: mensajeError(cause, 'No se pudo consultar el historial de la entidad.'),
        })
      })

    return () => controller.abort()
  }, [enlacePreparado, entidadInicial, idInicial])

  useEffect(() => {
    if (!yearId || grupos === null) {
      return
    }

    if (!grupoElegido.current) {
      const destino = yearGroups.some((group) => group.id === grupoInicial)
        ? grupoInicial
        : (yearGroups[0]?.id ?? '')

      setGroupId(destino)
      grupoElegido.current = true
      return
    }

    setGroupId((current) =>
      yearGroups.some((group) => group.id === current) ? current : (yearGroups[0]?.id ?? ''),
    )
  }, [grupoInicial, grupos, yearGroups, yearId])

  useEffect(() => {
    if (!groupId) {
      return
    }

    if (grupoAnterior.current === null) {
      grupoAnterior.current = groupId
      return
    }

    if (grupoAnterior.current === groupId) {
      return
    }

    grupoAnterior.current = groupId
    setRegistroId('')
  }, [groupId])

  const tituloVisible =
    alcance === 'registro' && idConsultado
      ? (etiquetas.get(idConsultado) || tituloEntidad)
      : tituloEntidad

  useEffect(() => {
    if (user?.rol !== 'RECTOR') {
      return
    }

    const controller = new AbortController()
    listarUsuarios(controller.signal)
      .then((lista) => {
        if (controller.signal.aborted) {
          return
        }

        setUsuarios(Array.isArray(lista) ? lista : [])
        setUsuariosError('')
        setUsuariosCargando(false)
      })
      .catch((cause: unknown) => {
        if (esAbort(cause) || controller.signal.aborted) {
          return
        }

        setUsuarios([])
        setUsuariosError(mensajeError(cause, 'No se pudo cargar el listado de usuarios.'))
        setUsuariosCargando(false)
      })

    return () => controller.abort()
  }, [user?.rol])

  useEffect(() => {
    if (user?.rol !== 'RECTOR' || !groupId) {
      return
    }

    const controller = new AbortController()
    const catalogoClave = groupId

    Promise.all([
      listarAsignacionesPorGrupo(groupId, controller.signal),
      consultarHorarioGrupo(groupId, controller.signal),
    ])
      .then(([lista, tablero]) => {
        if (controller.signal.aborted) {
          return
        }

        setAsignaciones(Array.isArray(lista) ? lista : [])
        setClases(Array.isArray(tablero?.clasesProgramadas) ? tablero.clasesProgramadas : [])
        setCatalogoError('')
        setCatalogoVisto(catalogoClave)
      })
      .catch((cause: unknown) => {
        if (esAbort(cause) || controller.signal.aborted) {
          return
        }

        setAsignaciones([])
        setClases([])
        setCatalogoError(
          mensajeError(cause, 'No se pudieron cargar las asignaciones y clases del grupo.'),
        )
        setCatalogoVisto(catalogoClave)
      })

    return () => controller.abort()
  }, [groupId, user?.rol])

  const opcionesUsuario = opcionesDeUsuarios(usuarios, user, usuarioId)
  const yearOptions = opcionesAniosEscolares(anios, aniosError)
  const groupOptions = gruposLoading
    ? [{ value: '', label: 'Cargando grupos…' }]
    : yearGroups.length
      ? yearGroups.map((item) => ({ value: item.id, label: item.codigo }))
      : [{ value: '', label: 'Sin grupos activos' }]

  async function cargarUsuario(idValor: string) {
    const id = idValor.trim()
    const error = id ? (UUID.test(id) ? '' : 'Elige una cuenta válida.') : 'Elige una cuenta.'
    setUsuarioId(id)
    setUsuarioIdError(error)

    if (error) {
      return
    }

    usuarioAbort.current?.abort()
    const controller = new AbortController()
    usuarioAbort.current = controller
    setConsultaUsuario({ estado: 'cargando', registros: [], error: '' })

    try {
      const registros = await consultarAuditoriaPorUsuario(id, controller.signal)

      if (controller.signal.aborted) {
        return
      }

      setConsultaUsuario({
        estado: 'listo',
        registros: ordenarRegistros(Array.isArray(registros) ? registros : []),
        error: '',
      })
    } catch (cause) {
      if (esAbort(cause) || controller.signal.aborted) {
        return
      }

      setConsultaUsuario({
        estado: 'error',
        registros: [],
        error: mensajeError(cause, 'No se pudo consultar el historial de la cuenta.'),
      })
    }
  }

  async function cargarHistorialGrupo() {
    if (!groupId || catalogoCargando || asignaciones === null || clases === null) {
      return
    }

    const codigo = grupo?.codigo ?? 'seleccionado'
    entidadAbort.current?.abort()
    const controller = new AbortController()
    entidadAbort.current = controller
    setAlcance('grupo')
    setIdConsultado('')
    setTituloEntidad(`Historial del grupo ${codigo}`)

    if (asignaciones.length === 0 && clases.length === 0) {
      setVacioEntidad(VACIO_GRUPO_SIN_DATOS)
      setConsultaEntidad({ estado: 'listo', registros: [], error: '' })
      return
    }

    setVacioEntidad(VACIO_GRUPO)
    setConsultaEntidad({ estado: 'cargando', registros: [], error: '' })

    try {
      const listas = await Promise.all([
        ...asignaciones
          .filter((item) => item.id)
          .map((item) =>
            consultarAuditoriaPorEntidad('AsignacionAcademica', item.id, controller.signal),
          ),
        ...clases
          .filter((clase) => clase.id)
          .map((clase) =>
            consultarAuditoriaPorEntidad('ClaseProgramada', clase.id, controller.signal),
          ),
      ])

      if (controller.signal.aborted) {
        return
      }

      setConsultaEntidad({
        estado: 'listo',
        registros: ordenarRegistros(listas.flatMap((lista) => (Array.isArray(lista) ? lista : []))),
        error: '',
      })
    } catch (cause) {
      if (esAbort(cause) || controller.signal.aborted) {
        return
      }

      setConsultaEntidad({
        estado: 'error',
        registros: [],
        error: mensajeError(cause, 'No se pudo consultar el historial del grupo.'),
      })
    }
  }

  function consultarRegistro(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!registroId) {
      return
    }

    void cargarEntidad(tipo, registroId)
  }

  function consultarUsuario(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void cargarUsuario(usuarioId)
  }

  function verEntidad(entidadValor: string, idValor: string) {
    const destino = esEntidadGrupo(entidadValor) ? entidadSeccion : otrosSeccion
    destino.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    void cargarEntidad(entidadValor, idValor)
  }

  function verUsuario(idValor: string) {
    usuarioSeccion.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    void cargarUsuario(idValor)
  }

  if (user?.rol !== 'RECTOR') {
    return (
      <section className={styles.workspace}>
        <div className={styles.intro}>
          <Text as="h2" variant="pageTitle">
            Auditoría
          </Text>
          <Text variant="body">
            El historial de cambios del sistema está reservado al rector.
          </Text>
        </div>
        <Card padding="lg">
          <p className={styles.status}>
            Inicia sesión con la cuenta del rector para consultar estos registros.
          </p>
        </Card>
      </section>
    )
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Auditoría
        </Text>
        <Text variant="body">
          Revisa quién creó, cambió, movió o retiró una asignación académica, una clase del
          horario, un aula, un docente, un plan de estudios, una restricción o un título
          profesional.
        </Text>
      </div>

      <div ref={entidadSeccion}>
        <Card className={styles.section} padding="lg">
          <SectionHeader
            title="Historial del grupo"
            description="Elige el año y el grupo. Puedes ver todo el grupo o una asignación o clase por su nombre."
          />
          {aniosError ? (
            <p className={styles.error} role="alert">
              {aniosError}
            </p>
          ) : null}
          {gruposError ? (
            <p className={styles.error} role="alert">
              {gruposError}
            </p>
          ) : null}
          {catalogoError ? (
            <p className={styles.error} role="alert">
              {catalogoError}
            </p>
          ) : null}
          <form className={styles.entityForm} onSubmit={consultarRegistro}>
            <SelectField
              id="auditoria-anio"
              label="Año lectivo"
              name="year"
              value={yearOptions.some((item) => item.value === yearId) ? yearId : ''}
              options={yearOptions}
              disabled={aniosLoading || Boolean(aniosError) || !anios?.length}
              onChange={(value) => {
                grupoElegido.current = true
                setYearFijado(true)
                setYearId(value)
                guardarAnioDeTrabajo(value)
              }}
            />
            <SelectField
              id="auditoria-grupo"
              label="Grupo"
              name="group"
              value={groupOptions.some((item) => item.value === groupId) ? groupId : ''}
              options={groupOptions}
              disabled={gruposLoading || Boolean(gruposError) || yearGroups.length === 0}
              onChange={(value) => {
                grupoElegido.current = true
                setGroupId(value)
              }}
            />
            <SelectField
              id="auditoria-tipo"
              label="Tipo"
              name="tipo"
              value={tipo}
              options={[...ENTIDADES_GRUPO]}
              onChange={(value) => {
                if (esEntidadGrupo(value)) {
                  setTipo(value)
                  setRegistroId('')
                }
              }}
            />
            <SelectField
              id="auditoria-registro"
              label={tipo === 'ClaseProgramada' ? 'Clase' : 'Asignación'}
              name="registro"
              value={opcionesRegistro.some((item) => item.value === registroId) ? registroId : ''}
              options={opcionesRegistro}
              disabled={catalogoCargando || Boolean(catalogoError) || !groupId}
              hint={
                tipo === 'ClaseProgramada'
                  ? 'Día, franja, asignatura y aula.'
                  : 'Asignatura, docente, grupo y estado.'
              }
              onChange={setRegistroId}
            />
            <div className={styles.actionsRow}>
              <Button
                type="button"
                variant="primary"
                size="sm"
                disabled={
                  !groupId ||
                  catalogoCargando ||
                  Boolean(catalogoError) ||
                  consultaEntidad.estado === 'cargando'
                }
                onClick={() => {
                  void cargarHistorialGrupo()
                }}
              >
                Ver historial del grupo
              </Button>
              <Button
                type="submit"
                variant="outline"
                size="sm"
                disabled={!registroId || consultaEntidad.estado === 'cargando'}
              >
                Consultar este registro
              </Button>
            </div>
          </form>
          <ResultadoConsulta
            consulta={consultaEntidad}
            vacio={vacioEntidad}
            titulo={tituloVisible}
            inicial="Elige un grupo o un registro para ver su historial."
          >
            <HistorialTabla
              registros={consultaEntidad.registros}
              etiquetas={etiquetas}
              usuarios={usuarios}
              sesion={user}
              origen={alcance}
              resolvedor={resolvedor}
              onEntidad={verEntidad}
              onUsuario={verUsuario}
            />
          </ResultadoConsulta>
        </Card>
      </div>

      <div ref={otrosSeccion}>
        <AuditoriaOtrosRegistros
          usuarios={usuarios}
          sesion={user}
          resolvedor={resolvedor}
          docentes={docentes}
          docentesError={docentesError}
          solicitud={solicitudOtra}
          onUsuario={verUsuario}
          onEntidad={verEntidad}
        />
      </div>

      <div ref={usuarioSeccion}>
        <Card className={styles.section} padding="lg">
          <SectionHeader
            title="Acciones de una cuenta"
            description="Muestra los cambios que esa cuenta guardó sobre asignaciones, clases, aulas, docentes, planes de estudio, restricciones y títulos."
          />
          {usuariosError && opcionesUsuario.length ? (
            <p className={styles.error} role="alert">
              {usuariosError} Puedes consultar la cuenta de esta sesión.
            </p>
          ) : null}
          <form className={styles.userForm} onSubmit={consultarUsuario}>
            <SelectField
              id="auditoria-usuario"
              label="Cuenta"
              name="usuarioId"
              value={opcionesUsuario.some((item) => item.value === usuarioId) ? usuarioId : ''}
              options={
                opcionesUsuario.length
                  ? opcionesUsuario
                  : [{ value: '', label: usuariosCargando ? 'Cargando cuentas…' : 'Sin cuentas' }]
              }
              disabled={usuariosCargando || opcionesUsuario.length === 0}
              error={opcionesUsuario.length === 0 ? usuariosError : usuarioIdError}
              onChange={(value) => {
                setUsuarioId(value)
                if (usuarioIdError) {
                  setUsuarioIdError('')
                }
              }}
            />
            <div className={styles.action}>
              <Button
                type="submit"
                variant="primary"
                size="sm"
                disabled={consultaUsuario.estado === 'cargando' || usuariosCargando || !usuarioId}
              >
                Consultar cuenta
              </Button>
            </div>
          </form>
          <ResultadoConsulta
            consulta={consultaUsuario}
            vacio="Esta cuenta no tiene cambios registrados en el historial."
            titulo=""
            inicial="Elige una cuenta para ver las acciones que registró."
          >
            <HistorialTabla
              registros={consultaUsuario.registros}
              etiquetas={etiquetas}
              usuarios={usuarios}
              sesion={user}
              origen="usuario"
              resolvedor={resolvedor}
              onEntidad={verEntidad}
              onUsuario={verUsuario}
            />
          </ResultadoConsulta>
        </Card>
      </div>
    </section>
  )
}

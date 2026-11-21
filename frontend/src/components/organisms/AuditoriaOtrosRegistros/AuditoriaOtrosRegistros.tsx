import { useEffect, useRef, useState, type FormEvent } from 'react'
import {
  anioEscolarVisible,
  aRestriccionVista,
  ApiError,
  consultarAuditoriaPorEntidad,
  etiquetaTipoAula,
  listarAulasPorSede,
  listarPlanesEstudio,
  listarPlanesEstudioRetirados,
  listarRestriccionesPorDocenteYAnio,
  listarRestriccionesRetiradasPorDocenteYAnio,
  listarTitulosActivosPorDocente,
  listarTitulosRetiradosPorDocente,
  opcionesAniosEscolares,
  opcionesCatalogoSedes,
  sedeVisible,
  useCatalogoAniosEscolares,
  useCatalogoSedes,
} from '../../../api'
import type { UsuarioListado } from '../../../api/usuarios'
import {
  consultaInicial,
  ENTIDADES_OTRAS,
  esEntidadOtra,
  etiquetaEntidad,
  ordenarRegistros,
  UUID,
  type ConsultaAuditoria,
  type EntidadOtra,
} from '../../../content/auditoria'
import type { ResolvedorAuditoria } from '../../../content/auditoriaCambios'
import { labelAvailability } from '../../../content/teacherRestrictions'
import type { Teacher } from '../../../content/teachers'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import { HistorialTabla, ResultadoConsulta } from '../AuditoriaWorkspace/AuditoriaTabla'
import { nombreDocente } from '../AuditoriaWorkspace/useResolvedorAuditoria'
import styles from '../AuditoriaWorkspace/AuditoriaWorkspace.module.css'

export type SolicitudOtroRegistro = {
  entidad: EntidadOtra
  id: string
  n: number
}

export type AuditoriaOtrosRegistrosProps = {
  usuarios: UsuarioListado[]
  sesion: { id: string; correo: string; rol: UsuarioListado['rol'] } | null
  resolvedor: ResolvedorAuditoria
  docentes: Teacher[] | null
  docentesError: boolean
  solicitud: SolicitudOtroRegistro | null
  onUsuario: (usuarioId: string) => void
  onEntidad: (entidad: string, idEntidad: string) => void
}

type Opcion = { value: string; label: string }
type Fuente = { clave: string; opciones: Opcion[]; error: string }

// Filtros que cada tipo necesita para poder listar sus registros.
const CONFIG: Record<
  EntidadOtra,
  { sede?: boolean; anio?: boolean; docente?: boolean; registro: string; hint: string }
> = {
  Aula: { sede: true, registro: 'Aula', hint: 'Nombre, tipo y estado del aula.' },
  Docente: { registro: 'Docente', hint: 'Apellidos y nombres.' },
  PlanEstudios: {
    anio: true,
    registro: 'Asignatura del plan',
    hint: 'Año, grado y asignatura.',
  },
  RestriccionDocente: {
    anio: true,
    docente: true,
    registro: 'Restricción',
    hint: 'Día, franja y tipo de restricción.',
  },
  TituloProfesional: {
    docente: true,
    registro: 'Título',
    hint: 'Nombre del título e institución.',
  },
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeError(cause: unknown, respaldo: string) {
  return cause instanceof ApiError && cause.message ? cause.message : respaldo
}

function unicas(opciones: Opcion[]) {
  const mapa = new Map<string, Opcion>()
  opciones.forEach((item) => mapa.set(item.value, item))
  return [...mapa.values()].sort((left, right) => left.label.localeCompare(right.label, 'es'))
}

export function AuditoriaOtrosRegistros({
  usuarios,
  sesion,
  resolvedor,
  docentes,
  docentesError,
  solicitud,
  onUsuario,
  onEntidad,
}: AuditoriaOtrosRegistrosProps) {
  const { sedes, error: sedesError } = useCatalogoSedes()
  const { anios, error: aniosError } = useCatalogoAniosEscolares()
  const [tipo, setTipo] = useState<EntidadOtra>(solicitud?.entidad ?? 'Aula')
  const [sedeElegida, setSedeElegida] = useState('')
  const [anioElegido, setAnioElegido] = useState('')
  const [docenteElegido, setDocenteElegido] = useState('')
  const [registroId, setRegistroId] = useState(solicitud?.id ?? '')
  const franjas = resolvedor.franjas
  const [fuente, setFuente] = useState<Fuente | null>(null)
  const [consulta, setConsulta] = useState<ConsultaAuditoria>(consultaInicial)
  const [idConsultado, setIdConsultado] = useState('')
  const consultaAbort = useRef<AbortController | null>(null)
  const solicitudAtendida = useRef(0)

  const sedeId = sedes?.some((item) => item.id === sedeElegida)
    ? sedeElegida
    : (sedeVisible(sedes ?? [])?.id ?? '')
  const anioId = anios?.some((item) => item.id === anioElegido)
    ? anioElegido
    : (anioEscolarVisible(anios ?? [])?.id ?? '')
  const docenteId = docentes?.some((item) => item.id === docenteElegido) ? docenteElegido : ''
  const config = CONFIG[tipo]

  // Clave de lo que hay que consultar para listar los registros; vacía si
  // falta algún filtro.
  const clave = (() => {
    if (tipo === 'Aula') {
      return sedeId ? `Aula|${sedeId}` : ''
    }

    if (tipo === 'PlanEstudios') {
      return anioId ? `PlanEstudios|${anioId}` : ''
    }

    if (tipo === 'RestriccionDocente') {
      return anioId && docenteId ? `RestriccionDocente|${anioId}|${docenteId}` : ''
    }

    if (tipo === 'TituloProfesional') {
      return docenteId ? `TituloProfesional|${docenteId}` : ''
    }

    return ''
  })()

  useEffect(() => {
    if (!clave) {
      return
    }

    const controller = new AbortController()
    const { signal } = controller

    const cargar = async (): Promise<Opcion[]> => {
      if (tipo === 'Aula') {
        const aulas = await listarAulasPorSede(sedeId, signal)

        return aulas.map((aula) => ({
          value: aula.id,
          label: `${aula.nombre} · ${etiquetaTipoAula(aula.tipo)}${aula.activa ? '' : ' · Inactiva'}`,
        }))
      }

      if (tipo === 'PlanEstudios') {
        const [activos, retirados] = await Promise.all([
          listarPlanesEstudio(signal),
          listarPlanesEstudioRetirados(signal),
        ])

        return [...activos, ...retirados]
          .filter((plan) => plan.anioEscolarId === anioId)
          .map((plan) => ({
            value: plan.id,
            label: `${plan.anioEscolar} · ${plan.gradoNombre} · ${plan.asignaturaNombre}${
              retirados.some((item) => item.id === plan.id) ? ' · Retirado' : ''
            }`,
          }))
      }

      if (tipo === 'RestriccionDocente') {
        const [activas, retiradas] = await Promise.all([
          listarRestriccionesPorDocenteYAnio(docenteId, anioId, signal),
          listarRestriccionesRetiradasPorDocenteYAnio(docenteId, anioId, signal),
        ])
        const describir = (
          vista: NonNullable<ReturnType<typeof aRestriccionVista>>,
          retirada: boolean,
        ) => ({
          value: vista.id,
          label: `${vista.day} · ${franjas.get(vista.bandId) ?? 'Franja'} · ${labelAvailability(vista.status)}${
            retirada ? ' · Retirada' : ''
          }`,
        })

        return [
          ...activas.map((item) => describir(item, false)),
          ...retiradas.flatMap((item) => {
            const vista = aRestriccionVista(item)
            return vista ? [describir(vista, true)] : []
          }),
        ]
      }

      const [activos, retirados] = await Promise.all([
        listarTitulosActivosPorDocente(docenteId, signal),
        listarTitulosRetiradosPorDocente(docenteId, signal),
      ])

      return [
        ...activos.map((titulo) => ({
          value: titulo.id,
          label: `${titulo.name} · ${titulo.institution}`,
        })),
        ...retirados.map((titulo) => ({
          value: titulo.id,
          label: `${titulo.name} · ${titulo.institution} · Retirado`,
        })),
      ]
    }

    cargar()
      .then((opciones) => {
        if (!signal.aborted) {
          setFuente({ clave, opciones: unicas(opciones), error: '' })
        }
      })
      .catch((cause) => {
        if (isAbortError(cause) || signal.aborted) {
          return
        }

        setFuente({
          clave,
          opciones: [],
          error: mensajeError(cause, 'No se pudieron cargar los registros.'),
        })
      })

    return () => controller.abort()
    // franjas solo mejora las etiquetas; no debe repetir la consulta.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [clave])

  const opcionesDocente: Opcion[] = (docentes ?? []).map((docente) => ({
    value: docente.id,
    label: nombreDocente(docente),
  }))

  const cargandoRegistros =
    tipo === 'Docente' ? docentes === null : Boolean(clave) && fuente?.clave !== clave
  const errorRegistros = tipo === 'Docente' ? (docentesError ? 'No se pudo cargar el listado de docentes.' : '') : fuente?.clave === clave ? fuente.error : ''
  const opcionesBase: Opcion[] =
    tipo === 'Docente' ? opcionesDocente : fuente?.clave === clave ? fuente.opciones : []

  const etiquetas = new Map(opcionesBase.map((item) => [item.value, item.label]))

  const opcionesRegistro = [
    {
      value: '',
      label: cargandoRegistros
        ? 'Cargando registros…'
        : clave || tipo === 'Docente'
          ? opcionesBase.length
            ? 'Elige un registro'
            : 'Sin registros'
          : 'Completa los filtros',
    },
    ...(registroId && !opcionesBase.some((item) => item.value === registroId)
      ? [{ value: registroId, label: 'Registro abierto desde otra consulta' }]
      : []),
    ...opcionesBase,
  ]

  async function consultarRegistro(entidad: EntidadOtra, id: string) {
    if (!UUID.test(id)) {
      return
    }

    consultaAbort.current?.abort()
    const controller = new AbortController()
    consultaAbort.current = controller
    setIdConsultado(id)
    setConsulta({ estado: 'cargando', registros: [], error: '' })

    try {
      const registros = await consultarAuditoriaPorEntidad(entidad, id, controller.signal)

      if (controller.signal.aborted) {
        return
      }

      setConsulta({
        estado: 'listo',
        registros: ordenarRegistros(Array.isArray(registros) ? registros : []),
        error: '',
      })
    } catch (cause) {
      if (isAbortError(cause) || controller.signal.aborted) {
        return
      }

      setConsulta({
        estado: 'error',
        registros: [],
        error: mensajeError(cause, 'No se pudo consultar el historial del registro.'),
      })
    }
  }

  // Apertura desde otra parte de la pantalla (p. ej. «Ver historial de la
  // entidad» en la actividad de una cuenta) o desde un enlace.
  useEffect(() => {
    if (!solicitud || solicitud.n === solicitudAtendida.current) {
      return
    }

    solicitudAtendida.current = solicitud.n
    setTipo(solicitud.entidad)
    setRegistroId(solicitud.id)
    void consultarRegistro(solicitud.entidad, solicitud.id)
  }, [solicitud])

  useEffect(() => () => consultaAbort.current?.abort(), [])

  function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (registroId) {
      void consultarRegistro(tipo, registroId)
    }
  }

  const titulo = idConsultado ? (etiquetas.get(idConsultado) ?? etiquetaEntidad(tipo)) : ''
  const yearOptions = opcionesAniosEscolares(anios, aniosError)

  return (
    <Card className={styles.section} padding="lg">
      <SectionHeader
        title="Historial de otros registros"
        description="Consulta los cambios de un aula, un docente, un plan de estudios, una restricción o un título profesional."
      />
      {aniosError ? (
        <p className={styles.error} role="alert">
          {aniosError}
        </p>
      ) : null}
      {sedesError ? (
        <p className={styles.error} role="alert">
          {sedesError}
        </p>
      ) : null}
      {errorRegistros ? (
        <p className={styles.error} role="alert">
          {errorRegistros}
        </p>
      ) : null}
      <form className={styles.entityForm} onSubmit={enviar}>
        <SelectField
          id="auditoria-otro-tipo"
          label="Tipo"
          name="tipoOtro"
          value={tipo}
          options={[...ENTIDADES_OTRAS]}
          onChange={(value) => {
            if (esEntidadOtra(value)) {
              setTipo(value)
              setRegistroId('')
            }
          }}
        />
        {config.sede ? (
          <SelectField
            id="auditoria-otro-sede"
            label="Sede"
            name="sedeOtro"
            value={sedeId}
            options={opcionesCatalogoSedes(sedes, sedesError)}
            disabled={Boolean(sedesError) || !sedes?.length}
            onChange={(value) => {
              setSedeElegida(value)
              setRegistroId('')
            }}
          />
        ) : null}
        {config.anio ? (
          <SelectField
            id="auditoria-otro-anio"
            label="Año lectivo"
            name="anioOtro"
            value={yearOptions.some((item) => item.value === anioId) ? anioId : ''}
            options={yearOptions}
            disabled={Boolean(aniosError) || !anios?.length}
            onChange={(value) => {
              setAnioElegido(value)
              setRegistroId('')
            }}
          />
        ) : null}
        {config.docente ? (
          <SelectField
            id="auditoria-otro-docente"
            label="Docente"
            name="docenteOtro"
            value={docenteId}
            options={[
              {
                value: '',
                label: docentes === null ? 'Cargando docentes…' : 'Elige un docente',
              },
              ...opcionesDocente,
            ]}
            disabled={docentes === null || docentesError}
            onChange={(value) => {
              setDocenteElegido(value)
              setRegistroId('')
            }}
          />
        ) : null}
        <SelectField
          id="auditoria-otro-registro"
          label={config.registro}
          name="registroOtro"
          value={opcionesRegistro.some((item) => item.value === registroId) ? registroId : ''}
          options={opcionesRegistro}
          disabled={cargandoRegistros || Boolean(errorRegistros) || (!clave && tipo !== 'Docente')}
          hint={config.hint}
          onChange={setRegistroId}
        />
        <div className={styles.actionsRow}>
          <Button
            type="submit"
            variant="primary"
            size="sm"
            disabled={!registroId || consulta.estado === 'cargando'}
          >
            Consultar este registro
          </Button>
        </div>
      </form>
      <ResultadoConsulta
        consulta={consulta}
        vacio="No hay registros de auditoría para este registro."
        titulo={titulo}
        inicial="Elige un tipo y un registro para ver su historial."
      >
        <HistorialTabla
          registros={consulta.registros}
          etiquetas={etiquetas}
          usuarios={usuarios}
          sesion={sesion}
          resolvedor={resolvedor}
          origen="registro"
          onEntidad={onEntidad}
          onUsuario={onUsuario}
        />
      </ResultadoConsulta>
      <Text as="p" variant="caption">
        Los cambios aparecen aquí solo si el sistema los registró al guardarlos.
      </Text>
    </Card>
  )
}

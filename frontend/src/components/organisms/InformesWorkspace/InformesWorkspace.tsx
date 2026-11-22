import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useAuth } from '../../../auth'
import {
  ApiError,
  consultarInformeAreasAsignaturas,
  consultarInformePerfilesProfesionales,
  consultarInformePlantaDocente,
  descargarInformeAreasAsignaturas,
  descargarInformePerfilesProfesionales,
  descargarInformePlantaDocente,
  listarCatalogoAreas,
  listarDocentes,
  opcionesAniosEscolares,
  useCatalogoAniosEscolares,
  type FormatoInformeInstitucional,
  type InformeAreasAsignaturas,
  type InformePerfilProfesional,
  type InformePlantaDocente,
  type TipoIdoneidadInforme,
  type TipoVinculacionInforme,
} from '../../../api'
import { labelNivelTitulo, labelTipoIdoneidad } from '../../../content/teacherProfile'
import { labelEstado, labelVinculacion, type Teacher, type TeacherStatus } from '../../../content/teachers'
import { Badge, type BadgeTone } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Table } from '../../atoms/Table'
import { Text } from '../../atoms/Text'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import { StatCard } from '../../molecules/StatCard'
import styles from './InformesWorkspace.module.css'

type TipoInforme = 'planta' | 'areas' | 'perfiles'

const INFORMES: { id: TipoInforme; label: string; hint: string }[] = [
  {
    id: 'planta',
    label: 'Planta docente',
    hint: 'Relación de docentes con su área de nombramiento, vinculación y horas contratadas. Con un año lectivo incluye la carga asignada y las horas extras.',
  },
  {
    id: 'areas',
    label: 'Áreas y asignaturas',
    hint: 'Áreas académicas y las asignaturas que pertenecen a cada una.',
  },
  {
    id: 'perfiles',
    label: 'Perfiles profesionales',
    hint: 'Títulos profesionales e idoneidades de los docentes, con vigencia a la fecha del informe.',
  },
]

const ESTADOS = [
  { value: '', label: 'Cualquier estado' },
  { value: 'ACTIVO', label: 'Activo' },
  { value: 'LICENCIA', label: 'Licencia' },
  { value: 'RETIRADO', label: 'Retirado' },
]

const VINCULACIONES = [
  { value: '', label: 'Cualquier vinculación' },
  { value: 'PLANTA', label: 'Planta' },
  { value: 'PROVISIONAL', label: 'Provisional' },
  { value: 'CONTRATO', label: 'Contrato' },
]

const ACTIVIDAD = [
  { value: '', label: 'Activas e inactivas' },
  { value: 'true', label: 'Solo activas' },
  { value: 'false', label: 'Solo inactivas' },
]

const TIPOS_IDONEIDAD = [
  { value: '', label: 'Cualquier tipo' },
  { value: 'PRINCIPAL', label: 'Principal' },
  { value: 'AUTORIZADA', label: 'Autorizada' },
  { value: 'EXCEPCIONAL', label: 'Excepcional' },
]

type Consulta =
  | { estado: 'inicial' }
  | { estado: 'cargando' }
  | { estado: 'listo'; tipo: 'planta'; informe: InformePlantaDocente }
  | { estado: 'listo'; tipo: 'areas'; informe: InformeAreasAsignaturas }
  | { estado: 'listo'; tipo: 'perfiles'; informe: InformePerfilProfesional }
  | { estado: 'error'; mensaje: string }

function esAbort(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function mensajeError(cause: unknown, fallback: string) {
  if (
    cause instanceof ApiError &&
    cause.message &&
    cause.message !== 'No se pudo completar la solicitud'
  ) {
    return cause.message
  }

  return fallback
}

function texto(value: string | null | undefined) {
  const limpio = value?.trim()
  return limpio ? limpio : '—'
}

function numero(value: number | null | undefined) {
  return value ?? 0
}

function siNo(value: boolean) {
  return value ? 'Sí' : 'No'
}

function fecha(value: string | null | undefined) {
  const iso = value?.slice(0, 10)

  if (!iso || !/^\d{4}-\d{2}-\d{2}$/.test(iso)) {
    return '—'
  }

  const [year, month, day] = iso.split('-')
  return `${day}/${month}/${year}`
}

function vigencia(desde: string | null, hasta: string | null) {
  if (!desde && !hasta) {
    return '—'
  }

  if (!hasta) {
    return `Desde ${fecha(desde)}`
  }

  return `${fecha(desde)} – ${fecha(hasta)}`
}

function tonoEstado(estado: TeacherStatus | null): BadgeTone {
  if (estado === 'ACTIVO') {
    return 'success'
  }

  if (estado === 'LICENCIA') {
    return 'warning'
  }

  return 'neutral'
}

function etiquetaEstado(estado: TeacherStatus | null) {
  return estado ? labelEstado(estado) : '—'
}

function etiquetaVinculacion(tipo: TipoVinculacionInforme | null) {
  return tipo ? labelVinculacion(tipo) : '—'
}

function etiquetaTipo(tipo: TipoIdoneidadInforme | null) {
  return tipo ? labelTipoIdoneidad(tipo) : '—'
}

function nombreDocente(docente: Teacher) {
  return `${docente.apellidos} ${docente.nombres}`.trim()
}

async function docentesParaFiltro(signal: AbortSignal) {
  const size = 100
  const primera = await listarDocentes({ page: 0, size }, signal)
  const docentes = [...primera.content]
  const paginas = Math.ceil(primera.total / size)

  for (let page = 1; page < paginas; page += 1) {
    const siguiente = await listarDocentes({ page, size }, signal)
    docentes.push(...siguiente.content)
  }

  return docentes.sort((left, right) =>
    nombreDocente(left).localeCompare(nombreDocente(right), 'es'),
  )
}

function nombreInforme(tipo: TipoInforme) {
  if (tipo === 'planta') {
    return 'la planta docente'
  }

  if (tipo === 'areas') {
    return 'las áreas y asignaturas'
  }

  return 'los perfiles profesionales'
}

export function InformesWorkspace() {
  const { user } = useAuth()
  const esRector = user?.rol === 'RECTOR'
  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares(esRector)
  const [tipo, setTipo] = useState<TipoInforme>('planta')
  const [areaId, setAreaId] = useState('')
  const [estado, setEstado] = useState('')
  const [vinculacion, setVinculacion] = useState('')
  const [activa, setActiva] = useState('')
  const [docenteId, setDocenteId] = useState('')
  const [anioId, setAnioId] = useState('')
  const [tipoIdoneidad, setTipoIdoneidad] = useState('')
  const [areas, setAreas] = useState<{ id: string; nombre: string }[] | null>(null)
  const [areasError, setAreasError] = useState('')
  const [docentes, setDocentes] = useState<Teacher[]>([])
  const [docentesError, setDocentesError] = useState('')
  const [consulta, setConsulta] = useState<Consulta>({ estado: 'inicial' })
  const [descarga, setDescarga] = useState<FormatoInformeInstitucional | null>(null)
  const [descargaError, setDescargaError] = useState('')
  const consultaAbort = useRef<AbortController | null>(null)
  const descargaAbort = useRef<AbortController | null>(null)

  useEffect(() => {
    if (!esRector) {
      return
    }

    const controller = new AbortController()

    listarCatalogoAreas(controller.signal)
      .then((items) => {
        setAreas(
          [...items].sort((left, right) => left.nombre.localeCompare(right.nombre, 'es')),
        )
        setAreasError('')
      })
      .catch((cause) => {
        if (esAbort(cause) || controller.signal.aborted) {
          return
        }

        setAreas([])
        setAreasError(mensajeError(cause, 'No se pudo cargar el listado de áreas.'))
      })

    docentesParaFiltro(controller.signal)
      .then((items) => {
        setDocentes(items)
        setDocentesError('')
      })
      .catch((cause) => {
        if (esAbort(cause) || controller.signal.aborted) {
          return
        }

        setDocentes([])
        setDocentesError(mensajeError(cause, 'No se pudo cargar el listado de docentes.'))
      })

    return () => controller.abort()
  }, [esRector])

  const areaActiva = areaId && areas && !areas.some((area) => area.id === areaId) ? '' : areaId
  const docenteActivo =
    docenteId && docentes.some((docente) => docente.id === docenteId) ? docenteId : ''
  const anioActivo = anioId && anios?.some((anio) => anio.id === anioId) ? anioId : ''
  const [tipoVisto, setTipoVisto] = useState(tipo)

  if (tipoVisto !== tipo) {
    setTipoVisto(tipo)
    setConsulta({ estado: 'inicial' })
    setDescarga(null)
    setDescargaError('')
  }

  useEffect(() => {
    consultaAbort.current?.abort()
    descargaAbort.current?.abort()
  }, [tipo])

  useEffect(
    () => () => {
      consultaAbort.current?.abort()
      descargaAbort.current?.abort()
    },
    [],
  )

  const informeActual = INFORMES.find((item) => item.id === tipo) ?? INFORMES[0]
  const ocupado = consulta.estado === 'cargando' || descarga !== null
  const puedeConsultar = esRector && !ocupado
  const opcionesAreas = !areas
    ? [{ value: '', label: 'Cargando áreas...' }]
    : [
        { value: '', label: 'Todas las áreas' },
        ...areas.map((area) => ({ value: area.id, label: area.nombre })),
      ]

  function filtrosPlanta() {
    return {
      areaNombramientoId: areaActiva || undefined,
      estado: estado || undefined,
      tipoVinculacion: vinculacion || undefined,
      anioEscolarId: anioActivo || undefined,
      docenteId: docenteActivo || undefined,
    }
  }

  function filtrosAreas() {
    return {
      areaId: areaActiva || undefined,
      activa: activa === '' ? undefined : activa === 'true',
    }
  }

  function filtrosPerfiles() {
    return {
      docenteId: docenteActivo || undefined,
      areaId: areaActiva || undefined,
      tipo: tipoIdoneidad || undefined,
    }
  }

  function consultar(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault()

    if (!esRector || ocupado) {
      return
    }

    consultaAbort.current?.abort()

    const tipoSolicitado = tipo
    const controller = new AbortController()
    consultaAbort.current = controller
    setConsulta({ estado: 'cargando' })
    setDescargaError('')

    const solicitud =
      tipoSolicitado === 'planta'
        ? consultarInformePlantaDocente(filtrosPlanta(), controller.signal).then(
            (informe) => ({ tipo: 'planta' as const, informe }),
          )
        : tipoSolicitado === 'areas'
          ? consultarInformeAreasAsignaturas(filtrosAreas(), controller.signal).then(
              (informe) => ({ tipo: 'areas' as const, informe }),
            )
          : consultarInformePerfilesProfesionales(filtrosPerfiles(), controller.signal).then(
              (informe) => ({ tipo: 'perfiles' as const, informe }),
            )

    solicitud
      .then((resultado) => {
        if (controller.signal.aborted) {
          return
        }

        setConsulta({ estado: 'listo', ...resultado })
      })
      .catch((cause) => {
        if (esAbort(cause) || controller.signal.aborted) {
          return
        }

        setConsulta({
          estado: 'error',
          mensaje: mensajeError(
            cause,
            `No se pudo consultar el informe de ${nombreInforme(tipoSolicitado)}.`,
          ),
        })
      })
  }

  async function descargar(formato: FormatoInformeInstitucional) {
    if (!esRector || ocupado) {
      return
    }

    const controller = new AbortController()
    descargaAbort.current = controller
    setDescarga(formato)
    setDescargaError('')

    try {
      if (tipo === 'planta') {
        await descargarInformePlantaDocente(formato, filtrosPlanta(), controller.signal)
      } else if (tipo === 'areas') {
        await descargarInformeAreasAsignaturas(formato, filtrosAreas(), controller.signal)
      } else {
        await descargarInformePerfilesProfesionales(
          formato,
          filtrosPerfiles(),
          controller.signal,
        )
      }
    } catch (cause) {
      if (!esAbort(cause) && !controller.signal.aborted) {
        setDescargaError(
          mensajeError(
            cause,
            `No se pudo descargar el ${formato === 'pdf' ? 'PDF' : 'Excel'} de ${nombreInforme(tipo)}.`,
          ),
        )
      }
    } finally {
      if (descargaAbort.current === controller) {
        setDescarga(null)
      }
    }
  }

  const planta =
    consulta.estado === 'listo' && consulta.tipo === 'planta' ? consulta.informe : null
  const areasInforme =
    consulta.estado === 'listo' && consulta.tipo === 'areas' ? consulta.informe : null
  const perfiles =
    consulta.estado === 'listo' && consulta.tipo === 'perfiles' ? consulta.informe : null

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Informes institucionales
        </Text>
        <Text variant="body">
          Consulta la planta docente, las áreas y asignaturas, o los perfiles profesionales, y
          descarga el mismo resultado en PDF o Excel.
        </Text>
      </div>

      {!esRector ? (
        <p className={styles.alert} role="alert">
          Estos informes son exclusivos del rector. Inicia sesión con esa cuenta para consultarlos.
        </p>
      ) : null}

      <div className={styles.tabs} role="tablist" aria-label="Tipo de informe">
        {INFORMES.map((item) => (
          <button
            key={item.id}
            type="button"
            role="tab"
            aria-selected={tipo === item.id}
            className={tipo === item.id ? styles.tabActive : styles.tab}
            disabled={!esRector}
            onClick={() => setTipo(item.id)}
          >
            {item.label}
          </button>
        ))}
      </div>

      <Card>
        <form className={styles.filters} onSubmit={consultar}>
          {tipo === 'planta' || tipo === 'perfiles' ? (
            <SelectField
              id="informe-teacher"
              label="Docente"
              name="teacher"
              value={docenteActivo}
              options={[
                { value: '', label: docentes.length ? 'Todos los docentes' : 'Cargando docentes...' },
                ...docentes.map((docente) => ({
                  value: docente.id,
                  label: nombreDocente(docente),
                })),
              ]}
              disabled={!esRector || Boolean(docentesError)}
              onChange={setDocenteId}
            />
          ) : null}
          {tipo === 'planta' ? (
            <SelectField
              id="informe-year"
              label="Año lectivo"
              name="year"
              value={anioActivo}
              options={[
                {
                  value: '',
                  label: aniosLoading ? 'Cargando años lectivos...' : 'Sin carga académica',
                },
                ...(anios && anios.length > 0 ? opcionesAniosEscolares(anios, '') : []),
              ]}
              disabled={!esRector || aniosLoading || Boolean(aniosError)}
              onChange={setAnioId}
            />
          ) : null}
          <SelectField
            id="informe-area"
            label={
              tipo === 'planta'
                ? 'Área de nombramiento'
                : tipo === 'perfiles'
                  ? 'Área de la idoneidad'
                  : 'Área'
            }
            name="area"
            value={areas ? areaActiva : ''}
            options={opcionesAreas}
            disabled={!esRector || areas === null || Boolean(areasError)}
            onChange={setAreaId}
          />
          {tipo === 'planta' ? (
            <>
              <SelectField
                id="informe-status"
                label="Estado"
                name="status"
                value={estado}
                options={ESTADOS}
                disabled={!esRector}
                onChange={setEstado}
              />
              <SelectField
                id="informe-contract"
                label="Vinculación"
                name="contract"
                value={vinculacion}
                options={VINCULACIONES}
                disabled={!esRector}
                onChange={setVinculacion}
              />
            </>
          ) : null}
          {tipo === 'areas' ? (
            <SelectField
              id="informe-active"
              label="Estado del área"
              name="active"
              value={activa}
              options={ACTIVIDAD}
              disabled={!esRector}
              onChange={setActiva}
            />
          ) : null}
          {tipo === 'perfiles' ? (
            <SelectField
              id="informe-suitability"
              label="Tipo de idoneidad"
              name="suitability"
              value={tipoIdoneidad}
              options={TIPOS_IDONEIDAD}
              disabled={!esRector}
              onChange={setTipoIdoneidad}
            />
          ) : null}
          <div className={styles.actions}>
            <Button type="submit" disabled={!puedeConsultar}>
              {consulta.estado === 'cargando' ? 'Consultando...' : 'Consultar informe'}
            </Button>
            <Button
              type="button"
              variant="outline"
              disabled={!puedeConsultar}
              onClick={() => descargar('pdf')}
            >
              {descarga === 'pdf' ? 'Descargando PDF...' : 'Descargar PDF'}
            </Button>
            <Button
              type="button"
              variant="outline"
              disabled={!puedeConsultar}
              onClick={() => descargar('excel')}
            >
              {descarga === 'excel' ? 'Descargando Excel...' : 'Descargar Excel'}
            </Button>
          </div>
        </form>
      </Card>

      <p className={styles.hint}>{informeActual.hint}</p>

      {areasError ? (
        <p className={styles.alert} role="alert">
          {areasError} Puedes consultar sin filtrar por área.
        </p>
      ) : null}
      {(tipo === 'planta' || tipo === 'perfiles') && docentesError ? (
        <p className={styles.alert} role="alert">
          {docentesError} Puedes consultar sin filtrar por docente.
        </p>
      ) : null}
      {tipo === 'planta' && aniosError ? (
        <p className={styles.alert} role="alert">
          {aniosError} Puedes consultar la planta sin carga académica.
        </p>
      ) : null}
      {consulta.estado === 'error' ? (
        <p className={styles.alert} role="alert">
          {consulta.mensaje}
        </p>
      ) : null}
      {descargaError ? (
        <p className={styles.alert} role="alert">
          {descargaError}
        </p>
      ) : null}
      {consulta.estado === 'inicial' ? (
        <p className={styles.notice}>
          Ajusta los filtros si lo necesitas y pulsa Consultar informe.
        </p>
      ) : null}
      {consulta.estado === 'cargando' ? (
        <p className={styles.notice}>Generando el informe...</p>
      ) : null}

      {planta ? <PlantaResultado informe={planta} /> : null}
      {areasInforme ? <AreasResultado informe={areasInforme} /> : null}
      {perfiles ? <PerfilesResultado informe={perfiles} /> : null}
    </section>
  )
}

function PlantaResultado({ informe }: { informe: InformePlantaDocente }) {
  const docentes = informe.docentes ?? []
  const conCarga = Boolean(informe.anioEscolarId)
  const descripcion = conCarga
    ? `Docentes del filtro actual. La carga corresponde al año ${informe.anioEscolar ?? ''}.`
    : 'Docentes incluidos en el filtro actual. Elige un año lectivo para ver la carga asignada.'

  return (
    <>
      <div className={styles.metrics} aria-label="Resumen de planta docente">
        <StatCard value={String(numero(informe.cantidadDocentes))} label="Docentes" wash="total" />
        <StatCard
          value={String(numero(informe.totalHorasSemanalesContratadas))}
          label="Horas semanales contratadas"
          wash="active"
        />
        {conCarga ? (
          <>
            <StatCard
              value={String(numero(informe.totalHorasSemanalesAsignadas))}
              label="Horas semanales asignadas"
              wash="warning"
            />
            <StatCard
              value={String(numero(informe.totalHorasSemanalesExtras))}
              label="Horas semanales extras"
              wash="total"
            />
          </>
        ) : null}
      </div>
      <Card className={styles.panel}>
        <SectionHeader title="Planta docente" description={descripcion} />
        {docentes.length === 0 ? (
          <p className={styles.notice}>No hay docentes para los filtros seleccionados.</p>
        ) : (
          <div className={styles.tableHost}>
            <Table minWidth={conCarga ? 980 : 760}>
              <thead>
                <tr>
                  <th>Docente</th>
                  <th>Área de nombramiento</th>
                  <th>Estado</th>
                  <th>Vinculación</th>
                  <th>Horas contratadas</th>
                  {conCarga ? (
                    <>
                      <th>Horas asignadas</th>
                      <th>Horas extras</th>
                    </>
                  ) : null}
                </tr>
              </thead>
              <tbody>
                {docentes.map((docente) => (
                  <tr key={docente.docenteId}>
                    <td>{texto(docente.nombreCompleto)}</td>
                    <td>{texto(docente.areaNombramiento)}</td>
                    <td>
                      <Badge tone={tonoEstado(docente.estado)}>
                        {etiquetaEstado(docente.estado)}
                      </Badge>
                    </td>
                    <td>{etiquetaVinculacion(docente.tipoVinculacion)}</td>
                    <td>{numero(docente.horasSemanalesContratadas)}</td>
                    {conCarga ? (
                      <>
                        <td>{numero(docente.horasSemanalesAsignadas)}</td>
                        <td>{numero(docente.horasSemanalesExtras)}</td>
                      </>
                    ) : null}
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        )}
      </Card>
    </>
  )
}

function AreasResultado({ informe }: { informe: InformeAreasAsignaturas }) {
  const areas = informe.areas ?? []
  const asignaturas = areas.flatMap((area) =>
    (area.asignaturas ?? []).map((asignatura) => ({ area, asignatura })),
  )

  return (
    <>
      <div className={styles.metrics} aria-label="Resumen de áreas y asignaturas">
        <StatCard value={String(numero(informe.cantidadAreas))} label="Áreas" wash="total" />
        <StatCard
          value={String(numero(informe.cantidadAsignaturas))}
          label="Asignaturas"
          wash="active"
        />
      </div>
      <Card className={styles.panel}>
        <SectionHeader title="Áreas" description="Clasificación y cantidad de asignaturas." />
        {areas.length === 0 ? (
          <p className={styles.notice}>No hay áreas para los filtros seleccionados.</p>
        ) : (
          <div className={styles.tableHost}>
            <Table minWidth={820}>
              <thead>
                <tr>
                  <th>Área</th>
                  <th>Código</th>
                  <th>Estado</th>
                  <th>Clasificación</th>
                  <th>Aplicación</th>
                  <th>Asignaturas</th>
                </tr>
              </thead>
              <tbody>
                {areas.map((area) => (
                  <tr key={area.areaId}>
                    <td>{texto(area.nombre)}</td>
                    <td>{texto(area.codigo)}</td>
                    <td>
                      <Badge tone={area.activa ? 'success' : 'neutral'}>
                        {area.activa ? 'Activa' : 'Inactiva'}
                      </Badge>
                    </td>
                    <td>{area.obligatoria ? 'Obligatoria' : 'Opcional'}</td>
                    <td>{area.soloMedia ? 'Solo media' : 'Todos los grados'}</td>
                    <td>{numero(area.cantidadAsignaturas)}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        )}
      </Card>
      <Card className={styles.panel}>
        <SectionHeader
          title="Asignaturas"
          description="Asignaturas de las áreas incluidas en el informe."
        />
        {asignaturas.length === 0 ? (
          <p className={styles.notice}>No hay asignaturas para esta consulta.</p>
        ) : (
          <div className={styles.tableHost}>
            <Table minWidth={920}>
              <thead>
                <tr>
                  <th>Área</th>
                  <th>Asignatura</th>
                  <th>Código</th>
                  <th>Estado</th>
                  <th>Media técnica</th>
                  <th>Idoneidad estricta</th>
                  <th>Docente exclusivo</th>
                </tr>
              </thead>
              <tbody>
                {asignaturas.map(({ area, asignatura }) => (
                  <tr key={asignatura.asignaturaId}>
                    <td>{texto(area.nombre)}</td>
                    <td>{texto(asignatura.nombre)}</td>
                    <td>{texto(asignatura.codigo)}</td>
                    <td>
                      <Badge tone={asignatura.activa ? 'success' : 'neutral'}>
                        {asignatura.activa ? 'Activa' : 'Inactiva'}
                      </Badge>
                    </td>
                    <td>{siNo(asignatura.esMediaTecnica)}</td>
                    <td>{siNo(asignatura.exigeIdoneidadEstricta)}</td>
                    <td>{siNo(asignatura.requiereDocenteExclusivo)}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        )}
      </Card>
    </>
  )
}

function PerfilesResultado({ informe }: { informe: InformePerfilProfesional }) {
  const docentes = informe.docentes ?? []
  const titulos = docentes.flatMap((docente) =>
    (docente.titulos ?? []).map((titulo) => ({ docente, titulo })),
  )
  const idoneidades = docentes.flatMap((docente) =>
    (docente.idoneidades ?? []).map((idoneidad) => ({ docente, idoneidad })),
  )

  return (
    <>
      <div className={styles.metrics} aria-label="Resumen de perfiles profesionales">
        <StatCard value={String(numero(informe.cantidadDocentes))} label="Docentes" wash="total" />
        <StatCard value={String(numero(informe.cantidadTitulos))} label="Títulos" wash="active" />
        <StatCard
          value={String(numero(informe.cantidadIdoneidades))}
          label="Idoneidades"
          wash="neutral"
        />
        <StatCard value={fecha(informe.fechaReferencia)} label="Fecha de referencia" wash="warning" />
      </div>
      <Card className={styles.panel}>
        <SectionHeader
          title="Docentes"
          description="Área de nombramiento y cantidad de títulos e idoneidades."
        />
        {docentes.length === 0 ? (
          <p className={styles.notice}>No hay docentes para los filtros seleccionados.</p>
        ) : (
          <div className={styles.tableHost}>
            <Table minWidth={720}>
              <thead>
                <tr>
                  <th>Docente</th>
                  <th>Área de nombramiento</th>
                  <th>Títulos</th>
                  <th>Idoneidades</th>
                </tr>
              </thead>
              <tbody>
                {docentes.map((docente) => (
                  <tr key={docente.docenteId}>
                    <td>{texto(docente.nombreCompleto)}</td>
                    <td>{texto(docente.areaNombramiento)}</td>
                    <td>{docente.titulos?.length ?? 0}</td>
                    <td>{docente.idoneidades?.length ?? 0}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        )}
      </Card>
      <Card className={styles.panel}>
        <SectionHeader title="Títulos profesionales" description="Formación registrada de cada docente." />
        {titulos.length === 0 ? (
          <p className={styles.notice}>No hay títulos profesionales para esta consulta.</p>
        ) : (
          <div className={styles.tableHost}>
            <Table minWidth={820}>
              <thead>
                <tr>
                  <th>Docente</th>
                  <th>Título</th>
                  <th>Nivel</th>
                  <th>Institución</th>
                  <th>Año</th>
                </tr>
              </thead>
              <tbody>
                {titulos.map(({ docente, titulo }) => (
                  <tr key={titulo.tituloId}>
                    <td>{texto(docente.nombreCompleto)}</td>
                    <td>{texto(titulo.nombreTitulo)}</td>
                    <td>{titulo.nivel ? labelNivelTitulo(titulo.nivel) : '—'}</td>
                    <td>{texto(titulo.institucion)}</td>
                    <td>{titulo.anioGraduacion ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        )}
      </Card>
      <Card className={styles.panel}>
        <SectionHeader
          title="Idoneidades"
          description="Habilitación académica vigente o no a la fecha de referencia."
        />
        {idoneidades.length === 0 ? (
          <p className={styles.notice}>No hay idoneidades para esta consulta.</p>
        ) : (
          <div className={styles.tableHost}>
            <Table minWidth={920}>
              <thead>
                <tr>
                  <th>Docente</th>
                  <th>Área</th>
                  <th>Asignatura</th>
                  <th>Tipo</th>
                  <th>Vigencia</th>
                  <th>En la fecha</th>
                </tr>
              </thead>
              <tbody>
                {idoneidades.map(({ docente, idoneidad }) => (
                  <tr key={idoneidad.idoneidadId}>
                    <td>{texto(docente.nombreCompleto)}</td>
                    <td>{texto(idoneidad.nombreArea)}</td>
                    <td>{texto(idoneidad.nombreAsignatura)}</td>
                    <td>{etiquetaTipo(idoneidad.tipo)}</td>
                    <td>{vigencia(idoneidad.vigenteDesde, idoneidad.vigenteHasta)}</td>
                    <td>
                      <Badge tone={idoneidad.vigenteEnFecha ? 'success' : 'neutral'}>
                        {idoneidad.vigenteEnFecha ? 'Vigente' : 'No vigente'}
                      </Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        )}
      </Card>
    </>
  )
}

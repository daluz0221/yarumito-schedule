import type { ReactNode } from 'react'
import { etiquetaUsuario } from '../../../api'
import type { RegistroAuditoriaResponse } from '../../../api/registrosAuditoria'
import type { UsuarioListado } from '../../../api/usuarios'
import { etiquetaEntidad, marcaTiempo, type ConsultaAuditoria } from '../../../content/auditoria'
import { describirCambios, type ResolvedorAuditoria } from '../../../content/auditoriaCambios'
import { Badge, type BadgeTone } from '../../atoms/Badge'
import { Table } from '../../atoms/Table'
import { Text } from '../../atoms/Text'
import { TextLink } from '../../atoms/TextLink'
import styles from './AuditoriaWorkspace.module.css'

function etiquetaAccion(accion: string) {
  if (accion === 'CREATE') {
    return 'Creación'
  }

  if (accion === 'UPDATE') {
    return 'Actualización'
  }

  if (accion === 'DELETE') {
    return 'Eliminación'
  }

  if (accion === 'PUBLICAR') {
    return 'Publicación'
  }

  return accion
}

function tonoAccion(accion: string): BadgeTone {
  if (accion === 'CREATE') {
    return 'success'
  }

  if (accion === 'UPDATE') {
    return 'info'
  }

  if (accion === 'DELETE') {
    return 'danger'
  }

  if (accion === 'PUBLICAR') {
    return 'warning'
  }

  return 'neutral'
}

function formatoFecha(value: RegistroAuditoriaResponse['fecha']) {
  const marca = marcaTiempo(value)

  if (!marca) {
    return typeof value === 'string' && value.trim() ? value : '—'
  }

  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(marca))
}

function etiquetaCuenta(
  usuarioId: string,
  usuarios: UsuarioListado[],
  sesion: { id: string; correo: string; rol: UsuarioListado['rol'] } | null,
) {
  const cuenta = usuarios.find((item) => item.id === usuarioId)

  if (cuenta) {
    return etiquetaUsuario(cuenta)
  }

  if (sesion?.id === usuarioId) {
    return etiquetaUsuario({
      correo: sesion.correo,
      rol: sesion.rol,
      activo: true,
    })
  }

  return 'Cuenta no listada'
}

function DetalleCambio({
  entidad,
  accion,
  antes,
  despues,
  resolvedor,
}: {
  entidad: string
  accion: string
  antes: unknown
  despues: unknown
  resolvedor: ResolvedorAuditoria
}) {
  const cambios = describirCambios(entidad, accion, antes, despues, resolvedor)

  return (
    <details className={styles.details}>
      <summary>Ver cambios</summary>
      {cambios.filas.length === 0 ? (
        <p className={styles.muted}>
          {cambios.modo === 'comparar'
            ? 'No se modificó ningún dato visible.'
            : 'Esta acción no guardó datos para mostrar.'}
        </p>
      ) : (
        <div className={styles.cambios}>
          {cambios.titulo ? <p className={styles.detailLabel}>{cambios.titulo}</p> : null}
          <dl className={styles.cambiosLista}>
            {cambios.filas.map((fila) => (
              <div key={fila.campo} className={styles.cambioFila}>
                <dt>{fila.campo}</dt>
                <dd>
                  {cambios.modo === 'comparar' ? (
                    <>
                      <span className={styles.valorAntes}>{fila.antes ?? 'Sin dato'}</span>
                      <span className={styles.flecha} aria-hidden="true">
                        →
                      </span>
                      <span className={styles.valorDespues}>{fila.despues ?? 'Sin dato'}</span>
                    </>
                  ) : (
                    <span>{fila.despues ?? fila.antes}</span>
                  )}
                </dd>
              </div>
            ))}
          </dl>
        </div>
      )}
    </details>
  )
}

export function HistorialTabla({
  registros,
  etiquetas,
  usuarios,
  sesion,
  resolvedor,
  onEntidad,
  onUsuario,
  origen,
}: {
  registros: RegistroAuditoriaResponse[]
  resolvedor: ResolvedorAuditoria
  etiquetas: Map<string, string>
  usuarios: UsuarioListado[]
  sesion: { id: string; correo: string; rol: UsuarioListado['rol'] } | null
  onEntidad: (entidad: string, idEntidad: string) => void
  onUsuario: (usuarioId: string) => void
  origen: 'registro' | 'grupo' | 'usuario'
}) {
  const cuentas = new Map(usuarios.map((item) => [item.id, etiquetaUsuario(item)]))

  if (sesion?.id && !cuentas.has(sesion.id)) {
    cuentas.set(
      sesion.id,
      etiquetaUsuario({ correo: sesion.correo, rol: sesion.rol, activo: true }),
    )
  }

  const resolvedorCompleto: ResolvedorAuditoria = {
    ...resolvedor,
    usuarios: cuentas,
    asignaciones: etiquetas,
  }

  return (
    <div className={styles.tableHost}>
      <Table minWidth={980}>
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Acción</th>
            <th>Registro</th>
            <th>Cuenta</th>
            <th>IP</th>
            <th>Cambios</th>
          </tr>
        </thead>
        <tbody>
          {registros.map((registro) => (
            <tr key={registro.id}>
              <td>{formatoFecha(registro.fecha)}</td>
              <td>
                <Badge tone={tonoAccion(registro.accion)}>
                  {etiquetaAccion(registro.accion)}
                </Badge>
              </td>
              <td>
                <p className={styles.registro}>
                  {etiquetas.get(registro.idEntidad) ?? etiquetaEntidad(registro.entidad)}
                </p>
                {origen === 'usuario' || origen === 'grupo' ? (
                  <TextLink onClick={() => onEntidad(registro.entidad, registro.idEntidad)}>
                    Ver historial de la entidad
                  </TextLink>
                ) : null}
              </td>
              <td>
                <p className={styles.registro}>
                  {etiquetaCuenta(registro.usuarioId, usuarios, sesion)}
                </p>
                {origen === 'registro' || origen === 'grupo' ? (
                  <TextLink onClick={() => onUsuario(registro.usuarioId)}>
                    Ver actividad de la cuenta
                  </TextLink>
                ) : null}
              </td>
              <td>{registro.ip?.trim() || 'No registrada'}</td>
              <td>
                <DetalleCambio
                  entidad={registro.entidad}
                  accion={registro.accion}
                  antes={registro.datosAntes}
                  despues={registro.datosDespues}
                  resolvedor={resolvedorCompleto}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    </div>
  )
}

export function ResultadoConsulta({
  consulta,
  vacio,
  titulo,
  inicial,
  children,
}: {
  consulta: ConsultaAuditoria
  vacio: string
  titulo: string
  inicial: string
  children: ReactNode
}) {
  if (consulta.estado === 'cargando') {
    return <p className={styles.status}>Consultando el historial…</p>
  }

  if (consulta.estado === 'error') {
    return (
      <p className={styles.error} role="alert">
        {consulta.error}
      </p>
    )
  }

  if (consulta.estado === 'inicial') {
    return <p className={styles.status}>{inicial}</p>
  }

  return (
    <div className={styles.resultado}>
      {titulo ? (
        <Text as="h3" variant="sectionTitle">
          {titulo}
        </Text>
      ) : null}
      {consulta.registros.length === 0 ? <p className={styles.status}>{vacio}</p> : children}
    </div>
  )
}

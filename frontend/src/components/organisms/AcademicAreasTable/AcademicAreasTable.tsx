import type { KeyboardEvent } from 'react'
import type { AreaResponse } from '../../../api/areas'
import {
  labelAplicacion,
  labelClasificacion,
  labelEstadoArea,
} from '../../../content/areas'
import { PENDING_BACKEND_LABEL } from '../../../content/teachers'
import { Badge, type BadgeTone } from '../../atoms/Badge'
import { BanIcon, CheckIcon, PencilIcon } from '../../atoms/icons'
import styles from './AcademicAreasTable.module.css'

export type AcademicAreasTableProps = {
  rows: AreaResponse[]
  selectedId: string
  emptyMessage: string
  onSelect: (areaId: string) => void
  onEdit: (area: AreaResponse) => void
  onDeactivate: (area: AreaResponse) => void
}

export function AcademicAreasTable({
  rows,
  selectedId,
  emptyMessage,
  onSelect,
  onEdit,
  onDeactivate,
}: AcademicAreasTableProps) {
  return (
    <div>
      <div className={styles.scroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Código</th>
              <th>Área</th>
              <th>Clasificación</th>
              <th>Aplicación</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((area) => {
              const selected = area.id === selectedId

              return (
                <tr
                  key={area.id}
                  className={selected ? styles.selected : styles.row}
                  tabIndex={0}
                  aria-selected={selected}
                  onClick={(event) => {
                    if (event.target instanceof Element && event.target.closest('button')) {
                      return
                    }

                    onSelect(area.id)
                  }}
                  onKeyDown={(event) => selectOnKey(event, () => onSelect(area.id))}
                >
                  <td className={styles.code}>{area.codigo}</td>
                  <td>
                    <AreaName nombre={area.nombre} selected={selected} />
                  </td>
                  <td>
                    <AreaValue
                      label={labelClasificacion(area.obligatoria)}
                      tone={area.obligatoria ? 'success' : 'muted'}
                    />
                  </td>
                  <td>
                    <AreaValue
                      label={labelAplicacion(area.soloMedia)}
                      tone={area.soloMedia ? 'amber' : 'info'}
                    />
                  </td>
                  <td>
                    <AreaValue
                      label={labelEstadoArea(area.activa)}
                      tone={area.activa === true ? 'success' : 'muted'}
                    />
                  </td>
                  <td>
                    <AreaActions
                      nombre={area.nombre}
                      activa={area.activa}
                      onEdit={() => onEdit(area)}
                      onDeactivate={() => onDeactivate(area)}
                    />
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>

      <ul className={styles.cards}>
        {rows.map((area) => {
          const selected = area.id === selectedId

          return (
            <li className={selected ? styles.mobileCardSelected : styles.mobileCard}>
              <button
                type="button"
                className={styles.mobileSelect}
                aria-pressed={selected}
                onClick={() => onSelect(area.id)}
              >
                <div className={styles.mobileHeader}>
                  <AreaName nombre={area.nombre} selected={selected} />
                  <AreaValue
                    label={labelEstadoArea(area.activa)}
                    tone={area.activa === true ? 'success' : 'muted'}
                  />
                </div>
                <p className={styles.code}>{area.codigo}</p>
                <p className={styles.meta}>
                  <AreaValue
                    label={labelClasificacion(area.obligatoria)}
                    tone={area.obligatoria ? 'success' : 'muted'}
                  />
                  <AreaValue
                    label={labelAplicacion(area.soloMedia)}
                    tone={area.soloMedia ? 'amber' : 'info'}
                  />
                </p>
              </button>
              <AreaActions
                nombre={area.nombre}
                activa={area.activa}
                onEdit={() => onEdit(area)}
                onDeactivate={() => onDeactivate(area)}
              />
            </li>
          )
        })}
      </ul>

      {rows.length === 0 ? <p className={styles.empty}>{emptyMessage}</p> : null}
    </div>
  )
}

function AreaName({ nombre, selected }: { nombre: string; selected: boolean }) {
  return (
    <span className={styles.name}>
      <strong>{nombre}</strong>
      {selected ? <span className={styles.selectedLabel}>Seleccionada</span> : null}
    </span>
  )
}

function AreaActions({
  nombre,
  activa,
  onEdit,
  onDeactivate,
}: {
  nombre: string
  activa?: boolean
  onEdit: () => void
  onDeactivate: () => void
}) {
  const inactive = activa === false
  const toggleLabel = inactive ? 'Activar' : 'Inactivar'

  return (
    <span className={styles.actions}>
      <button
        type="button"
        className={styles.iconAction}
        onClick={onEdit}
        aria-label={`Editar ${nombre}`}
        title="Editar"
      >
        <PencilIcon />
      </button>
      <button
        type="button"
        className={`${styles.iconAction} ${inactive ? styles.iconActionOn : styles.iconActionOff}`}
        onClick={onDeactivate}
        aria-label={`${toggleLabel} ${nombre}`}
        title={toggleLabel}
      >
        {inactive ? <CheckIcon /> : <BanIcon />}
      </button>
    </span>
  )
}

function AreaValue({ label, tone }: { label: string | null; tone: BadgeTone }) {
  if (!label) {
    return <span className={styles.pending}>{PENDING_BACKEND_LABEL}</span>
  }

  return <Badge tone={tone}>{label}</Badge>
}

function selectOnKey(event: KeyboardEvent<HTMLTableRowElement>, select: () => void) {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    select()
  }
}

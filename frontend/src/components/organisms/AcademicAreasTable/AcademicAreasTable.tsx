import type { KeyboardEvent } from 'react'
import type { AreaResponse } from '../../../api/areas'
import {
  labelAplicacion,
  labelClasificacion,
  labelEstadoArea,
} from '../../../content/areas'
import { PENDING_BACKEND_LABEL } from '../../../content/teachers'
import { TextLink } from '../../atoms/TextLink'
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
                    {renderFlag(
                      labelClasificacion(area.obligatoria),
                      area.obligatoria === true,
                    )}
                  </td>
                  <td>{renderFlag(labelAplicacion(area.soloMedia), false)}</td>
                  <td>{renderFlag(labelEstadoArea(area.activa), area.activa === true)}</td>
                  <td>
                    <AreaActions
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
            <li key={area.id}>
              <button
                type="button"
                className={selected ? styles.mobileCardSelected : styles.mobileCard}
                aria-pressed={selected}
                onClick={() => onSelect(area.id)}
              >
                <div className={styles.mobileHeader}>
                  <AreaName nombre={area.nombre} selected={selected} />
                  {renderFlag(labelEstadoArea(area.activa), area.activa === true)}
                </div>
                <p className={styles.code}>{area.codigo}</p>
                <p>
                  {renderFlag(
                    labelClasificacion(area.obligatoria),
                    area.obligatoria === true,
                  )}
                  {' · '}
                  {renderFlag(labelAplicacion(area.soloMedia), false)}
                </p>
              </button>
              <div className={styles.mobileActions}>
                <AreaActions
                  onEdit={() => onEdit(area)}
                  onDeactivate={() => onDeactivate(area)}
                />
              </div>
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
  onEdit,
  onDeactivate,
}: {
  onEdit: () => void
  onDeactivate: () => void
}) {
  return (
    <span className={styles.actions}>
      <TextLink onClick={onEdit}>Editar</TextLink>
      <TextLink onClick={onDeactivate}>Inactivar</TextLink>
    </span>
  )
}

function renderFlag(label: string | null, positive: boolean) {
  if (!label) {
    return <span className={styles.pending}>{PENDING_BACKEND_LABEL}</span>
  }

  return <span className={positive ? styles.positive : undefined}>{label}</span>
}

function selectOnKey(event: KeyboardEvent<HTMLTableRowElement>, select: () => void) {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    select()
  }
}

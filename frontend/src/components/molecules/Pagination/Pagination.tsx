import { Select } from '../../atoms/Select'
import { TextLink } from '../../atoms/TextLink'
import styles from './Pagination.module.css'

export type PaginationProps = {
  page: number
  pageSize: number
  total: number
  onPageChange: (page: number) => void
  onPageSizeChange: (pageSize: number) => void
}

export function Pagination({
  page,
  pageSize,
  total,
  onPageChange,
  onPageSizeChange,
}: PaginationProps) {
  const start = total === 0 ? 0 : (page - 1) * pageSize + 1
  const end = Math.min(page * pageSize, total)
  const isFirst = page <= 1
  const isLast = end >= total

  return (
    <div className={styles.bar}>
      <p className={styles.summary}>
        Mostrando {start}-{end} de {total} docentes
      </p>
      <div className={styles.controls}>
        <label className={styles.pageSize}>
          Filas por página:
          <Select
            selectSize="sm"
            className={styles.pageSizeSelect}
            value={String(pageSize)}
            options={[
              { value: '6', label: '6' },
              { value: '10', label: '10' },
              { value: '14', label: '14' },
            ]}
            onChange={(event) => onPageSizeChange(Number(event.target.value))}
            aria-label="Filas por página"
          />
        </label>
        <TextLink disabled={isFirst} onClick={() => onPageChange(page - 1)}>
          Anterior
        </TextLink>
        <TextLink disabled={isLast} onClick={() => onPageChange(page + 1)}>
          Siguiente
        </TextLink>
      </div>
    </div>
  )
}

import styles from './ValidityStatus.module.css'

export type ValidityStatusProps = {
  current: boolean
  since: string
}

export function ValidityStatus({ current, since }: ValidityStatusProps) {
  return (
    <span className={styles.status}>
      <span className={current ? styles.current : styles.closed}>
        {current ? 'Vigente' : 'Finalizado'}
      </span>
      <span className={styles.since}>
        {current ? `Desde ${since}` : since}
      </span>
    </span>
  )
}

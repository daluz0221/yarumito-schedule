import { Text } from '../../atoms/Text'
import styles from './MetaField.module.css'

export type MetaFieldProps = {
  label: string
  value: string
}

export function MetaField({ label, value }: MetaFieldProps) {
  return (
    <div className={styles.field}>
      <Text variant="caption">{label}</Text>
      <p className={styles.value}>{value}</p>
    </div>
  )
}

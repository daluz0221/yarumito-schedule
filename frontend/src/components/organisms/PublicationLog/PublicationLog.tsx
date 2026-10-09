import { publicationCopy, type PublicationRecord } from '../../../content/publication'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Table } from '../../atoms/Table'
import { Text } from '../../atoms/Text'
import styles from './PublicationLog.module.css'

export type PublicationLogProps = {
  record: PublicationRecord | null
  onPublish: () => void
}

export function PublicationLog({ record, onPublish }: PublicationLogProps) {
  const published = Boolean(record)

  return (
    <Card as="section" padding="lg" className={styles.card}>
      <Text as="h3" variant="sectionTitle">
        {publicationCopy.logTitle}
      </Text>

      {record ? (
        <Table minWidth={560}>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Acción</th>
              <th>Versión</th>
              <th>Responsable</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{record.at}</td>
              <td>{record.action}</td>
              <td>v{record.version}</td>
              <td>{record.actor}</td>
            </tr>
          </tbody>
        </Table>
      ) : (
        <p className={styles.empty}>{publicationCopy.logEmpty}</p>
      )}

      <div className={styles.footer}>
        <p className={styles.note}>{publicationCopy.logNote}</p>
        <Button variant="primary" size="sm" disabled={published} onClick={onPublish}>
          {published ? publicationCopy.published : publicationCopy.publish}
        </Button>
      </div>
    </Card>
  )
}

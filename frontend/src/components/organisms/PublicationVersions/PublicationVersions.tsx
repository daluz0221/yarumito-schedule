import type { HorarioVersionResponse } from '../../../api'
import { publicationCopy } from '../../../content/publication'
import { Badge } from '../../atoms/Badge'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { Text } from '../../atoms/Text'
import styles from './PublicationVersions.module.css'

export type PublicationVersionsProps = {
  versions: HorarioVersionResponse[] | null
  busy?: boolean
  onCreateDraft?: (horarioId: string) => void
  onPublishDraft?: (horarioId: string) => void
  onDiscardDraft?: (horarioId: string) => void
}

function tono(estado: HorarioVersionResponse['estado']) {
  if (estado === 'PUBLICADO') {
    return 'success' as const
  }

  if (estado === 'ARCHIVADO') {
    return 'neutral' as const
  }

  return 'warning' as const
}

function fechaVisible(value: string | null) {
  if (!value) {
    return 'Sin fecha de publicación'
  }

  const fecha = new Date(value)

  if (Number.isNaN(fecha.getTime())) {
    return value
  }

  return fecha.toLocaleString('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function agrupar(versions: HorarioVersionResponse[]) {
  const grupos: { sedeId: string; sedeNombre: string; items: HorarioVersionResponse[] }[] = []

  versions.forEach((version) => {
    const sedeId = version.sedeId ?? 'sin-sede'
    const actual = grupos.find((grupo) => grupo.sedeId === sedeId)

    if (actual) {
      actual.items.push(version)
      return
    }

    grupos.push({
      sedeId,
      sedeNombre: version.sedeNombre?.trim() || 'Sede',
      items: [version],
    })
  })

  return grupos
}

export function PublicationVersions({
  versions,
  busy = false,
  onCreateDraft,
  onPublishDraft,
  onDiscardDraft,
}: PublicationVersionsProps) {
  const grupos = versions ? agrupar(versions) : []
  const hayArchivadas = (versions ?? []).some((version) => version.estado === 'ARCHIVADO')

  return (
    <Card as="section" padding="lg" className={styles.card}>
      <Text as="h3" variant="sectionTitle">
        {publicationCopy.versionsTitle}
      </Text>

      {versions === null ? <Text variant="caption">{publicationCopy.versionsLoading}</Text> : null}

      {versions && versions.length === 0 ? (
        <Text variant="caption">{publicationCopy.versionsEmpty}</Text>
      ) : null}

      {grupos.map((grupo) => {
        const tieneBorrador = grupo.items.some((version) => version.estado === 'BORRADOR')

        return (
          <section key={grupo.sedeId} className={styles.sede}>
            <p className={styles.sedeName}>{grupo.sedeNombre}</p>
            {grupo.items.map((version) => (
              <article key={version.id} className={styles.row}>
                <div className={styles.meta}>
                  <p className={styles.version}>
                    v{version.version}
                    {version.estado === 'PUBLICADO' ? ' · Vigente' : ''}
                    {version.estado === 'BORRADOR' && version.version > 1 ? ' · Mejora' : ''}
                  </p>
                  <Badge tone={tono(version.estado)}>{version.estado}</Badge>
                  <Text variant="caption">
                    {(version.estado === 'BORRADOR' ? version.notas?.trim() : '') ||
                      fechaVisible(version.fechaPublicacion)}
                  </Text>
                </div>
                <div className={styles.actions}>
                  {version.estado === 'PUBLICADO' && !tieneBorrador && onCreateDraft ? (
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={busy}
                      onClick={() => onCreateDraft?.(version.id)}
                    >
                      {publicationCopy.createDraft}
                    </Button>
                  ) : null}
                  {version.estado === 'BORRADOR' && version.version > 1 ? (
                    <>
                      <Button
                        variant="outline"
                        size="sm"
                        disabled={busy}
                        onClick={() => onDiscardDraft?.(version.id)}
                      >
                        {publicationCopy.discardDraft}
                      </Button>
                      <Button
                        variant="primary"
                        size="sm"
                        disabled={busy}
                        onClick={() => onPublishDraft?.(version.id)}
                      >
                        {publicationCopy.publishDraft}
                      </Button>
                    </>
                  ) : null}
                </div>
              </article>
            ))}
          </section>
        )
      })}

      {versions && versions.length > 0 && !hayArchivadas ? (
        <Text variant="caption">{publicationCopy.previousEmpty}</Text>
      ) : null}
    </Card>
  )
}

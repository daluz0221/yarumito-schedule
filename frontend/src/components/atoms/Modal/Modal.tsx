import { useEffect, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { Text } from '../Text'
import styles from './Modal.module.css'

export type ModalSize = 'md' | 'sm'

export type ModalProps = {
  open: boolean
  title: string
  description?: string
  children?: ReactNode
  footer?: ReactNode
  size?: ModalSize
  onClose: () => void
}

export function Modal({
  open,
  title,
  description,
  children,
  footer,
  size = 'md',
  onClose,
}: ModalProps) {
  useEffect(() => {
    if (!open) {
      return
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    window.addEventListener('keydown', onKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', onKeyDown)
    }
  }, [open, onClose])

  if (!open) {
    return null
  }

  return createPortal(
    <div className={styles.overlay} onClick={onClose} role="presentation">
      <div
        className={[styles.dialog, size === 'sm' ? styles.sm : '']
          .filter(Boolean)
          .join(' ')}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={(event) => event.stopPropagation()}
      >
        <header className={styles.header}>
          <Text variant="sectionTitle" as="h2" className={styles.title}>
            {title}
          </Text>
          {description ? <Text variant="body">{description}</Text> : null}
        </header>
        {children ? <div className={styles.body}>{children}</div> : null}
        {footer ? <div className={styles.footer}>{footer}</div> : null}
      </div>
    </div>,
    document.body,
  )
}

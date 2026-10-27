import { useRef } from 'react'
import { FormField } from '../FormField'
import styles from './FileField.module.css'

export type FileFieldProps = {
  id: string
  label: string
  hint?: string
  fileName?: string
  placeholder?: string
  onFileChange?: (fileName: string) => void
}

export function FileField({
  id,
  label,
  hint,
  fileName,
  placeholder = 'Adjuntar archivo',
  onFileChange,
}: FileFieldProps) {
  const inputRef = useRef<HTMLInputElement>(null)

  return (
    <FormField id={id} label={label} hint={hint}>
      <button
        type="button"
        className={[styles.trigger, fileName ? styles.hasFile : '']
          .filter(Boolean)
          .join(' ')}
        onClick={() => inputRef.current?.click()}
      >
        {fileName || placeholder}
      </button>
      <input
        ref={inputRef}
        id={id}
        type="file"
        className={styles.input}
        onChange={(event) => {
          const file = event.target.files?.[0]
          onFileChange?.(file?.name ?? '')
        }}
      />
    </FormField>
  )
}

import { useState } from 'react'
import { Button } from '../../atoms/Button'
import { Modal } from '../../atoms/Modal'
import { TextInput } from '../../atoms/TextInput'
import { FormField } from '../../molecules/FormField'
import styles from './RestrictionTeacherModal.module.css'

export type RestrictionTeacherChoice = {
  id: string
  name: string
  areaName: string
  active: boolean
}

export type RestrictionTeacherModalProps = {
  open: boolean
  teachers: RestrictionTeacherChoice[]
  onSelect: (teacherId: string) => void
  onClose: () => void
}

function normalize(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}

export function RestrictionTeacherModal({
  open,
  teachers,
  onSelect,
  onClose,
}: RestrictionTeacherModalProps) {
  const [query, setQuery] = useState('')
  const needle = normalize(query)
  const visible = teachers.filter((teacher) => {
    if (!teacher.active) {
      return false
    }

    if (!needle) {
      return true
    }

    return normalize(teacher.name).includes(needle)
  })

  return (
    <Modal open={open} title="Seleccionar docente" onClose={onClose}>
      <div className={styles.body}>
        <FormField id="restriction-teacher-search" label="Buscar docente">
          <TextInput
            id="restriction-teacher-search"
            name="teacherQuery"
            inputSize="sm"
            placeholder="Buscar por nombre"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </FormField>
        <p className={styles.caption}>Docentes activos · Sede Principal</p>
        <div className={styles.choices}>
          {visible.length === 0 ? (
            <p className={styles.empty}>No se encontraron docentes.</p>
          ) : (
            visible.map((teacher) => (
              <Button
                key={teacher.id}
                variant="muted"
                size="sm"
                onClick={() => onSelect(teacher.id)}
              >
                {teacher.name} · {teacher.areaName}
              </Button>
            ))
          )}
          <Button variant="muted" size="sm" onClick={onClose}>
            Cancelar
          </Button>
        </div>
      </div>
    </Modal>
  )
}

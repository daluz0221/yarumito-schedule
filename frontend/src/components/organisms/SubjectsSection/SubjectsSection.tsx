import { useState } from 'react'
import type { AreaResponse } from '../../../api/areas'
import type { AsignaturaResponse } from '../../../api/asignaturas'
import { Button } from '../../atoms/Button'
import { Card } from '../../atoms/Card'
import { TextInput } from '../../atoms/TextInput'
import { TextLink } from '../../atoms/TextLink'
import { FormField } from '../../molecules/FormField'
import { SectionHeader } from '../../molecules/SectionHeader'
import { SelectField } from '../../molecules/SelectField'
import { DeactivateSubjectModal } from '../DeactivateSubjectModal'
import {
  SubjectFormModal,
  type SubjectFormSource,
} from '../SubjectFormModal'
import styles from './SubjectsSection.module.css'

const statusOptions = [
  { value: '', label: 'Todas' },
  { value: 'activa', label: 'Activas' },
  { value: 'inactiva', label: 'Inactivas' },
]

const typeOptions = [
  { value: '', label: 'Todas' },
  { value: 'regular', label: 'Regular' },
  { value: 'media', label: 'Media técnica' },
]

type SubjectModal =
  | { name: 'closed' }
  | { name: 'create' }
  | { name: 'edit'; subject: SubjectFormSource }
  | { name: 'deactivate'; subjectName: string }

export type SubjectsSectionProps = {
  areas: AreaResponse[]
  selectedAreaId: string
  areaName: string | null
  subjects?: AsignaturaResponse[]
  subjectsLoading?: boolean
  subjectsError?: string
}

export function SubjectsSection({
  areas,
  selectedAreaId,
  areaName,
  subjects = [],
  subjectsLoading = false,
  subjectsError = '',
}: SubjectsSectionProps) {
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('')
  const [type, setType] = useState('')
  const [modal, setModal] = useState<SubjectModal>({ name: 'closed' })
  const closeModal = () => setModal({ name: 'closed' })
  const normalizedQuery = query.trim().toLowerCase()
  const visibleSubjects = subjects.filter((subject) => {
    if (status === 'activa' && subject.activa === false) {
      return false
    }

    if (status === 'inactiva' && subject.activa !== false) {
      return false
    }

    if (type === 'media' && !subject.esMediaTecnica) {
      return false
    }

    if (type === 'regular' && subject.esMediaTecnica) {
      return false
    }

    if (!normalizedQuery) {
      return true
    }

    return (
      subject.codigo.toLowerCase().includes(normalizedQuery) ||
      subject.nombre.toLowerCase().includes(normalizedQuery) ||
      (subject.abreviatura ?? '').toLowerCase().includes(normalizedQuery)
    )
  })
  const emptyMessage = subjectsError
    ? subjectsError
    : subjectsLoading
      ? 'Cargando asignaturas...'
      : !areaName
        ? 'Selecciona un área para ver sus asignaturas.'
        : subjects.length === 0
          ? 'Esta área no tiene asignaturas registradas.'
          : 'No se encontraron asignaturas con esos filtros.'

  return (
    <Card className={styles.section}>
      <SectionHeader
        title="Asignaturas"
        description={
          areaName
            ? `Asignaturas del área ${areaName}`
            : 'Selecciona un área para consultar sus asignaturas.'
        }
        action={
          <Button
            variant="primary"
            size="sm"
            onClick={() => setModal({ name: 'create' })}
          >
            + Nueva asignatura
          </Button>
        }
      />

      <form className={styles.filters} onSubmit={(event) => event.preventDefault()}>
        <FormField id="subject-search" label="Buscar asignatura">
          <TextInput
            id="subject-search"
            name="query"
            inputSize="sm"
            placeholder="Buscar por código o nombre..."
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </FormField>
        <SelectField
          id="subject-status"
          label="Estado"
          name="status"
          value={status}
          options={statusOptions}
          onChange={setStatus}
        />
        <SelectField
          id="subject-type"
          label="Tipo"
          name="type"
          value={type}
          options={typeOptions}
          onChange={setType}
        />
      </form>

      <div className={styles.tableHost}>
        <div className={styles.scroll}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Código</th>
                <th>Asignatura</th>
                <th>Idoneidad</th>
                <th>Media Técnica</th>
                <th>Máx. consecutivas</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {visibleSubjects.map((subject) => (
                <tr key={subject.id}>
                  <td>{subject.codigo}</td>
                  <td>{subject.nombre}</td>
                  <td className={subject.exigeIdoneidadEstricta ? styles.positive : undefined}>
                    {subject.exigeIdoneidadEstricta ? 'Estricto' : 'No'}
                  </td>
                  <td>{subject.esMediaTecnica ? 'Sí' : 'No'}</td>
                  <td>{subject.maxClasesConsecutivas}</td>
                  <td className={subject.activa ? styles.positive : undefined}>
                    {subject.activa ? 'Activa' : 'Inactiva'}
                  </td>
                  <td className={styles.actions}>
                    <TextLink
                      onClick={() => setModal({ name: 'edit', subject })}
                    >
                      Editar
                    </TextLink>
                    <TextLink
                      onClick={() =>
                        setModal({ name: 'deactivate', subjectName: subject.nombre })
                      }
                    >
                      Inactivar
                    </TextLink>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {visibleSubjects.length > 0 ? (
          <ul className={styles.cards}>
            {visibleSubjects.map((subject) => (
              <li key={subject.id} className={styles.card}>
                <div className={styles.cardHeader}>
                  <strong>{subject.nombre}</strong>
                  <span className={subject.activa ? styles.positive : undefined}>
                    {subject.activa ? 'Activa' : 'Inactiva'}
                  </span>
                </div>
                <p>
                  {subject.codigo}
                  {' · '}
                  <span className={subject.exigeIdoneidadEstricta ? styles.positive : undefined}>
                    {subject.exigeIdoneidadEstricta ? 'Estricto' : 'No'}
                  </span>
                  {' · '}
                  {subject.maxClasesConsecutivas} consecutivas
                </p>
                <div className={styles.actions}>
                  <TextLink onClick={() => setModal({ name: 'edit', subject })}>
                    Editar
                  </TextLink>
                  <TextLink
                    onClick={() =>
                      setModal({ name: 'deactivate', subjectName: subject.nombre })
                    }
                  >
                    Inactivar
                  </TextLink>
                </div>
              </li>
            ))}
          </ul>
        ) : null}
        {visibleSubjects.length === 0 ? (
          <p className={styles.empty}>{emptyMessage}</p>
        ) : null}
      </div>

      {modal.name === 'create' || modal.name === 'edit' ? (
        <SubjectFormModal
          key={modal.name === 'edit' ? modal.subject.id : selectedAreaId || 'create'}
          open
          mode={modal.name}
          areas={areas}
          selectedAreaId={selectedAreaId}
          subject={modal.name === 'edit' ? modal.subject : undefined}
          onClose={closeModal}
        />
      ) : null}

      {modal.name === 'deactivate' ? (
        <DeactivateSubjectModal
          open
          subjectName={modal.subjectName}
          onClose={closeModal}
        />
      ) : null}
    </Card>
  )
}

import { useState } from 'react'
import {
  anioEscolarVisible,
  gruposVisibles,
  opcionesAniosEscolares,
  useCatalogoAniosEscolares,
  useCatalogoGrados,
  useCatalogoGrupos,
} from '../../../api'
import {
  filterSchoolGroups,
  matchesAcademicYear,
  schoolGroupStats,
} from '../../../content/academicYear'
import { useAnioDeTrabajo } from '../../../content/anioDeTrabajo'
import { Text } from '../../atoms/Text'
import { SelectField } from '../../molecules/SelectField'
import { SchoolGroupsSection } from '../SchoolGroupsSection'
import styles from './SchoolGroupsWorkspace.module.css'

export function SchoolGroupsWorkspace() {
  const { anios, error, loading } = useCatalogoAniosEscolares()
  const {
    grupos,
    error: gruposError,
    loading: gruposLoading,
    recargar: recargarGrupos,
  } = useCatalogoGrupos()
  const { grados } = useCatalogoGrados()
  const catalogo = anios
  const [yearId, elegirAnio] = useAnioDeTrabajo()
  const selectedYearId = catalogo?.some((year) => year.id === yearId)
    ? yearId
    : catalogo?.length
      ? (anioEscolarVisible(catalogo)?.id ?? '')
      : ''
  const [grade, setGrade] = useState('')
  const [status, setStatus] = useState('')
  const [query, setQuery] = useState('')

  const year = catalogo?.find((item) => item.id === selectedYearId) ?? null
  const fuenteGrupos = grupos === null ? null : gruposVisibles(grupos, grados)
  const yearGroups =
    year && fuenteGrupos ? fuenteGrupos.filter((item) => matchesAcademicYear(item, year)) : []
  const rows = filterSchoolGroups(yearGroups, { grade, status, query })

  const selectYear = (nextYearId: string) => {
    elegirAnio(nextYearId)
    setGrade('')
    setStatus('')
    setQuery('')
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Grupos
        </Text>
        <Text variant="body">
          Consulta y crea los grupos de cada grado para el año lectivo seleccionado.
        </Text>
      </div>

      {loading ? <p className={styles.notice}>Cargando años escolares…</p> : null}
      {gruposLoading ? <p className={styles.notice}>Cargando grupos…</p> : null}
      {error ? (
        <p className={styles.alert} role="alert">
          {error}
        </p>
      ) : null}
      {gruposError ? (
        <p className={styles.alert} role="alert">
          {gruposError}
        </p>
      ) : null}

      {year ? (
        <>
          <div className={styles.yearSelect}>
            <SelectField
              id="groups-academic-year"
              label="Año lectivo"
              name="academicYear"
              value={year.id}
              options={opcionesAniosEscolares(catalogo, error)}
              onChange={selectYear}
            />
          </div>

          {fuenteGrupos ? (
            <SchoolGroupsSection
              year={year}
              groups={yearGroups}
              stats={schoolGroupStats(yearGroups)}
              grade={grade}
              status={status}
              query={query}
              rows={rows}
              emptyMessage={
                yearGroups.length === 0
                  ? 'Este año lectivo aún no tiene grupos registrados.'
                  : 'No se encontraron grupos con esos filtros.'
              }
              onGradeChange={setGrade}
              onStatusChange={setStatus}
              onQueryChange={setQuery}
              onGroupCreated={recargarGrupos}
            />
          ) : null}
        </>
      ) : loading ? null : (
        <p className={styles.notice}>
          No hay años escolares registrados. Crea un año lectivo antes de configurar grupos.
        </p>
      )}
    </section>
  )
}

import { useEffect, useState } from 'react'
import {
  anioEscolarVisible,
  gruposVisibles,
  useCatalogoAniosEscolares,
  useCatalogoGrados,
  useCatalogoGrupos,
} from '../../../api'
import {
  academicYears,
  filterSchoolGroups,
  matchesAcademicYear,
  schoolGroupStats,
  schoolGroups,
} from '../../../content/academicYear'
import { Text } from '../../atoms/Text'
import { AcademicYearCard } from '../AcademicYearCard'
import { SchoolGroupsSection } from '../SchoolGroupsSection'
import styles from './AcademicYearWorkspace.module.css'

export function AcademicYearWorkspace() {
  const { anios, error, loading } = useCatalogoAniosEscolares()
  const {
    grupos,
    error: gruposError,
    loading: gruposLoading,
    recargar: recargarGrupos,
  } = useCatalogoGrupos()
  const { grados } = useCatalogoGrados()
  const catalogo = error ? academicYears : anios
  const [yearId, setYearId] = useState('')
  const [grade, setGrade] = useState('')
  const [status, setStatus] = useState('')
  const [query, setQuery] = useState('')

  useEffect(() => {
    if (!catalogo?.length) {
      return
    }

    setYearId((current) =>
      catalogo.some((year) => year.id === current)
        ? current
        : (anioEscolarVisible(catalogo)?.id ?? ''),
    )
  }, [catalogo])

  const year = catalogo?.find((item) => item.id === yearId) ?? null
  const fuenteGrupos = gruposError
    ? schoolGroups
    : grupos === null
      ? null
      : gruposVisibles(grupos, grados)
  const yearGroups = year && fuenteGrupos
    ? fuenteGrupos.filter((item) => matchesAcademicYear(item, year))
    : []
  const rows = filterSchoolGroups(yearGroups, { grade, status, query })

  const selectYear = (nextYearId: string) => {
    setYearId(nextYearId)
    setGrade('')
    setStatus('')
    setQuery('')
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Año lectivo y grupos
        </Text>
        <Text variant="body">
          Configura el período académico y los grupos activos de la institución
          para cada año lectivo.
        </Text>
      </div>

      {loading ? <p className={styles.notice}>Cargando años escolares…</p> : null}
      {gruposLoading ? <p className={styles.notice}>Cargando grupos…</p> : null}
      {error ? (
        <p className={styles.alert} role="alert">
          {error} Se muestran los años de referencia locales para que puedas seguir
          revisando los grupos.
        </p>
      ) : null}
      {gruposError ? (
        <p className={styles.alert} role="alert">
          {gruposError} Se muestran los grupos de referencia locales para que puedas
          seguir configurando el año lectivo.
        </p>
      ) : null}

      {year ? (
        <>
          <AcademicYearCard years={catalogo ?? []} year={year} onYearChange={selectYear} />

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
        <Text variant="body">
          No hay años escolares registrados. Los grupos se configuran sobre un año
          lectivo.
        </Text>
      )}
    </section>
  )
}

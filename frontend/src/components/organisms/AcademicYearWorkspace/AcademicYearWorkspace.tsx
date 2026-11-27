import { useState } from 'react'
import {
  aAcademicYear,
  anioEscolarVisible,
  useCatalogoAniosEscolares,
} from '../../../api'
import type { AcademicYear } from '../../../content/academicYear'
import { useAnioDeTrabajo } from '../../../content/anioDeTrabajo'
import { Button } from '../../atoms/Button'
import { Text } from '../../atoms/Text'
import { AcademicYearCard } from '../AcademicYearCard'
import { AcademicYearFormModal } from '../AcademicYearFormModal'
import { ParametrosInstitucionalesSection } from '../ParametrosInstitucionalesSection'
import styles from './AcademicYearWorkspace.module.css'

export function AcademicYearWorkspace() {
  const { anios, error, loading, incorporar, retirar } = useCatalogoAniosEscolares()
  const catalogo = anios
  const [yearId, elegirAnio] = useAnioDeTrabajo()
  const selectedYearId = catalogo?.some((year) => year.id === yearId)
    ? yearId
    : catalogo?.length
      ? (anioEscolarVisible(catalogo)?.id ?? '')
      : ''
  const [crear, setCrear] = useState(false)

  const year = catalogo?.find((item) => item.id === selectedYearId) ?? null
  const selectYear = (nextYearId: string) => {
    elegirAnio(nextYearId)
  }

  const guardarAnio = (anio: AcademicYear) => {
    incorporar(anio)
    elegirAnio(anio.id)
  }

  return (
    <section className={styles.workspace}>
      <div className={styles.intro}>
        <Text as="h2" variant="pageTitle">
          Año lectivo
        </Text>
        <Text variant="body">
          Configura el período académico y la carga semanal de los docentes.
        </Text>
      </div>

      {loading ? <p className={styles.notice}>Cargando años escolares…</p> : null}
      {error ? (
        <p className={styles.alert} role="alert">
          {error}
        </p>
      ) : null}

      {year ? (
        <>
          <AcademicYearCard
            years={catalogo ?? []}
            year={year}
            onYearChange={selectYear}
            onSaved={guardarAnio}
            onMissing={retirar}
          />

          <ParametrosInstitucionalesSection
            key={year.id}
            yearId={year.id}
            yearLabel={year.year}
          />
        </>
      ) : loading ? null : (
        <div className={styles.intro}>
          <Text variant="body">
            No hay años escolares registrados. Crea el primero para comenzar.
          </Text>
          <Button variant="primary" size="sm" onClick={() => setCrear(true)}>
            + Nuevo año lectivo
          </Button>
          <AcademicYearFormModal
            open={crear}
            mode="create"
            years={catalogo ?? []}
            onClose={() => setCrear(false)}
            onCreated={(anio) => {
              const mapped = aAcademicYear(anio)

              if (mapped) {
                guardarAnio(mapped)
              }
            }}
          />
        </div>
      )}
    </section>
  )
}

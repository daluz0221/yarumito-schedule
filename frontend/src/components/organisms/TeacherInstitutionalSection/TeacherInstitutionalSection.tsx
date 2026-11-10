import { useEffect, useState } from 'react'
import {
  listarEscalafones,
  listarRegimenesDocentes,
  regimenDesdeEscalafon,
} from '../../../api'
import type { OpcionCatalogo } from '../../../api/catalogos'
import {
  registerContractOptions,
  sanitizeDigits,
  type CrearDocenteForm,
  type CrearDocenteFormErrors,
} from '../../../content/teachers'
import type { SelectOption } from '../../atoms/Select'
import { FormSection } from '../../molecules/FormSection'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'

export type TeacherInstitutionalValues = Pick<
  CrearDocenteForm,
  | 'tipoVinculacion'
  | 'areaNombramientoId'
  | 'numeroDecreto'
  | 'fechaDecreto'
  | 'escalafon'
  | 'fechaVinculacion'
>

export type TeacherInstitutionalSectionProps = {
  values: TeacherInstitutionalValues
  areaOptions: SelectOption[]
  areaHint?: string
  areasDisabled?: boolean
  errors?: CrearDocenteFormErrors
  onChange: (field: keyof TeacherInstitutionalValues, value: string) => void
}

export function TeacherInstitutionalSection({
  values,
  areaOptions,
  areaHint,
  areasDisabled = false,
  errors = {},
  onChange,
}: TeacherInstitutionalSectionProps) {
  const [regimen, setRegimen] = useState(() => regimenDesdeEscalafon(values.escalafon))
  const [regimenes, setRegimenes] = useState<OpcionCatalogo[]>([])
  const [escalafones, setEscalafones] = useState<OpcionCatalogo[]>([])
  const [escalafonesError, setEscalafonesError] = useState('')
  const sinEscalafon = regimen === 'ETNO_AFRO' || regimen === 'NO_APLICA'

  useEffect(() => {
    const inferido = regimenDesdeEscalafon(values.escalafon)
    if (inferido) {
      setRegimen((current) => current || inferido)
    }
  }, [values.escalafon])

  useEffect(() => {
    const controller = new AbortController()

    listarRegimenesDocentes(controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) {
          setRegimenes(items ?? [])
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setRegimenes([])
        }
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!regimen) {
      setEscalafones([])
      setEscalafonesError('')
      return
    }

    const controller = new AbortController()
    setEscalafonesError('')
    if (regimen === 'ETNO_AFRO' || regimen === 'NO_APLICA') {
      setEscalafones([])
    }

    listarEscalafones(regimen, controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) {
          setEscalafones(items ?? [])
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setEscalafones([])
          setEscalafonesError('No se pudieron cargar los escalafones.')
        }
      })

    return () => controller.abort()
  }, [regimen])

  const escalafonActual = values.escalafon.trim()
  const escalafonConocido = escalafones.some((item) => item.codigo === escalafonActual)
  const escalafonOptions = [
    { value: '', label: sinEscalafon ? 'Sin escalafón' : 'Seleccionar escalafón' },
    ...(escalafonActual && !escalafonConocido && !sinEscalafon
      ? [{ value: escalafonActual, label: `${escalafonActual} (valor actual)` }]
      : []),
    ...(sinEscalafon
      ? []
      : escalafones.map((item) => ({ value: item.codigo, label: item.nombre }))),
  ]

  return (
    <FormSection
      title="Información institucional"
      description="Información necesaria para identificar la vinculación académica del docente dentro de la institución."
    >
      <SelectField
        id="tipoVinculacion"
        name="tipoVinculacion"
        label="Tipo de vinculación"
        required
        value={values.tipoVinculacion}
        options={registerContractOptions}
        error={errors.tipoVinculacion}
        onChange={(value) => onChange('tipoVinculacion', value)}
      />
      <SelectField
        id="areaNombramientoId"
        name="areaNombramientoId"
        label="Área de nombramiento"
        required
        value={values.areaNombramientoId}
        options={areaOptions}
        hint={areaHint}
        error={errors.areaNombramientoId}
        disabled={areasDisabled}
        onChange={(value) => onChange('areaNombramientoId', value)}
      />
      <TextField
        id="numeroDecreto"
        name="numeroDecreto"
        label="Número de decreto"
        placeholder="Ej. 12344"
        inputMode="numeric"
        value={values.numeroDecreto}
        error={errors.numeroDecreto}
        onChange={(event) =>
          onChange('numeroDecreto', sanitizeDigits(event.target.value))
        }
      />
      <TextField
        id="fechaDecreto"
        name="fechaDecreto"
        type="date"
        label="Fecha de decreto"
        value={values.fechaDecreto}
        error={errors.fechaDecreto}
        onChange={(event) => onChange('fechaDecreto', event.target.value)}
      />
      <SelectField
        id="regimen"
        name="regimen"
        label="Régimen docente"
        value={regimen}
        options={[
          { value: '', label: regimenes.length ? 'Seleccionar régimen' : 'Cargando regímenes...' },
          ...regimenes.map((item) => ({ value: item.codigo, label: item.nombre })),
        ]}
        hint="El régimen define las opciones de escalafón. No se guarda aparte del escalafón."
        onChange={(codigo) => {
          setRegimen(codigo)
          onChange('escalafon', '')
        }}
      />
      <SelectField
        id="escalafon"
        name="escalafon"
        label="Escalafón"
        value={sinEscalafon ? '' : values.escalafon}
        options={escalafonOptions}
        disabled={!regimen || sinEscalafon}
        hint={
          sinEscalafon
            ? 'Este régimen no asigna un escalafón.'
            : escalafonesError
              ? escalafonesError
              : !regimen
                ? 'Elige primero el régimen docente.'
                : undefined
        }
        error={errors.escalafon}
        onChange={(codigo) => onChange('escalafon', codigo)}
      />
      <TextField
        id="fechaVinculacion"
        name="fechaVinculacion"
        type="date"
        label="Fecha de vinculación"
        value={values.fechaVinculacion}
        error={errors.fechaVinculacion}
        onChange={(event) => onChange('fechaVinculacion', event.target.value)}
      />
    </FormSection>
  )
}

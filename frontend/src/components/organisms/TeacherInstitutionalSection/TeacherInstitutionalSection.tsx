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
      <TextField
        id="escalafon"
        name="escalafon"
        label="Escalafón"
        placeholder="Ej. 2A"
        value={values.escalafon}
        error={errors.escalafon}
        onChange={(event) => onChange('escalafon', event.target.value)}
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

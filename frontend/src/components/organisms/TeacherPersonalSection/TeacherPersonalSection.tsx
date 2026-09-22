import {
  documentTypeOptions,
  sanitizeDigits,
  sanitizeDocumentNumber,
  type CrearDocenteForm,
  type CrearDocenteFormErrors,
} from '../../../content/teachers'
import { FormSection } from '../../molecules/FormSection'
import { SelectField } from '../../molecules/SelectField'
import { TextField } from '../../molecules/TextField'

export type TeacherPersonalValues = Pick<
  CrearDocenteForm,
  | 'nombres'
  | 'apellidos'
  | 'tipoDocumento'
  | 'numeroDocumento'
  | 'telefono'
  | 'correoInstitucional'
>

export type TeacherPersonalSectionProps = {
  values: TeacherPersonalValues
  errors?: CrearDocenteFormErrors
  onChange: (field: keyof TeacherPersonalValues, value: string) => void
}

export function TeacherPersonalSection({
  values,
  errors = {},
  onChange,
}: TeacherPersonalSectionProps) {
  return (
    <FormSection
      title="Datos personales"
      description="Información básica de identificación y contacto del docente."
    >
      <TextField
        id="nombres"
        name="nombres"
        label="Nombres"
        required
        placeholder="Ej. Ana María"
        autoComplete="given-name"
        value={values.nombres}
        error={errors.nombres}
        onChange={(event) => onChange('nombres', event.target.value)}
      />
      <TextField
        id="apellidos"
        name="apellidos"
        label="Apellidos"
        required
        placeholder="Ej. Gómez Rodríguez"
        autoComplete="family-name"
        value={values.apellidos}
        error={errors.apellidos}
        onChange={(event) => onChange('apellidos', event.target.value)}
      />
      <SelectField
        id="tipoDocumento"
        name="tipoDocumento"
        label="Tipo de documento"
        required
        value={values.tipoDocumento}
        options={documentTypeOptions}
        error={errors.tipoDocumento}
        onChange={(value) => onChange('tipoDocumento', value)}
      />
      <TextField
        id="numeroDocumento"
        name="numeroDocumento"
        label="Número de documento"
        required
        placeholder="Ej. 1023456789"
        inputMode={
          values.tipoDocumento === 'PA' || values.tipoDocumento === 'PPT'
            ? 'text'
            : 'numeric'
        }
        value={values.numeroDocumento}
        error={errors.numeroDocumento}
        onChange={(event) =>
          onChange(
            'numeroDocumento',
            sanitizeDocumentNumber(values.tipoDocumento, event.target.value),
          )
        }
      />
      <TextField
        id="telefono"
        name="telefono"
        label="Teléfono"
        type="tel"
        placeholder="Ej. 3001234567"
        autoComplete="tel"
        inputMode="numeric"
        value={values.telefono}
        error={errors.telefono}
        onChange={(event) => onChange('telefono', sanitizeDigits(event.target.value))}
      />
      <TextField
        id="correoInstitucional"
        name="correoInstitucional"
        type="email"
        label="Correo institucional"
        placeholder="Ej. ana.gomez@yarumito.edu.co"
        autoComplete="email"
        value={values.correoInstitucional}
        error={errors.correoInstitucional}
        onChange={(event) => onChange('correoInstitucional', event.target.value)}
      />
    </FormSection>
  )
}

import type { AreaResponse } from '../api/areas'

// Alineado con TipoAulaRequerida del backend. Jackson lo serializa con el nombre del enum.
export const TIPO_AULA_REQUERIDA = [
  'AULA',
  'LABORATORIO',
  'SALA_SISTEMAS',
  'CANCHA',
] as const

export type TipoAulaRequerida = (typeof TIPO_AULA_REQUERIDA)[number]

// El campo es opcional en el API: null significa sin requisito. Este valor solo existe en el formulario.
export const SIN_REQUISITO_AULA = 'SIN_REQUISITO'

export type SubjectClassroomFormValue = '' | TipoAulaRequerida | typeof SIN_REQUISITO_AULA

export function subjectClassroomFormValue(value: string): SubjectClassroomFormValue {
  if (
    value === '' ||
    value === SIN_REQUISITO_AULA ||
    (TIPO_AULA_REQUERIDA as readonly string[]).includes(value)
  ) {
    return value as SubjectClassroomFormValue
  }

  return ''
}

export const subjectYesNoOptions = [
  { value: '', label: 'Seleccione' },
  { value: 'si', label: 'Sí' },
  { value: 'no', label: 'No' },
]

export const subjectStatusOptions = [
  { value: '', label: 'Seleccione un estado' },
  { value: 'activa', label: 'Activa' },
  { value: 'inactiva', label: 'Inactiva' },
]

export const subjectColorOptions = [
  { value: '', label: 'Sin color específico' },
  { value: 'azul', label: 'Azul' },
  { value: 'verde', label: 'Verde' },
  { value: 'amarillo', label: 'Amarillo' },
  { value: 'naranja', label: 'Naranja' },
  { value: 'rojo', label: 'Rojo' },
  { value: 'morado', label: 'Morado' },
]

export const subjectClassroomOptions: {
  value: SubjectClassroomFormValue
  label: string
}[] = [
  { value: '', label: 'Seleccione' },
  { value: 'AULA', label: 'Aula' },
  { value: 'LABORATORIO', label: 'Laboratorio' },
  { value: 'SALA_SISTEMAS', label: 'Sala de sistemas' },
  { value: 'CANCHA', label: 'Cancha' },
  { value: SIN_REQUISITO_AULA, label: 'Sin requisito específico' },
]

export const subjectFormHint =
  'Idoneidad estricta exige las condiciones configuradas posteriormente. Docente exclusivo no selecciona al docente aquí. Máximo inicial: 2 bloques consecutivos. Aula: Aula, Laboratorio, Sala de sistemas, Cancha o Sin requisito específico.'

export function areaSelectOptions(areas: AreaResponse[]) {
  return [
    { value: '', label: 'Seleccione un área' },
    ...areas.map((area) => ({
      value: area.id,
      label: area.nombre,
    })),
  ]
}

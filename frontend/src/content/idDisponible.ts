export function idDisponible(
  elegido: string,
  opciones: readonly { id: string }[] | null | undefined,
  listo: boolean,
  fallback: string,
) {
  if (!listo) {
    return elegido
  }

  return opciones?.some((item) => item.id === elegido) ? elegido : fallback
}

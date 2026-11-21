import { useEffect, useState } from 'react'
import {
  horaCorta,
  listarAsignaturas,
  listarAulasPorSede,
  listarCatalogoAniosEscolares,
  listarCatalogoAreas,
  listarCatalogoGrados,
  listarCatalogoGrupos,
  listarCatalogoSedes,
  listarCatalogoTurnos,
  listarDocentes,
  listarFranjasPorTurno,
} from '../../../api'
import type { ResolvedorAuditoria } from '../../../content/auditoriaCambios'
import { formatTeacherName, type Teacher } from '../../../content/teachers'

export function nombreDocente(docente: Teacher) {
  return `${docente.apellidos} ${docente.nombres}`.trim()
}

export async function todosLosDocentes(signal: AbortSignal) {
  const size = 100
  const primera = await listarDocentes({ page: 0, size }, signal)
  const docentes = [...primera.content]
  const paginas = Math.ceil(primera.total / size)

  for (let page = 1; page < paginas; page += 1) {
    const siguiente = await listarDocentes({ page, size }, signal)
    docentes.push(...siguiente.content)
  }

  return docentes.sort((left, right) =>
    nombreDocente(left).localeCompare(nombreDocente(right), 'es'),
  )
}

function vacio(): ResolvedorAuditoria {
  return {
    anios: new Map(),
    grados: new Map(),
    sedes: new Map(),
    grupos: new Map(),
    docentes: new Map(),
    areas: new Map(),
    asignaturas: new Map(),
    turnos: new Map(),
    franjas: new Map(),
    aulas: new Map(),
    usuarios: new Map(),
    asignaciones: new Map(),
  }
}

function ok<T>(resultado: PromiseSettledResult<T>, respaldo: T) {
  return resultado.status === 'fulfilled' ? resultado.value : respaldo
}

// Carga los catálogos necesarios para mostrar nombres en lugar de
// identificadores en el historial. Si alguno falla, solo ese dato queda como
// «Registro no disponible».
export function useResolvedorAuditoria(enabled: boolean) {
  const [estado, setEstado] = useState<{
    resolvedor: ResolvedorAuditoria
    docentes: Teacher[] | null
    docentesError: boolean
  }>({ resolvedor: vacio(), docentes: null, docentesError: false })

  useEffect(() => {
    if (!enabled) {
      return
    }

    const controller = new AbortController()
    const { signal } = controller

    const cargar = async () => {
      const [anios, grados, sedes, grupos, docentes, areas, turnos] = await Promise.allSettled([
        listarCatalogoAniosEscolares(signal),
        listarCatalogoGrados(signal),
        listarCatalogoSedes(signal),
        listarCatalogoGrupos(signal),
        todosLosDocentes(signal),
        listarCatalogoAreas(signal),
        listarCatalogoTurnos(signal),
      ])

      const listaAreas = ok(areas, [])
      const listaSedes = ok(sedes, [])
      const listaTurnos = ok(turnos, [])

      const [asignaturas, franjas, aulas] = await Promise.all([
        Promise.allSettled(listaAreas.map((area) => listarAsignaturas(area.id, signal))),
        Promise.allSettled(listaTurnos.map((turno) => listarFranjasPorTurno(turno.id, signal))),
        Promise.allSettled(listaSedes.map((sede) => listarAulasPorSede(sede.id, signal))),
      ])

      const resolvedor = vacio()
      ok(anios, []).forEach((item) => resolvedor.anios.set(item.id, String(item.year)))
      ok(grados, []).forEach((item) => resolvedor.grados.set(item.id, item.nombre))
      listaSedes.forEach((item) => resolvedor.sedes.set(item.id, item.nombre))
      ok(grupos, []).forEach((item) =>
        resolvedor.grupos.set(item.id, `${item.codigo} · ${item.gradoNombre}`),
      )
      const listaDocentes = ok(docentes, null)
      listaDocentes?.forEach((item) => resolvedor.docentes.set(item.id, formatTeacherName(item)))
      listaAreas.forEach((item) => resolvedor.areas.set(item.id, item.nombre))
      asignaturas.forEach((lista) =>
        ok(lista, []).forEach((item) => resolvedor.asignaturas.set(item.id, item.nombre)),
      )
      listaTurnos.forEach((item) => resolvedor.turnos.set(item.id, item.nombre))
      franjas.forEach((lista, indice) =>
        ok(lista, []).forEach((franja) =>
          resolvedor.franjas.set(
            franja.id,
            `${listaTurnos[indice].nombre} · ${horaCorta(franja.horaInicio)}–${horaCorta(franja.horaFin)}`,
          ),
        ),
      )
      aulas.forEach((lista) =>
        ok(lista, []).forEach((aula) => resolvedor.aulas.set(aula.id, aula.nombre)),
      )

      return { resolvedor, docentes: listaDocentes, docentesError: docentes.status === 'rejected' }
    }

    cargar()
      .then((siguiente) => {
        if (!signal.aborted) {
          setEstado({
            resolvedor: siguiente.resolvedor,
            docentes: siguiente.docentes ?? [],
            docentesError: siguiente.docentesError,
          })
        }
      })
      .catch(() => undefined)

    return () => controller.abort()
  }, [enabled])

  return estado
}

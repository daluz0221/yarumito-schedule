import { jsx, jsxs } from "react/jsx-runtime";
import { useEffect, useMemo, useState } from "react";
import {
  anioEscolarVisible,
  ApiError,
  confirmarAsignacion,
  consultarAsignacion,
  etiquetaGrado,
  listarAreas,
  listarAsignaturas,
  listarBorradores,
  listarAsignacionesPorGrupo,
  asignarActividadDocente,
  listarActividadesDocente,
  listarDocentes,
  listarPlanesEstudio,
  listarRestriccionesPorAnio,
  opcionesAniosEscolares,
  registrarAsignacion,
  useCatalogoAniosEscolares,
  useCatalogoGrupos
} from "../../../api";
import {
  assignmentPriorityNote,
  assignmentPrioritySteps,
  assignmentTabs,
  filasActividadesInstitucionales,
  mergeCurricularAssignments,
  summarizeRows
} from "../../../content/academicAssignment";
import { formatTeacherName } from "../../../content/teachers";
import { Card } from "../../atoms/Card";
import { Text } from "../../atoms/Text";
import { ActivityAssignmentFormModal } from "../ActivityAssignmentFormModal";
import { AssignmentFormModal } from "../AssignmentFormModal";
import { AssignmentSection } from "../AssignmentSection";
import { AssignmentSelector } from "../AssignmentSelector";
import { AssignmentSummary } from "../AssignmentSummary";
import { PlanSubjectPickerModal } from "../PlanSubjectPickerModal";
import styles from "./AssignmentWorkspace.module.css";
function isAbortError(error) {
  return error instanceof DOMException && error.name === "AbortError";
}
function mensajeError(cause, fallback) {
  return cause instanceof ApiError && cause.message ? cause.message : fallback;
}
function materiasDelGrado(planes, yearId, gradoId, areaPorAsignatura) {
  const grouped = /* @__PURE__ */ new Map();
  planes.filter((plan) => plan.anioEscolarId === yearId && plan.gradoId === gradoId).forEach((plan) => {
    const current = grouped.get(plan.asignaturaId);
    if (!current) {
      grouped.set(plan.asignaturaId, {
        key: plan.asignaturaId,
        area: areaPorAsignatura.get(plan.asignaturaId)?.trim() || "\u2014",
        subject: plan.asignaturaNombre?.trim() || "Asignatura",
        requiredHours: plan.horasSemanales
      });
      return;
    }
    current.requiredHours += plan.horasSemanales;
  });
  return [...grouped.values()];
}
function fuenteAsignacion(item) {
  return {
    id: item.id,
    asignaturaId: item.asignaturaId,
    asignaturaNombre: item.asignaturaNombre,
    docenteId: item.docenteId,
    docenteNombre: item.docenteNombre,
    horasSemanales: item.horasSemanales,
    observacion: item.observacion,
    estado: item.estado
  };
}
function AssignmentWorkspace() {
  const { anios, error: aniosError, loading: aniosLoading } = useCatalogoAniosEscolares();
  const { grupos, error: gruposError, loading: gruposLoading } = useCatalogoGrupos();
  const [yearId, setYearId] = useState("");
  const [groupId, setGroupId] = useState("");
  const [tab, setTab] = useState("curricular");
  const [actividadesDocentes, setActividadesDocentes] = useState(
    null
  );
  const [actividadesError, setActividadesError] = useState("");
  const [asignacionesGrupo, setAsignacionesGrupo] = useState(
    null
  );
  const [asignacionesError, setAsignacionesError] = useState("");
  const [borradores, setBorradores] = useState(null);
  const [borradoresError, setBorradoresError] = useState("");
  const [planes, setPlanes] = useState(null);
  const [planesError, setPlanesError] = useState("");
  const [areaPorAsignatura, setAreaPorAsignatura] = useState(
    () => /* @__PURE__ */ new Map()
  );
  const [catalog, setCatalog] = useState(null);
  const [dialog, setDialog] = useState(null);
  const [saveError, setSaveError] = useState("");
  const [saving, setSaving] = useState(false);
  const [confirmingId, setConfirmingId] = useState(null);
  const [actionError, setActionError] = useState("");
  const [revision, setRevision] = useState(0);
  const [conteoRestricciones, setConteoRestricciones] = useState({});
  useEffect(() => {
    if (!anios?.length) {
      return;
    }
    setYearId(
      (current) => anios.some((year2) => year2.id === current) ? current : anioEscolarVisible(anios)?.id ?? ""
    );
  }, [anios]);
  useEffect(() => {
    if (!yearId) {
      setConteoRestricciones({});
      return;
    }
    const controller = new AbortController();
    listarRestriccionesPorAnio(yearId, controller.signal).then((items) => {
      if (controller.signal.aborted) {
        return;
      }
      const counts = {};
      items.forEach((item) => {
        counts[item.docenteId] = (counts[item.docenteId] ?? 0) + 1;
      });
      setConteoRestricciones(counts);
    }).catch((cause) => {
      if (isAbortError(cause) || controller.signal.aborted) {
        return;
      }
      setConteoRestricciones({});
    });
    return () => controller.abort();
  }, [yearId]);
  const yearGroups = useMemo(() => {
    return (grupos ?? []).filter((group2) => group2.anioEscolarId === yearId && group2.activo).sort(
      (left, right) => left.gradoNivel - right.gradoNivel || left.codigo.localeCompare(right.codigo, "es")
    );
  }, [grupos, yearId]);
  useEffect(() => {
    if (!yearId || grupos === null) {
      return;
    }
    setGroupId(
      (current) => yearGroups.some((group2) => group2.id === current) ? current : yearGroups[0]?.id ?? ""
    );
  }, [grupos, yearGroups, yearId]);
  useEffect(() => {
    const controller = new AbortController();
    listarPlanesEstudio(controller.signal).then((items) => {
      setPlanes(items);
      setPlanesError("");
    }).catch((cause) => {
      if (isAbortError(cause) || controller.signal.aborted) {
        return;
      }
      setPlanes([]);
      setPlanesError(
        mensajeError(cause, "No se pudo cargar el plan de estudios de este grado.")
      );
    });
    return () => controller.abort();
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      listarDocentes({ page: 0, size: 100, estado: "ACTIVO" }, controller.signal),
      listarAreas(controller.signal)
    ]).then(async ([teachersPage, areas2]) => {
      if (controller.signal.aborted) {
        return;
      }
      const teachers2 = [...teachersPage.content].sort(
        (left, right) => formatTeacherName(left).localeCompare(formatTeacherName(right), "es")
      );
      setCatalog({ teachers: teachers2, areas: areas2, error: "" });
      try {
        const groups = await Promise.all(
          areas2.map(
            (area) => listarAsignaturas(area.id, controller.signal).then(
              (subjects2) => subjects2.map((subject) => [subject.id, area.nombre])
            )
          )
        );
        if (!controller.signal.aborted) {
          setAreaPorAsignatura(new Map(groups.flat()));
        }
      } catch (cause) {
        if (!isAbortError(cause) && !controller.signal.aborted) {
          setAreaPorAsignatura(/* @__PURE__ */ new Map());
        }
      }
    }).catch((cause) => {
      if (isAbortError(cause)) {
        return;
      }
      setCatalog({
        teachers: [],
        areas: [],
        error: mensajeError(cause, "No se pudieron cargar los docentes.")
      });
    });
    return () => controller.abort();
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    listarBorradores(controller.signal).then((items) => {
      setBorradores(items);
      setBorradoresError("");
    }).catch((cause) => {
      if (isAbortError(cause) || controller.signal.aborted) {
        return;
      }
      setBorradores([]);
      setBorradoresError(
        mensajeError(cause, "No se pudieron cargar los borradores de asignaci\xF3n.")
      );
    });
    return () => controller.abort();
  }, [revision]);
  useEffect(() => {
    if (!groupId) {
      setAsignacionesGrupo([]);
      setAsignacionesError("");
      return;
    }
    const controller = new AbortController();
    setAsignacionesGrupo(null);
    setAsignacionesError("");
    listarAsignacionesPorGrupo(groupId, controller.signal).then((items) => {
      setAsignacionesGrupo(items);
      setAsignacionesError("");
    }).catch((cause) => {
      if (isAbortError(cause) || controller.signal.aborted) {
        return;
      }
      setAsignacionesGrupo([]);
      setAsignacionesError(
        mensajeError(cause, "No se pudieron cargar las asignaciones de este grupo.")
      );
    });
    return () => controller.abort();
  }, [groupId, revision]);
  useEffect(() => {
    if (!catalog) {
      return;
    }
    if (catalog.teachers.length === 0) {
      setActividadesDocentes([]);
      setActividadesError("");
      return;
    }
    const controller = new AbortController();
    Promise.all(
      catalog.teachers.map(async (teacher) => {
        try {
          return await listarActividadesDocente(teacher.id, controller.signal);
        } catch (cause) {
          if (isAbortError(cause) || controller.signal.aborted) {
            throw cause;
          }
          return null;
        }
      })
    ).then((groups) => {
      if (controller.signal.aborted) {
        return;
      }
      const failed = groups.some((groupItems) => groupItems === null);
      setActividadesDocentes(groups.flatMap((groupItems) => groupItems ?? []));
      setActividadesError(
        failed ? "No se pudieron cargar las actividades de todos los docentes." : ""
      );
    }).catch((cause) => {
      if (isAbortError(cause) || controller.signal.aborted) {
        return;
      }
      setActividadesDocentes([]);
      setActividadesError(
        mensajeError(cause, "No se pudieron cargar las actividades de los docentes.")
      );
    });
    return () => controller.abort();
  }, [catalog, revision]);
  const year = anios?.find((item) => item.id === yearId) ?? null;
  const group = yearGroups.find((item) => item.id === groupId) ?? null;
  const yearStatus = year?.status ?? "PLANEACION";
  const gradeLabel = group ? etiquetaGrado(group.gradoNombre) : "";
  const readOnly = yearStatus === "CERRADO";
  const teachers = catalog?.teachers ?? [];
  const areas = catalog?.areas ?? [];
  const subjects = group ? materiasDelGrado(planes ?? [], yearId, group.gradoId, areaPorAsignatura) : [];
  const curricularRows = planes !== null && asignacionesGrupo !== null ? mergeCurricularAssignments(subjects, asignacionesGrupo.map(fuenteAsignacion)) : [];
  const activityOptions = filasActividadesInstitucionales([]);
  const activityRows = actividadesDocentes === null ? [] : filasActividadesInstitucionales(actividadesDocentes);
  const visibleRows = tab === "curricular" ? curricularRows : activityRows;
  const summary = summarizeRows(curricularRows);
  const pendingSubjects = curricularRows.filter((row) => !row.assignmentId);
  const borradoresGrupo = (borradores ?? []).filter((item) => item.grupoId === group?.id);
  const curricularLista = planes !== null && asignacionesGrupo !== null;
  const canCreate = !readOnly && (tab === "curricular" ? Boolean(group) && curricularLista && pendingSubjects.length > 0 && !planesError : teachers.length > 0 && actividadesDocentes !== null);
  const yearOptions = opcionesAniosEscolares(anios, aniosError);
  const groupOptions = gruposLoading ? [{ value: "", label: "Cargando grupos\u2026" }] : gruposError ? [{ value: "", label: "Grupos no disponibles" }] : yearGroups.map((item) => ({ value: item.id, label: item.codigo }));
  const closeDialog = () => {
    if (saving) {
      return;
    }
    setDialog(null);
    setSaveError("");
  };
  const selectYear = (nextYearId) => {
    setYearId(nextYearId);
    setDialog(null);
    setActionError("");
  };
  const upsertAssignment = async (values) => {
    if (!group || !year) {
      return;
    }
    if (!values.teacherId) {
      setSaveError("Selecciona un docente registrado.");
      return;
    }
    setSaving(true);
    setSaveError("");
    try {
      await registrarAsignacion({
        anioEscolarId: year.id,
        docenteId: values.teacherId,
        grupoId: group.id,
        asignaturaId: values.subjectKey,
        horasSemanales: values.assignedHours,
        observacion: values.note.trim() || null
      });
      setRevision((current) => current + 1);
      setDialog(null);
      setSaveError("");
      setActionError("");
    } catch (cause) {
      setSaveError(mensajeError(cause, "No se pudo guardar la asignaci\xF3n acad\xE9mica."));
    } finally {
      setSaving(false);
    }
  };
  const confirmAssignment = async (row) => {
    if (!row.assignmentId || confirmingId) {
      return;
    }
    setConfirmingId(row.assignmentId);
    setActionError("");
    try {
      const actual = await consultarAsignacion(row.assignmentId);
      if (actual.estado !== "BORRADOR") {
        setActionError("La asignaci\xF3n ya no est\xE1 en borrador.");
        setRevision((current) => current + 1);
        return;
      }
      await confirmarAsignacion(row.assignmentId);
      setRevision((current) => current + 1);
    } catch (cause) {
      setActionError(mensajeError(cause, "No se pudo confirmar la asignaci\xF3n."));
    } finally {
      setConfirmingId(null);
    }
  };
  const upsertActivity = async (values) => {
    if (!values.teacherId) {
      setSaveError("Selecciona un docente registrado.");
      return;
    }
    const justificacion = values.note.trim();
    if (!justificacion) {
      setSaveError("Ingresa la justificaci\xF3n de la actividad.");
      return;
    }
    setSaving(true);
    setSaveError("");
    try {
      await asignarActividadDocente(values.teacherId, {
        actividadId: values.subjectKey,
        horasSemanales: values.assignedHours,
        justificacion
      });
      setRevision((current) => current + 1);
      setDialog(null);
      setSaveError("");
      setActionError("");
    } catch (cause) {
      setSaveError(mensajeError(cause, "No se pudo guardar la actividad del docente."));
    } finally {
      setSaving(false);
    }
  };
  const openSubject = (row) => {
    if (row.assignmentId) {
      setDialog(null);
      return;
    }
    setSaveError("");
    setDialog({ name: "create", row });
  };
  return /* @__PURE__ */ jsxs("section", { className: styles.workspace, children: [
    /* @__PURE__ */ jsxs("div", { className: styles.intro, children: [
      /* @__PURE__ */ jsx(Text, { as: "h2", variant: "pageTitle", children: "Asignaci\xF3n acad\xE9mica" }),
      /* @__PURE__ */ jsx(Text, { variant: "body", children: "Asigna docentes a las asignaturas y grupos de acuerdo con el plan de estudios y el perfil acad\xE9mico disponible." })
    ] }),
    /* @__PURE__ */ jsx("div", { className: styles.tabs, role: "tablist", "aria-label": "Tipo de asignaci\xF3n", children: assignmentTabs.map((item) => /* @__PURE__ */ jsx(
      "button",
      {
        type: "button",
        role: "tab",
        "aria-selected": tab === item.id,
        className: tab === item.id ? styles.tabActive : styles.tab,
        onClick: () => {
          setTab(item.id);
          setDialog(null);
        },
        children: item.label
      },
      item.id
    )) }),
    /* @__PURE__ */ jsxs(Card, { className: styles.priority, children: [
      /* @__PURE__ */ jsx(Text, { as: "h3", variant: "sectionTitle", children: "Prioridad para completar la carga" }),
      /* @__PURE__ */ jsx(Text, { variant: "body", children: assignmentPrioritySteps }),
      /* @__PURE__ */ jsx(Text, { variant: "caption", children: assignmentPriorityNote })
    ] }),
    /* @__PURE__ */ jsx(
      AssignmentSelector,
      {
        yearId: year?.id ?? "",
        groupId: group?.id ?? "",
        gradeLabel,
        yearStatus,
        yearOptions,
        groupOptions,
        yearsDisabled: aniosLoading || Boolean(aniosError) || !anios?.length,
        groupsDisabled: gruposLoading || Boolean(gruposError),
        onYearChange: selectYear,
        onGroupChange: (nextGroupId) => {
          setGroupId(nextGroupId);
          setDialog(null);
          setActionError("");
        }
      }
    ),
    aniosError ? /* @__PURE__ */ jsx("p", { className: styles.error, children: aniosError }) : null,
    gruposError ? /* @__PURE__ */ jsx("p", { className: styles.error, children: gruposError }) : null,
    group && year && curricularLista ? /* @__PURE__ */ jsx(
      AssignmentSummary,
      {
        groupCode: group.codigo,
        gradeLabel,
        requiredHours: summary.requiredHours,
        assignedHours: summary.assignedHours,
        status: summary.status,
        subjectCount: subjects.length,
        year: year.year
      }
    ) : gruposLoading || aniosLoading ? /* @__PURE__ */ jsx("p", { className: styles.message, children: "Cargando a\xF1o lectivo y grupos..." }) : /* @__PURE__ */ jsx("p", { className: styles.message, children: "Este a\xF1o lectivo a\xFAn no tiene grupos activos para asignar carga." }),
    catalog === null ? /* @__PURE__ */ jsx("p", { className: styles.message, children: "Cargando docentes..." }) : null,
    catalog?.error ? /* @__PURE__ */ jsx("p", { className: styles.error, children: catalog.error }) : null,
    planesError ? /* @__PURE__ */ jsx("p", { className: styles.error, children: planesError }) : null,
    asignacionesError ? /* @__PURE__ */ jsx("p", { className: styles.error, children: asignacionesError }) : null,
    borradoresError ? /* @__PURE__ */ jsx("p", { className: styles.error, children: borradoresError }) : null,
    actividadesError ? /* @__PURE__ */ jsx("p", { className: styles.error, children: actividadesError }) : null,
    actionError ? /* @__PURE__ */ jsx("p", { className: styles.error, children: actionError }) : null,
    tab === "curricular" && group && borradores && !borradoresError ? /* @__PURE__ */ jsxs("p", { className: styles.message, children: [
      "Borradores pendientes de confirmar: ",
      borradoresGrupo.length,
      " en este grupo y",
      " ",
      borradores.length,
      " en total."
    ] }) : null,
    group && tab === "curricular" && !curricularLista && !planesError && !asignacionesError ? /* @__PURE__ */ jsx("p", { className: styles.message, children: "Cargando asignaciones del grupo..." }) : null,
    tab === "activity" && catalog && actividadesDocentes === null ? /* @__PURE__ */ jsx("p", { className: styles.message, children: "Cargando actividades de los docentes..." }) : null,
    (tab === "activity" ? catalog !== null && actividadesDocentes !== null : Boolean(group) && curricularLista) ? /* @__PURE__ */ jsx(
      AssignmentSection,
      {
        title: tab === "curricular" ? "Asignaciones curriculares" : "Actividades y proyectos",
        description: tab === "curricular" ? "Las horas requeridas provienen del plan de estudios; las asignadas corresponden a la asignaci\xF3n acad\xE9mica." : "Complementa la carga con actividades y proyectos institucionales cuando el plan curricular ya est\xE1 cubierto.",
        caption: tab === "curricular" ? "La asignaci\xF3n queda en borrador hasta que se confirma. El grado se deriva del grupo seleccionado." : "La asignaci\xF3n queda guardada en el docente. No depende del grupo seleccionado ni reemplaza el plan de estudios.",
        rows: visibleRows,
        emptyMessage: tab === "curricular" ? "Este grado todav\xEDa no tiene asignaturas en el plan de estudios." : "No hay actividades institucionales configuradas.",
        canCreate,
        createLabel: tab === "curricular" ? "+ Asignar docente" : "+ Asignar actividad",
        readOnly,
        confirmingId,
        editableAssigned: tab !== "activity",
        groupId: tab === "curricular" ? group?.id : void 0,
        onCreate: () => {
          setSaveError("");
          setDialog(tab === "curricular" ? { name: "pick-subject" } : { name: "create-activity" });
        },
        onAssign: (row) => {
          setSaveError("");
          setDialog(
            tab === "curricular" ? { name: "create", row } : { name: "create-activity", row }
          );
        },
        onEdit: () => void 0,
        onRemove: () => void 0,
        onConfirm: (row) => {
          void confirmAssignment(row);
        }
      }
    ) : null,
    dialog?.name === "pick-subject" && group && year ? /* @__PURE__ */ jsx(
      PlanSubjectPickerModal,
      {
        open: true,
        year: year.year,
        groupCode: group.codigo,
        gradeLabel,
        subjects: pendingSubjects,
        onClose: closeDialog,
        onSelect: openSubject
      }
    ) : null,
    dialog?.name === "create" && group && year ? /* @__PURE__ */ jsx(
      AssignmentFormModal,
      {
        open: true,
        mode: "create",
        year: year.year,
        restrictionCounts: conteoRestricciones,
        groupCode: group.codigo,
        gradeLabel,
        subjects: pendingSubjects.length > 0 ? pendingSubjects : curricularRows,
        teachers,
        areas,
        row: dialog.row,
        saving,
        error: saveError,
        onClose: closeDialog,
        onSubmit: (values) => {
          void upsertAssignment(values);
        }
      },
      `create-${dialog.row?.key ?? "new"}`
    ) : null,
    dialog?.name === "create-activity" ? /* @__PURE__ */ jsx(
      ActivityAssignmentFormModal,
      {
        open: true,
        mode: "create",
        year: year?.year ?? 0,
        restrictionCounts: conteoRestricciones,
        activities: activityOptions,
        teachers,
        row: dialog.row,
        saving,
        error: saveError,
        onClose: closeDialog,
        onSubmit: (values) => {
          void upsertActivity(values);
        }
      },
      `create-activity-${dialog.row?.key ?? "new"}`
    ) : null
  ] });
}
export {
  AssignmentWorkspace
};

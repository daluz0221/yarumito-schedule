import { schoolGroups } from "./academicYear";
import { findStudyPlanGrade, studyPlanEntries } from "./studyPlan";
import { formatTeacherName } from "./teachers";
const assignmentTabs = [
  { id: "curricular", label: "Asignaciones curriculares" },
  { id: "activity", label: "Actividades y proyectos" }
];
const assignmentKindOptions = [
  { value: "PRINCIPAL", label: "Principal" },
  { value: "IDONEIDAD", label: "Idoneidad" },
  { value: "TRANSVERSAL", label: "Transversal" },
  { value: "ACTIVIDAD", label: "Actividad / proyecto" }
];
const curricularKindOptions = assignmentKindOptions.filter(
  (option) => option.value !== "ACTIVIDAD"
);
const assignmentKindHint = "Principal: \xE1rea de nombramiento \xB7 Idoneidad: v\xE1lida para la asignatura \xB7 Transversal: permitida por plan, idoneidad y reglas institucionales.";
const assignmentKindDetails = [
  {
    value: "PRINCIPAL",
    label: "Principal",
    description: "Corresponde al \xE1rea principal de nombramiento del docente."
  },
  {
    value: "IDONEIDAD",
    label: "Idoneidad",
    description: "Requiere una idoneidad v\xE1lida para la asignatura. Un docente puede tener m\xFAltiples idoneidades."
  },
  {
    value: "TRANSVERSAL",
    label: "Transversal \xB7 HU-06",
    description: "La asignatura debe pertenecer al Plan de Estudios y cumplir idoneidad + reglas institucionales. \xC9tica, Educaci\xF3n Art\xEDstica y Religi\xF3n son ejemplos condicionados; no habilitada para cualquier docente."
  }
];
const assignmentKindModalNote = "Las filas actuales coinciden con el \xE1rea principal. El Rector conserva la decisi\xF3n final; el tipo no sustituye la verificaci\xF3n acad\xE9mica.";
const assignmentKindModalHint = "Selecci\xF3n demostrativa. Debe comprobarse idoneidad y, para transversales, el Plan de Estudios y las reglas vigentes antes de formalizar la asignaci\xF3n.";
const assignmentTeacherPriorityHint = "Prioridad: \xE1rea principal y despu\xE9s idoneidades v\xE1lidas. Sin l\xEDmite de idoneidades.";
const assignmentRestrictionWarning = "Este docente tiene restricciones de disponibilidad registradas. Se evaluar\xE1n al construir el horario; no se declara conflicto en esta asignaci\xF3n.";
const activityAssignmentHint = "No corresponde a una asignatura del Plan de Estudios. La justificaci\xF3n queda registrada con la asignaci\xF3n.";
const institutionalActivities = [
  {
    id: "6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e501",
    name: "Proyecto de acompa\xF1amiento institucional",
    area: "Institucional",
    weeklyHours: 2
  },
  {
    id: "6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e502",
    name: "Proyecto de convivencia",
    area: "Institucional",
    weeklyHours: 2
  },
  {
    id: "6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e503",
    name: "Proyecto de emprendimiento",
    area: "Institucional",
    weeklyHours: 2
  }
];
const gradeNameToId = {
  Sexto: "6",
  S\u00E9ptimo: "7",
  Octavo: "8",
  Noveno: "9",
  D\u00E9cimo: "10",
  Und\u00E9cimo: "11"
};
const assignmentPrioritySteps = "1. \xC1rea principal \u2192 2. Idoneidades v\xE1lidas \u2192 3. Transversales permitidas \u2192 4. Actividades / proyectos institucionales";
const assignmentPriorityNote = "El Rector conserva la decisi\xF3n final. No se realizar\xE1n asignaciones autom\xE1ticas.";
const assignmentFormHint = "Las horas requeridas vienen del plan de estudios. El docente se elige entre los registrados y el tipo gu\xEDa la prioridad de carga, sin automatizar la decisi\xF3n.";
const seedAssignments = [
  {
    id: "2026-601-mat",
    yearId: "2026",
    groupId: "601",
    subjectKey: "2026-6-mat",
    tab: "curricular",
    teacherId: null,
    teacherName: "Carlos P\xE9rez",
    assignedHours: 5,
    kind: "PRINCIPAL",
    note: "Asignaci\xF3n curricular del grupo 601"
  },
  {
    id: "2026-601-bio",
    yearId: "2026",
    groupId: "601",
    subjectKey: "2026-6-bio",
    tab: "curricular",
    teacherId: null,
    teacherName: "Diana Marcela Ruiz",
    assignedHours: 2,
    kind: "PRINCIPAL",
    note: "Asignaci\xF3n curricular del grupo 601"
  }
];
function groupsForYear(yearId) {
  return schoolGroups.filter((group) => group.yearId === yearId && group.status === "ACTIVO");
}
function assignmentGroupOptions(yearId) {
  return groupsForYear(yearId).map((group) => ({
    value: group.id,
    label: group.code
  }));
}
function gradeIdFromGroup(group) {
  return gradeNameToId[group.grade] ?? "6";
}
function gradeLabelFromGroup(group) {
  return findStudyPlanGrade(gradeIdFromGroup(group)).label;
}
function planEntriesForGroup(yearId, group) {
  const gradeId = gradeIdFromGroup(group);
  return studyPlanEntries.filter(
    (entry) => entry.yearId === yearId && entry.gradeId === gradeId
  );
}
function labelAssignmentKind(kind) {
  if (!kind) {
    return "\u2014";
  }
  return assignmentKindOptions.find((option) => option.value === kind)?.label ?? kind;
}
function assignmentStatus(requiredHours, assignedHours, hasTeacher) {
  if (!hasTeacher || assignedHours <= 0) {
    return "SIN_ASIGNAR";
  }
  if (assignedHours >= requiredHours) {
    return "COMPLETA";
  }
  return "PARCIAL";
}
function labelAssignmentStatus(status) {
  const labels = {
    COMPLETA: "Completa",
    PARCIAL: "Parcial",
    SIN_ASIGNAR: "Sin asignar"
  };
  return labels[status];
}
function resolveAssignmentTeacher(assignment, teachers) {
  if (!assignment) {
    return { teacherId: null, teacherName: "" };
  }
  const byId = assignment.teacherId ? teachers.find((teacher2) => teacher2.id === assignment.teacherId) : void 0;
  const byName = assignment.teacherName ? teachers.find(
    (teacher2) => formatTeacherName(teacher2).toLowerCase() === assignment.teacherName?.toLowerCase()
  ) : void 0;
  const teacher = byId ?? byName;
  return {
    teacherId: teacher?.id ?? assignment.teacherId,
    teacherName: teacher ? formatTeacherName(teacher) : assignment.teacherName ?? ""
  };
}
function buildAssignmentRows(subjects, assignments, teachers) {
  return subjects.map((item) => {
    const assignment = assignments.find((record) => record.subjectKey === item.key);
    const teacher = resolveAssignmentTeacher(assignment, teachers);
    const assignedHours = assignment?.assignedHours ?? 0;
    const assigned = Boolean(teacher.teacherName) && assignedHours > 0;
    const status = assignmentStatus(item.requiredHours, assignedHours, assigned);
    return {
      key: item.key,
      area: item.area,
      subject: item.subject,
      requiredHours: item.requiredHours,
      assignedHours,
      teacherId: teacher.teacherId,
      teacherName: teacher.teacherName,
      kind: assignment?.kind ?? null,
      kindLabel: labelAssignmentKind(assignment?.kind ?? null),
      status,
      assigned,
      note: assignment?.note ?? "",
      assignmentId: null,
      estado: null
    };
  });
}
function filaCurricular(item, assignment) {
  const assignedHours = assignment?.horasSemanales ?? 0;
  const teacherName = assignment?.docenteNombre?.trim() ?? "";
  const assigned = Boolean(teacherName) && assignedHours > 0;
  return {
    key: item.key,
    area: item.area,
    subject: item.subject,
    requiredHours: item.requiredHours,
    assignedHours,
    teacherId: assignment?.docenteId ?? null,
    teacherName,
    kind: null,
    kindLabel: assignment ? "Curricular" : "\u2014",
    status: assignmentStatus(item.requiredHours, assignedHours, assigned),
    assigned,
    note: assignment?.observacion?.trim() ?? "",
    assignmentId: assignment?.id ?? null,
    estado: assignment?.estado ?? null
  };
}
function mergeCurricularAssignments(subjects, assignments) {
  const bySubject = /* @__PURE__ */ new Map();
  assignments.forEach((item) => {
    const current = bySubject.get(item.asignaturaId);
    if (!current || current.estado !== "BORRADOR" && item.estado === "BORRADOR") {
      bySubject.set(item.asignaturaId, item);
    }
  });
  const covered = new Set(subjects.map((item) => item.key));
  const rows = subjects.map((item) => filaCurricular(item, bySubject.get(item.key)));
  bySubject.forEach((assignment) => {
    if (covered.has(assignment.asignaturaId)) {
      return;
    }
    rows.push(
      filaCurricular(
        {
          key: assignment.asignaturaId,
          area: "\u2014",
          subject: assignment.asignaturaNombre?.trim() || "Asignatura",
          requiredHours: assignment.horasSemanales
        },
        assignment
      )
    );
  });
  return rows;
}
function curricularSubjects(entries) {
  return entries.map((entry) => ({
    key: entry.id,
    area: entry.area,
    subject: entry.subject,
    requiredHours: entry.weeklyHours
  }));
}
function activitySubjects() {
  return institutionalActivities.map((item) => ({
    key: item.id,
    area: item.area,
    subject: item.name,
    requiredHours: item.weeklyHours
  }));
}
const tipoActividadLabel = {
  PROYECTO_PEDAGOGICO: "Proyecto pedag\xF3gico",
  ACTIVIDAD_INSTITUCIONAL: "Actividad institucional",
  DIRECCION_DE_GRUPO: "Direcci\xF3n de grupo",
  COMITE: "Comit\xE9",
  OTRO: "Otro"
};
function etiquetaTipoActividad(tipo) {
  return tipoActividadLabel[tipo] ?? (tipo || "Actividad / proyecto");
}
function filaActividadVacia(item) {
  return {
    key: item.key,
    area: item.area,
    subject: item.subject,
    requiredHours: item.requiredHours,
    assignedHours: 0,
    teacherId: null,
    teacherName: "",
    kind: null,
    kindLabel: "\u2014",
    status: "SIN_ASIGNAR",
    assigned: false,
    note: "",
    assignmentId: null,
    estado: null
  };
}
function filaActividadAsignada(item, assignment) {
  const assignedHours = assignment.horasSemanales;
  const teacherName = assignment.docenteNombre?.trim() ?? "";
  return {
    key: item.key,
    area: item.area,
    subject: item.subject,
    requiredHours: item.requiredHours,
    assignedHours,
    teacherId: assignment.docenteId,
    teacherName,
    kind: "ACTIVIDAD",
    kindLabel: etiquetaTipoActividad(assignment.tipoActividad),
    status: assignmentStatus(item.requiredHours, assignedHours, Boolean(teacherName)),
    assigned: Boolean(teacherName) && assignedHours > 0,
    note: assignment.justificacion?.trim() ?? "",
    assignmentId: null,
    estado: null
  };
}
function filasActividadesInstitucionales(assignments) {
  const subjects = activitySubjects();
  const pending = /* @__PURE__ */ new Map();
  assignments.forEach((item) => {
    const list = pending.get(item.actividadId) ?? [];
    list.push(item);
    pending.set(item.actividadId, list);
  });
  const rows = [];
  const used = /* @__PURE__ */ new Set();
  subjects.forEach((subject) => {
    const matches = pending.get(subject.key) ?? [];
    if (matches.length === 0) {
      rows.push(filaActividadVacia(subject));
      return;
    }
    matches.forEach((match, index) => {
      used.add(match.id);
      rows.push(
        filaActividadAsignada(
          index === 0 ? subject : { ...subject, key: match.id },
          match
        )
      );
    });
  });
  assignments.forEach((assignment) => {
    if (used.has(assignment.id)) {
      return;
    }
    rows.push(
      filaActividadAsignada(
        {
          key: assignment.id,
          area: "Institucional",
          subject: assignment.actividadNombre?.trim() || "Actividad institucional",
          requiredHours: assignment.horasSemanales
        },
        assignment
      )
    );
  });
  return rows;
}
function summarizeRows(rows) {
  const requiredHours = rows.reduce((total, row) => total + row.requiredHours, 0);
  const assignedHours = rows.reduce((total, row) => total + row.assignedHours, 0);
  const hasAny = rows.some((row) => row.assigned);
  const allComplete = rows.length > 0 && rows.every((row) => row.status === "COMPLETA");
  const status = allComplete ? "COMPLETA" : hasAny ? "PARCIAL" : "SIN_ASIGNAR";
  return { requiredHours, assignedHours, status };
}
function nextAssignmentId(yearId, groupId, subjectKey) {
  return `${yearId}-${groupId}-${subjectKey}`;
}
function subjectPlanHint(gradeLabel, row) {
  if (!row) {
    return `Plan de estudios de ${gradeLabel}.`;
  }
  const pending = Math.max(row.requiredHours - row.assignedHours, 0);
  return `Plan de estudios de ${gradeLabel} \xB7 Requeridas: ${row.requiredHours} h \xB7 Asignadas: ${row.assignedHours} h \xB7 Pendientes: ${pending} h`;
}
function hoursStatusCopy(requiredHours, assignedHours) {
  if (assignedHours > requiredHours) {
    const extra = assignedHours - requiredHours;
    return {
      tone: "warning",
      text: `Excede \xB7 ${assignedHours} h asignadas frente a ${requiredHours} h requeridas. Revisa la asignaci\xF3n: supera el plan en ${extra} h.`
    };
  }
  if (assignedHours === requiredHours && assignedHours > 0) {
    return {
      tone: "success",
      text: `Completa \xB7 ${assignedHours} h asignadas de ${requiredHours} h requeridas.`
    };
  }
  const pending = Math.max(requiredHours - assignedHours, 0);
  return {
    tone: "pending",
    text: `Pendiente \xB7 ${assignedHours} h asignadas de ${requiredHours} h requeridas. Faltan ${pending} h.`
  };
}
function normalizeLabel(value) {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();
}
function teacherMatchesArea(teacherArea, subjectArea) {
  return Boolean(teacherArea) && normalizeLabel(teacherArea) === normalizeLabel(subjectArea);
}
function teacherCompatibilityHint(teacherStatus, teacherArea, subjectArea, hasValidIdoneidad) {
  if (teacherMatchesArea(teacherArea, subjectArea)) {
    return `${teacherStatus} \xB7 \xC1rea principal: ${teacherArea} \xB7 Coincidencia por \xE1rea principal.`;
  }
  if (hasValidIdoneidad) {
    return `${teacherStatus} \xB7 \xC1rea principal: ${teacherArea || "sin registrar"} \xB7 Idoneidad v\xE1lida para la asignatura.`;
  }
  return `${teacherStatus} \xB7 \xC1rea principal: ${teacherArea || "sin registrar"} \xB7 Sin coincidencia autom\xE1tica; el Rector decide.`;
}
function namedTeacherValue(name) {
  return `name:${name}`;
}
function parseTeacherSelection(value) {
  if (value.startsWith("name:")) {
    return { teacherId: null, teacherName: value.slice(5) };
  }
  return { teacherId: value, teacherName: "" };
}
export {
  activityAssignmentHint,
  activitySubjects,
  assignmentFormHint,
  assignmentGroupOptions,
  assignmentKindDetails,
  assignmentKindHint,
  assignmentKindModalHint,
  assignmentKindModalNote,
  assignmentKindOptions,
  assignmentPriorityNote,
  assignmentPrioritySteps,
  assignmentRestrictionWarning,
  assignmentStatus,
  assignmentTabs,
  assignmentTeacherPriorityHint,
  buildAssignmentRows,
  curricularKindOptions,
  curricularSubjects,
  filasActividadesInstitucionales,
  gradeIdFromGroup,
  gradeLabelFromGroup,
  groupsForYear,
  hoursStatusCopy,
  institutionalActivities,
  labelAssignmentKind,
  labelAssignmentStatus,
  mergeCurricularAssignments,
  namedTeacherValue,
  nextAssignmentId,
  parseTeacherSelection,
  planEntriesForGroup,
  resolveAssignmentTeacher,
  seedAssignments,
  subjectPlanHint,
  summarizeRows,
  teacherCompatibilityHint,
  teacherMatchesArea
};

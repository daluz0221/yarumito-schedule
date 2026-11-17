import { jsx, jsxs } from "react/jsx-runtime";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  activityAssignmentHint,
  assignmentRestrictionWarning,
  namedTeacherValue,
  parseTeacherSelection
} from "../../../content/academicAssignment";
import { formatTeacherName } from "../../../content/teachers";
import { Button } from "../../atoms/Button";
import { Modal } from "../../atoms/Modal";
import { TextArea } from "../../atoms/TextArea";
import { FormField } from "../../molecules/FormField";
import { ModalActions } from "../../molecules/ModalActions";
import { SelectField } from "../../molecules/SelectField";
import { TextField } from "../../molecules/TextField";
import styles from "./ActivityAssignmentFormModal.module.css";
const requiredChoiceMessage = "Seleccione una opci\xF3n.";
const hoursMessage = "Ingresa las horas asignadas.";
const justificationMessage = "Ingresa la justificaci\xF3n de la actividad.";
function initialTeacherValue(row, teachers) {
  if (row?.teacherId && teachers.some((teacher) => teacher.id === row.teacherId)) {
    return row.teacherId;
  }
  if (row?.teacherName) {
    const match = teachers.find(
      (teacher) => formatTeacherName(teacher).toLowerCase() === row.teacherName.toLowerCase()
    );
    return match?.id ?? namedTeacherValue(row.teacherName);
  }
  return "";
}
function ActivityAssignmentFormModal({
  open,
  mode,
  year,
  restrictionCounts,
  activities,
  teachers,
  row,
  saving = false,
  error = "",
  onClose,
  onSubmit
}) {
  const navigate = useNavigate();
  const editing = mode === "edit";
  const initialActivity = row?.key ?? activities.find((item) => !item.assigned)?.key ?? activities[0]?.key ?? "";
  const [values, setValues] = useState(() => ({
    subjectKey: initialActivity,
    teacherValue: initialTeacherValue(row, teachers),
    hours: row && row.assignedHours > 0 ? String(row.assignedHours) : String(row?.requiredHours || 1),
    note: row?.note ?? ""
  }));
  const [attempted, setAttempted] = useState(false);
  const formId = `activity-assignment-${mode}-form`;
  const hoursValue = Number(values.hours);
  const hoursInvalid = !values.hours || !Number.isInteger(hoursValue) || hoursValue < 1;
  const selectedTeacher = teachers.find((teacher) => teacher.id === values.teacherValue);
  const restrictionCount = selectedTeacher ? restrictionCounts?.[selectedTeacher.id] ?? 0 : 0;
  const teacherOptions = [
    { value: "", label: "Seleccionar docente" },
    ...teachers.filter((teacher) => teacher.estado === "ACTIVO").map((teacher) => ({
      value: teacher.id,
      label: formatTeacherName(teacher)
    })),
    ...row?.teacherName && !teachers.some(
      (teacher) => formatTeacherName(teacher).toLowerCase() === row.teacherName.toLowerCase()
    ) ? [{ value: namedTeacherValue(row.teacherName), label: row.teacherName }] : []
  ];
  const justification = values.note.trim();
  const justificationInvalid = justification.length === 0;
  const handleSubmit = (event) => {
    event.preventDefault();
    if (saving) {
      return;
    }
    setAttempted(true);
    if (!values.subjectKey || !values.teacherValue || hoursInvalid || justificationInvalid) {
      return;
    }
    const teacher = parseTeacherSelection(values.teacherValue);
    const catalogTeacher = teacher.teacherId ? teachers.find((item) => item.id === teacher.teacherId) : void 0;
    onSubmit({
      subjectKey: values.subjectKey,
      teacherId: teacher.teacherId,
      teacherName: catalogTeacher ? formatTeacherName(catalogTeacher) : teacher.teacherName,
      assignedHours: hoursValue,
      note: justification
    });
  };
  const patch = (partial) => {
    setValues((current) => ({ ...current, ...partial }));
  };
  return /* @__PURE__ */ jsx(
    Modal,
    {
      open,
      title: editing ? "Editar actividad o proyecto" : "Asignar actividad o proyecto",
      description: `A\xF1o lectivo ${year} \xB7 Asignaci\xF3n institucional por docente`,
      onClose,
      footer: /* @__PURE__ */ jsx(
        ModalActions,
        {
          formId,
          confirmLabel: saving ? "Guardando\u2026" : editing ? "Guardar cambios" : "Guardar asignaci\xF3n",
          confirmType: "submit",
          disabled: saving,
          onCancel: onClose
        }
      ),
      children: /* @__PURE__ */ jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [
        /* @__PURE__ */ jsx(
          SelectField,
          {
            id: `${formId}-teacher`,
            label: "Docente",
            name: "teacher",
            required: true,
            value: values.teacherValue,
            options: teacherOptions,
            error: attempted && !values.teacherValue ? requiredChoiceMessage : void 0,
            onChange: (teacherValue) => patch({ teacherValue })
          }
        ),
        /* @__PURE__ */ jsx(
          SelectField,
          {
            id: `${formId}-activity`,
            label: "Actividad institucional",
            name: "activity",
            required: true,
            value: values.subjectKey,
            options: activities.map((item) => ({
              value: item.key,
              label: item.subject
            })),
            disabled: editing,
            hint: activityAssignmentHint,
            error: attempted && !values.subjectKey ? requiredChoiceMessage : void 0,
            onChange: (subjectKey) => patch({ subjectKey })
          }
        ),
        /* @__PURE__ */ jsx(
          TextField,
          {
            id: `${formId}-hours`,
            label: "Horas asignadas",
            name: "hours",
            required: true,
            inputMode: "numeric",
            placeholder: "Ej. 1",
            value: values.hours,
            error: attempted && hoursInvalid ? hoursMessage : void 0,
            onChange: (event) => patch({ hours: event.target.value.replace(/\D/g, "") })
          }
        ),
        /* @__PURE__ */ jsx(
          FormField,
          {
            id: `${formId}-note`,
            label: "Justificaci\xF3n",
            required: true,
            error: attempted && justificationInvalid ? justificationMessage : void 0,
            children: /* @__PURE__ */ jsx(
              TextArea,
              {
                id: `${formId}-note`,
                name: "note",
                rows: 3,
                required: true,
                placeholder: "Por qu\xE9 se asigna esta actividad",
                value: values.note,
                onChange: (event) => patch({ note: event.target.value })
              }
            )
          }
        ),
        error ? /* @__PURE__ */ jsx("p", { className: styles.error, children: error }) : null,
        restrictionCount > 0 ? /* @__PURE__ */ jsx("p", { className: styles.warning, children: assignmentRestrictionWarning }) : selectedTeacher ? /* @__PURE__ */ jsx("p", { className: styles.note, children: "Sin restricciones de disponibilidad registradas." }) : null,
        selectedTeacher ? /* @__PURE__ */ jsx("div", { className: styles.links, children: /* @__PURE__ */ jsx(
          Button,
          {
            type: "button",
            variant: "outline",
            size: "sm",
            onClick: () => navigate(`/dashboard/restricciones?docente=${selectedTeacher.id}`),
            children: "Ver restricciones"
          }
        ) }) : null
      ] })
    }
  );
}
export {
  ActivityAssignmentFormModal
};

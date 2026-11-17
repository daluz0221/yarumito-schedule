import { jsx, jsxs } from "react/jsx-runtime";
import {
  labelAssignmentStatus
} from "../../../content/academicAssignment";
import { rutaHistorialEntidad } from "../../../content/auditoria";
import { Button } from "../../atoms/Button";
import { IconAction, IconActions } from "../../atoms/IconAction";
import { TextLink } from "../../atoms/TextLink";
import styles from "./AssignmentTable.module.css";
function AssignmentTable({
  rows,
  emptyMessage,
  readOnly = false,
  confirmingId = null,
  editableAssigned = true,
  groupId,
  onAssign,
  onEdit,
  onRemove,
  onConfirm
}) {
  return /* @__PURE__ */ jsxs("div", { children: [
    /* @__PURE__ */ jsx("div", { className: styles.scroll, children: /* @__PURE__ */ jsxs("table", { className: styles.table, children: [
      /* @__PURE__ */ jsx("thead", { children: /* @__PURE__ */ jsxs("tr", { children: [
        /* @__PURE__ */ jsx("th", { children: "\xC1rea" }),
        /* @__PURE__ */ jsx("th", { children: "Asignatura" }),
        /* @__PURE__ */ jsx("th", { children: "Horas requeridas" }),
        /* @__PURE__ */ jsx("th", { children: "Horas asignadas" }),
        /* @__PURE__ */ jsx("th", { children: "Docente" }),
        /* @__PURE__ */ jsx("th", { children: "Tipo de asignaci\xF3n" }),
        /* @__PURE__ */ jsx("th", { children: "Estado" }),
        /* @__PURE__ */ jsx("th", { children: "Acciones" })
      ] }) }),
      /* @__PURE__ */ jsx("tbody", { children: rows.map((row) => /* @__PURE__ */ jsxs("tr", { children: [
        /* @__PURE__ */ jsx("td", { children: row.area }),
        /* @__PURE__ */ jsx("td", { children: row.subject }),
        /* @__PURE__ */ jsx("td", { children: row.requiredHours }),
        /* @__PURE__ */ jsx("td", { children: row.assignedHours }),
        /* @__PURE__ */ jsx("td", { children: row.teacherName || "Sin docente" }),
        /* @__PURE__ */ jsx("td", { children: row.kindLabel }),
        /* @__PURE__ */ jsx("td", { children: /* @__PURE__ */ jsx(StatusChip, { row }) }),
        /* @__PURE__ */ jsx("td", { children: /* @__PURE__ */ jsx(
          RowActions,
          {
            row,
            readOnly,
            confirmingId,
            editableAssigned,
            groupId,
            onAssign,
            onEdit,
            onRemove,
            onConfirm
          }
        ) })
      ] }, row.key)) })
    ] }) }),
    /* @__PURE__ */ jsx("ul", { className: styles.cards, children: rows.map((row) => /* @__PURE__ */ jsxs("li", { className: styles.card, children: [
      /* @__PURE__ */ jsxs("div", { className: styles.cardHeader, children: [
        /* @__PURE__ */ jsx("strong", { children: row.subject }),
        /* @__PURE__ */ jsx(StatusChip, { row })
      ] }),
      /* @__PURE__ */ jsxs("p", { children: [
        row.area,
        " \xB7 ",
        row.assignedHours,
        "/",
        row.requiredHours,
        " h"
      ] }),
      /* @__PURE__ */ jsxs("p", { children: [
        row.teacherName || "Sin docente",
        " \xB7 ",
        row.kindLabel
      ] }),
      /* @__PURE__ */ jsx(
        RowActions,
        {
          row,
          readOnly,
          confirmingId,
          editableAssigned,
          groupId,
          onAssign,
          onEdit,
          onRemove,
          onConfirm
        }
      )
    ] }, row.key)) }),
    rows.length === 0 ? /* @__PURE__ */ jsx("p", { className: styles.empty, children: emptyMessage }) : null
  ] });
}
function StatusChip({ row }) {
  const persistence = row.estado === "BORRADOR" ? " \xB7 Borrador" : row.estado === "CONFIRMADA" ? " \xB7 Confirmada" : "";
  return /* @__PURE__ */ jsxs("span", { className: `${styles.status} ${styles[row.status.toLowerCase()]}`, children: [
    labelAssignmentStatus(row.status),
    persistence
  ] });
}
function HistorialAsignacion({ row, groupId }) {
  if (!row.assignmentId || !groupId) {
    return null;
  }
  return /* @__PURE__ */ jsx(
    TextLink,
    {
      className: styles.history,
      to: rutaHistorialEntidad("AsignacionAcademica", row.assignmentId, groupId),
      children: "Ver historial"
    }
  );
}
function RowActions({
  row,
  readOnly,
  confirmingId,
  editableAssigned,
  groupId,
  onAssign,
  onEdit,
  onRemove,
  onConfirm
}) {
  const historial = /* @__PURE__ */ jsx(HistorialAsignacion, { row, groupId });
  let accion = null;
  if (readOnly) {
    accion = /* @__PURE__ */ jsx("span", { className: styles.muted, children: "Solo consulta" });
  } else if (row.estado === "CONFIRMADA") {
    accion = /* @__PURE__ */ jsx("span", { className: styles.muted, children: "Confirmada" });
  } else if (row.estado === "BORRADOR" && onConfirm) {
    const pending = confirmingId === row.assignmentId;
    accion = /* @__PURE__ */ jsx(
      Button,
      {
        variant: "outline",
        size: "sm",
        disabled: pending || Boolean(confirmingId),
        onClick: () => onConfirm(row),
        children: pending ? "Confirmando\u2026" : "Confirmar"
      }
    );
  } else if (!row.assigned) {
    accion = /* @__PURE__ */ jsx(Button, { variant: "outline", size: "sm", onClick: () => onAssign(row), children: "Asignar docente" });
  } else if (!editableAssigned) {
    accion = /* @__PURE__ */ jsx("span", { className: styles.muted, children: "Asignada" });
  } else {
    const subject = row.subject || "asignaci\xF3n";
    accion = /* @__PURE__ */ jsxs(IconActions, { children: [
      /* @__PURE__ */ jsx(IconAction, { label: `Editar ${subject}`, onClick: () => onEdit(row) }),
      /* @__PURE__ */ jsx(
        IconAction,
        {
          tone: "danger",
          label: `Quitar asignaci\xF3n de ${subject}`,
          onClick: () => onRemove(row)
        }
      )
    ] });
  }
  if (!accion && !historial) {
    return null;
  }
  return /* @__PURE__ */ jsxs("span", { className: styles.actions, children: [
    accion,
    historial
  ] });
}
export {
  AssignmentTable
};

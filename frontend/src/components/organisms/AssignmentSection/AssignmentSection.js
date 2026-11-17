import { jsx, jsxs } from "react/jsx-runtime";
import { Button } from "../../atoms/Button";
import { Card } from "../../atoms/Card";
import { Text } from "../../atoms/Text";
import { SectionHeader } from "../../molecules/SectionHeader";
import { AssignmentTable } from "../AssignmentTable";
import styles from "./AssignmentSection.module.css";
function AssignmentSection({
  title,
  description,
  caption,
  rows,
  emptyMessage,
  canCreate,
  createLabel = "+ Asignar docente",
  readOnly = false,
  confirmingId = null,
  editableAssigned = true,
  groupId,
  onCreate,
  onAssign,
  onEdit,
  onRemove,
  onConfirm
}) {
  return /* @__PURE__ */ jsxs(Card, { className: styles.section, children: [
    /* @__PURE__ */ jsx(
      SectionHeader,
      {
        title,
        description,
        action: readOnly ? void 0 : /* @__PURE__ */ jsx(Button, { variant: "primary", size: "sm", disabled: !canCreate, onClick: onCreate, children: createLabel })
      }
    ),
    /* @__PURE__ */ jsx("div", { className: styles.tableHost, children: /* @__PURE__ */ jsx(
      AssignmentTable,
      {
        rows,
        emptyMessage,
        readOnly,
        confirmingId,
        editableAssigned,
        groupId,
        onAssign,
        onEdit,
        onRemove,
        onConfirm
      }
    ) }),
    /* @__PURE__ */ jsx(Text, { variant: "caption", children: caption })
  ] });
}
export {
  AssignmentSection
};

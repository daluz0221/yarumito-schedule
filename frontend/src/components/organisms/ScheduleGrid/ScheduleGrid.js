import { jsxs as _jsxs, jsx as _jsx } from "react/jsx-runtime";
import { availabilityDays, morningBands, } from '../../../content/teacherRestrictions';
import { rutaHistorialEntidad } from '../../../content/auditoria';
import { blockKey } from '../../../content/scheduleConstruction';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { TextLink } from '../../atoms/TextLink';
import styles from './ScheduleGrid.module.css';
export function ScheduleGrid({ groupCode, groupId, shiftLabel, readOnly, bands = morningBands, blocks, onProgram, onEdit, }) {
    const pause = bands.some((band) => band.kind === 'break');
    const slots = bands.filter((band) => band.kind === 'class');
    return (_jsxs(Card, { className: styles.card, children: [_jsxs("div", { className: styles.heading, children: [_jsxs(Text, { as: "h3", variant: "sectionTitle", children: ["Horario por grupo \u00B7 ", groupCode] }), _jsxs(Text, { variant: "caption", children: ["Turno: ", shiftLabel] })] }), _jsx("div", { className: styles.scroll, children: _jsxs("div", { className: styles.grid, role: "grid", "aria-label": `Horario del grupo ${groupCode}`, children: [_jsx("div", { className: styles.corner, role: "columnheader", children: "Franja" }), availabilityDays.map((day) => (_jsx("div", { className: styles.head, role: "columnheader", children: day }, day))), bands.map((band) => {
                            if (band.kind === 'break') {
                                return (_jsx("div", { className: styles.break, role: "row", children: band.label }, band.id));
                            }
                            const index = slots.findIndex((item) => item.id === band.id) + 1;
                            return (_jsxs("div", { className: styles.row, role: "row", children: [_jsxs("div", { className: styles.time, role: "rowheader", children: [_jsx("span", { children: index }), _jsx("span", { children: band.label })] }), availabilityDays.map((day) => (_jsx(Slot, { day: day, bandId: band.id, block: blocks.get(blockKey(day, band.id)), readOnly: readOnly, groupId: groupId, onProgram: onProgram, onEdit: onEdit }, `${day}-${band.id}`)))] }, band.id));
                        })] }) }), pause ? (_jsx(Text, { variant: "caption", children: "Verde sin observaciones \u00B7 Rosa conflicto \u00B7 Amarillo advertencia. El descanso no se programa." })) : null] }));
}
function Slot({ day, bandId, block, readOnly, groupId, onProgram, onEdit }) {
    if (!block) {
        return (_jsx("div", { className: styles.cell, role: "gridcell", children: _jsx("button", { type: "button", className: styles.program, disabled: readOnly, onClick: () => onProgram(day, bandId), children: "Programar" }) }));
    }
    const label = [block.subject, block.teacherName, block.classroom, block.reason]
        .filter(Boolean)
        .join('. ');
    return (_jsx("div", { className: styles.cell, role: "gridcell", children: _jsxs("div", { className: styles.occupied, children: [_jsxs("button", { type: "button", className: `${styles.block} ${styles[block.tone]}`, title: block.reason || undefined, disabled: readOnly || Boolean(block.locked), "aria-label": block.locked ? `Clase bloqueada. ${label}` : `Editar clase. ${label}`, onClick: () => onEdit(block.id), children: [_jsx("span", { className: styles.subject, children: block.subject }), _jsx("span", { children: block.teacherName }), _jsx("span", { children: block.classroom })] }), _jsx(TextLink, { className: styles.history, to: rutaHistorialEntidad('ClaseProgramada', block.id, groupId), children: "Ver historial" })] }) }));
}

import { jsxs as _jsxs, jsx as _jsx } from "react/jsx-runtime";
import { Select } from '../../atoms/Select';
import styles from './Pagination.module.css';
export function Pagination({ page, pageSize, total, onPageChange, onPageSizeChange, }) {
    const start = total === 0 ? 0 : (page - 1) * pageSize + 1;
    const end = Math.min(page * pageSize, total);
    const isFirst = page <= 1;
    const isLast = end >= total;
    return (_jsxs("div", { className: styles.bar, children: [_jsxs("p", { className: styles.summary, children: ["Mostrando ", start, "-", end, " de ", total, " docentes"] }), _jsxs("div", { className: styles.controls, children: [_jsxs("label", { className: styles.pageSize, children: ["Filas por p\u00E1gina:", _jsx(Select, { selectSize: "sm", className: styles.pageSizeSelect, value: String(pageSize), options: [
                                    { value: '6', label: '6' },
                                    { value: '10', label: '10' },
                                    { value: '14', label: '14' },
                                ], onChange: (event) => onPageSizeChange(Number(event.target.value)), "aria-label": "Filas por p\u00E1gina" })] }), isFirst ? null : (_jsx("button", { type: "button", className: styles.pageButton, onClick: () => onPageChange(page - 1), children: "Anterior" })), isLast ? null : (_jsx("button", { type: "button", className: `${styles.pageButton} ${styles.pageButtonNext}`, onClick: () => onPageChange(page + 1), children: "Siguiente" }))] })] }));
}

import { jsx as _jsx } from "react/jsx-runtime";
import { StatCard } from '../../molecules/StatCard';
import styles from './StatsSection.module.css';
export function StatsSection({ items, label = 'Resumen' }) {
    return (_jsx("section", { className: styles.section, "aria-label": label, children: items.map((stat) => (_jsx(StatCard, { value: stat.value, label: stat.label, wash: stat.wash }, stat.id))) }));
}

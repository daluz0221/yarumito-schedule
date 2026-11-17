import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { dashboardAlerts } from '../../../content/dashboard';
import { AlertsCard } from '../../molecules/AlertsCard';
import { ProcessStatusCard } from '../../molecules/ProcessStatusCard';
import styles from './InsightsSection.module.css';
export function InsightsSection() {
    return (_jsxs("section", { className: styles.section, "aria-label": "Estado e informaci\u00F3n", children: [_jsx(ProcessStatusCard, { year: "2026", status: "En construcci\u00F3n" }), _jsx(AlertsCard, { items: dashboardAlerts })] }));
}

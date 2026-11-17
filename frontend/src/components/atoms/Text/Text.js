import { jsx as _jsx } from "react/jsx-runtime";
import styles from './Text.module.css';
const defaultTags = {
    title: 'h1',
    subtitle: 'p',
    display: 'h2',
    pageTitle: 'h1',
    sectionTitle: 'h2',
    body: 'p',
    caption: 'p',
    stat: 'p',
    nav: 'span',
    brand: 'p',
};
export function Text({ as, variant, children, className }) {
    const Component = as ?? defaultTags[variant];
    const classes = [styles.text, styles[variant], className ?? '']
        .filter(Boolean)
        .join(' ');
    return _jsx(Component, { className: classes, children: children });
}

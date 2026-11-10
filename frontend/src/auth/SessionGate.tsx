import styles from './SessionGate.module.css'

export function SessionGate() {
  return (
    <main className={styles.gate} aria-busy="true" aria-live="polite">
      <p className={styles.copy}>Verificando sesión...</p>
    </main>
  )
}

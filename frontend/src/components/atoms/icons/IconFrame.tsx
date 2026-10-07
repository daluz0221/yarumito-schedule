import type { ReactNode } from 'react'

export type IconFrameProps = {
  size?: number
  children: ReactNode
}

export function IconFrame({ size = 20, children }: IconFrameProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      {children}
    </svg>
  )
}

import type { ResumeStatus } from '../api/types'

const STYLES: Record<ResumeStatus, string> = {
  PENDING: 'bg-slate-100 text-slate-600',
  PROCESSED: 'bg-emerald-100 text-emerald-800',
  FAILED: 'bg-rose-100 text-rose-800',
}

export function StatusBadge({ status }: { status: ResumeStatus }) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${STYLES[status]}`}>
      {status}
    </span>
  )
}

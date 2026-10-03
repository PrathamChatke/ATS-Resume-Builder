export function ScoreBadge({ score }: { score: number | null }) {
  if (score === null) {
    return <span className="text-sm text-slate-400">—</span>
  }

  const colorClasses =
    score >= 80
      ? 'bg-emerald-100 text-emerald-800'
      : score >= 50
        ? 'bg-amber-100 text-amber-800'
        : 'bg-rose-100 text-rose-800'

  return (
    <span className={`inline-flex h-8 w-12 items-center justify-center rounded-full text-sm font-semibold ${colorClasses}`}>
      {score}
    </span>
  )
}

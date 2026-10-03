import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api } from '../api/client'
import type { ResumeDetail, ScoreCategory } from '../api/types'
import { ScoreBadge } from '../components/ScoreBadge'
import { StatusBadge } from '../components/StatusBadge'

const POLL_INTERVAL_MS = 3000

const CATEGORY_LABELS: Record<ScoreCategory, string> = {
  KEYWORD_MATCH: 'Keyword match',
  FORMATTING: 'Formatting & length',
  STRUCTURE: 'Section structure',
  IMPACT: 'Impact & quantification',
  CONTACT_INFO: 'Contact info',
}

const PRIORITY_STYLES: Record<string, string> = {
  HIGH: 'border-rose-200 bg-rose-50 text-rose-800',
  MEDIUM: 'border-amber-200 bg-amber-50 text-amber-800',
  LOW: 'border-slate-200 bg-slate-50 text-slate-700',
}

export function ResumeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [resume, setResume] = useState<ResumeDetail | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    let cancelled = false
    let timer: ReturnType<typeof setTimeout>

    const load = () => {
      api
        .get<ResumeDetail>(`/resumes/${id}`)
        .then((data) => {
          if (cancelled) return
          setResume(data)
          if (data.status === 'PENDING') {
            timer = setTimeout(load, POLL_INTERVAL_MS)
          }
        })
        .catch(() => !cancelled && setError('Could not load this resume.'))
    }

    load()
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [id])

  if (error) return <p className="text-sm text-rose-600">{error}</p>
  if (!resume) return <p className="text-sm text-slate-400">Loading…</p>

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="rounded-lg border border-slate-200 bg-white p-6">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-xl font-semibold text-slate-900">{resume.candidateName ?? 'Unnamed candidate'}</h1>
            <p className="text-sm text-slate-500">{resume.candidateEmail}</p>
            <p className="mt-1 text-sm text-slate-400">{resume.originalFilename}</p>
          </div>
          <div className="flex flex-col items-end gap-2">
            <StatusBadge status={resume.status} />
            {resume.overallScore !== null && <ScoreBadge score={resume.overallScore} />}
          </div>
        </div>

        {resume.status === 'PENDING' && (
          <p className="mt-4 text-sm text-slate-500">Scanning in progress — this page updates automatically.</p>
        )}
        {resume.status === 'FAILED' && (
          <p className="mt-4 text-sm text-rose-600">{resume.failureReason ?? 'Processing failed.'}</p>
        )}
      </div>

      {resume.status === 'PROCESSED' && (
        <>
          <div className="rounded-lg border border-slate-200 bg-white p-6">
            <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">Score breakdown</h2>
            <div className="space-y-3">
              {Object.entries(resume.categoryScores).map(([category, score]) => (
                <div key={category}>
                  <div className="mb-1 flex justify-between text-sm">
                    <span className="text-slate-600">{CATEGORY_LABELS[category as ScoreCategory] ?? category}</span>
                    <span className="font-medium text-slate-900">{score}</span>
                  </div>
                  <div className="h-2 rounded-full bg-slate-100">
                    <div
                      className="h-2 rounded-full bg-slate-900"
                      style={{ width: `${score}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <div className="rounded-lg border border-slate-200 bg-white p-6">
              <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Matched keywords</h2>
              <div className="flex flex-wrap gap-2">
                {resume.matchedKeywords.length === 0 && <p className="text-sm text-slate-400">None found.</p>}
                {resume.matchedKeywords.map((keyword) => (
                  <span key={keyword} className="rounded-full bg-emerald-100 px-2.5 py-1 text-xs text-emerald-800">
                    {keyword}
                  </span>
                ))}
              </div>
            </div>
            <div className="rounded-lg border border-slate-200 bg-white p-6">
              <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Missing keywords</h2>
              <div className="flex flex-wrap gap-2">
                {resume.missingKeywords.length === 0 && <p className="text-sm text-slate-400">None — great match.</p>}
                {resume.missingKeywords.map((keyword) => (
                  <span key={keyword} className="rounded-full bg-rose-100 px-2.5 py-1 text-xs text-rose-800">
                    {keyword}
                  </span>
                ))}
              </div>
            </div>
          </div>

          <div className="rounded-lg border border-slate-200 bg-white p-6">
            <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
              Suggestions to improve this resume
            </h2>
            <div className="space-y-3">
              {resume.suggestions.length === 0 && (
                <p className="text-sm text-slate-400">No suggestions were generated for this resume.</p>
              )}
              {resume.suggestions.map((suggestion, index) => (
                <div key={index} className={`rounded-md border p-4 text-sm ${PRIORITY_STYLES[suggestion.priority]}`}>
                  <div className="mb-1 flex items-center justify-between">
                    <span className="font-medium">{suggestion.category}</span>
                    <span className="text-xs uppercase tracking-wide opacity-70">{suggestion.priority}</span>
                  </div>
                  <p>{suggestion.text}</p>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  )
}

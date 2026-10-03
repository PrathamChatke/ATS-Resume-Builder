import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { JobPosting, Page, ResumeStatus, ResumeSummary } from '../api/types'
import { ScoreBadge } from '../components/ScoreBadge'
import { StatusBadge } from '../components/StatusBadge'

const PAGE_SIZE = 20

export function DashboardPage() {
  const [jobPostings, setJobPostings] = useState<JobPosting[]>([])
  const [jobPostingId, setJobPostingId] = useState<string>('')
  const [status, setStatus] = useState<ResumeStatus | ''>('')
  const [minScore, setMinScore] = useState<string>('')
  const [page, setPage] = useState(0)

  const [resumePage, setResumePage] = useState<Page<ResumeSummary> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.get<JobPosting[]>('/job-postings').then(setJobPostings).catch(() => setJobPostings([]))
  }, [])

  useEffect(() => {
    const params = new URLSearchParams()
    if (jobPostingId) params.set('jobPostingId', jobPostingId)
    if (status) params.set('status', status)
    if (minScore) params.set('minScore', minScore)
    params.set('page', String(page))
    params.set('size', String(PAGE_SIZE))

    setLoading(true)
    api
      .get<Page<ResumeSummary>>(`/resumes?${params.toString()}`)
      .then(setResumePage)
      .catch(() => setError('Could not load resumes.'))
      .finally(() => setLoading(false))
  }, [jobPostingId, status, minScore, page])

  const jobTitleById = new Map(jobPostings.map((jp) => [jp.id, jp.title]))

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">Candidates</h1>
        <Link
          to="/upload"
          className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800"
        >
          Upload resume
        </Link>
      </div>

      <div className="mb-4 flex flex-wrap gap-3">
        <select
          value={jobPostingId}
          onChange={(e) => {
            setPage(0)
            setJobPostingId(e.target.value)
          }}
          className="rounded-md border border-slate-300 px-3 py-1.5 text-sm"
        >
          <option value="">All job postings</option>
          {jobPostings.map((jp) => (
            <option key={jp.id} value={jp.id}>
              {jp.title}
            </option>
          ))}
        </select>

        <select
          value={status}
          onChange={(e) => {
            setPage(0)
            setStatus(e.target.value as ResumeStatus | '')
          }}
          className="rounded-md border border-slate-300 px-3 py-1.5 text-sm"
        >
          <option value="">All statuses</option>
          <option value="PENDING">Pending</option>
          <option value="PROCESSED">Processed</option>
          <option value="FAILED">Failed</option>
        </select>

        <input
          type="number"
          min={0}
          max={100}
          placeholder="Min ATS score"
          value={minScore}
          onChange={(e) => {
            setPage(0)
            setMinScore(e.target.value)
          }}
          className="w-36 rounded-md border border-slate-300 px-3 py-1.5 text-sm"
        />
      </div>

      {error && <p className="text-sm text-rose-600">{error}</p>}

      <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs font-medium uppercase tracking-wide text-slate-500">
            <tr>
              <th className="px-4 py-3">Candidate</th>
              <th className="px-4 py-3">File</th>
              <th className="px-4 py-3">Job posting</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">ATS score</th>
              <th className="px-4 py-3">Uploaded</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {!loading && resumePage?.content.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-slate-400">
                  No resumes yet.
                </td>
              </tr>
            )}
            {resumePage?.content.map((resume) => (
              <tr key={resume.id} className="hover:bg-slate-50">
                <td className="px-4 py-3">
                  <Link to={`/resumes/${resume.id}`} className="font-medium text-slate-900 hover:underline">
                    {resume.candidateName ?? 'Unnamed candidate'}
                  </Link>
                  {resume.candidateEmail && <div className="text-xs text-slate-400">{resume.candidateEmail}</div>}
                </td>
                <td className="px-4 py-3 text-slate-600">{resume.originalFilename}</td>
                <td className="px-4 py-3 text-slate-600">
                  {resume.jobPostingId ? (jobTitleById.get(resume.jobPostingId) ?? '—') : '—'}
                </td>
                <td className="px-4 py-3">
                  <StatusBadge status={resume.status} />
                </td>
                <td className="px-4 py-3">
                  <ScoreBadge score={resume.overallScore} />
                </td>
                <td className="px-4 py-3 text-slate-500">{new Date(resume.createdAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {resumePage && resumePage.totalPages > 1 && (
        <div className="mt-4 flex items-center justify-center gap-3 text-sm">
          <button
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
            className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
          >
            Previous
          </button>
          <span className="text-slate-500">
            Page {resumePage.number + 1} of {resumePage.totalPages}
          </span>
          <button
            disabled={page >= resumePage.totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}

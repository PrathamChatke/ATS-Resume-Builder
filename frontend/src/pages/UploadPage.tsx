import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import type { JobPosting, ResumeUploadResult } from '../api/types'

export function UploadPage() {
  const [jobPostings, setJobPostings] = useState<JobPosting[]>([])
  const [jobPostingId, setJobPostingId] = useState<string>('')
  const [candidateName, setCandidateName] = useState('')
  const [file, setFile] = useState<File | null>(null)

  const [showNewJobForm, setShowNewJobForm] = useState(false)
  const [newJobTitle, setNewJobTitle] = useState('')
  const [newJobDescription, setNewJobDescription] = useState('')

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<ResumeUploadResult | null>(null)

  const loadJobPostings = () => api.get<JobPosting[]>('/job-postings').then(setJobPostings).catch(() => {})

  useEffect(() => {
    loadJobPostings()
  }, [])

  const handleCreateJobPosting = async (event: FormEvent) => {
    event.preventDefault()
    const created = await api.post<JobPosting>('/job-postings', { title: newJobTitle, description: newJobDescription })
    setNewJobTitle('')
    setNewJobDescription('')
    setShowNewJobForm(false)
    await loadJobPostings()
    setJobPostingId(String(created.id))
  }

  const handleUpload = async (event: FormEvent) => {
    event.preventDefault()
    if (!file) {
      setError('Please choose a resume file (PDF or DOCX).')
      return
    }
    setError(null)
    setResult(null)
    setSubmitting(true)
    try {
      const form = new FormData()
      form.append('file', file)
      if (jobPostingId) form.append('jobPostingId', jobPostingId)
      if (candidateName) form.append('candidateName', candidateName)

      const response = await api.postForm<ResumeUploadResult>('/resumes', form)
      setResult(response)
      setFile(null)
      setCandidateName('')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Upload failed. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Upload a resume</h1>

      <form onSubmit={handleUpload} className="space-y-5 rounded-lg border border-slate-200 bg-white p-6">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Resume file (PDF or DOCX)</label>
          <input
            type="file"
            accept=".pdf,.docx"
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            className="block w-full text-sm text-slate-600 file:mr-4 file:rounded-md file:border-0 file:bg-slate-900 file:px-4 file:py-2 file:text-sm file:font-medium file:text-white hover:file:bg-slate-800"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Candidate name (optional)</label>
          <input
            value={candidateName}
            onChange={(e) => setCandidateName(e.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">
            Job posting (optional — enables keyword matching against the role)
          </label>
          <select
            value={jobPostingId}
            onChange={(e) => setJobPostingId(e.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
          >
            <option value="">No specific job posting (general ATS score)</option>
            {jobPostings.map((jp) => (
              <option key={jp.id} value={jp.id}>
                {jp.title}
              </option>
            ))}
          </select>
          <button
            type="button"
            onClick={() => setShowNewJobForm((v) => !v)}
            className="mt-2 text-sm text-slate-500 hover:text-slate-800"
          >
            {showNewJobForm ? 'Cancel' : '+ New job posting'}
          </button>
        </div>

        {showNewJobForm && (
          <div className="space-y-3 rounded-md border border-slate-200 bg-slate-50 p-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Title</label>
              <input
                value={newJobTitle}
                onChange={(e) => setNewJobTitle(e.target.value)}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Description</label>
              <textarea
                value={newJobDescription}
                onChange={(e) => setNewJobDescription(e.target.value)}
                rows={5}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              />
            </div>
            <button
              type="button"
              onClick={handleCreateJobPosting}
              disabled={!newJobTitle || !newJobDescription}
              className="rounded-md bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
            >
              Save job posting
            </button>
          </div>
        )}

        {error && <p className="text-sm text-rose-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {submitting ? 'Uploading…' : 'Upload and scan'}
        </button>
      </form>

      {result && (
        <div className="mt-4 rounded-md border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
          Resume uploaded and is being scanned.{' '}
          <Link to={`/resumes/${result.id}`} className="font-medium underline">
            View progress
          </Link>
        </div>
      )}
    </div>
  )
}

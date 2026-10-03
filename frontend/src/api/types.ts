export type Role = 'ADMIN' | 'RECRUITER'

export interface AuthResponse {
  token: string
  fullName: string
  email: string
  role: Role
}

export interface JobPosting {
  id: number
  title: string
  description: string
  createdAt: string
}

export type ResumeStatus = 'PENDING' | 'PROCESSED' | 'FAILED'

export interface ResumeUploadResult {
  id: number
  status: ResumeStatus
}

export interface ResumeSummary {
  id: number
  candidateName: string | null
  candidateEmail: string | null
  originalFilename: string
  status: ResumeStatus
  jobPostingId: number | null
  overallScore: number | null
  createdAt: string
}

export type Priority = 'HIGH' | 'MEDIUM' | 'LOW'

export interface Suggestion {
  category: string
  priority: Priority
  text: string
}

export type ScoreCategory = 'KEYWORD_MATCH' | 'FORMATTING' | 'STRUCTURE' | 'IMPACT' | 'CONTACT_INFO'

export interface ResumeDetail {
  id: number
  candidateName: string | null
  candidateEmail: string | null
  originalFilename: string
  status: ResumeStatus
  failureReason: string | null
  jobPostingId: number | null
  overallScore: number | null
  categoryScores: Partial<Record<ScoreCategory, number>>
  matchedKeywords: string[]
  missingKeywords: string[]
  suggestions: Suggestion[]
  createdAt: string
  processedAt: string | null
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE job_postings (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    created_by  BIGINT       NOT NULL REFERENCES users (id),
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE resumes (
    id                 BIGSERIAL PRIMARY KEY,
    candidate_name     VARCHAR(255),
    candidate_email    VARCHAR(255),
    original_filename  VARCHAR(500) NOT NULL,
    storage_path       VARCHAR(1000) NOT NULL,
    content_type       VARCHAR(100) NOT NULL,
    job_posting_id     BIGINT REFERENCES job_postings (id),
    uploaded_by        BIGINT NOT NULL REFERENCES users (id),
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failure_reason     TEXT,
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    processed_at       TIMESTAMP
);

CREATE INDEX idx_resumes_job_posting_id ON resumes (job_posting_id);
CREATE INDEX idx_resumes_status ON resumes (status);
CREATE INDEX idx_resumes_uploaded_by ON resumes (uploaded_by);

CREATE TABLE resume_scores (
    id               BIGSERIAL PRIMARY KEY,
    resume_id        BIGINT NOT NULL UNIQUE REFERENCES resumes (id) ON DELETE CASCADE,
    overall_score    SMALLINT NOT NULL,
    category_scores  JSONB NOT NULL,
    matched_keywords JSONB NOT NULL,
    missing_keywords JSONB NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_resume_scores_overall_score ON resume_scores (overall_score);

CREATE TABLE resume_suggestions (
    id              BIGSERIAL PRIMARY KEY,
    resume_id       BIGINT NOT NULL REFERENCES resumes (id) ON DELETE CASCADE,
    category        VARCHAR(50) NOT NULL,
    priority        VARCHAR(10) NOT NULL,
    suggestion_text TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_resume_suggestions_resume_id ON resume_suggestions (resume_id);

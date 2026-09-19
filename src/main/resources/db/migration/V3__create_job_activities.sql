CREATE TABLE job_activities (
    id            UUID PRIMARY KEY,
    user_id       UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    activity_date TIMESTAMPTZ  NOT NULL,
    search_type   VARCHAR(255) NOT NULL,
    job_type      VARCHAR(255) NOT NULL,
    company_name  VARCHAR(255) NOT NULL,
    status        VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_job_activities_user_id ON job_activities (user_id);
CREATE INDEX ix_job_activities_activity_date ON job_activities (activity_date);

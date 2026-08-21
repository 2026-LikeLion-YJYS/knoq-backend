-- Adds needs_analysis.user_edited, used by NeedsAnalysisService to skip re-aggregating
-- productCategory/preferredColor/preferredMaterial/preferredSize on POST re-analysis
-- once the customer has edited them via PUT.
-- Required on any DB running with hibernate.ddl-auto=validate (prod) — that mode never
-- adds missing columns on its own. Safe to re-run.

ALTER TABLE needs_analysis
    ADD COLUMN IF NOT EXISTS user_edited TINYINT(1) NOT NULL DEFAULT 0;

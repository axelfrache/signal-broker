CREATE TABLE IF NOT EXISTS labeled_tickets (
    "ticketId" VARCHAR(255) PRIMARY KEY,
    "schemaVersion" BIGINT NOT NULL,
    "subject" TEXT,
    "contact" TEXT,
    "confidence" DOUBLE PRECISION NOT NULL,
    "labeledAt" DOUBLE PRECISION,
    "ticketType" VARCHAR(255),
    "receivedAt" DOUBLE PRECISION,
    "body" TEXT,
    "category" VARCHAR(255),
    "priority" VARCHAR(255),
    "commonId" BIGINT
);

ALTER TABLE labeled_tickets
    ADD COLUMN IF NOT EXISTS "commonId" BIGINT;

CREATE TABLE IF NOT EXISTS ticket_comments (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    author_name TEXT NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    schema_version INT NOT NULL
);

-- Phase 10 (chatbot-service): NO-OP.
--
-- This migration originally truncated product_embeddings and rebuilt the
-- embedding column as vector(1536). That is now redundant: V13 already creates
-- the table at vector(1536) for OpenAI text-embedding-3-small and V14 is a
-- no-op, so a fresh install is 1536-dim without this step. Kept as a no-op
-- (not deleted) so Flyway history stays linear — and, critically, so it never
-- truncates an already-populated product_embeddings table on an existing DB.
select 1;

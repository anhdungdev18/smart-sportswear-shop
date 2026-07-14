-- Phase 10 (chatbot-service): NO-OP.
--
-- This migration originally switched product_embeddings to BAAI/bge-m3 (1024-dim).
-- The project reverted to OpenAI text-embedding-3-small (1536-dim), so the table
-- created by V13 (vector(1536)) is kept as-is and this migration does nothing.
--
-- Kept as a no-op (instead of deleted) so Flyway checksum history stays linear.
-- Embeddings are populated by: chatbot-service/scripts/generate_embeddings.py
select 1;

-- Phase 10 (chatbot-service): vector search support
--
-- Prerequisites (run ONCE in Supabase SQL Editor as superuser BEFORE this migration):
--   CREATE EXTENSION IF NOT EXISTS vector;
--
-- This migration creates the product_embeddings table used by chatbot-service
-- for pgvector cosine similarity search (text-embedding-3-small, 1536 dims).
-- Populated by: chatbot-service/scripts/generate_embeddings.py

create table if not exists product_embeddings (
    product_id  uuid        not null references products (id) on delete cascade,
    embedding   vector(1536) not null,
    document_text text,
    updated_at  timestamptz  not null default now(),
    primary key (product_id)
);

-- IVFFlat approximate-nearest-neighbor index (cosine distance)
-- lists=20 is appropriate for catalogs up to ~2000 products;
-- rebuild with lists=100 if product count grows beyond ~10 000.
create index if not exists idx_product_embeddings_ivfflat
    on product_embeddings
    using ivfflat (embedding vector_cosine_ops)
    with (lists = 20);

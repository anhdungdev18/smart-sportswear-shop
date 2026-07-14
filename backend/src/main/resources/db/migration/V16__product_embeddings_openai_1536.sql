-- Finalize the product vector index on OpenAI text-embedding-3-small.
-- Existing vectors from any previous 1024-dimension experiment are incompatible
-- and must be regenerated with chatbot-service/scripts/generate_embeddings.py.

drop index if exists idx_product_embeddings_ivfflat;
truncate table product_embeddings;

alter table product_embeddings drop column if exists embedding;
alter table product_embeddings add column embedding vector(1536) not null;

create index idx_product_embeddings_ivfflat
    on product_embeddings
    using ivfflat (embedding vector_cosine_ops)
    with (lists = 20);

-- Phase 10 (chatbot-service): persistent conversation history
--
-- chat_messages stores every turn (user + assistant) for all chat sessions.
-- Used by chatbot-service to:
--   1. Restore chat_history on cold session start (after server restart / TTL expiry)
--   2. Analytics / quality review of LLM replies
--
-- The in-memory / Redis session store is the hot cache.
-- This table is the durable backing store.

create table if not exists chat_messages (
    id         bigserial       primary key,
    session_id text            not null,
    user_id    text,                                    -- null for guest sessions
    role       text            not null,                -- 'user' | 'assistant'
    content    text            not null,
    intent     text,                                    -- classified intent for this turn (assistant rows only)
    created_at timestamptz     not null default now()
);

-- Efficient lookup by session for history reload
create index if not exists idx_chat_messages_session
    on chat_messages (session_id, created_at desc);

-- Optional: lookup by user across sessions
create index if not exists idx_chat_messages_user
    on chat_messages (user_id, created_at desc)
    where user_id is not null;

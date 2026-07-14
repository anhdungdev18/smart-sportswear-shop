CREATE TABLE recommendation_logs (
    id UUID PRIMARY KEY,

    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(255),

    source_type VARCHAR(30) NOT NULL,
    recommendation_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(30) NOT NULL,

    source_product_id UUID REFERENCES products(id) ON DELETE SET NULL,
    source_product_ids JSONB,
    cart_id UUID REFERENCES carts(id) ON DELETE SET NULL,

    recommended_product_id UUID NOT NULL REFERENCES products(id),

    position_index INTEGER,
    algorithm VARCHAR(30) NOT NULL,

    support DOUBLE PRECISION,
    confidence DOUBLE PRECISION,
    lift DOUBLE PRECISION,
    pair_count BIGINT,

    reason VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_recommendation_logs_owner
        CHECK (user_id IS NOT NULL OR session_id IS NOT NULL),

    CONSTRAINT chk_recommendation_logs_source_type
        CHECK (source_type IN ('PRODUCT_DETAIL', 'CART')),

    CONSTRAINT chk_recommendation_logs_recommendation_type
        CHECK (recommendation_type IN ('FREQUENTLY_BOUGHT_TOGETHER', 'CART_RECOMMENDATION')),

    CONSTRAINT chk_recommendation_logs_event_type
        CHECK (event_type IN ('IMPRESSION', 'CLICK', 'ADD_TO_CART')),

    CONSTRAINT chk_recommendation_logs_algorithm
        CHECK (algorithm IN ('ASSOCIATION_RULE', 'FALLBACK')),

    CONSTRAINT chk_recommendation_logs_position
        CHECK (position_index IS NULL OR position_index >= 1)
);

CREATE INDEX idx_recommendation_logs_created_at
ON recommendation_logs (created_at DESC);

CREATE INDEX idx_recommendation_logs_event_created
ON recommendation_logs (event_type, created_at DESC);

CREATE INDEX idx_recommendation_logs_recommended_product
ON recommendation_logs (recommended_product_id, event_type, created_at DESC);

CREATE INDEX idx_recommendation_logs_user_created
ON recommendation_logs (user_id, created_at DESC)
WHERE user_id IS NOT NULL;

CREATE INDEX idx_recommendation_logs_session_created
ON recommendation_logs (session_id, created_at DESC)
WHERE session_id IS NOT NULL;
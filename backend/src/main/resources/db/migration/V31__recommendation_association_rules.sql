CREATE TABLE association_rules (
    id UUID PRIMARY KEY,

    antecedent_product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    consequent_product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,

    support DOUBLE PRECISION NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    lift DOUBLE PRECISION NOT NULL,

    antecedent_count BIGINT NOT NULL,
    consequent_count BIGINT NOT NULL,
    pair_count BIGINT NOT NULL,
    total_transactions BIGINT NOT NULL,

    model_version VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_association_rules_not_self
        CHECK (antecedent_product_id <> consequent_product_id),

    CONSTRAINT chk_association_rules_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED')),

    CONSTRAINT uq_association_rules_version_pair
        UNIQUE (antecedent_product_id, consequent_product_id, model_version)
);

CREATE INDEX idx_association_rules_antecedent_score
ON association_rules (
    antecedent_product_id,
    confidence DESC,
    lift DESC,
    support DESC
);

CREATE INDEX idx_association_rules_consequent
ON association_rules (consequent_product_id);

CREATE INDEX idx_association_rules_status_version
ON association_rules (status, model_version);


CREATE TABLE association_rule_rebuild_logs (
    id UUID PRIMARY KEY,
    model_version VARCHAR(50) NOT NULL,

    status VARCHAR(30) NOT NULL,
    total_transactions BIGINT NOT NULL DEFAULT 0,
    total_rules BIGINT NOT NULL DEFAULT 0,

    min_support DOUBLE PRECISION NOT NULL,
    min_confidence DOUBLE PRECISION NOT NULL,
    min_lift DOUBLE PRECISION NOT NULL,

    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    error_message TEXT,

    CONSTRAINT chk_association_rule_rebuild_status
        CHECK (status IN ('RUNNING', 'SUCCESS', 'FAILED'))
);
-- VNPay transaction identifiers and provider-generated reconciliation IDs can
-- exceed the original 30-character assumption. Widening is backward compatible.
alter table payments
    alter column gateway_transaction_no type varchar(100);

alter table refunds
    alter column gateway_transaction_no type varchar(100);

-- Collections already have an optional brand_id and the API contract supports
-- brand-scoped collections. Keep the database constraint aligned with the enum.
alter table collections drop constraint if exists chk_collections_type;
alter table collections add constraint chk_collections_type check (
    collection_type in (
        'SEASONAL', 'SPORT', 'CAMPAIGN', 'CAPSULE', 'NEW_ARRIVAL', 'BRAND'
    )
);

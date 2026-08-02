-- Allow repeatable builds of the same provider/model and future models with
-- dimensions different from the initial Voyage 1024 configuration.
alter table visual_search.model_versions
    drop constraint if exists uq_visual_model_provider_model;

alter table visual_search.model_versions
    add column revision integer not null default 1,
    add constraint chk_visual_model_revision check (revision > 0),
    add constraint uq_visual_model_revision unique (provider, model, revision);

alter table visual_search.image_embeddings
    alter column embedding type vector using embedding::vector;

alter table visual_search.image_embeddings
    add column failure_code varchar(100);

alter table visual_search.indexing_job_items
    add column source_provider varchar(30),
    add column failure_code varchar(100);

update visual_search.image_embeddings
set failure_code = case
    when last_error like 'Voyage temporarily unavailable%' then 'RetryableEventError'
    when last_error is not null then 'PermanentEventError'
    else null end
where failure_code is null;

update visual_search.indexing_job_items ji
set source_provider = case
    when pi.image_url like 'https://res.cloudinary.com/%' then 'cloudinary'
    when pi.image_url like 'https://cdn.shopify.com/%' then 'shopify'
    when pi.image_url like 'https://%' then 'unsupported_host'
    else 'relative_or_non_https'
end
from product_images pi
where pi.id = ji.image_id and ji.source_provider is null;

with counts as (
    select m.id,
           count(pi.id)::int as target_count,
           count(e.image_id) filter (where e.status = 'READY')::int as ready_count,
           count(e.image_id) filter (where e.status = 'FAILED')::int as failed_count
    from visual_search.model_versions m
    left join products p on p.status = 'ACTIVE'
    left join product_images pi on pi.product_id = p.id
    left join visual_search.image_embeddings e
      on e.image_id = pi.id and e.model_version_id = m.id
    group by m.id
)
update visual_search.model_versions m
set target_image_count = counts.target_count,
    ready_image_count = counts.ready_count,
    failed_image_count = counts.failed_count,
    updated_at = now()
from counts where counts.id = m.id;

-- Optional brand tag on collections. A collection may still span products from
-- multiple brands; this field is only for grouping and storefront filtering.
alter table collections add column brand_id uuid;

alter table collections
    add constraint fk_collections_brand
    foreign key (brand_id) references brands (id) on delete set null;

create index idx_collections_brand_id on collections (brand_id);

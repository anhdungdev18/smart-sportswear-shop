-- Optional brand tag on collections. A collection may still span products from
-- multiple brands; this field is only for grouping and storefront filtering.
alter table collections add column if not exists brand_id uuid;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_collections_brand'
          and conrelid = 'collections'::regclass
    ) then
        alter table collections
            add constraint fk_collections_brand
            foreign key (brand_id) references brands (id) on delete set null;
    end if;
end
$$;

create index if not exists idx_collections_brand_id on collections (brand_id);

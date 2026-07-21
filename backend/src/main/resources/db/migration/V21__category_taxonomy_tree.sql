alter table categories
    add column if not exists parent_id uuid references categories (id) on delete restrict;

alter table categories
    add column if not exists node_type varchar(20) not null default 'LEAF';

alter table categories
    add column if not exists sort_order integer not null default 0;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'chk_categories_node_type'
    ) then
        alter table categories
            add constraint chk_categories_node_type
                check (node_type in ('GROUP', 'LEAF'));
    end if;
end $$;

create index if not exists idx_categories_parent_id on categories (parent_id);
create index if not exists idx_categories_status_parent_sort
    on categories (status, parent_id, sort_order, name);

insert into categories (
    id, name, slug, description, parent_id, status, node_type,
    sort_order, created_at, updated_at
)
values
    ('11111111-1111-1111-1111-111111111111', 'Áo', 'ao',
     'Nhóm danh mục cho các sản phẩm phần thân trên.', null, 'ACTIVE', 'GROUP', 10, now(), now()),
    ('22222222-2222-2222-2222-222222222222', 'Quần', 'quan',
     'Nhóm danh mục cho các sản phẩm phần thân dưới.', null, 'ACTIVE', 'GROUP', 20, now(), now()),
    ('33333333-3333-3333-3333-333333333333', 'Giày', 'giay',
     'Nhóm danh mục cho giày thể thao theo bộ môn.', null, 'ACTIVE', 'GROUP', 30, now(), now()),
    ('44444444-4444-4444-4444-444444444444', 'Phụ kiện', 'phu-kien',
     'Nhóm danh mục cho phụ kiện và đồ hỗ trợ.', null, 'ACTIVE', 'GROUP', 40, now(), now())
on conflict (slug) do nothing;

-- Existing databases can already contain these slugs with different UUIDs.
-- Resolve every relationship by slug instead of assuming the seed UUID.
update categories
set parent_id = null,
    node_type = 'GROUP',
    sort_order = case slug
        when 'ao' then 10
        when 'quan' then 20
        when 'giay' then 30
        when 'phu-kien' then 40
        else sort_order
    end
where slug in ('ao', 'quan', 'giay', 'phu-kien');

update categories
set parent_id = (select id from categories where slug = 'ao'),
    node_type = 'LEAF'
where slug in ('ao-chay-bo', 'ao-da-bong', 'ao-bong-ro', 'ao-cau-long-tennis')
  and parent_id is null;

update categories
set parent_id = (select id from categories where slug = 'quan'),
    node_type = 'LEAF'
where slug in ('quan-chay-bo', 'quan-da-bong')
  and parent_id is null;

update categories
set parent_id = (select id from categories where slug = 'giay'),
    node_type = 'LEAF'
where slug in (
    'giay-chay-bo', 'giay-da-bong-fg', 'giay-da-bong-tf',
    'giay-futsal', 'giay-bong-ro', 'giay-cau-long'
)
  and parent_id is null;

update categories
set parent_id = (select id from categories where slug = 'phu-kien'),
    node_type = 'LEAF'
where slug in ('phu-kien-da-bong', 'gang-tay-thu-mon', 'bong-the-thao')
  and parent_id is null;

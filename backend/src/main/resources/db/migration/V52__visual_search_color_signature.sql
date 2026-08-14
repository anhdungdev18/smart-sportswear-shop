alter table visual_search.image_embeddings
    add column if not exists color_signature real[];

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'chk_visual_color_signature_length') then
        alter table visual_search.image_embeddings
            add constraint chk_visual_color_signature_length
            check (color_signature is null or cardinality(color_signature) = 14);
    end if;
end $$;

-- Optional brand tag on collections: lets admin group/filter collections by brand
-- without constraining which products can be assigned (a collection may still span
-- multiple brands). ON DELETE SET NULL so removing a brand just clears the tag.
ALTER TABLE collections ADD COLUMN brand_id UUID;

ALTER TABLE collections
    ADD CONSTRAINT fk_collections_brand
    FOREIGN KEY (brand_id) REFERENCES brands (id) ON DELETE SET NULL;

CREATE INDEX idx_collections_brand_id ON collections (brand_id);

-- Normalize imported catalog records whose names already state a gender.
-- Remaining legacy NULL values represent gender-neutral products.
UPDATE products
SET gender = 'MEN'
WHERE lower(name) LIKE 'men''s %'
   OR lower(name) LIKE 'mens %';

UPDATE products
SET gender = 'WOMEN'
WHERE lower(name) LIKE 'women''s %'
   OR lower(name) LIKE 'womens %'
   OR lower(name) LIKE 'wmns %';

UPDATE products
SET gender = 'UNISEX'
WHERE gender IS NULL;

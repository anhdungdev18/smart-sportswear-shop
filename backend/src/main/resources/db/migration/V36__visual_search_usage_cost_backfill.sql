-- Older visual-search usage rows recorded pixels but left estimated cost at 0.
-- Backfill with the MVP reference price ($0.0006 per megapixel); runtime writes
-- use IMAGE_COST_PER_MEGAPIXEL_USD so future pricing changes remain configurable.
update visual_search.usage_events
set estimated_cost_usd = round((image_pixels::numeric / 1000000) * 0.0006, 8)
where estimated_cost_usd = 0 and image_pixels > 0;

-- Shipping/Shipment phase.
--
-- V1/V2/V3/V4/V5 are left untouched. The only genuine schema gap this phase
-- needs to close is here: NotificationType gains a new ORDER_SHIPPING value
-- (fired when a shipment's own status transitions to SHIPPING - see
-- ShipmentService), and V5's chk_notifications_type check constraint would
-- reject that value being inserted. Same pattern V4 already established for
-- "extend a constraint without touching the migration that created it":
-- drop and recreate the constraint, add nothing else.
--
-- shipping_methods and shipments themselves (also from V3) already have every
-- column this phase needs - carrierName is served by the existing "provider"
-- column, and estimatedDeliveryDate is computed on the fly from shipped_at/
-- shipping_methods.estimated_days_min/max rather than persisted, so neither
-- table needs a column added. No migration touches them.
alter table notifications drop constraint chk_notifications_type;
alter table notifications add constraint chk_notifications_type check (
    type in ('ORDER_CREATED', 'ORDER_CANCELLED', 'ORDER_DELIVERED', 'ORDER_SHIPPING', 'PASSWORD_RESET')
);

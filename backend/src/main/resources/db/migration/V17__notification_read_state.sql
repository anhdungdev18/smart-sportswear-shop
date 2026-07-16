-- Notification Operations: in-app read state
--
-- Adds read_at so the storefront in-app inbox can show an unread count and let
-- a user mark items read. Nullable: null = unread, a timestamp = when it was read.
--
-- Orthogonal to `status` (PENDING/SENT/FAILED): status is about the EMAIL send
-- outcome, read_at is about the recipient viewing the notification in-app.
-- They are separate concerns and never derived from each other.

alter table notifications add column if not exists read_at timestamptz;

-- Partial index for the hot path: unread-count and unread-list per user.
create index if not exists idx_notifications_user_unread
    on notifications (user_id, created_at desc)
    where read_at is null;

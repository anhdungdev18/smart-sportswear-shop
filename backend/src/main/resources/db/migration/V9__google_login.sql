-- Google / social login phase.
--
-- V1 created password_hash as NOT NULL and had no login_provider concept.
-- Two additive changes only:
--
-- 1. login_provider: tracks how each user account was created (LOCAL =
--    email+password, GOOGLE = Google OAuth). Defaulting to LOCAL means every
--    existing user row already correctly describes itself with no data
--    migration needed.
--
-- 2. password_hash nullable: Google-authenticated users have no password.
--    The NOT NULL constraint was safe before this phase because every code
--    path that creates a user always set a BCrypt hash - now GoogleAuthService
--    creates users without one. Removing NOT NULL is the right constraint
--    change; AuthService.login and CustomUserDetails.getPassword() are both
--    updated to guard the null case explicitly so the change is safe.
alter table users add column login_provider varchar(20) not null default 'LOCAL';

alter table users add constraint chk_users_login_provider check (
    login_provider in ('LOCAL', 'GOOGLE')
);

alter table users alter column password_hash drop not null;

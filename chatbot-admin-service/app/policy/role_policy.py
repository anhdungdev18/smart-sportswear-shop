from app.auth.actor_context import ActorContext


READ_ONLY_ROLES = {"ADMIN"}


def assert_can_use_admin_copilot(actor: ActorContext) -> None:
    if actor.role.upper() not in READ_ONLY_ROLES:
        raise PermissionError("Role is not allowed to use Admin Copilot tools")

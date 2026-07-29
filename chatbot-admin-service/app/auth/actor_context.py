from dataclasses import dataclass


@dataclass(frozen=True)
class ActorContext:
    actor_id: str
    role: str
    subject: str

    @property
    def is_admin(self) -> bool:
        return self.role.upper() == "ADMIN"

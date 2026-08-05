from datetime import datetime
from typing import Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict


class CatalogEvent(BaseModel):
    model_config = ConfigDict(extra="forbid")

    eventId: UUID
    eventType: Literal[
        "PRODUCT_ACTIVATED",
        "PRODUCT_DEACTIVATED",
        "PRODUCT_REINDEX_REQUESTED",
    ]
    eventVersion: Literal[1]
    productId: UUID
    occurredAt: datetime
    traceId: str

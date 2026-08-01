from app.providers.voyage import VoyageProviderError
from app.services.errors import ImageDownloadError, ImagePipelineError


class RetryableEventError(RuntimeError):
    """A transient failure which should be routed through a TTL retry queue."""


class PermanentEventError(RuntimeError):
    """An invalid/non-recoverable event which should be routed to the DLQ."""


def classify_processing_error(error: Exception) -> Exception:
    if isinstance(error, (RetryableEventError, PermanentEventError)):
        return error
    if isinstance(error, (ImageDownloadError, VoyageProviderError)):
        return RetryableEventError(str(error))
    if isinstance(error, (ImagePipelineError, ValueError)):
        return PermanentEventError(str(error))
    return RetryableEventError(type(error).__name__)

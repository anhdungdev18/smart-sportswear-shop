"""Visual product search service runtime bootstrap."""

import asyncio
import sys


# psycopg async connections require a selector loop on Windows. Configure it
# before FastAPI, the worker or test clients create their event loop.
if sys.platform == "win32":
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

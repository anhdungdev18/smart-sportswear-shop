from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_KNOWLEDGE_DIR = Path(__file__).parent.parent.parent / "knowledge"

# ── Vietnamese stop words to exclude from keyword scoring ───────────────────
_STOP_WORDS = {
    "có", "không", "và", "của", "là", "tôi", "shop", "cửa", "hàng",
    "thế", "nào", "làm", "sao", "được", "thì", "bao", "lâu", "cho",
    "như", "với", "về", "hay", "hoặc", "mà", "đã", "sẽ", "đang",
    "một", "các", "để", "từ", "theo", "khi", "nếu", "vào", "ra",
    "ở", "tại", "trên", "dưới", "trong", "ngoài", "cần", "muốn",
    "xin", "hỏi", "bạn", "mình", "này", "đó", "ấy",
}


@dataclass
class KnowledgeChunk:
    source: str     # filename e.g. "return-policy.md"
    section: str    # ## heading
    content: str    # body text of that section


# ── Markdown parser ──────────────────────────────────────────────────────────

def _parse_md_file(path: Path) -> list[KnowledgeChunk]:
    """Split a markdown file into chunks at each ## heading."""
    text = path.read_text(encoding="utf-8")
    source = path.name

    # Skip lines starting with # (document title) or > (blockquotes / notes)
    chunks: list[KnowledgeChunk] = []
    current_section = ""
    current_lines: list[str] = []

    for line in text.splitlines():
        if line.startswith("## "):
            if current_section and current_lines:
                chunks.append(KnowledgeChunk(
                    source=source,
                    section=current_section,
                    content="\n".join(current_lines).strip(),
                ))
            current_section = line.lstrip("# ").strip()
            current_lines = []
        elif line.startswith("# ") or line.startswith("> "):
            continue
        else:
            current_lines.append(line)

    if current_section and current_lines:
        chunks.append(KnowledgeChunk(
            source=source,
            section=current_section,
            content="\n".join(current_lines).strip(),
        ))

    return chunks


# ── In-memory corpus (loaded once) ──────────────────────────────────────────

_corpus: list[KnowledgeChunk] = []
_loaded = False


def _ensure_loaded() -> None:
    global _corpus, _loaded
    if _loaded:
        return
    if not _KNOWLEDGE_DIR.exists():
        logger.warning(f"knowledge_repository | dir not found: {_KNOWLEDGE_DIR}")
        _loaded = True
        return

    for md_file in sorted(_KNOWLEDGE_DIR.glob("*.md")):
        chunks = _parse_md_file(md_file)
        _corpus.extend(chunks)
        logger.info(f"knowledge_repository | loaded {md_file.name} → {len(chunks)} chunks")

    logger.info(f"knowledge_repository | total_chunks={len(_corpus)}")
    _loaded = True


# ── Scoring ──────────────────────────────────────────────────────────────────

def _tokenize(text: str) -> list[str]:
    """Lowercase, split by whitespace/punctuation, remove stop words and short tokens."""
    tokens = re.split(r"[\s,?!.;:()\[\]/|]+", text.lower())
    return [t for t in tokens if len(t) >= 2 and t not in _STOP_WORDS]


def _score_chunk(chunk: KnowledgeChunk, query_tokens: list[str]) -> float:
    if not query_tokens:
        return 0.0
    title_lower = chunk.section.lower()
    content_lower = chunk.content.lower()
    score = 0.0
    for token in query_tokens:
        if token in title_lower:
            score += 3.0   # section title match weighted highest
        if token in content_lower:
            score += 1.0
    # Normalize by query length to avoid rewarding long queries unfairly
    return score / len(query_tokens)


# ── Public API ───────────────────────────────────────────────────────────────

def search(query: str, limit: int = 3) -> list[KnowledgeChunk]:
    """
    Keyword search over in-memory knowledge corpus.
    Returns top-scoring chunks, ordered by score descending.
    """
    _ensure_loaded()
    if not _corpus:
        return []

    tokens = _tokenize(query)
    logger.debug(f"knowledge_repository | tokens={tokens}")

    scored = [(chunk, _score_chunk(chunk, tokens)) for chunk in _corpus]
    scored.sort(key=lambda x: x[1], reverse=True)

    # Only return chunks with a non-zero score
    results = [chunk for chunk, score in scored if score > 0][:limit]
    logger.info(f"knowledge_repository | query={query!r} hits={len(results)}/{len(scored)}")
    return results


def chunk_count() -> int:
    _ensure_loaded()
    return len(_corpus)

"""Deterministic Vietnamese-to-catalog terms for product semantic retrieval."""
from __future__ import annotations

from app.retrieval.product.parser.query_parser import normalize_text

_RULES: tuple[tuple[tuple[str, ...], str], ...] = (
    (("buoi toi", "de nhin", "phan quang"), "reflect"),
    (("san khach",), "away jersey"),
    (("nhat ban", "da mem"), "Mizuno Morelia Japan MIJ"),
    (("chay dua", "ngay thi dau"), "race"),
    (("ba lo", "marathon"), "vest singlet"),
    (("dai tay", "troi lanh"), "L/S long sleeve"),
    (("kiem soat bong", "tien ve"), "Adidas Predator"),
    (("co dien", "bieu tuong"), "icon jersey"),
    (("hai lop",), "twin layer"),
    (("khong day",), "laceless"),
    (("toc do", "tien dao", "but toc"), "Mercurial Vapor Superfly"),
)


def expand(query: str) -> str:
    normalized = normalize_text(query)
    additions = [
        terms
        for triggers, terms in _RULES
        if any(trigger in normalized for trigger in triggers)
    ]
    return " ".join([query, *additions]).strip()

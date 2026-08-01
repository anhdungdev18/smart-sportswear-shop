from dataclasses import dataclass
import math
from typing import Iterable


REQUIRED_STRATA = frozenset({"tops", "bottoms", "shoes"})


@dataclass(frozen=True, slots=True)
class BenchmarkObservation:
    expected_product_ids: frozenset[str]
    expected_stratum: str
    returned_product_ids: tuple[str, ...]
    returned_stratum: str | None
    latency_ms: float


@dataclass(frozen=True, slots=True)
class BenchmarkMetrics:
    sample_count: int
    strata: tuple[str, ...]
    recall_at_1: float
    recall_at_5: float
    category_accuracy: float
    latency_p95_ms: float
    passed: bool
    failures: tuple[str, ...]
    not_applicable_strata: tuple[str, ...] = ("accessories",)


def percentile_nearest_rank(values: Iterable[float], percentile: float) -> float:
    ordered = sorted(values)
    if not ordered:
        return 0.0
    rank = max(1, math.ceil(percentile * len(ordered)))
    return float(ordered[rank - 1])


def evaluate_benchmark(observations: list[BenchmarkObservation]) -> BenchmarkMetrics:
    sample_count = len(observations)
    strata = tuple(sorted({item.expected_stratum for item in observations}))
    recall_at_1 = sum(
        bool(item.returned_product_ids[:1] and item.expected_product_ids.intersection(item.returned_product_ids[:1]))
        for item in observations
    ) / sample_count if sample_count else 0.0
    recall_at_5 = sum(
        bool(item.expected_product_ids.intersection(item.returned_product_ids[:5])) for item in observations
    ) / sample_count if sample_count else 0.0
    category_accuracy = sum(
        item.returned_stratum == item.expected_stratum for item in observations
    ) / sample_count if sample_count else 0.0
    p95 = percentile_nearest_rank((item.latency_ms for item in observations), 0.95)

    failures: list[str] = []
    if sample_count < 100:
        failures.append("sample_count must be at least 100")
    missing_strata = REQUIRED_STRATA.difference(strata)
    if missing_strata:
        failures.append(f"missing required strata: {', '.join(sorted(missing_strata))}")
    if recall_at_5 < 0.80:
        failures.append("recall@5 is below 80%")
    if category_accuracy < 0.90:
        failures.append("category accuracy is below 90%")
    if p95 > 3000:
        failures.append("p95 latency exceeds 3000 ms")

    return BenchmarkMetrics(
        sample_count=sample_count,
        strata=strata,
        recall_at_1=round(recall_at_1, 4),
        recall_at_5=round(recall_at_5, 4),
        category_accuracy=round(category_accuracy, 4),
        latency_p95_ms=round(p95, 2),
        passed=not failures,
        failures=tuple(failures),
    )

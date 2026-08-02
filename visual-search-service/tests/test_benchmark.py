from app.services.benchmark import BenchmarkObservation, evaluate_benchmark, percentile_nearest_rank


def observation(index: int, *, correct: bool = True, latency: float = 100) -> BenchmarkObservation:
    expected = f"product-{index}"
    returned = (expected, "other") if correct else ("other", expected)
    stratum = ("tops", "bottoms", "shoes")[index % 3]
    return BenchmarkObservation(frozenset({expected}), stratum, returned, stratum if correct else "other", latency)


def test_nearest_rank_percentile() -> None:
    assert percentile_nearest_rank(range(1, 101), 0.95) == 95


def test_acceptance_metrics_pass_at_thresholds() -> None:
    observations = [observation(index, correct=index < 90, latency=3000 if index == 94 else 100) for index in range(100)]
    metrics = evaluate_benchmark(observations)
    assert metrics.recall_at_5 == 1.0
    assert metrics.category_accuracy == 0.9
    assert metrics.latency_p95_ms == 100
    assert metrics.passed is True


def test_acceptance_rejects_small_unrepresentative_or_slow_dataset() -> None:
    metrics = evaluate_benchmark([BenchmarkObservation(frozenset({"p"}), "tops", (), None, 3001)])
    assert metrics.passed is False
    assert any("sample_count" in failure for failure in metrics.failures)
    assert any("missing required strata" in failure for failure in metrics.failures)
    assert any("recall@5" in failure for failure in metrics.failures)
    assert any("category accuracy" in failure for failure in metrics.failures)
    assert any("p95 latency" in failure for failure in metrics.failures)

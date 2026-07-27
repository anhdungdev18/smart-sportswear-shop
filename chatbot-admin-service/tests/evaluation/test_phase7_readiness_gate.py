import asyncio

from app.evaluations.phase7_readiness import evaluate_phase7_readiness


def test_phase7_readiness_gate_passes():
    report = asyncio.run(evaluate_phase7_readiness())

    assert report["status"] == "PASS"
    assert report["metrics"]["toolSelectionAccuracy"] >= 0.90
    assert report["metrics"]["groundedNumericAccuracy"] >= 0.95
    assert report["metrics"]["readOnlyTaskSuccess"] >= 0.85
    assert report["metrics"]["roleBypassBlocked"] is True
    assert report["metrics"]["infiniteLoopDetected"] is False
    assert report["metrics"]["maxToolCallsObserved"] <= 1

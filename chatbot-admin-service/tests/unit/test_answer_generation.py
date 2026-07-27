from app.graph.nodes.generate_answer import generate_grounded_answer


def test_answer_reports_inventory_risk_split_from_tool_result():
    reply, warnings, numbers = generate_grounded_answer(
        "INVENTORY_RISK",
        "get_inventory_risks",
        [{"risk": "STOCKOUT"}, {"risk": "OVERSTOCK"}, {"risk": "STOCKOUT"}],
    )

    assert "3 SKU" in reply
    assert "STOCKOUT" in reply
    assert "rows=3" in numbers
    assert warnings == []

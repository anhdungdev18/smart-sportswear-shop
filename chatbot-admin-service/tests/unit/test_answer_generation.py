from app.graph.nodes.generate_answer import generate_grounded_answer


def test_answer_reports_inventory_risk_split_from_tool_result():
    reply, warnings, numbers = generate_grounded_answer(
        "INVENTORY_RISK",
        "get_inventory_risks",
        [{"risk": "STOCKOUT"}, {"risk": "OVERSTOCK"}, {"risk": "STOCKOUT"}],
    )

    assert "3 SKU" in reply
    assert "nguy cơ hết hàng" in reply
    assert "rows=3" in numbers
    assert warnings == []


def test_answer_formats_order_overview_as_chat_message():
    reply, warnings, numbers = generate_grounded_answer(
        "ORDER_OVERVIEW",
        "get_order_overview",
        {
            "totalOrders": 15009,
            "byStatus": [
                {"status": "PENDING", "count": 742},
                {"status": "CONFIRMED", "count": 2659},
            ],
        },
    )

    assert "15.009 đơn hàng" in reply
    assert "PENDING" not in reply
    assert "order_overview" not in reply
    assert "totalOrders=15009" in numbers
    assert warnings == []


def test_answer_formats_sales_overview_as_chat_message():
    reply, warnings, numbers = generate_grounded_answer(
        "SALES_OVERVIEW",
        "get_sales_overview",
        {
            "grossRevenue": 5641570000.0,
            "realizedRevenue": 12827740000.0,
            "totalOrders": 15009,
        },
    )

    assert "5.641.570.000₫" in reply
    assert "12.827.740.000₫" in reply
    assert "sales_overview" not in reply
    assert "Số liệu nguồn" not in reply
    assert "grossRevenue=5641570000.0" in numbers
    assert warnings == []


def test_answer_explains_sales_revenue_gap_for_why_question():
    reply, warnings, numbers = generate_grounded_answer(
        "SALES_OVERVIEW",
        "get_sales_overview",
        {
            "grossRevenue": 5641570000.0,
            "realizedRevenue": 12827740000.0,
            "totalOrders": 15009,
        },
        "tại sao doanh thu ghi nhận và doanh thu thực nhận lại khác nhau nhiều như vậy",
    )

    assert "đo hai lát cắt khác nhau" in reply
    assert "PAID" in reply
    assert "DELIVERED" in reply
    assert "7.186.170.000₫" in reply
    assert "Số liệu nguồn" not in reply
    assert "grossRevenue=5641570000.0" in numbers
    assert warnings == []


def test_answer_uses_revenue_breakdown_when_available():
    reply, warnings, numbers = generate_grounded_answer(
        "SALES_OVERVIEW",
        "get_revenue_breakdown",
        {
            "grossRevenue": 1000000,
            "realizedRevenue": 1500000,
            "difference": 500000,
            "codDeliveredUnpaid": {"orders": 3, "amount": 250000},
        },
        "tai sao doanh thu chenh nhau",
        "EXPLANATION",
    )

    assert "500.000" in reply
    assert "COD" in reply
    assert "get_revenue_breakdown" not in reply
    assert "grossRevenue=1000000" in numbers
    assert warnings == []

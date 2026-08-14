import pytest

from app.persistence.repository import blend_visual_and_color_similarity


def test_visual_shape_outweighs_an_unrelated_exact_color_match() -> None:
    correct_shape = blend_visual_and_color_similarity(visual_score=0.90, color_score=0.55)
    wrong_shape_same_color = blend_visual_and_color_similarity(visual_score=0.65, color_score=1.0)

    assert correct_shape > wrong_shape_same_color


def test_color_remains_a_tiebreaker_for_visually_similar_products() -> None:
    matching_color = blend_visual_and_color_similarity(visual_score=0.80, color_score=0.90)
    wrong_color = blend_visual_and_color_similarity(visual_score=0.80, color_score=0.30)

    assert matching_color > wrong_color
    assert matching_color == pytest.approx(0.815)

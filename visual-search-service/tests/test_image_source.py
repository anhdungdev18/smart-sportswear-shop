import pytest

from app.config import Settings
from app.services.errors import ImagePipelineError
from app.services.image_source import ImageSourceResolver


@pytest.fixture
def resolver() -> ImageSourceResolver:
    return ImageSourceResolver(Settings(cloudinary_cloud_name="demo"))


def test_cloudinary_public_id_uses_safe_transformation(resolver: ImageSourceResolver):
    source = resolver.resolve(
        "https://res.cloudinary.com/demo/image/upload/v1/old.jpg",
        "catalog/shoe one.jpg",
    )
    assert source.provider == "cloudinary"
    assert source.download_url == (
        "https://res.cloudinary.com/demo/image/upload/"
        "c_limit,w_1024,h_1024,q_auto:good,f_jpg/catalog/shoe%20one.jpg"
    )


def test_cloudinary_falls_back_to_validated_url_without_public_id(resolver: ImageSourceResolver):
    url = "https://res.cloudinary.com/demo/image/upload/v1/item.webp"
    assert resolver.resolve(url).download_url == url


@pytest.mark.parametrize(
    "url",
    [
        "http://cdn.shopify.com/s/files/item.jpg",
        "https://evil.example/s/files/item.jpg",
        "https://cdn.shopify.com.evil.example/s/files/item.jpg",
        "https://user@cdn.shopify.com/s/files/item.jpg",
        "https://cdn.shopify.com/admin/item.jpg",
        "https://res.cloudinary.com/other/image/upload/item.jpg",
    ],
)
def test_rejects_non_allowlisted_or_malformed_sources(resolver: ImageSourceResolver, url: str):
    with pytest.raises(ImagePipelineError):
        resolver.resolve(url)


def test_shopify_s_files_url_is_supported(resolver: ImageSourceResolver):
    source = resolver.resolve("https://cdn.shopify.com/s/files/1/001/item.jpg?v=2")
    assert source.provider == "shopify"

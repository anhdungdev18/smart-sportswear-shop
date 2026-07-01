import { notFound } from "next/navigation";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { Breadcrumb } from "@/components/Breadcrumb";
import { ProductGallery } from "@/components/ProductGallery";
import { ProductInfoPanel } from "@/components/ProductInfoPanel";
import { ProductDescriptionTabs } from "@/components/ProductDescriptionTabs";
import { ProductCard } from "@/components/ProductCard";
import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import { mapProductDetail, mapProductListItem } from "@/lib/mappers";
import type { ProductDetail } from "@/types/api";

export default async function ProductDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;

  let product: ReturnType<typeof mapProductDetail> | null = null;

  try {
    const result = await apiFetch<ProductDetail>(endpoints.product(slug));
    product = mapProductDetail(result.data);
  } catch {
    notFound();
  }

  if (!product) notFound();

  const galleryImages =
    product.images.length > 0
      ? product.images
      : ["/images/placeholder.png"];

  return (
    <>
      <Header />
      <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
        <Breadcrumb
          items={[
            { label: "Trang chủ", href: "/" },
            { label: product.name },
          ]}
        />
        <div className="mx-auto max-w-[1380px] px-4 pb-16">
          <div className="grid grid-cols-1 gap-10 md:grid-cols-2">
            <ProductGallery images={galleryImages} alt={product.name} />
            <ProductInfoPanel
              name={product.name}
              sku={product.sku ?? product.slug}
              ratingPercentage={product.reviewSummary?.ratingPercentage ?? 0}
              reviewCount={product.reviewSummary?.totalReviews ?? 0}
              price={product.price}
              colors={product.colors}
              sizes={product.sizes}
            />
          </div>

          {product.description && (
            <div className="mt-10">
              <ProductDescriptionTabs description={product.description} />
            </div>
          )}

          {product.relatedProducts.length > 0 && (
            <section className="mt-16">
              <h2 className="mb-6 text-center text-2xl font-semibold tracking-[2px] text-ivy-dark uppercase">
                Sản phẩm tương tự
              </h2>
              <div className="grid grid-cols-2 gap-6 sm:grid-cols-3 lg:grid-cols-4">
                {product.relatedProducts.slice(0, 4).map((p) => (
                  <ProductCard key={p.id} product={p} />
                ))}
              </div>
            </section>
          )}
        </div>
      </main>
      <Footer />
    </>
  );
}

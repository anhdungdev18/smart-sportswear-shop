import { notFound } from "next/navigation";
import { Breadcrumb } from "@/components/Breadcrumb";
import { ProductGallery } from "@/components/ProductGallery";
import { ProductPurchasePanel } from "@/components/ProductPurchasePanel";
import { ProductDescriptionTabs } from "@/components/ProductDescriptionTabs";
import { ProductCard } from "@/components/ProductCard";
import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import { mapProductDetail } from "@/lib/mappers";
import type { ProductDetail } from "@/types/api";

export default async function ProductDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;

  let rawProduct: ProductDetail | null = null;
  try {
    const result = await apiFetch<ProductDetail>(endpoints.product(slug));
    rawProduct = result.data;
  } catch {
    notFound();
  }

  if (!rawProduct) notFound();

  const product = mapProductDetail(rawProduct);
  const galleryImages = product.images.length > 0 ? product.images : ["/images/placeholder.png"];

  return (
    <main className="site-main flex-1 border-b border-ivy-hairline pt-[78px]">
      <Breadcrumb
        items={[
          { label: "Trang chủ", href: "/" },
          { label: product.name },
        ]}
      />
      <div className="mx-auto max-w-[1368px] px-4 pb-16 md:px-0">
        <div className="grid grid-cols-1 gap-10 lg:grid-cols-[620px_minmax(0,1fr)] lg:gap-12">
          <ProductGallery images={galleryImages} alt={product.name} />
          <ProductPurchasePanel
            productId={product.id}
            name={product.name}
            sku={product.sku ?? product.slug}
            ratingPercentage={product.reviewSummary?.ratingPercentage ?? 0}
            reviewCount={product.reviewSummary?.totalReviews ?? 0}
            price={product.price}
            colors={product.colors}
            sizes={product.sizes}
            variants={rawProduct.variants}
          />
        </div>
        {(product.description || rawProduct.attributes) && (
          <div className="mt-12">
            <ProductDescriptionTabs description={product.description} attributes={rawProduct.attributes} />
          </div>
        )}
        {product.relatedProducts.length > 0 && (
          <section className="mt-16">
            <h2 className="mb-8 text-center text-[20px] font-semibold uppercase tracking-[3px] text-ivy-dark md:text-[42px]">
              Sản phẩm tương tự
            </h2>
            <div className="grid grid-cols-2 gap-7 sm:grid-cols-3 lg:grid-cols-4">
              {product.relatedProducts.slice(0, 4).map((p) => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          </section>
        )}
      </div>
    </main>
  );
}

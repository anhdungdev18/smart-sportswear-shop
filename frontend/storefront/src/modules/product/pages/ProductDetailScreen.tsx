import { notFound } from "next/navigation";
import { Breadcrumb } from "@/components/shared/Breadcrumb";
import { ProductCard } from "@/modules/product/components/ProductCard";
import { ProductDescriptionTabs } from "@/modules/product/components/ProductDescriptionTabs";
import { ProductGallery } from "@/modules/product/components/ProductGallery";
import { ProductPurchasePanel } from "@/modules/product/components/ProductPurchasePanel";
import { mapProductDetail } from "@/modules/product/mappers";
import { NO_IMAGE } from "@/modules/ui/placeholder";
import { fetchProductDetail } from "@/modules/product/queries";
import type { ProductDetail } from "@/modules/product/types";

export async function ProductDetailScreen({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const rawProduct: ProductDetail | null = await fetchProductDetail(slug);
  if (!rawProduct) notFound();

  const product = mapProductDetail(rawProduct);
  const galleryImages = product.images.length > 0 ? product.images : [NO_IMAGE];

  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <Breadcrumb items={[{ label: "Trang chủ", href: "/" }, { label: product.name }]} />
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
              {product.relatedProducts.slice(0, 4).map((relatedProduct) => (
                <ProductCard key={relatedProduct.id} product={relatedProduct} />
              ))}
            </div>
          </section>
        )}
      </div>
    </main>
  );
}

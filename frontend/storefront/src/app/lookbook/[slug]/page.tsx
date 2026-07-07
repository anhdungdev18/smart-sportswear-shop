import Image from "next/image";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Breadcrumb } from "@/components/shared/Breadcrumb";
import { fetchCollectionDetail } from "@/modules/category/queries";
import type { CollectionDetail } from "@/modules/category/types";
import { ProductCard } from "@/modules/product/components/ProductCard";
import { mapProductListItem } from "@/modules/product/mappers";

export default async function LookbookPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const collection: CollectionDetail | null = await fetchCollectionDetail(slug);
  if (!collection) notFound();

  const products = collection.products.map(mapProductListItem);
  const heroImage = collection.bannerImageUrl ?? collection.coverImageUrl;
  const eyebrow = [collection.season, collection.year].filter(Boolean).join(" ") || "BỘ SƯU TẬP";

  return (
    <main className="site-main flex-1">
      {heroImage ? (
        <div className="relative h-[60vh] min-h-[400px] w-full overflow-hidden md:h-[80vh]">
          <Image
            src={heroImage}
            alt={collection.name}
            fill
            priority
            sizes="100vw"
            className="object-cover"
          />
          <div className="absolute inset-0 bg-black/30" />
          <div className="absolute inset-0 flex flex-col items-center justify-center text-center text-white">
            <p className="mb-3 text-[11px] font-semibold uppercase tracking-[5px] opacity-80">
              {eyebrow}
            </p>
            <h1 className="text-[36px] font-bold uppercase tracking-[4px] md:text-[64px]">
              {collection.name}
            </h1>
          </div>
        </div>
      ) : (
        <div className="pt-[78px]">
          <Breadcrumb
            items={[
              { label: "Trang chủ", href: "/" },
              { label: "Bộ sưu tập", href: "/bo-suu-tap" },
              { label: collection.name },
            ]}
          />
        </div>
      )}

      {heroImage && (
        <div className="border-b border-ivy-hairline">
          <Breadcrumb
            items={[
              { label: "Trang chủ", href: "/" },
              { label: "Bộ sưu tập", href: "/bo-suu-tap" },
              { label: collection.name },
            ]}
          />
        </div>
      )}

      <section className="mx-auto max-w-[680px] px-6 py-14 text-center">
        <p className="mb-3 text-[11px] font-semibold uppercase tracking-[4px] text-ivy-text-muted">
          {eyebrow}
        </p>
        <h2 className="mb-5 text-[28px] font-bold uppercase tracking-[2px] text-ivy-dark md:text-[36px]">
          {collection.name}
        </h2>
        {(collection.description || collection.shortDescription) && (
          <p className="text-[15px] leading-[1.85] text-ivy-text">
            {collection.description ?? collection.shortDescription}
          </p>
        )}
      </section>

      {collection.coverImageUrl && (
        <div className="mx-auto mb-4 max-w-[1368px] px-4 md:px-0">
          <div className="relative aspect-[16/7] w-full overflow-hidden">
            <Image
              src={collection.coverImageUrl}
              alt={collection.name}
              fill
              sizes="(max-width: 768px) 100vw, 1368px"
              className="object-cover"
            />
          </div>
        </div>
      )}

      {products.length > 0 && (
        <section className="mx-auto max-w-[1368px] px-4 pb-20 pt-14 md:px-0">
          <h3 className="mb-10 text-center text-[13px] font-semibold uppercase tracking-[4px] text-ivy-text-muted">
            Sản phẩm trong bộ sưu tập
          </h3>
          <div className="grid grid-cols-2 gap-x-6 gap-y-10 sm:grid-cols-3 lg:grid-cols-4">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
          <div className="mt-12 text-center">
            <Link
              href="/bo-suu-tap"
              className="inline-block border border-ivy-dark px-8 py-3 text-[13px] font-semibold uppercase tracking-[2px] text-ivy-dark hover:bg-ivy-dark hover:text-white transition-colors"
            >
              Xem tất cả bộ sưu tập
            </Link>
          </div>
        </section>
      )}
    </main>
  );
}

import Image from "next/image";
import Link from "next/link";
import type { CollectionSummary } from "@/modules/category/types";

export function FeaturedCollections({ collections }: { collections: CollectionSummary[] }) {
  const featured = collections.filter((c) => c.isFeatured);
  const items = (featured.length >= 3 ? featured : collections).slice(0, 3);
  if (items.length === 0) return null;

  return (
    <section className="mb-16">
      <div className="mb-8 text-center">
        <h2 className="inline-block border-b border-ivy-dark pb-2 font-[Montserrat,sans-serif] text-[18px] font-light uppercase tracking-wide text-ivy-dark md:text-[28px]">
          Bộ sưu tập nổi bật
        </h2>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {items.map((col) => {
          const year = [...new Set([col.season, col.year].filter(Boolean).map(String))].join(" ");
          const cover = col.bannerImageUrl || col.coverImageUrl;
          return (
            <Link
              key={col.id}
              href={`/lookbook/${col.slug}`}
              className="group flex flex-col overflow-hidden rounded-xl border border-ivy-hairline"
            >
              <div className="relative aspect-[4/5] overflow-hidden bg-[#f3f3f3]">
                {cover ? (
                  <Image
                    src={cover}
                    alt={col.name}
                    fill
                    sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 440px"
                    className="object-cover transition-transform duration-500 group-hover:scale-105"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-[13px] text-ivy-text-muted">
                    Chưa có ảnh
                  </div>
                )}
                <span className="absolute left-4 top-4 bg-ivy-dark px-3 py-1 text-[11px] font-semibold uppercase tracking-wide text-white">
                  Nổi bật
                </span>
              </div>
              <div className="p-5 text-center">
                {year && (
                  <p className="mb-1 text-[11px] font-semibold uppercase tracking-[3px] text-ivy-text-muted">
                    {year}
                  </p>
                )}
                <h3 className="mb-3 text-[17px] font-bold uppercase tracking-[1px] text-ivy-dark transition-opacity group-hover:opacity-70">
                  {col.name}
                </h3>
                <span className="inline-block text-[12px] font-semibold uppercase tracking-[2px] text-ivy-dark underline underline-offset-4">
                  Xem bộ sưu tập
                </span>
              </div>
            </Link>
          );
        })}
      </div>
    </section>
  );
}

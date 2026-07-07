import Image from "next/image";
import { ARTICLE_PROMO_BANNER, RELATED_NEWS_ITEMS } from "@/modules/content/data/editorial";

export function ArticleRelatedSidebar() {
  return (
    <aside className="flex flex-col gap-8 lg:sticky lg:top-24">
      <div>
        <div className="mb-5 text-lg font-semibold text-ivy-dark">Tin mới nhất</div>
        <ul className="flex flex-col gap-4">
          {RELATED_NEWS_ITEMS.map((item) => (
            <li key={item.href}>
              <a href={item.href} className="flex gap-3">
                <div className="relative h-20 w-20 flex-shrink-0 overflow-hidden rounded-lg">
                  <Image
                    src={item.image}
                    alt={item.title}
                    fill
                    sizes="80px"
                    className="object-cover"
                  />
                </div>
                <div className="min-w-0">
                  <h4 className="mb-1 line-clamp-2 text-[13px] leading-[18px] font-semibold text-ivy-dark">
                    {item.title}
                  </h4>
                  <p className="text-xs text-[#A8A9AD]">{item.date}</p>
                </div>
              </a>
            </li>
          ))}
        </ul>
      </div>

      <section className="overflow-hidden rounded-tl-none rounded-tr-3xl rounded-br-none rounded-bl-3xl">
        <a href={ARTICLE_PROMO_BANNER.href} className="block">
          <Image
            src={ARTICLE_PROMO_BANNER.image}
            alt="Khuyến mãi"
            width={400}
            height={500}
            sizes="(min-width: 1024px) 320px, 100vw"
            className="h-auto w-full"
          />
        </a>
      </section>
    </aside>
  );
}

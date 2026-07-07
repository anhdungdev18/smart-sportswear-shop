import { Breadcrumb } from "@/components/shared/Breadcrumb";
import { ABOUT_IMAGES } from "@/modules/content/data/about";

// The live page's entire "story" (CEO quote, timeline, stats, core values,
// activity gallery) is composed of full-bleed editorial images with text
// baked into the pixels — this route is faithfully just an ordered stack
// of those real images.
export default function AboutPage() {
  return (
    <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
      <Breadcrumb
        items={[
          { label: "Trang chủ", href: "/" },
          { label: "Giới thiệu về IVY moda" },
        ]}
      />
      <div className="mx-auto max-w-[1380px]">
        {ABOUT_IMAGES.map((img) => (
          // plain <img> deliberately: 13 large full-bleed editorial images render
          // more reliably as static assets than through next/image's resize pipeline.
          // eslint-disable-next-line @next/next/no-img-element
          <img
            key={img.src}
            src={img.src}
            alt="Giới thiệu về IVY moda"
            width={img.w}
            height={img.h}
            className="block w-full"
          />
        ))}
      </div>
    </main>
  );
}

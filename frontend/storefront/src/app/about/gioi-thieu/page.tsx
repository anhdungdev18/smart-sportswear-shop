import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { Breadcrumb } from "@/components/Breadcrumb";

// The live page's entire "story" (CEO quote, timeline, stats, core values,
// activity gallery) is composed of full-bleed editorial images with the
// text baked into the pixels — there is no live HTML text to extract here,
// so this route is faithfully just an ordered stack of those real images.
const ABOUT_IMAGES = [
  { src: "/images/ivymoda/about/d9b35efcb2e9b92794dcd64cafb07f66.jpg", w: 2134, h: 982 },
  { src: "/images/ivymoda/about/3e954d8077a8893b32dc9c29ea1ca7fa.jpg", w: 2134, h: 953 },
  { src: "/images/ivymoda/about/01e12c182a7ac719b25821c7bf71964d.jpg", w: 2134, h: 744 },
  { src: "/images/ivymoda/about/62814f0335a3abeeffb126422a5f4a80.jpg", w: 2134, h: 1236 },
  { src: "/images/ivymoda/about/71fd257ed566e95e2fa47ab0b1d9e763.jpg", w: 2134, h: 1492 },
  { src: "/images/ivymoda/about/f37e3ae74e7bd972bc3bdfdcee47d835.webp", w: 2134, h: 1026 },
  { src: "/images/ivymoda/about/faad3add6b48490cd4f14387d8533a91.webp", w: 2134, h: 1305 },
  { src: "/images/ivymoda/about/4eeadc06afb1d79a8041ad73e737a568.jpg", w: 2134, h: 1303 },
  { src: "/images/ivymoda/about/6f8462667caf84b5904ae7c3cdbb5ed4.jpg", w: 2134, h: 1493 },
  { src: "/images/ivymoda/about/96990ec5a3cafbe4441949dc6d5a11d4.jpg", w: 2134, h: 1724 },
  { src: "/images/ivymoda/about/e8d1e3e0f3d4364156a9152fbb8480c9.jpg", w: 2134, h: 1251 },
  { src: "/images/ivymoda/about/ddf0ce2eb0964fb6914ed482971250e0.jpg", w: 2134, h: 2197 },
  { src: "/images/ivymoda/about/b5508e1b929d1e1b24d86c61d6d52f92.jpg", w: 2134, h: 1401 },
];

export default function AboutPage() {
  return (
    <>
      <Header />
      <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
        <Breadcrumb
          items={[
            { label: "Trang chủ", href: "/" },
            { label: "Giới thiệu về IVY moda" },
          ]}
        />
        <div className="mx-auto max-w-[1380px]">
          {ABOUT_IMAGES.map((img) => (
            // plain <img> deliberately: 13 large (up to ~1MB) full-bleed
            // editorial images render more reliably as static assets than
            // routed through next/image's on-demand resize pipeline.
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
      <Footer />
    </>
  );
}

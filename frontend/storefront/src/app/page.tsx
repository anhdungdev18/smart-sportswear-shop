import Link from "next/link";
import { ProductCard } from "@/components/storefront/ProductCard";
import { collections, featuredProducts, lookbooks, storyPillars } from "@/lib/mock-catalog";

const heroSlides = [
  { title: "Summer Motion", copy: "Drop đầu mùa cho running, training và phối đồ chuyển động.", href: "/collections/summer-motion", image: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=1600&q=80" },
  { title: "Footwear Lab", copy: "Dành riêng cho giày futsal, running và lifestyle hiệu năng cao.", href: "/collections/footwear-lab", image: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1600&q=80" },
  { title: "Accessory Atelier", copy: "Phụ kiện hoàn thiện outfit theo tinh thần retail thời trang thể thao.", href: "/collections/accessory-atelier", image: "https://images.unsplash.com/photo-1523398002811-999ca8dec234?auto=format&fit=crop&w=1600&q=80" },
];

export default function HomePage() {
  return (
    <main className="page-shell">
      <section className="hero hero-slider-shell">
        <div className="shell hero-slider-grid">
          {heroSlides.map((slide, index) => (
            <article key={slide.title} className={`hero-visual hero-slide-card ${index === 0 ? "is-main" : "is-side"}`} style={{ backgroundImage: `linear-gradient(180deg, rgba(18,18,18,0.06), rgba(18,18,18,0.56)), url(${slide.image})` }}>
              <span className="hero-badge">Bộ sưu tập 2026</span>
              <div className="hero-copy">
                <p className="eyebrow">New Season</p>
                <h1>{slide.title}</h1>
                <p>{slide.copy}</p>
                <div className="hero-actions">
                  <Link href={slide.href} className="button-primary">Khám phá ngay</Link>
                  <Link href="/products" className="button-secondary">Xem catalogue</Link>
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="shell campaign-strip">
        <article><strong>New Arrival</strong><span>Hàng mới theo nhịp ra mắt mùa hè.</span></article>
        <article><strong>Signature Shoes</strong><span>Khu giày tách riêng để đẩy mạnh conversion.</span></article>
        <article><strong>Editorial Layout</strong><span>Storytelling và commerce đứng cùng một mặt tiền.</span></article>
      </section>

      <section className="shell metrics-grid">
        <article className="metric-card"><strong>03</strong><span>trục danh mục lớn: thời trang, giày và phụ kiện.</span></article>
        <article className="metric-card"><strong>12</strong><span>bộ sưu tập có thể mở rộng độc lập theo mùa hoặc chiến dịch.</span></article>
        <article className="metric-card"><strong>360°</strong><span>không gian để nối API sản phẩm, review, promotion và checkout sau.</span></article>
      </section>

      <section className="section">
        <div className="shell">
          <div className="section-heading">
            <p className="eyebrow">Shop by Collection</p>
            <h2>Bộ sưu tập nổi bật</h2>
          </div>
          <div className="collection-grid">
            {collections.map((collection) => (
              <Link key={collection.slug} href={`/collections/${collection.slug}`} className="collection-tile" style={{ backgroundImage: `url(${collection.cover})` }}>
                <p className="eyebrow">{collection.kicker}</p>
                <h3>{collection.name}</h3>
                <p>{collection.description}</p>
                <span className="text-link">Mở trang bộ sưu tập</span>
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="section">
        <div className="shell">
          <div className="section-heading">
            <p className="eyebrow">New Arrival Edit</p>
            <h2>Sản phẩm chủ lực</h2>
          </div>
          <div className="products-grid">
            {featuredProducts.slice(0, 4).map((product) => (<ProductCard key={product.slug} product={product} />))}
          </div>
        </div>
      </section>

      <section className="shell">
        <article className="editorial-banner">
          <img src="https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=1200&q=80" alt="Bộ ảnh editorial thể thao" />
          <div className="editorial-copy">
            <p className="eyebrow">Editorial Campaign</p>
            <h2>Minimal Motion</h2>
            <Link href="/lookbook" className="button-primary">Xem lookbook</Link>
          </div>
        </article>
      </section>

      <section className="section">
        <div className="shell">
          <div className="section-heading">
            <p className="eyebrow">Lookbook</p>
            <h2>Không gian ảnh và câu chuyện</h2>
          </div>
          <div className="lookbook-grid">
            {lookbooks.map((item) => (
              <Link key={item.slug} href="/lookbook" className="lookbook-card">
                <img src={item.image} alt={item.title} />
                <div>
                  <p className="eyebrow">{item.kicker}</p>
                  <h3>{item.title}</h3>
                  <p>{item.description}</p>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="section">
        <div className="shell">
          <div className="section-heading">
            <p className="eyebrow">Brand Notes</p>
            <h2>Tối giản, cao cấp, dễ mở rộng</h2>
          </div>
          <div className="story-grid">
            {storyPillars.map((pillar) => (
              <article key={pillar.title} className="story-card">
                <p className="eyebrow">{pillar.kicker}</p>
                <h3>{pillar.title}</h3>
                <p>{pillar.description}</p>
                <ul>
                  {pillar.points.map((point) => (<li key={point}>{point}</li>))}
                </ul>
              </article>
            ))}
          </div>
        </div>
      </section>
    </main>
  );
}

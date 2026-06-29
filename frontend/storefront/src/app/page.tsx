import { ArrowsClockwise, CaretLeft, CaretRight, CheckCircle, CreditCard, ShieldCheck, Truck } from "@phosphor-icons/react/dist/ssr";
import type { CSSProperties } from "react";
import { FloatingActions, ProductCard, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { getStorefrontHomeData } from "@/modules/catalog/api";
import type { Product } from "@/modules/catalog/products";
import { homeDataCopy, homeCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

export default async function StorefrontHome() {
  const language = await getRequestLanguage();
  const homeData = await getStorefrontHomeData();
  const t = homeCopy[language];
  const d = homeDataCopy[language];

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <section className="hero-slider" aria-label="Thanh Hung Futsal slideshow">
          <div className="slide-track" style={{ "--slide-count": homeData.heroSlides.length } as CSSProperties}>
            {homeData.heroSlides.map((slide) => (
              <a className="hero-slide" href={slide.href} key={slide.desktop}>
                <picture>
                  <source media="(max-width: 767px)" srcSet={slide.mobile} />
                  <img src={slide.desktop} alt={slide.alt} />
                </picture>
              </a>
            ))}
          </div>
          <button className="slider-arrow left" aria-label={language === "vi" ? "Slide trước" : "Previous slide"}>
            <CaretLeft size={24} weight="bold" />
          </button>
          <button className="slider-arrow right" aria-label={language === "vi" ? "Slide tiếp theo" : "Next slide"}>
            <CaretRight size={24} weight="bold" />
          </button>
          <div className="slider-dots" aria-hidden="true">
            {homeData.heroSlides.map((slide, index) => (
              <span className={index === 0 ? "active" : ""} key={slide.alt} />
            ))}
          </div>
        </section>

        <ServiceStrip language={language} />

        <section className="shell section product-section">
          <SectionTitle title={t.joma} />
          <div className="product-grid featured-grid">
            {homeData.featuredProducts.map((product) => (
              <ProductCard product={product} initialLanguage={language} key={product.slug} />
            ))}
          </div>
        </section>

        <ProductRail title={t.hotDeals} products={homeData.hotDeals} seeMore={t.seeMore} language={language} />

        <BrandMosaic tiles={homeData.brandTiles} />

        <section className="shell section tight-section">
          <SectionTitle title={t.need} />
          <div className="quick-grid">
            {homeData.quickCategories.map((category, index) => (
              <a className="quick-card" href={category.href} key={category.href}>
                <img src={category.image} alt={d.quickCategories[index]} />
                <strong>{d.quickCategories[index]}</strong>
              </a>
            ))}
          </div>
        </section>

        <section className="shell section tight-section">
          <SectionTitle title={t.popular} />
          <div className="popular-grid">
            {homeData.popularCategories.map(([, image], index) => (
              <a className="popular-card" href="/products" key={d.popularCategories[index]}>
                <img src={image} alt={d.popularCategories[index]} />
                <span>{d.popularCategories[index]}</span>
              </a>
            ))}
          </div>
        </section>

        <section className="store-experience">
          <div className="shell experience-grid">
            <img src={homeData.images.store} alt={t.storeExperience} />
            <div className="experience-copy">
              <h2>{t.storeExperience}</h2>
              {t.storeChecks.map((item) => (
                <p key={item}>
                  <CheckCircle size={18} weight="fill" />
                  {item}
                </p>
              ))}
              <div className="service-card-grid">
                <div>
                  <strong>{t.storeCount}</strong>
                  <span>{t.storeCountDetail}</span>
                </div>
                <div>
                  <strong>2013</strong>
                  <span>{t.experienceYears}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <ProductTabs title={t.featuredProducts} tabs={homeData.productTabs} language={language} />

        <ProductRail title={t.hotSale} products={homeData.hotSale} seeMore={t.seeMore} language={language} />

        <section className="shell section tight-section">
          <SectionTitle title={t.customers} />
          <a className="customer-banner" href="/pages/khach-hang-cua-thanhhung-futsal">
            <img src={homeData.images.store} alt={t.customers} />
          </a>
        </section>

        <section className="shell section tight-section">
          <div className="section-title with-link">
            <h2>{t.news}</h2>
            <a href="/blogs/tin-tuc">{t.seeAll}</a>
          </div>
          <div className="blog-grid">
            {homeData.blogPosts.map((post) => (
              <article className="blog-card" key={post.title}>
                <img src={post.image} alt={post.title} />
                <h3>{post.title}</h3>
              </article>
            ))}
          </div>
        </section>

        <section className="shell section tight-section">
          <SectionTitle title={t.channel} />
          <div className="instagram-grid">
            {homeData.images.instagram.map((image, index) => (
              <img src={image} alt="Instagram Thanh Hung Futsal" key={index} />
            ))}
          </div>
        </section>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

function ServiceStrip({ language }: { language: "vi" | "en" }) {
  const icons = [ShieldCheck, Truck, ArrowsClockwise, CreditCard];
  const d = homeDataCopy[language];

  return (
    <section className="service-strip" aria-label={d.serviceStrip}>
      <div className="shell service-strip-grid">
        {d.serviceHighlights.map(([title, desc], index) => {
          const Icon = icons[index];

          return (
            <div className="service-strip-item" key={title}>
              <Icon size={30} weight="bold" />
              <div>
                <strong>{title}</strong>
                <span>{desc}</span>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}

function BrandMosaic({ tiles }: { tiles: Awaited<ReturnType<typeof getStorefrontHomeData>>["brandTiles"] }) {
  return (
    <section className="shell section tight-section">
      <div className="brand-mosaic">
        {tiles.map((tile) => (
          <a href={tile.href} className="brand-tile" key={tile.title}>
            <img src={tile.image} alt={tile.title} />
          </a>
        ))}
      </div>
    </section>
  );
}

function ProductTabs({
  title: sectionTitle,
  tabs,
  language
}: {
  title: string;
  tabs: Awaited<ReturnType<typeof getStorefrontHomeData>>["productTabs"];
  language: "vi" | "en";
}) {
  const d = homeDataCopy[language];

  return (
    <section className="shell section product-section tabbed-products">
      <SectionTitle title={sectionTitle} />
      <div className="product-tabs">
        {tabs.map(([title], index) => (
          <input defaultChecked={index === 0} id={`home-product-tab-${index}`} key={`input-${title}`} name="home-product-tabs" type="radio" />
        ))}
        <div className="tab-labels">
          {tabs.map(([title], index) => (
            <label htmlFor={`home-product-tab-${index}`} key={title}>
              {d.productTabLabels[index]}
            </label>
          ))}
        </div>
        {tabs.map(([title, tabProducts], index) => (
          <div className="tab-panel" key={title} data-tab-index={index}>
            <div className="product-grid">
              {tabProducts.map((product, productIndex) => (
                <ProductCard product={product} initialLanguage={language} key={`${title}-${product.slug}-${productIndex}`} />
              ))}
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

function SectionTitle({ title }: { title: string }) {
  return (
    <div className="section-title">
      <h2>{title}</h2>
    </div>
  );
}

function ProductRail({ title, products: railProducts, seeMore, language }: { title: string; products: Product[]; seeMore: string; language: "vi" | "en" }) {
  return (
    <section className="shell section product-section">
      <div className="section-title with-link">
        <h2>{title}</h2>
        <a href="/products">{seeMore}</a>
      </div>
      <div className="product-grid">
        {railProducts.map((product) => (
          <ProductCard product={product} initialLanguage={language} key={`${title}-${product.slug}`} />
        ))}
      </div>
    </section>
  );
}

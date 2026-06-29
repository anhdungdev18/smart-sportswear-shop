import { notFound } from "next/navigation";
import { ProductBuyBox } from "@/components/cart/ProductBuyBox";
import { FloatingActions, ProductCard, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { ProductGallery } from "@/components/ui/ProductGallery";
import { getStorefrontProduct, listStorefrontProductSlugs, listStorefrontProducts } from "@/modules/catalog/api";
import { getLocalizedProductName } from "@/modules/catalog/products";
import { commonPageCopy, productDetailCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

type ProductDetailPageProps = {
  params: Promise<{ slug: string }>;
};

export async function generateStaticParams() {
  return listStorefrontProductSlugs();
}

export async function generateMetadata({ params }: ProductDetailPageProps) {
  const language = await getRequestLanguage();
  const { slug } = await params;
  const product = await getStorefrontProduct(slug);
  const productName = product ? getLocalizedProductName(product, language) : "";

  return {
    title: product ? `${productName} | Thanh Hung Futsal` : commonPageCopy[language].notFoundProduct
  };
}

export default async function ProductDetailPage({ params }: ProductDetailPageProps) {
  const language = await getRequestLanguage();
  const common = commonPageCopy[language];
  const t = productDetailCopy[language];
  const { slug } = await params;
  const product = await getStorefrontProduct(slug);

  if (!product) {
    notFound();
  }

  const productName = getLocalizedProductName(product, language);
  const recommendations = (await listStorefrontProducts({ limit: 8 })).filter((item) => item.slug !== product.slug).slice(0, 4);

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <div className="breadcrumb-wrap">
          <div className="shell breadcrumb">
            <a href="/">{common.home}</a>
            <span>/</span>
            <a href="/products">{common.products}</a>
            <span>/</span>
            <span>{productName}</span>
          </div>
        </div>

        <section className="shell product-detail-page">
          <ProductGallery images={product.gallery} name={productName} initialLanguage={language} />
          <ProductBuyBox product={product} initialLanguage={language} />
        </section>

        <section className="shell detail-tabs">
          {t.warrantyTitles.map((title, index) => (
            <details open={index === 0} key={title}>
              <summary>{title}</summary>
              <p>{t.warrantyBody}</p>
            </details>
          ))}
        </section>

        <section className="shell section product-section">
          <div className="section-title">
            <h2>{t.related}</h2>
          </div>
          <div className="product-grid recommendation-grid">
            {recommendations.map((item) => (
              <ProductCard product={item} initialLanguage={language} key={item.slug} />
            ))}
          </div>
        </section>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

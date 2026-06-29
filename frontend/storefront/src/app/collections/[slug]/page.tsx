import { FloatingActions, ProductCard, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { mockImages } from "@/modules/catalog/mockContent";
import { products } from "@/modules/catalog/products";
import { collectionFallbackCopy, commonPageCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

type CollectionFallbackPageProps = {
  params: Promise<{ slug: string }>;
};

function titleFromSlug(slug: string) {
  return slug
    .split("-")
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

export async function generateMetadata({ params }: CollectionFallbackPageProps) {
  const { slug } = await params;
  return {
    title: `${titleFromSlug(slug)} | Thanh Hung Futsal`
  };
}

export default async function CollectionFallbackPage({ params }: CollectionFallbackPageProps) {
  const language = await getRequestLanguage();
  const common = commonPageCopy[language];
  const t = collectionFallbackCopy[language];
  const { slug } = await params;
  const title = titleFromSlug(slug);
  const normalized = slug.toLowerCase();
  const collectionProducts = products.filter((product) =>
    [product.name, product.brand, product.category, product.tag].some((value) => normalized.includes(value.toLowerCase()))
  );
  const displayProducts = collectionProducts.length ? collectionProducts : products;

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <div className="breadcrumb-wrap">
          <div className="shell breadcrumb">
            <a href="/">{common.home}</a>
            <span>/</span>
            <span>{title}</span>
          </div>
        </div>

        <section className="shell collection-banner">
          <img src={mockImages.artificialTurf} alt={title} />
          <div>
            <span className="promo-kicker">{language === "vi" ? "Danh mục" : "Collection"}</span>
            <h1>{title}</h1>
            <p>{t.description}</p>
          </div>
        </section>

        <section className="shell section product-section">
          <div className="section-title with-link">
            <h2>{t.suggested}</h2>
            <a href="/products">{common.allProducts}</a>
          </div>
          <div className="product-grid">
            {displayProducts.map((product) => (
              <ProductCard product={product} initialLanguage={language} key={product.slug} />
            ))}
          </div>
        </section>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

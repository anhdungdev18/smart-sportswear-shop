import { FloatingActions, ProductCard, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { searchStorefrontProducts } from "@/modules/catalog/api";
import { commonPageCopy, searchPageCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

type SearchPageProps = {
  searchParams: Promise<{ q?: string }>;
};

export async function generateMetadata() {
  const language = await getRequestLanguage();
  return {
    title: searchPageCopy[language].metadataTitle
  };
}

export default async function SearchPage({ searchParams }: SearchPageProps) {
  const language = await getRequestLanguage();
  const t = commonPageCopy[language];
  const s = searchPageCopy[language];
  const { q = "" } = await searchParams;
  const keyword = q.trim();
  const results = await searchStorefrontProducts(keyword);

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <div className="breadcrumb-wrap">
          <div className="shell breadcrumb">
            <a href="/">{t.home}</a>
            <span>/</span>
            <span>{t.search}</span>
          </div>
        </div>
        <section className="shell collection-head">
          <h1>{s.heading}</h1>
          <p>
            {keyword
              ? s.resultsFor(results.length, q)
              : s.noKeyword}
          </p>
        </section>
        <section className="shell section product-section">
          <div className="product-grid">
            {results.map((product) => (
              <ProductCard product={product} initialLanguage={language} key={product.slug} />
            ))}
          </div>
          {!results.length ? <div className="empty-state">{s.emptyResult}</div> : null}
        </section>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

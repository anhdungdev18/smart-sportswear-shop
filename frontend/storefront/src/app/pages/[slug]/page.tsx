import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { mockImages } from "@/modules/catalog/mockContent";
import { commonPageCopy, staticFallbackCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

type StaticFallbackPageProps = {
  params: Promise<{ slug: string }>;
};

function titleFromSlug(slug: string, language: "vi" | "en") {
  return staticFallbackCopy[language].pages[slug as keyof typeof staticFallbackCopy.vi.pages]?.title ?? slug.split("-").join(" ").toUpperCase();
}

export async function generateMetadata({ params }: StaticFallbackPageProps) {
  const language = await getRequestLanguage();
  const { slug } = await params;
  return {
    title: `${titleFromSlug(slug, language)} | Thanh Hung Futsal`
  };
}

export default async function StaticFallbackPage({ params }: StaticFallbackPageProps) {
  const language = await getRequestLanguage();
  const common = commonPageCopy[language];
  const pageCopy = staticFallbackCopy[language];
  const { slug } = await params;
  const content = pageCopy.pages[slug as keyof typeof pageCopy.pages] ?? {
    title: titleFromSlug(slug, language),
    body: pageCopy.fallbackBody
  };

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <div className="breadcrumb-wrap">
          <div className="shell breadcrumb">
            <a href="/">{common.home}</a>
            <span>/</span>
            <span>{content.title}</span>
          </div>
        </div>
        <section className="shell store-page">
          <img src={mockImages.store} alt={content.title} />
          <div>
            <span className="promo-kicker">{pageCopy.kicker}</span>
            <h1>{content.title}</h1>
            {content.body.map((paragraph) => (
              <p key={paragraph}>{paragraph}</p>
            ))}
          </div>
        </section>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

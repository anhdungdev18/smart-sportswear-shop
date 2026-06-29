import { CheckCircle } from "@phosphor-icons/react/dist/ssr";
import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { mockImages } from "@/modules/catalog/mockContent";
import { aboutStoreCopy, commonPageCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

export async function generateMetadata() {
  const language = await getRequestLanguage();
  return {
    title: aboutStoreCopy[language].metadataTitle
  };
}

export default async function AboutStorePage() {
  const language = await getRequestLanguage();
  const common = commonPageCopy[language];
  const t = aboutStoreCopy[language];

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <div className="breadcrumb-wrap">
          <div className="shell breadcrumb">
            <a href="/">{common.home}</a>
            <span>/</span>
            <span>{t.breadcrumb}</span>
          </div>
        </div>

        <section className="shell store-page">
          <img src={mockImages.store} alt={t.alt} />
          <div>
            <span className="promo-kicker">{t.kicker}</span>
            <h1>{t.title}</h1>
            <p>{t.intro}</p>
            <div className="store-checks">
              {t.values.map((item) => (
                <span key={item}>
                  <CheckCircle size={18} weight="fill" />
                  {item}
                </span>
              ))}
            </div>
          </div>
        </section>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

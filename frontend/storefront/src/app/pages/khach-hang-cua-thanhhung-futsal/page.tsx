import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { mockImages } from "@/modules/catalog/mockContent";
import { commonPageCopy, customerCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

export async function generateMetadata() {
  const language = await getRequestLanguage();
  return {
    title: customerCopy[language].metadataTitle
  };
}

const customerImages = [
  mockImages.artificialTurf,
  mockImages.futsal,
  mockImages.accessories,
  mockImages.jersey,
  mockImages.ball,
  mockImages.store
];

export default async function CustomersPage() {
  const language = await getRequestLanguage();
  const common = commonPageCopy[language];
  const t = customerCopy[language];

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

        <section className="shell content-hero">
          <div>
            <span className="promo-kicker">{t.kicker}</span>
            <h1>{t.title}</h1>
            <p>{t.intro}</p>
          </div>
        </section>

        <section className="shell customer-wall">
          {customerImages.map((image, index) => (
            <figure key={image}>
              <img src={image} alt={`${t.imageAlt} ${index + 1}`} />
              <figcaption>{t.caption} #{index + 1}</figcaption>
            </figure>
          ))}
        </section>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

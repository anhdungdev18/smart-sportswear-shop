import { EnvelopeSimple, MapPin, Phone } from "@phosphor-icons/react/dist/ssr";
import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { mockImages } from "@/modules/catalog/mockContent";
import { contactCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

export async function generateMetadata() {
  const language = await getRequestLanguage();
  return {
    title: contactCopy[language].metadataTitle
  };
}

export default async function ContactPage() {
  const language = await getRequestLanguage();
  const t = contactCopy[language];

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <div className="breadcrumb-wrap">
          <div className="shell breadcrumb">
            <a href="/">{t.breadcrumbHome}</a>
            <span>/</span>
            <span>{t.breadcrumbCurrent}</span>
          </div>
        </div>

        <section className="shell contact-page">
          <div className="contact-info">
            <span className="promo-kicker">{t.kicker}</span>
            <h1>{t.title}</h1>
            <p>{t.intro}</p>
            <div className="contact-lines">
              <span>
                <Phone size={18} weight="fill" />
                {t.hotline}
              </span>
              <span>
                <EnvelopeSimple size={18} weight="fill" />
                {t.email}
              </span>
              <span>
                <MapPin size={18} weight="fill" />
                {t.storeSystem}
              </span>
            </div>
            <div className="map-placeholder">
              <img src={mockImages.map} alt={t.mapAlt} />
              <span>{t.mapLabel}</span>
            </div>
          </div>

          <form className="contact-form">
            <label>
              {t.fullName}
              <input placeholder={t.fullNamePlaceholder} />
            </label>
            <label>
              {t.phone}
              <input placeholder={t.phonePlaceholder} />
            </label>
            <label>
              {t.message}
              <textarea placeholder={t.messagePlaceholder} />
            </label>
            <button className="btn btn-primary" type="button">{t.submit}</button>
          </form>
        </section>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

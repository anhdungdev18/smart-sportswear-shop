import { AllProductsCatalog } from "@/components/catalog/AllProductsCatalog";
import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { collectionMetaCopy, commonPageCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

export async function generateMetadata() {
  const language = await getRequestLanguage();
  return {
    title: collectionMetaCopy[language].kids
  };
}

export default async function KidsShoesPage() {
  const language = await getRequestLanguage();
  const t = commonPageCopy[language];

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <div className="breadcrumb-wrap">
          <div className="shell breadcrumb">
            <a href="/">{t.home}</a>
            <span>/</span>
            <a href="/collections/all">{t.category}</a>
            <span>/</span>
            <span>{collectionMetaCopy[language].kidsBreadcrumb}</span>
          </div>
        </div>

        <AllProductsCatalog preset="kids" language={language} />
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

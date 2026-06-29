import { CartPageClient } from "@/components/cart/CartPageClient";
import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { cartPageCopy, commonPageCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

export async function generateMetadata() {
  const language = await getRequestLanguage();
  return {
    title: cartPageCopy[language].metadataTitle
  };
}

export default async function CartPage() {
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
            <span>{t.cart}</span>
          </div>
        </div>
        <CartPageClient language={language} />
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

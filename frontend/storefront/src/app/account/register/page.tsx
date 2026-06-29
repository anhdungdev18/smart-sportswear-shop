import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { accountCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

export async function generateMetadata() {
  const language = await getRequestLanguage();
  return {
    title: accountCopy[language].registerMetadata
  };
}

export default async function RegisterPage() {
  const language = await getRequestLanguage();
  const t = accountCopy[language];

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main className="account-page shell">
        <form className="account-form">
          <h1>{t.registerTitle}</h1>
          <label>{t.fullName}<input placeholder={t.fullNamePlaceholder} /></label>
          <label>Email<input type="email" placeholder="email@example.com" /></label>
          <label>{t.password}<input type="password" placeholder={t.createPassword} /></label>
          <button className="btn btn-primary" type="button">{t.registerButton}</button>
          <a href="/account/login">{t.haveAccount}</a>
        </form>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

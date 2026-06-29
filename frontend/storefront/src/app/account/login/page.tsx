import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { accountCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

export async function generateMetadata() {
  const language = await getRequestLanguage();
  return {
    title: accountCopy[language].loginMetadata
  };
}

export default async function LoginPage() {
  const language = await getRequestLanguage();
  const t = accountCopy[language];

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main className="account-page shell">
        <form className="account-form">
          <h1>{t.loginTitle}</h1>
          <label>Email<input type="email" placeholder="email@example.com" /></label>
          <label>{t.password}<input type="password" placeholder={t.passwordPlaceholder} /></label>
          <button className="btn btn-primary" type="button">{t.loginButton}</button>
          <a href="/account/register">{t.createAccount}</a>
        </form>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

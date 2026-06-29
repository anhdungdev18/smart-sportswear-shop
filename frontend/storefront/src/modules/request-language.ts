import { cookies } from "next/headers";
import { languageCookieName, normalizeLanguage, type Language } from "@/modules/i18n";

export async function getRequestLanguage(): Promise<Language> {
  const cookieStore = await cookies();
  return normalizeLanguage(cookieStore.get(languageCookieName)?.value);
}

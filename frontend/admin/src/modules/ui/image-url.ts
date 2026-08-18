/**
 * Accept URLs copied either as plain text or as a Markdown link.
 * Some legacy seed data contains `[https://...](https://...)`, which is not a
 * valid `next/image` src even though the URL inside it is valid.
 */
export function normalizeImageUrl(value: string | null | undefined, fallback: string): string {
  const trimmed = value?.trim();
  if (!trimmed) return fallback;

  const markdownLink = trimmed.match(/^\[[^\]]*\]\((https?:\/\/[^\s)]+)\)$/i);
  return markdownLink?.[1] ?? trimmed;
}

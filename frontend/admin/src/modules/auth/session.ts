export const ACCESS_TOKEN_COOKIE = "sss_access_token";
export const REFRESH_TOKEN_COOKIE = "sss_refresh_token";
export const ACCESS_TOKEN_STORAGE_KEY = "sss_access_token";
export const REFRESH_TOKEN_STORAGE_KEY = "sss_refresh_token";

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

function setCookie(name: string, value: string, maxAge: number) {
  document.cookie = `${name}=${encodeURIComponent(value)}; max-age=${maxAge}; path=/; SameSite=Lax`;
}

function removeCookie(name: string) {
  document.cookie = `${name}=; max-age=0; path=/; SameSite=Lax`;
}

/**
 * Persists auth tokens to both cookies (for middleware/server components) and
 * localStorage (for browser-client.ts to read).
 * accessToken: 15 min cookie (matches typical JWT expiry)
 * refreshToken: 7 day cookie
 */
export function persistAuthSession(tokens: AuthTokens): void {
  if (typeof window === "undefined") return;

  const { accessToken, refreshToken } = tokens;

  // Store in localStorage for browser-client.ts
  window.localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, accessToken);
  window.localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, refreshToken);

  // Store in cookies so server components / middleware can read them
  setCookie(ACCESS_TOKEN_COOKIE, accessToken, 60 * 60 * 24 * 7); // 7 days (server decides actual expiry)
  setCookie(REFRESH_TOKEN_COOKIE, refreshToken, 60 * 60 * 24 * 30); // 30 days
}

/**
 * Clears all auth tokens from localStorage and cookies.
 */
export function clearAuthSession(): void {
  if (typeof window === "undefined") return;

  window.localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY);
  window.localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);

  removeCookie(ACCESS_TOKEN_COOKIE);
  removeCookie(REFRESH_TOKEN_COOKIE);
}

/**
 * Reads the access token from localStorage first, then falls back to cookie.
 */
export function getBrowserAccessToken(): string | null {
  if (typeof window === "undefined") return null;

  return (
    window.localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY) ??
    readCookie(ACCESS_TOKEN_COOKIE)
  );
}

/**
 * Reads the refresh token from localStorage first, then falls back to cookie.
 */
export function getBrowserRefreshToken(): string | null {
  if (typeof window === "undefined") return null;

  return (
    window.localStorage.getItem(REFRESH_TOKEN_STORAGE_KEY) ??
    readCookie(REFRESH_TOKEN_COOKIE)
  );
}

function readCookie(name: string): string | null {
  if (typeof document === "undefined") return null;

  const match = document.cookie
    .split("; ")
    .find((part) => part.startsWith(`${name}=`));

  if (!match) return null;

  try {
    return decodeURIComponent(match.split("=").slice(1).join("="));
  } catch {
    return match.split("=").slice(1).join("=");
  }
}

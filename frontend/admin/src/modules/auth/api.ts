import { browserApiRequest } from "@/modules/api/browser-client";
import type { AuthTokens } from "@/modules/auth/session";

export interface AdminUser {
  id: string;
  email: string;
  fullName: string;
  role: string;
  status: string;
  phone: string | null;
}

export interface LoginResponseData {
  user: AdminUser;
  tokens: AuthTokens;
}

export async function adminLogin(
  email: string,
  password: string
): Promise<LoginResponseData> {
  return browserApiRequest<LoginResponseData>("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password })
  });
}

export async function adminLogout(refreshToken: string): Promise<void> {
  await browserApiRequest<void>("/api/v1/auth/logout", {
    method: "POST",
    body: JSON.stringify({ refreshToken })
  });
}

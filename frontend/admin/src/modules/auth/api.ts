import { browserApiRequest } from "@/modules/api/browser-client";
import type { AuthTokens } from "@/modules/auth/session";

export interface AdminUser {
  id: number;
  email: string;
  fullName: string;
  role: string;
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

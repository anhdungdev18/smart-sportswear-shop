import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import type { AuthResponse, AuthUser } from "@/modules/auth/types";

export async function login(payload: { email: string; password: string }) {
  const result = await apiFetch<AuthResponse>(endpoints.auth.login, {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return result.data;
}

export async function register(payload: {
  fullName: string;
  email: string;
  password: string;
  phone?: string;
}) {
  const result = await apiFetch<AuthResponse>(endpoints.auth.register, {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return result.data;
}

export async function getMe() {
  const result = await apiFetch<AuthUser>(endpoints.auth.me);
  return result.data;
}

export async function updateMe(payload: { fullName?: string; phone?: string }) {
  const result = await apiFetch<AuthUser>(endpoints.auth.me, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
  return result.data;
}

export async function logout(refreshToken: string) {
  await apiFetch(endpoints.auth.logout, {
    method: "POST",
    body: JSON.stringify({ refreshToken }),
  });
}

export async function forgotPassword(email: string) {
  await apiFetch(endpoints.auth.forgotPassword, {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}

export async function resetPassword(token: string, newPassword: string) {
  await apiFetch(endpoints.auth.resetPassword, {
    method: "POST",
    body: JSON.stringify({ token, newPassword }),
  });
}

export type { AuthResponse, AuthTokens, AuthUser } from "@/modules/auth/types";

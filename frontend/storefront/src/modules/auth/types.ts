export interface AuthUser {
  id: string;
  fullName: string;
  email: string;
  phone?: string | null;
  role: string;
  status: string;
  loginProvider?: string | null;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface AuthResponse {
  user: AuthUser;
  tokens: AuthTokens;
}

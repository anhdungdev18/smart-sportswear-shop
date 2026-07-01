export type AdminUserResponse = {
  id: string;
  fullName: string;
  email: string;
  phone: string | null;
  role: string;
  status: string;
  lastLoginAt: string | null;
  createdAt: string;
};

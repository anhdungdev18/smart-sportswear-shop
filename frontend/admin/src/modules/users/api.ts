import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminUserResponse } from "@/modules/users/types";

export async function listAdminUsers() {
  return apiRequest<AdminUserResponse[]>(adminEndpoints.users, {
    next: { revalidate: 30 }
  });
}

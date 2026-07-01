import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminUserResponse } from "@/modules/users/types";

export async function fetchAdminUserDetail(id: string) {
  return browserApiRequest<AdminUserResponse>(adminEndpoints.userDetail(id), {
    method: "GET"
  });
}

export async function updateAdminUserStatus(id: string, status: string) {
  return browserApiRequest<AdminUserResponse>(adminEndpoints.userStatus(id), {
    method: "PATCH",
    body: JSON.stringify({ status })
  });
}

export async function updateAdminUserRole(id: string, role: string) {
  return browserApiRequest<AdminUserResponse>(adminEndpoints.userRole(id), {
    method: "PATCH",
    body: JSON.stringify({ role })
  });
}

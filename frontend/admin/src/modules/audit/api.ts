import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AuditLogResponse } from "@/modules/audit/types";

export async function listAuditLogs() {
  return apiRequest<AuditLogResponse[]>(adminEndpoints.auditLogs, {
    query: { limit: 50 },
    next: { revalidate: 30 }
  });
}

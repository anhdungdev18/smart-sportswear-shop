import { browserApiRequest } from "@/modules/api/browser-client";
import type { VisualSearchCoverage, VisualSearchJobs, VisualSearchOperations, VisualSearchRetry, VisualSearchUsage } from "./types";

const base = "/api/v1/admin/visual-search";

export async function getVisualSearchDashboard() {
  const [coverage, operations, usage, jobs] = await Promise.all([
    browserApiRequest<VisualSearchCoverage>(`${base}/coverage`),
    browserApiRequest<VisualSearchOperations>(`${base}/operations`),
    browserApiRequest<VisualSearchUsage>(`${base}/usage`, { query: { days: 30 } }),
    browserApiRequest<VisualSearchJobs>(`${base}/jobs`, { query: { limit: 10 } }),
  ]);
  return { coverage, operations, usage, jobs };
}

export function retryFailedEmbeddings() {
  return browserApiRequest<VisualSearchRetry>(`${base}/retry-failed`, { method: "POST" });
}

export function backfillMissingEmbeddings() {
  return browserApiRequest<VisualSearchRetry>(`${base}/backfill-missing`, { method: "POST" });
}

export function reindexVisualSearch(target: { imageId?: string; productId?: string }) {
  return browserApiRequest<VisualSearchRetry>(`${base}/reindex`, {
    method: "POST",
    body: JSON.stringify(target),
  });
}

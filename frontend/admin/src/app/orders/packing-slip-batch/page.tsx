import { PackingSlipBatchClient } from "@/components/orders/PackingSlipBatchClient";

export default async function PackingSlipBatchPage({
  searchParams
}: {
  searchParams: Promise<{ ids?: string; payload?: string }>;
}) {
  const { ids, payload } = await searchParams;
  const orderIds = (ids ?? "").split(",").map((id) => id.trim()).filter(Boolean);
  return <PackingSlipBatchClient orderIds={orderIds} payloadKey={payload} />;
}

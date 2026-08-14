import { PackingSlipClient } from "@/components/orders/PackingSlipClient";

export default async function PackingSlipPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <PackingSlipClient orderId={id} />;
}

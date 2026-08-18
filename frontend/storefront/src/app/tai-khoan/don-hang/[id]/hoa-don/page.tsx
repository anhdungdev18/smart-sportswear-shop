import { OrderInvoiceClient } from "@/modules/account/components/OrderInvoiceClient";

export default async function OrderInvoicePage({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ payload?: string }>;
}) {
  const { id } = await params;
  const { payload } = await searchParams;
  return <OrderInvoiceClient orderId={id} payloadKey={payload} />;
}

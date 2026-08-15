import { OrderInvoiceClient } from "@/modules/account/components/OrderInvoiceClient";

export default async function OrderInvoicePage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <OrderInvoiceClient orderId={id} />;
}

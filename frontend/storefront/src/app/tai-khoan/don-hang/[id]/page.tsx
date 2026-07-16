import { OrderDetailRouteView } from "@/modules/account/pages/OrderDetailRouteView";

export default async function OrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <OrderDetailRouteView orderId={id} />;
}

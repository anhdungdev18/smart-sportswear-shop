import { OrderDetailPageClient } from "@/modules/account/components/OrderDetailPageClient";

export function OrderDetailRouteView({ orderId }: { orderId: string }) {
  return <OrderDetailPageClient orderId={orderId} />;
}

const ORDER_STATUS_LABELS: Record<string, string> = {
  PENDING_CONFIRMATION: "Chờ xác nhận",
  CANCELLATION_REQUESTED: "Đã yêu cầu hủy",
  CANCELLATION_APPROVED: "Đã duyệt hủy",
  CONFIRMED: "Đã xác nhận",
  PACKING: "Đang đóng gói",
  SHIPPING: "Đang giao hàng",
  DELIVERED: "Đã giao hàng",
  CANCELLED: "Đã hủy",
};

export function getOrderStatusLabel(status: string) {
  return ORDER_STATUS_LABELS[status] ?? status.replaceAll("_", " ");
}

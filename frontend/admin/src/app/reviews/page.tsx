import { AdminReviewsClient } from "@/components/reviews/AdminReviewsClient";
import { listAdminReviews } from "@/modules/reviews/api";

export default async function ReviewsPage() {
  const items = await listAdminReviews().catch(() => []);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Đánh giá sản phẩm</h1>
          <p>Duyệt trạng thái review của khách hàng và xem chi tiết nội dung đánh giá.</p>
        </div>
      </section>

      <AdminReviewsClient initialReviews={items} />
    </main>
  );
}

"use client";

import { useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { fetchAdminReviewDetail, updateAdminReviewStatus } from "@/modules/reviews/browser-api";
import type { AdminReviewResponse } from "@/modules/reviews/types";

const statuses = ["PENDING", "APPROVED", "REJECTED"] as const;

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

export function AdminReviewsClient({ initialReviews }: { initialReviews: AdminReviewResponse[] }) {
  const [reviews, setReviews] = useState(initialReviews);
  const [selectedId, setSelectedId] = useState("");
  const [selectedReview, setSelectedReview] = useState<AdminReviewResponse | null>(null);
  const [statusDrafts, setStatusDrafts] = useState<Record<string, string>>(
    Object.fromEntries(initialReviews.map((item) => [item.id, item.status]))
  );
  const [message, setMessage] = useState<string | null>(null);
  const [savingKey, setSavingKey] = useState<string | null>(null);

  async function handleSelect(review: AdminReviewResponse) {
    setSelectedId(review.id);
    setSavingKey(`detail:${review.id}`);
    setMessage(null);

    try {
      const detail = await fetchAdminReviewDetail(review.id);
      setReviews((current) => current.map((item) => (item.id === detail.id ? detail : item)));
      setSelectedReview(detail);
      setStatusDrafts((current) => ({ ...current, [detail.id]: detail.status }));
    } catch (error) {
      setMessage(extractError(error, "Không tải được chi tiết đánh giá"));
      setSelectedReview(review);
    } finally {
      setSavingKey(null);
    }
  }

  async function handleUpdateStatus(review: AdminReviewResponse) {
    try {
      setSavingKey(`status:${review.id}`);
      setMessage(null);
      const updated = await updateAdminReviewStatus(review.id, statusDrafts[review.id] ?? review.status);
      setReviews((current) => current.map((item) => (item.id === updated.id ? updated : item)));
      setSelectedReview((current) => (current?.id === updated.id ? updated : current));
      setMessage(`Đã cập nhật trạng thái review của ${updated.reviewerName}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được trạng thái đánh giá"));
    } finally {
      setSavingKey(null);
    }
  }

  return (
    <div className="admin-grid admin-grid-2">
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Danh sách đánh giá</h2>
            <p className="panel-copy">Duyệt review sản phẩm từ khách hàng ngay trên backend admin.</p>
          </div>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <table className="data-table">
          <thead>
            <tr>
              <th>Người đánh giá</th>
              <th>Tiêu đề</th>
              <th>Điểm</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {reviews.map((review) => (
              <tr
                key={review.id}
                className={selectedId === review.id ? "row-selected" : ""}
                onClick={() => void handleSelect(review)}
              >
                <td>
                  <strong>{review.reviewerName}</strong>
                  <div className="table-subtle">{new Date(review.createdAt).toLocaleString("vi-VN")}</div>
                </td>
                <td>{review.title}</td>
                <td>{review.rating}/5</td>
                <td>{savingKey === `detail:${review.id}` ? "Đang tải..." : review.status}</td>
                <td>
                  <div className="admin-inline-form">
                    <select
                      className="select"
                      value={statusDrafts[review.id] ?? review.status}
                      onChange={(event) =>
                        setStatusDrafts((current) => ({ ...current, [review.id]: event.target.value }))
                      }
                      onClick={(event) => event.stopPropagation()}
                    >
                      {statuses.map((status) => (
                        <option value={status} key={status}>{status}</option>
                      ))}
                    </select>
                    <button
                      className="admin-btn secondary"
                      type="button"
                      onClick={(event) => {
                        event.stopPropagation();
                        void handleUpdateStatus(review);
                      }}
                      disabled={savingKey === `status:${review.id}`}
                    >
                      {savingKey === `status:${review.id}` ? "Đang lưu..." : "Lưu"}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <h2>{selectedReview ? "Chi tiết đánh giá" : "Chọn một đánh giá"}</h2>
        </div>
        {selectedReview ? (
          <div className="admin-stack">
            <div className="admin-subcard admin-subcard-tight">
              <strong>{selectedReview.title}</strong>
              <div className="table-subtle">Reviewer: {selectedReview.reviewerName}</div>
              <div className="table-subtle">Product ID: {selectedReview.productId}</div>
              <div className="table-subtle">User ID: {selectedReview.userId}</div>
              <div className="table-subtle">Điểm: {selectedReview.rating}/5</div>
              <div className="table-subtle">Verified purchase: {selectedReview.verifiedPurchase ? "Có" : "Không"}</div>
              <div className="table-subtle">Trạng thái: {selectedReview.status}</div>
            </div>
            <div className="admin-subcard">
              <strong>Nội dung đánh giá</strong>
              <p>{selectedReview.content}</p>
            </div>
          </div>
        ) : (
          <div className="empty-state">Chọn một review ở bảng bên trái để xem chi tiết.</div>
        )}
      </section>
    </div>
  );
}

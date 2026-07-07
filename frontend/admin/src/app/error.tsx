"use client";

import { useEffect } from "react";

export default function GlobalError({
  error,
  reset
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  const isNetworkError =
    error.message?.toLowerCase().includes("fetch failed") ||
    error.message?.toLowerCase().includes("econnrefused") ||
    error.message?.toLowerCase().includes("network");

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        minHeight: "60vh",
        gap: 16,
        padding: 32,
        textAlign: "center"
      }}
    >
      <div style={{ fontSize: 48 }}>⚠️</div>
      <h2 style={{ fontSize: "1.25rem", fontWeight: 700, margin: 0 }}>
        {isNetworkError ? "Không kết nối được backend" : "Trang bị lỗi"}
      </h2>
      <p style={{ color: "var(--text-muted)", margin: 0, maxWidth: 420 }}>
        {isNetworkError
          ? "Backend tại localhost:8080 chưa chạy hoặc Redis chưa khởi động. Chạy docker compose up redis -d rồi khởi động lại backend."
          : error.message ?? "Đã xảy ra lỗi không mong muốn."}
      </p>
      <button
        className="admin-btn"
        type="button"
        onClick={reset}
        style={{ marginTop: 8 }}
      >
        Thử lại
      </button>
    </div>
  );
}

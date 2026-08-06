"use client";

import { useEffect, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { getApiErrorMessage } from "@/lib/api-errors";
import { emitSessionChange } from "@/lib/session";
import { useAuthenticated } from "@/lib/use-authenticated";
import { getWishlist, removeWishlistItem } from "@/modules/account/api";
import { NO_IMAGE } from "@/modules/ui/placeholder";
import type { WishlistResponse } from "@/modules/account/types";

export function WishlistPageClient() {
  const authenticated = useAuthenticated();
  const [wishlist, setWishlist] = useState<WishlistResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      if (!authenticated) {
        setLoading(false);
        return;
      }
      try {
        const response = await getWishlist();
        setWishlist(response);
      } catch (err) {
        setError(getApiErrorMessage(err, "Không thể tải danh sách yêu thích."));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [authenticated]);

  const handleRemove = async (productId: string) => {
    try {
      const response = await removeWishlistItem(productId);
      setWishlist(response);
      emitSessionChange();
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể xóa sản phẩm khỏi danh sách yêu thích."));
    }
  };

  return (
    <main className="page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1368px] px-4 py-12 md:px-0">
        <div className="mb-10">
          <p className="mb-3 text-[13px] uppercase tracking-[0.24em] text-ivy-text-muted">Điểm Đến Thể Thao</p>
          <h1 className="text-[40px] font-semibold uppercase tracking-[0.06em] text-ivy-dark">Danh sách yêu thích</h1>
          <p className="mt-4 max-w-[700px] text-[15px] leading-7 text-ivy-text">
            Lưu lại những sản phẩm bạn quan tâm để xem lại nhanh hơn khi bộ sưu tập mới hoặc ưu đãi được cập nhật.
          </p>
        </div>

        {error ? <p className="mb-6 text-[14px] text-[#C62127]">{error}</p> : null}

        {!authenticated ? (
          <div className="border border-ivy-hairline px-6 py-10 text-[15px] text-ivy-text">
            Bạn cần đăng nhập để đồng bộ danh sách yêu thích.
          </div>
        ) : loading ? (
          <div className="border border-ivy-hairline px-6 py-10 text-[15px] text-ivy-text">
            Đang tải danh sách yêu thích...
          </div>
        ) : wishlist && wishlist.items.length > 0 ? (
          <div className="grid grid-cols-2 gap-7 sm:grid-cols-3 lg:grid-cols-4">
            {wishlist.items.map((item) => (
              <article key={item.id} className="group">
                <Link href={`/sanpham/${item.productId}`} className="relative mb-4 block aspect-[0.78] overflow-hidden bg-[#f5f5f5]">
                  <Image
                    src={item.thumbnail || NO_IMAGE}
                    alt={item.productName}
                    fill
                    sizes="(max-width: 768px) 50vw, 25vw"
                    className="object-cover transition duration-300 group-hover:scale-[1.02]"
                  />
                </Link>
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h2 className="text-[16px] text-ivy-dark">{item.productName}</h2>
                    <p className="mt-2 text-[13px] text-ivy-text">
                      Đã lưu {new Date(item.createdAt).toLocaleDateString("vi-VN")}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => void handleRemove(item.productId)}
                    className="text-[12px] uppercase tracking-[0.06em] text-ivy-text underline"
                  >
                    Xóa
                  </button>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="border border-ivy-hairline px-6 py-10 text-[15px] text-ivy-text">
            Chưa có sản phẩm nào trong danh sách yêu thích.
          </div>
        )}
      </div>
    </main>
  );
}

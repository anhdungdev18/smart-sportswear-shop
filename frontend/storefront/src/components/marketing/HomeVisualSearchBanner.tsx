"use client";

import { Camera, ArrowRight, Sparkles } from "lucide-react";
import { VisualSearchDialog } from "@/modules/visual-search/VisualSearchDialog";

export function HomeVisualSearchBanner() {
  return (
    <VisualSearchDialog
      trigger={(open) => (
        <section className="mx-auto mb-20 max-w-342 px-4 md:px-0">
          <div className="relative overflow-hidden rounded-2xl bg-ivy-dark px-6 py-10 md:px-14 md:py-14">
            <div
              aria-hidden
              className="pointer-events-none absolute -right-16 -top-16 size-72 rounded-full bg-white/5 blur-2xl"
            />
            <div
              aria-hidden
              className="pointer-events-none absolute -bottom-24 left-1/3 size-72 rounded-full bg-white/5 blur-2xl"
            />
            <div className="relative flex flex-col items-start gap-6 md:flex-row md:items-center md:justify-between">
              <div className="max-w-xl">
                <span className="mb-4 inline-flex items-center gap-2 rounded-full border border-white/25 px-3 py-1 text-[12px] font-semibold uppercase tracking-wide text-white/90">
                  <Sparkles className="size-4" />
                  Tìm kiếm thông minh bằng AI
                </span>
                <h2 className="text-[26px] font-bold uppercase leading-tight tracking-wide text-white md:text-[34px]">
                  Tìm sản phẩm bằng hình ảnh
                </h2>
                <p className="mt-3 text-[15px] leading-7 text-white/70">
                  Chụp hoặc tải lên một tấm ảnh — hệ thống sẽ tìm ngay những sản
                  phẩm giống nhất tại Điểm Đến Thể Thao. Không cần biết tên, chỉ
                  cần hình ảnh.
                </p>
              </div>
              <button
                type="button"
                onClick={open}
                className="group inline-flex shrink-0 items-center gap-3 rounded-full bg-white px-7 py-4 text-[15px] font-semibold text-ivy-dark transition-transform hover:scale-[1.03]"
              >
                <Camera className="size-5" />
                Thử ngay
                <ArrowRight className="size-5 transition-transform group-hover:translate-x-1" />
              </button>
            </div>
          </div>
        </section>
      )}
    />
  );
}

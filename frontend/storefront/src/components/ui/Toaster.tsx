"use client";

import { useEffect, useState } from "react";
import { CheckCircle2, XCircle, Info, X } from "lucide-react";
import { subscribeToast, type ToastItem } from "@/lib/toast";

const DURATION = 3200;

const STYLES: Record<ToastItem["kind"], { icon: typeof CheckCircle2; accent: string }> = {
  success: { icon: CheckCircle2, accent: "text-[#257A4D]" },
  error: { icon: XCircle, accent: "text-[#C62127]" },
  info: { icon: Info, accent: "text-ivy-dark" },
};

export function Toaster() {
  const [items, setItems] = useState<ToastItem[]>([]);

  useEffect(() => {
    return subscribeToast((item) => {
      setItems((prev) => [...prev, item]);
      window.setTimeout(() => {
        setItems((prev) => prev.filter((i) => i.id !== item.id));
      }, DURATION);
    });
  }, []);

  if (items.length === 0) return null;

  return (
    <div className="pointer-events-none fixed right-4 top-4 z-[200] flex w-[calc(100vw-2rem)] max-w-[360px] flex-col gap-2">
      {items.map((item) => {
        const { icon: Icon, accent } = STYLES[item.kind];
        return (
          <div
            key={item.id}
            className="toast-enter pointer-events-auto flex items-start gap-3 rounded-xl border border-ivy-hairline bg-white px-4 py-3 shadow-lg"
            role="status"
            aria-live="polite"
          >
            <Icon className={`mt-0.5 size-5 shrink-0 ${accent}`} />
            <p className="flex-1 text-[13.5px] leading-snug text-ivy-dark">{item.message}</p>
            <button
              type="button"
              aria-label="Đóng"
              onClick={() => setItems((prev) => prev.filter((i) => i.id !== item.id))}
              className="-mr-1 -mt-0.5 rounded-full p-1 text-ivy-text-muted transition-colors hover:bg-[#f3f3f3]"
            >
              <X className="size-4" />
            </button>
          </div>
        );
      })}
    </div>
  );
}

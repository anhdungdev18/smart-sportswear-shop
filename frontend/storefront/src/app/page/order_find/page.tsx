"use client";

import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { Breadcrumb } from "@/components/Breadcrumb";

export default function OrderFindPage() {
  return (
    <>
      <Header />
      <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
        <Breadcrumb
          items={[
            { label: "Trang chủ", href: "/" },
            { label: "Tra cứu đơn hàng" },
          ]}
        />
        <div className="mx-auto max-w-[1380px] px-4 pb-24">
          <form
            onSubmit={(e) => e.preventDefault()}
            className="mx-auto max-w-md py-10"
          >
            <h3 className="mb-6 text-center text-2xl font-semibold text-ivy-dark">
              Tra cứu đơn hàng
            </h3>

            <div className="mb-4 flex items-center gap-4">
              <label className="w-2/5 text-sm text-ivy-text">
                Mã đơn <span className="text-red-600">*</span>
              </label>
              <input
                type="text"
                name="order_invoice_no"
                required
                placeholder="IVM123456"
                className="w-3/5 rounded-lg border border-ivy-hairline px-4 py-2.5 text-sm placeholder:text-ivy-text-muted"
              />
            </div>

            <div className="mb-6 flex items-center gap-4">
              <label className="w-2/5 text-sm text-ivy-text">
                Số điện thoại <span className="text-red-600">*</span>
              </label>
              <input
                type="text"
                name="shipping_phone"
                required
                placeholder="02432052222"
                className="w-3/5 rounded-lg border border-ivy-hairline px-4 py-2.5 text-sm placeholder:text-ivy-text-muted"
              />
            </div>

            <div className="flex justify-center">
              <button
                type="submit"
                className="rounded-lg bg-ivy-dark px-10 py-3 text-sm font-semibold uppercase tracking-wide text-white transition-colors hover:bg-ivy-accent"
              >
                Tra cứu
              </button>
            </div>
          </form>
        </div>
      </main>
      <Footer />
    </>
  );
}

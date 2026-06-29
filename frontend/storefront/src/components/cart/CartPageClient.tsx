"use client";

import { Minus, Plus, Trash } from "@phosphor-icons/react";
import { formatVnd, useCart } from "@/modules/cart/CartContext";
import type { Language } from "@/modules/i18n";

const cartCopy = {
  vi: {
    title: "GIỎ HÀNG",
    empty: "Giỏ hàng của bạn đang trống. Hãy chọn thêm sản phẩm để tiếp tục kiểm kê luồng mua hàng.",
    continueShopping: "Tiếp tục mua hàng",
    noSize: "Chưa chọn size",
    decrease: "Giảm số lượng",
    increase: "Tăng số lượng",
    remove: "Xóa",
    summary: "TÓM TẮT ĐƠN HÀNG",
    subtotal: "Tạm tính",
    shipping: "Phí vận chuyển",
    contact: "Liên hệ",
    checkout: "Tiến hành đặt hàng",
    clearCart: "Xóa giỏ hàng"
  },
  en: {
    title: "CART",
    empty: "Your cart is empty. Add products to continue testing the purchase flow.",
    continueShopping: "Continue shopping",
    noSize: "No size selected",
    decrease: "Decrease quantity",
    increase: "Increase quantity",
    remove: "Remove",
    summary: "ORDER SUMMARY",
    subtotal: "Subtotal",
    shipping: "Shipping",
    contact: "Contact us",
    checkout: "Proceed to checkout",
    clearCart: "Clear cart"
  }
} as const;

export function CartPageClient({ language = "vi" }: { language?: Language }) {
  const { lines, subtotal, updateQuantity, removeItem, clearCart } = useCart();
  const t = cartCopy[language];

  if (!lines.length) {
    return (
      <section className="shell cart-empty">
        <h1>{t.title}</h1>
        <p>{t.empty}</p>
        <a className="btn btn-primary" href="/products">{t.continueShopping}</a>
      </section>
    );
  }

  return (
    <section className="shell cart-page">
      <div>
        <h1>{t.title}</h1>
        <div className="cart-lines">
          {lines.map((line) => (
            <article className="cart-line" key={`${line.slug}-${line.size}`}>
              <img src={line.image} alt="" />
              <div>
                <h2>{line.name}</h2>
                <span>{line.size ? `Size ${line.size}` : t.noSize}</span>
                <strong>{line.price}</strong>
              </div>
              <div className="cart-line-actions">
                <div className="cart-qty">
                  <button type="button" aria-label={t.decrease} onClick={() => updateQuantity(line.slug, line.size, line.quantity - 1)}>
                    <Minus size={15} weight="bold" />
                  </button>
                  <strong>{line.quantity}</strong>
                  <button type="button" aria-label={t.increase} onClick={() => updateQuantity(line.slug, line.size, line.quantity + 1)}>
                    <Plus size={15} weight="bold" />
                  </button>
                </div>
                <button className="cart-remove" type="button" onClick={() => removeItem(line.slug, line.size)}>
                  <Trash size={17} weight="bold" />
                  {t.remove}
                </button>
              </div>
            </article>
          ))}
        </div>
      </div>
      <aside className="cart-summary">
        <h2>{t.summary}</h2>
        <div>
          <span>{t.subtotal}</span>
          <strong>{formatVnd(subtotal)}</strong>
        </div>
        <div>
          <span>{t.shipping}</span>
          <strong>{t.contact}</strong>
        </div>
        <button className="btn btn-primary" type="button">{t.checkout}</button>
        <button className="btn btn-secondary" type="button" onClick={clearCart}>{t.clearCart}</button>
      </aside>
    </section>
  );
}

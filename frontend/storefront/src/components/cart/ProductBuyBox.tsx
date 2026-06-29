"use client";

import { Minus, Plus, ShoppingBag, X } from "@phosphor-icons/react";
import { useEffect, useState } from "react";
import { useCart } from "@/modules/cart/CartContext";
import { getLocalizedProductName, Product } from "@/modules/catalog/products";
import { buyBoxCopy, languageCookieName, type Language } from "@/modules/i18n";

function readLanguage(): Language {
  const cookieValue = document.cookie
    .split("; ")
    .find((part) => part.startsWith(`${languageCookieName}=`))
    ?.split("=")[1];
  const stored = cookieValue ?? window.localStorage.getItem(languageCookieName);

  return stored === "en" || stored === "vi" ? stored : "vi";
}

export function ProductBuyBox({ product, initialLanguage = "vi" }: { product: Product; initialLanguage?: Language }) {
  const firstAvailableSize = product.sizes.find((size) => !product.unavailableSizes?.includes(size)) ?? product.sizes[0];
  const [selectedSize, setSelectedSize] = useState(firstAvailableSize);
  const [quantity, setQuantity] = useState(1);
  const [modalOpen, setModalOpen] = useState(false);
  const [language, setLanguage] = useState<Language>(initialLanguage);
  const { addItem } = useCart();
  const t = buyBoxCopy[language];
  const productName = getLocalizedProductName(product, language);

  useEffect(() => {
    setLanguage(readLanguage());
  }, []);

  function addToCart() {
    addItem({ product: { ...product, name: productName }, size: selectedSize, quantity });
    setModalOpen(true);
  }

  return (
    <>
      <div className="buy-panel sticky-buy">
        <h1>{productName}</h1>
        <div className="detail-meta">
          <span>{t.brand}: {product.brand}</span>
          <span>{t.status}: {t.inStock}</span>
        </div>
        <div className="detail-price-row">
          <span className="price">{product.price}</span>
          {product.oldPrice ? <span className="old-price">{product.oldPrice}</span> : null}
          {product.sale ? <span className="detail-sale">{product.sale}</span> : null}
        </div>

        <div className="size-block">
          <strong>{t.chooseSize}</strong>
          <div className="size-grid" aria-label={t.chooseSize}>
            {product.sizes.map((size) => {
              const disabled = product.unavailableSizes?.includes(size);
              return (
                <button
                  className={size === selectedSize ? "active" : ""}
                  disabled={disabled}
                  key={size}
                  type="button"
                  onClick={() => setSelectedSize(size)}
                >
                  {size}
                </button>
              );
            })}
          </div>
        </div>

        <div className="quantity-row">
          <span>{t.quantity}</span>
          <div>
            <button type="button" aria-label={t.decrease} onClick={() => setQuantity((value) => Math.max(1, value - 1))}>
              <Minus size={16} weight="bold" />
            </button>
            <strong>{quantity}</strong>
            <button type="button" aria-label={t.increase} onClick={() => setQuantity((value) => value + 1)}>
              <Plus size={16} weight="bold" />
            </button>
          </div>
        </div>

        <button className="btn btn-primary add-cart" type="button" onClick={addToCart}>
          <ShoppingBag size={20} weight="bold" />
          {t.addToCart}
        </button>
        <a className="consult-link" href="tel:0900000000">
          {t.consult}
        </a>

        <div className="policy-list">
          {t.policies.map((item) => (
            <div key={item}>
              <strong>{item}</strong>
              <span>{t.policyDetail}</span>
            </div>
          ))}
        </div>
      </div>

      {modalOpen ? (
        <div className="cart-modal-layer" role="dialog" aria-modal="true" aria-label={t.added}>
          <button className="cart-modal-backdrop" type="button" aria-label={t.closePopup} onClick={() => setModalOpen(false)} />
          <div className="cart-modal">
            <button className="cart-modal-close" type="button" aria-label={t.closePopup} onClick={() => setModalOpen(false)}>
              <X size={20} weight="bold" />
            </button>
            <img src={product.image} alt="" />
            <div>
              <h2>{t.added}</h2>
              <p>{productName}</p>
              <span>{t.size} {selectedSize} · {t.quantityShort} {quantity}</span>
              <div className="cart-modal-actions">
                <a className="btn btn-primary" href="/cart">{t.viewCart}</a>
                <button className="btn btn-secondary" type="button" onClick={() => setModalOpen(false)}>
                  {t.continueShopping}
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}

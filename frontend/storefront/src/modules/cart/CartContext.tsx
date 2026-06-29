"use client";

import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { Product, getProductBySlug } from "@/modules/catalog/products";

export type CartLine = {
  slug: string;
  name: string;
  image: string;
  price: string;
  size?: string;
  quantity: number;
};

type AddToCartInput = {
  product: Product;
  size?: string;
  quantity?: number;
};

type CartContextValue = {
  lines: CartLine[];
  count: number;
  subtotal: number;
  addItem: (input: AddToCartInput) => void;
  updateQuantity: (slug: string, size: string | undefined, quantity: number) => void;
  removeItem: (slug: string, size?: string) => void;
  clearCart: () => void;
};

const CartContext = createContext<CartContextValue | null>(null);
const storageKey = "thf-cart";

function priceToNumber(price: string) {
  return Number(price.replace(/\D/g, "")) || 0;
}

export function formatVnd(value: number) {
  return `${value.toLocaleString("vi-VN")}₫`;
}

export function CartProvider({ children }: { children: React.ReactNode }) {
  const [lines, setLines] = useState<CartLine[]>([]);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    try {
      const raw = window.localStorage.getItem(storageKey);
      if (raw) {
        setLines(JSON.parse(raw));
      }
    } catch {
      setLines([]);
    } finally {
      setHydrated(true);
    }
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    window.localStorage.setItem(storageKey, JSON.stringify(lines));
  }, [hydrated, lines]);

  const value = useMemo<CartContextValue>(() => {
    const count = lines.reduce((sum, line) => sum + line.quantity, 0);
    const subtotal = lines.reduce((sum, line) => sum + priceToNumber(line.price) * line.quantity, 0);

    return {
      lines,
      count,
      subtotal,
      addItem: ({ product, size, quantity = 1 }) => {
        setLines((current) => {
          const existing = current.find((line) => line.slug === product.slug && line.size === size);

          if (existing) {
            return current.map((line) =>
              line.slug === product.slug && line.size === size ? { ...line, quantity: line.quantity + quantity } : line
            );
          }

          return [
            ...current,
            {
              slug: product.slug,
              name: product.name,
              image: product.image,
              price: product.price,
              size,
              quantity
            }
          ];
        });
      },
      updateQuantity: (slug, size, quantity) => {
        setLines((current) =>
          current
            .map((line) => (line.slug === slug && line.size === size ? { ...line, quantity: Math.max(1, quantity) } : line))
            .filter((line) => getProductBySlug(line.slug))
        );
      },
      removeItem: (slug, size) => {
        setLines((current) => current.filter((line) => !(line.slug === slug && line.size === size)));
      },
      clearCart: () => setLines([])
    };
  }, [lines]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);

  if (!context) {
    throw new Error("useCart must be used inside CartProvider");
  }

  return context;
}

"use client";

import { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { cn } from "@/lib/utils";
import { HeartIcon, ShoppingBagIcon } from "@/components/icons";
import type { Product } from "@/types/ivy";

function formatPrice(price: number): string {
  return `${price.toLocaleString("vi-VN")}đ`;
}

export function ProductCard({ product }: { product: Product }) {
  const [selectedColorId, setSelectedColorId] = useState<string | undefined>(
    product.colors.find((c) => c.active)?.id ?? product.colors[0]?.id
  );
  const [sizePopupOpen, setSizePopupOpen] = useState(false);

  return (
    <div
      className="item-new-prod w-full"
      onMouseEnter={() => setSizePopupOpen(true)}
      onMouseLeave={() => setSizePopupOpen(false)}
    >
      <div className="product relative">
        {product.ribbon ? (
          <span
            className={cn(
              "absolute left-0 top-2 z-10 px-3 py-1 text-xs font-semibold uppercase text-white",
              product.ribbon === "bestseller" ? "bg-[#AC2F33]" : "bg-[#E7973E]"
            )}
          >
            {product.ribbon === "bestseller" ? "Best Seller" : "NEW"}
          </span>
        ) : null}
        {product.discountPercent ? (
          <span className="badget absolute right-2 top-2 z-10 rounded-tl rounded-br rounded-tr-none rounded-bl-none bg-[#AC2F33] px-2.5 py-1 text-sm font-semibold text-white">
            -{product.discountPercent}%
          </span>
        ) : null}

        <div className="thumb-product group relative mb-[17px] overflow-hidden">
          <Link href={product.href} className="block">
            <Image
              src={product.image}
              alt={product.name}
              width={400}
              height={560}
              className="h-full w-full object-cover"
            />
            <Image
              src={product.hoverImage}
              alt={product.name}
              width={400}
              height={560}
              className="hover-img invisible absolute inset-0 h-full w-full object-cover opacity-0 transition-all duration-300 ease-in-out group-hover:visible group-hover:opacity-100"
            />
          </Link>

          <div className="add-to-cart absolute bottom-0 right-0">
            <button
              type="button"
              className="flex h-8 w-8 items-center justify-center rounded-tl-lg rounded-br-lg border border-transparent bg-[#221F20] text-white transition-colors hover:border-[#221F20] hover:bg-white hover:text-[#221F20]"
              aria-label="Thêm vào giỏ hàng"
            >
              <ShoppingBagIcon className="h-4 w-4" />
            </button>
          </div>

          {product.sizes.length > 0 ? (
            <ul
              className={cn(
                "list-size absolute right-0 border border-[#E7E8E9] bg-white pt-4 transition-all duration-300 ease-in-out",
                sizePopupOpen
                  ? "visible bottom-[35px] opacity-100"
                  : "invisible bottom-0 opacity-0"
              )}
            >
              {product.sizes.map((size) => (
                <li key={size.id} className={cn(size.disabled && "unactive")}>
                  <a
                    href={size.disabled ? undefined : product.href}
                    className={cn(
                      "mb-4 block px-4 text-base font-semibold leading-6 text-[#57585A] hover:text-[#221F20]",
                      size.disabled && "pointer-events-none text-[#D1D2D4]"
                    )}
                  >
                    {size.label.toUpperCase()}
                  </a>
                </li>
              ))}
            </ul>
          ) : null}
        </div>

        <div className="info-product">
          <div className="list-color mb-[13px] flex justify-between">
            <ul className="flex">
              {product.colors.map((color) => (
                <li
                  key={color.id}
                  className={cn(
                    "relative mr-2.5 h-[18px] w-[18px] overflow-hidden rounded-full",
                    selectedColorId === color.id &&
                      "ring-2 ring-[#221F20] ring-offset-1"
                  )}
                >
                  <button
                    type="button"
                    onClick={() => setSelectedColorId(color.id)}
                    aria-label={color.label}
                    className="block h-full w-full cursor-pointer"
                  >
                    <Image
                      src={color.image}
                      alt={color.label}
                      width={18}
                      height={18}
                      className="h-full w-full object-cover"
                    />
                  </button>
                </li>
              ))}
            </ul>
            <button
              type="button"
              aria-label="Yêu thích"
              className="cursor-pointer"
            >
              <HeartIcon className="h-[18px] w-[18px] text-[#57585A]" />
            </button>
          </div>

          <h3 className="title-product">
            <Link
              href={product.href}
              className="mb-[10px] block truncate text-sm leading-4 font-normal text-[#57585A] capitalize"
            >
              {product.name}
            </Link>
          </h3>

          <div className="price-product">
            <ins className="inline-block align-middle text-base leading-6 font-semibold text-[#3E3E3F] no-underline">
              {formatPrice(product.price)}
            </ins>
            {product.oldPrice ? (
              <del className="ml-2 inline-block align-middle text-xs leading-4 font-normal text-[#A8A9AD]">
                {formatPrice(product.oldPrice)}
              </del>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  );
}

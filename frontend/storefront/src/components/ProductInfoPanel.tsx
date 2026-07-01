"use client";

import { useState } from "react";
import Image from "next/image";
import { Star, Ruler, Minus, Plus } from "lucide-react";
import { HeartIcon } from "@/components/icons";
import { cn } from "@/lib/utils";

interface ProductColor {
  id: string;
  image: string;
  label: string;
  active?: boolean;
}

interface ProductSize {
  id: string;
  label: string;
  quantity: number;
}

interface ProductInfoPanelProps {
  name: string;
  sku: string;
  ratingPercentage: number;
  reviewCount: number;
  price: number;
  colors: ProductColor[];
  sizes: ProductSize[];
}

export function ProductInfoPanel({
  name,
  sku,
  ratingPercentage,
  reviewCount,
  price,
  colors,
  sizes,
}: ProductInfoPanelProps) {
  const initialColor = colors.find((color) => color.active) ?? colors[0];
  const [selectedColorId, setSelectedColorId] = useState(initialColor?.id);
  const initialSize = sizes.find((size) => size.quantity > 0);
  const [selectedSizeId, setSelectedSizeId] = useState(initialSize?.id);
  const [quantity, setQuantity] = useState(1);

  const selectedColor =
    colors.find((color) => color.id === selectedColorId) ?? initialColor;
  const filledStars = Math.floor(ratingPercentage / 20);

  const decrementQuantity = () => {
    setQuantity((current) => Math.max(1, current - 1));
  };

  const incrementQuantity = () => {
    setQuantity((current) => current + 1);
  };

  const handleQuantityInput = (value: string) => {
    const parsed = Number.parseInt(value, 10);
    setQuantity(Number.isNaN(parsed) ? 1 : Math.max(1, parsed));
  };

  return (
    <div className="flex flex-col">
      <h1 className="mb-3 text-[28px] font-semibold uppercase text-ivy-dark">
        {name}
      </h1>

      <div className="mb-4 flex flex-wrap items-center gap-3 text-sm text-ivy-text-muted">
        <span>
          SKU: <span className="text-ivy-text-muted">{sku}</span>
        </span>
        <span className="flex items-center gap-0.5" aria-label={`${filledStars} trên 5 sao`}>
          {Array.from({ length: 5 }).map((_, index) => (
            <Star
              key={index}
              size={14}
              className={
                index < filledStars
                  ? "fill-[#F5A623] text-[#F5A623]"
                  : "fill-ivy-disabled text-ivy-disabled"
              }
            />
          ))}
        </span>
        <span>({reviewCount} đánh giá)</span>
      </div>

      <div className="mb-6 text-2xl font-bold text-ivy-dark">
        {price.toLocaleString("vi-VN")}đ
      </div>

      <div className="mb-6">
        <div className="mb-2 text-sm font-semibold text-ivy-dark">
          Màu sắc: <span className="font-normal">{selectedColor?.label}</span>
        </div>
        <div className="flex flex-wrap gap-3">
          {colors.map((color) => (
            <button
              key={color.id}
              type="button"
              aria-label={color.label}
              aria-pressed={selectedColorId === color.id}
              onClick={() => setSelectedColorId(color.id)}
              className={cn(
                "relative h-12 w-12 overflow-hidden rounded-full ring-1 ring-ivy-hairline transition",
                selectedColorId === color.id &&
                  "ring-2 ring-ivy-dark ring-offset-2"
              )}
            >
              <Image
                src={color.image}
                alt={color.label}
                fill
                sizes="48px"
                className="object-cover"
              />
            </button>
          ))}
        </div>
      </div>

      <div className="mb-6">
        <div className="mb-2 text-sm font-semibold text-ivy-dark">Kích thước</div>
        <div className="flex flex-wrap gap-3">
          {sizes.map((size) => {
            const isOutOfStock = size.quantity === 0;
            const isSelected = selectedSizeId === size.id;
            return (
              <button
                key={size.id}
                type="button"
                disabled={isOutOfStock}
                data-selected={isSelected}
                onClick={() => setSelectedSizeId(size.id)}
                className={cn(
                  "min-w-[48px] rounded-tl-lg rounded-br-lg h-10 border border-ivy-hairline px-3 text-sm uppercase text-ivy-text transition",
                  isSelected && "border-ivy-dark font-semibold text-ivy-dark",
                  isOutOfStock &&
                    "cursor-not-allowed border-ivy-hairline text-ivy-disabled line-through"
                )}
              >
                {size.label}
              </button>
            );
          })}
        </div>
        <a
          href="#"
          className="mt-3 inline-flex items-center gap-2 text-sm text-ivy-text-muted underline"
        >
          <Ruler size={16} />
          Kiểm tra size của bạn
        </a>
      </div>

      <div className="mb-6">
        <div className="mb-2 text-sm font-semibold text-ivy-dark">Số lượng</div>
        <div className="flex items-center">
          <button
            type="button"
            aria-label="Giảm số lượng"
            onClick={decrementQuantity}
            className="flex h-10 w-10 items-center justify-center border border-ivy-hairline text-ivy-dark"
          >
            <Minus size={16} />
          </button>
          <input
            type="number"
            name="quantity"
            min={1}
            value={quantity}
            onChange={(event) => handleQuantityInput(event.target.value)}
            className="h-10 w-12 border-y border-ivy-hairline text-center text-ivy-dark [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
          />
          <button
            type="button"
            aria-label="Tăng số lượng"
            onClick={incrementQuantity}
            className="flex h-10 w-10 items-center justify-center border border-ivy-hairline text-ivy-dark"
          >
            <Plus size={16} />
          </button>
        </div>
      </div>

      <div className="mb-4 flex flex-col gap-3 md:flex-row">
        <button
          type="button"
          onClick={() => console.log("Add to cart", { selectedColorId, selectedSizeId, quantity })}
          className="h-12 rounded-tl-lg rounded-br-lg bg-ivy-dark px-8 font-semibold uppercase text-white"
        >
          Thêm vào giỏ
        </button>
        <button
          type="button"
          onClick={() => console.log("Buy now", { selectedColorId, selectedSizeId, quantity })}
          className="h-12 rounded-tl-lg rounded-br-lg border border-ivy-dark px-8 font-semibold uppercase text-ivy-dark"
        >
          Mua hàng
        </button>
        <button
          type="button"
          aria-label="Thêm vào yêu thích"
          onClick={() => console.log("Toggle wishlist")}
          className="flex h-12 w-12 items-center justify-center self-start rounded-tl-lg rounded-br-lg border border-ivy-dark"
        >
          <HeartIcon size={20} className="text-ivy-dark" />
        </button>
      </div>

      <a href="#" className="text-sm text-ivy-dark underline">
        Tìm tại cửa hàng
      </a>
    </div>
  );
}

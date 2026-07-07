"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { Minus, Plus, Ruler, Star } from "lucide-react";
import { HeartIcon } from "@/components/shared/icons";
import { getApiErrorMessage } from "@/lib/api-errors";
import { cn } from "@/lib/utils";
import { emitSessionChange, getAccessToken } from "@/lib/session";
import { addWishlistItem } from "@/modules/account/api";
import { addCartItem } from "@/modules/cart/api";
import type { ProductVariant } from "@/modules/product/types";

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

interface ProductPurchasePanelProps {
  productId: string;
  name: string;
  sku: string;
  ratingPercentage: number;
  reviewCount: number;
  price: number;
  colors: ProductColor[];
  sizes: ProductSize[];
  variants: ProductVariant[];
}

export function ProductPurchasePanel({
  productId,
  name,
  sku,
  ratingPercentage,
  reviewCount,
  price,
  colors,
  sizes,
  variants,
}: ProductPurchasePanelProps) {
  const router = useRouter();
  const [selectedColorLabel, setSelectedColorLabel] = useState(
    colors.find((color) => color.active)?.label ?? colors[0]?.label ?? "",
  );
  const [selectedSizeLabel, setSelectedSizeLabel] = useState(
    sizes.find((size) => size.quantity > 0)?.label ?? sizes[0]?.label ?? "",
  );
  const [quantity, setQuantity] = useState(1);
  const [pending, setPending] = useState<"cart" | "buy" | "wishlist" | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const selectedColor = colors.find((color) => color.label === selectedColorLabel);
  const filledStars = Math.floor(ratingPercentage / 20);

  const availableSizes = useMemo(() => {
    if (variants.length === 0) {
      return sizes;
    }

    const sizeMap = new Map<string, ProductSize>();

    variants
      .filter((variant) => !selectedColorLabel || variant.color === selectedColorLabel)
      .forEach((variant) => {
        const label = variant.size ?? "";
        const existing = sizeMap.get(label);
        const nextQuantity = variant.availableQuantity;

        if (!existing || nextQuantity > existing.quantity) {
          sizeMap.set(label, {
            id: variant.id,
            label,
            quantity: nextQuantity,
          });
        }
      });

    return Array.from(sizeMap.values());
  }, [variants, sizes, selectedColorLabel]);

  const selectedVariant = useMemo(() => {
    if (variants.length === 0) return null;

    const exactMatch = variants.find(
      (variant) => variant.color === selectedColorLabel && variant.size === selectedSizeLabel,
    );
    if (exactMatch) return exactMatch;

    const sameColor = variants.find((variant) => variant.color === selectedColorLabel);
    if (sameColor) return sameColor;

    return variants[0] ?? null;
  }, [variants, selectedColorLabel, selectedSizeLabel]);

  useEffect(() => {
    if (availableSizes.length === 0) {
      setSelectedSizeLabel("");
      return;
    }

    const stillValid = availableSizes.some((size) => size.label === selectedSizeLabel);
    if (stillValid) return;

    const nextSize =
      availableSizes.find((size) => size.quantity > 0)?.label ?? availableSizes[0]?.label ?? "";

    setSelectedSizeLabel(nextSize);
  }, [availableSizes, selectedSizeLabel]);

  const decrementQuantity = () => setQuantity((current) => Math.max(1, current - 1));
  const incrementQuantity = () => setQuantity((current) => current + 1);

  const handleCart = async (mode: "cart" | "buy") => {
    if (!selectedVariant) {
      setError("Sản phẩm này hiện chưa có biến thể khả dụng.");
      return;
    }

    if (selectedVariant.availableQuantity < quantity) {
      setError("Số lượng bạn chọn đang vượt quá tồn kho khả dụng.");
      return;
    }

    setPending(mode);
    setMessage(null);
    setError(null);
    try {
      await addCartItem(selectedVariant.id, quantity);
      emitSessionChange();
      setMessage(
        mode === "buy"
          ? "Sản phẩm đã được thêm vào giỏ, đang chuyển sang thanh toán."
          : "Đã thêm sản phẩm vào giỏ hàng.",
      );
      if (mode === "buy") {
        router.push("/thanh-toan");
      }
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể thêm sản phẩm vào giỏ."));
    } finally {
      setPending(null);
    }
  };

  const handleWishlist = async () => {
    if (!getAccessToken()) {
      router.push("/dang-nhap");
      return;
    }

    setPending("wishlist");
    setMessage(null);
    setError(null);
    try {
      await addWishlistItem(productId);
      emitSessionChange();
      setMessage("Đã thêm sản phẩm vào danh sách yêu thích.");
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể thêm vào yêu thích."));
    } finally {
      setPending(null);
    }
  };

  return (
    <div className="flex flex-col pt-2">
      <h1 className="mb-3 text-[30px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">{name}</h1>

      <div className="mb-4 flex flex-wrap items-center gap-3 text-[14px] text-ivy-text-muted">
        <span>SKU: {sku}</span>
        <span className="flex items-center gap-0.5" aria-label={`${filledStars} trên 5 sao`}>
          {Array.from({ length: 5 }).map((_, index) => (
            <Star
              key={index}
              size={14}
              className={index < filledStars ? "fill-[#F5A623] text-[#F5A623]" : "fill-ivy-disabled text-ivy-disabled"}
            />
          ))}
        </span>
        <span>({reviewCount} đánh giá)</span>
      </div>

      <div className="mb-6 text-[30px] font-semibold text-ivy-dark">{price.toLocaleString("vi-VN")}đ</div>

      <div className="mb-6 border-t border-ivy-hairline pt-6">
        <div className="mb-3 text-[14px] font-semibold uppercase tracking-[0.03em] text-ivy-dark">
          Màu sắc: <span className="font-normal normal-case tracking-normal text-ivy-text">{selectedColor?.label}</span>
        </div>
        <div className="flex flex-wrap gap-3">
          {colors.map((color) => (
            <button
              key={color.id}
              type="button"
              aria-label={color.label}
              aria-pressed={selectedColorLabel === color.label}
              onClick={() => setSelectedColorLabel(color.label)}
              className={cn(
                "relative h-12 w-12 overflow-hidden rounded-full border border-[#d8d8db] transition",
                selectedColorLabel === color.label && "ring-2 ring-ivy-dark ring-offset-2",
              )}
            >
              <Image src={color.image} alt={color.label} fill sizes="48px" className="object-cover" />
            </button>
          ))}
        </div>
      </div>

      <div className="mb-6 border-t border-ivy-hairline pt-6">
        <div className="mb-3 text-[14px] font-semibold uppercase tracking-[0.03em] text-ivy-dark">Kích thước</div>
        <div className="flex flex-wrap gap-3">
          {availableSizes.map((size) => {
            const isOutOfStock = size.quantity === 0;
            const isSelected = selectedSizeLabel === size.label;
            return (
              <button
                key={size.id}
                type="button"
                disabled={isOutOfStock}
                data-selected={isSelected}
                onClick={() => setSelectedSizeLabel(size.label)}
                className={cn(
                  "h-10 min-w-[50px] rounded-tl-[14px] rounded-br-[14px] border border-ivy-hairline px-3 text-[13px] uppercase text-ivy-text transition",
                  isSelected && "border-ivy-dark font-semibold text-ivy-dark",
                  isOutOfStock && "cursor-not-allowed border-ivy-hairline text-ivy-disabled line-through",
                )}
              >
                {size.label}
              </button>
            );
          })}
        </div>
        <a href="#" className="mt-3 inline-flex items-center gap-2 text-[14px] text-ivy-text underline">
          <Ruler size={16} />
          Kiểm tra size của bạn
        </a>
      </div>

      <div className="mb-7 border-t border-ivy-hairline pt-6">
        <div className="mb-3 text-[14px] font-semibold uppercase tracking-[0.03em] text-ivy-dark">Số lượng</div>
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
            onChange={(event) => {
              const parsed = Number.parseInt(event.target.value, 10);
              setQuantity(Number.isNaN(parsed) ? 1 : Math.max(1, parsed));
            }}
            className="h-10 w-14 border-y border-ivy-hairline text-center text-ivy-dark [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
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

      {error ? <p className="mb-4 text-[14px] text-[#C62127]">{error}</p> : null}
      {message ? <p className="mb-4 text-[14px] text-[#257A4D]">{message}</p> : null}

      <div className="mb-5 flex flex-col gap-3 md:flex-row">
        <button
          type="button"
          onClick={() => void handleCart("cart")}
          disabled={pending !== null}
          className="h-12 rounded-tl-[18px] rounded-br-[18px] bg-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.03em] text-white disabled:opacity-60"
        >
          {pending === "cart" ? "Đang thêm..." : "Thêm vào giỏ"}
        </button>
        <button
          type="button"
          onClick={() => void handleCart("buy")}
          disabled={pending !== null}
          className="h-12 rounded-tl-[18px] rounded-br-[18px] border border-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.03em] text-ivy-dark disabled:opacity-60"
        >
          {pending === "buy" ? "Đang xử lý..." : "Mua hàng"}
        </button>
        <button
          type="button"
          aria-label="Thêm vào yêu thích"
          onClick={() => void handleWishlist()}
          disabled={pending !== null}
          className="flex h-12 w-12 items-center justify-center self-start rounded-tl-[18px] rounded-br-[18px] border border-ivy-dark disabled:opacity-60"
        >
          <HeartIcon size={20} className="text-ivy-dark" />
        </button>
      </div>

      <a href="#" className="text-[14px] text-ivy-dark underline">
        Tìm tại cửa hàng
      </a>
    </div>
  );
}


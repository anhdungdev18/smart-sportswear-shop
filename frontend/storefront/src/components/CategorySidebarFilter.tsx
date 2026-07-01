"use client";

import { useState } from "react";
import { Plus, Minus } from "lucide-react";
import { cn } from "@/lib/utils";

interface ColorSwatch {
  label: string;
  hex: string;
}

const SIZE_OPTIONS = ["S", "M", "L", "XL", "XXL"] as const;

const COLOR_SWATCHES: ColorSwatch[] = [
  { label: "Đen", hex: "#221F20" },
  { label: "Đỏ", hex: "#D73831" },
  { label: "Cam", hex: "#E7973E" },
  { label: "Vàng", hex: "#EEB256" },
  { label: "Cam đất", hex: "#DC633A" },
  { label: "Đỏ đô", hex: "#AC2F33" },
  { label: "Trắng", hex: "#F7F8F9" },
  { label: "Xám nhạt", hex: "#E7E8E9" },
  { label: "Xám", hex: "#D1D2D4" },
  { label: "Xám đậm", hex: "#BCBDC0" },
  { label: "Be", hex: "#A8A9AD" },
  { label: "Nâu nhạt", hex: "#939598" },
  { label: "Nâu", hex: "#808285" },
  { label: "Nâu đậm", hex: "#6C6D70" },
];

const DISCOUNT_OPTIONS = ["Dưới 30%", "30% - 50%", "Trên 50%"] as const;

const MATERIAL_OPTIONS = ["Cotton", "Lụa", "Len"] as const;

const FILTER_GROUPS = [
  { id: "size", label: "Size" },
  { id: "color", label: "Màu sắc" },
  { id: "price", label: "Mức giá" },
  { id: "discount", label: "Mức chiết khấu" },
  { id: "advanced", label: "Nâng cao" },
] as const;

type GroupId = (typeof FILTER_GROUPS)[number]["id"];

const MIN_PRICE = 0;
const MAX_PRICE = 5_000_000;

function formatVnd(value: number) {
  return `${value.toLocaleString("vi-VN")}đ`;
}

export function CategorySidebarFilter() {
  const [openGroups, setOpenGroups] = useState<Record<GroupId, boolean>>({
    size: true,
    color: true,
    price: true,
    discount: true,
    advanced: false,
  });
  const [materialOpen, setMaterialOpen] = useState(false);

  const [selectedSize, setSelectedSize] = useState<string | null>(null);
  const [selectedColor, setSelectedColor] = useState<string | null>(null);
  const [selectedDiscount, setSelectedDiscount] = useState<string | null>(null);
  const [selectedMaterials, setSelectedMaterials] = useState<string[]>([]);
  const [priceRange, setPriceRange] = useState<[number, number]>([MIN_PRICE, MAX_PRICE]);

  function toggleGroup(id: GroupId) {
    setOpenGroups((prev) => ({ ...prev, [id]: !prev[id] }));
  }

  function toggleMaterial(option: string) {
    setSelectedMaterials((prev) =>
      prev.includes(option) ? prev.filter((m) => m !== option) : [...prev, option]
    );
  }

  function handleMinChange(value: number) {
    setPriceRange(([, max]) => [Math.min(value, max), max]);
  }

  function handleMaxChange(value: number) {
    setPriceRange(([min]) => [min, Math.max(value, min)]);
  }

  return (
    // Simplification: the real site swaps to a full-screen filter drawer modal on mobile;
    // here we just let the parent page stack this sidebar above the product grid instead.
    <div className="w-full lg:max-w-[270px]">
      <ul>
        {FILTER_GROUPS.map((group, index) => {
          const isOpen = openGroups[group.id];
          const isLast = index === FILTER_GROUPS.length - 1;

          return (
            <li
              key={group.id}
              className={cn(
                "pb-4 mb-4",
                !isLast && "border-b border-[#F7F8F9]"
              )}
            >
              <p
                role="button"
                tabIndex={0}
                onClick={() => toggleGroup(group.id)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    toggleGroup(group.id);
                  }
                }}
                className="mb-[18px] flex cursor-pointer items-center justify-between text-base leading-5 text-ivy-dark"
              >
                <span>{group.label}</span>
                {isOpen ? (
                  <Minus className="size-4 shrink-0" />
                ) : (
                  <Plus className="size-4 shrink-0" />
                )}
              </p>

              <div className={cn(isOpen ? "block" : "hidden")}>
                {group.id === "size" && (
                  <div className="flex flex-wrap gap-2">
                    {SIZE_OPTIONS.map((size) => {
                      const checked = selectedSize === size;
                      return (
                        <button
                          key={size}
                          type="button"
                          onClick={() => setSelectedSize(size)}
                          aria-pressed={checked}
                          className={cn(
                            "relative flex size-12 items-center justify-center rounded-tl-lg rounded-br-lg border text-[12px] leading-4",
                            checked
                              ? "border-ivy-dark text-ivy-dark"
                              : "border-ivy-hairline text-ivy-text-muted"
                          )}
                        >
                          {size}
                        </button>
                      );
                    })}
                  </div>
                )}

                {group.id === "color" && (
                  <div className="flex flex-wrap gap-3">
                    {COLOR_SWATCHES.map((swatch) => {
                      const checked = selectedColor === swatch.label;
                      return (
                        <button
                          key={swatch.label}
                          type="button"
                          title={swatch.label}
                          aria-label={swatch.label}
                          aria-pressed={checked}
                          onClick={() => setSelectedColor(swatch.label)}
                          className={cn(
                            "size-[18px] rounded-full border border-ivy-hairline ring-offset-2",
                            checked && "ring-2 ring-ivy-dark"
                          )}
                          style={{ backgroundColor: swatch.hex }}
                        />
                      );
                    })}
                  </div>
                )}

                {group.id === "price" && (
                  <div>
                    <div className="relative h-1 rounded-full bg-ivy-hairline">
                      <div
                        className="absolute h-1 rounded-full bg-ivy-dark"
                        style={{
                          left: `${(priceRange[0] / MAX_PRICE) * 100}%`,
                          right: `${100 - (priceRange[1] / MAX_PRICE) * 100}%`,
                        }}
                      />
                    </div>
                    <div className="relative mt-2 h-4">
                      <input
                        type="range"
                        min={MIN_PRICE}
                        max={MAX_PRICE}
                        step={100_000}
                        value={priceRange[0]}
                        onChange={(e) => handleMinChange(Number(e.target.value))}
                        aria-label="Mức giá tối thiểu"
                        className="pointer-events-auto absolute inset-x-0 top-0 w-full accent-ivy-dark"
                      />
                      <input
                        type="range"
                        min={MIN_PRICE}
                        max={MAX_PRICE}
                        step={100_000}
                        value={priceRange[1]}
                        onChange={(e) => handleMaxChange(Number(e.target.value))}
                        aria-label="Mức giá tối đa"
                        className="pointer-events-auto absolute inset-x-0 top-0 w-full accent-ivy-dark"
                      />
                    </div>
                    <div className="mt-3 flex items-center justify-between text-[12px] text-ivy-text-muted">
                      <span>{formatVnd(priceRange[0])}</span>
                      <span>{formatVnd(priceRange[1])}</span>
                    </div>
                  </div>
                )}

                {group.id === "discount" && (
                  <ul className="flex flex-col gap-3">
                    {DISCOUNT_OPTIONS.map((option) => {
                      const checked = selectedDiscount === option;
                      return (
                        <li key={option}>
                          <label className="flex cursor-pointer items-center gap-2 text-[14px] text-ivy-text">
                            <input
                              type="radio"
                              name="discount"
                              checked={checked}
                              onChange={() => setSelectedDiscount(option)}
                              className="size-4 accent-ivy-dark"
                            />
                            <span className={cn(checked && "text-ivy-dark")}>{option}</span>
                          </label>
                        </li>
                      );
                    })}
                  </ul>
                )}

                {group.id === "advanced" && (
                  <div>
                    <p
                      role="button"
                      tabIndex={0}
                      onClick={() => setMaterialOpen((prev) => !prev)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter" || e.key === " ") {
                          e.preventDefault();
                          setMaterialOpen((prev) => !prev);
                        }
                      }}
                      className="mb-3 flex cursor-pointer items-center justify-between text-[14px] font-semibold leading-4 text-ivy-dark"
                    >
                      <span>Chất liệu</span>
                      {materialOpen ? (
                        <Minus className="size-3.5 shrink-0" />
                      ) : (
                        <Plus className="size-3.5 shrink-0" />
                      )}
                    </p>
                    <div className={cn(materialOpen ? "block" : "hidden")}>
                      <ul className="flex flex-col gap-3">
                        {MATERIAL_OPTIONS.map((option) => {
                          const checked = selectedMaterials.includes(option);
                          return (
                            <li key={option}>
                              <label className="flex cursor-pointer items-center gap-2 text-[14px] text-ivy-text">
                                <input
                                  type="checkbox"
                                  checked={checked}
                                  onChange={() => toggleMaterial(option)}
                                  className="size-4 accent-ivy-dark"
                                />
                                <span className={cn(checked && "text-ivy-dark")}>{option}</span>
                              </label>
                            </li>
                          );
                        })}
                      </ul>
                    </div>
                  </div>
                )}
              </div>
            </li>
          );
        })}
      </ul>

      <div className="flex gap-3">
        <button
          type="button"
          className="h-10 rounded-full border border-ivy-dark bg-white px-6 text-ivy-dark"
        >
          BỎ LỌC
        </button>
        <button type="button" className="h-10 rounded-full bg-ivy-dark px-6 text-white">
          LỌC
        </button>
      </div>
    </div>
  );
}

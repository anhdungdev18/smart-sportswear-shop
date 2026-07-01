"use client";

import { useMemo, useState } from "react";
import { cn } from "@/lib/utils";
import { SearchIcon, PhoneCallIcon, ChevronDownIcon } from "@/components/icons";

type RegionKey = "bac" | "trung" | "nam";

interface Store {
  name: string;
  phone: string;
}

interface Province {
  name: string;
  stores: Store[] | null; // null => "Đang cập nhật" placeholder
}

const REGIONS: { key: RegionKey; label: string; heading: string }[] = [
  { key: "bac", label: "Miền Bắc", heading: "Cửa hàng Miền Bắc" },
  { key: "trung", label: "Miền Trung", heading: "Cửa hàng Miền Trung" },
  { key: "nam", label: "Miền Nam", heading: "Cửa hàng Miền Nam" },
];

const PROVINCES_BY_REGION: Record<RegionKey, Province[]> = {
  bac: [
    {
      name: "Hà Nội",
      stores: [
        {
          name: "IVY moda 267 Đ. Quang Trung, P. Quang Trung (Hà Đông), TP. Hà Nội",
          phone: "0243 834 1002",
        },
        {
          name: "IVY moda 261-263 Cao Lỗ, Uy Nỗ, Đông Anh, Hà Nội",
          phone: "0243 834 1003",
        },
      ],
    },
    { name: "Hải Phòng", stores: null },
    { name: "Bắc Giang", stores: null },
    { name: "Hải Dương", stores: null },
    { name: "Hưng Yên", stores: null },
    { name: "Lào Cai", stores: null },
    { name: "Nam Định", stores: null },
    { name: "Ninh Bình", stores: null },
    { name: "Phú Thọ", stores: null },
    { name: "Quảng Ninh", stores: null },
    { name: "Thái Bình", stores: null },
    { name: "Thái Nguyên", stores: null },
    { name: "Tuyên Quang", stores: null },
    { name: "Vĩnh Yên", stores: null },
    { name: "Yên Bái", stores: null },
  ],
  trung: [
    { name: "Đà Nẵng", stores: null },
    { name: "Huế", stores: null },
    { name: "Nghệ An", stores: null },
  ],
  nam: [
    { name: "TP. Hồ Chí Minh", stores: null },
    { name: "Cần Thơ", stores: null },
    { name: "Bình Dương", stores: null },
  ],
};

function directionsUrl(address: string) {
  return `https://www.google.com/maps/dir/Current+Location/${encodeURIComponent(address)}`;
}

export function StoreProvinceList() {
  const [activeRegion, setActiveRegion] = useState<RegionKey>("bac");
  const [openProvinces, setOpenProvinces] = useState<Record<string, boolean>>({
    "Hà Nội": true,
  });
  const [query, setQuery] = useState("");

  const activeRegionMeta = REGIONS.find((r) => r.key === activeRegion)!;

  const provinces = useMemo(() => {
    const list = PROVINCES_BY_REGION[activeRegion];
    const q = query.trim().toLowerCase();
    if (!q) return list;
    return list.filter((p) => p.name.toLowerCase().includes(q));
  }, [activeRegion, query]);

  function toggleProvince(name: string) {
    setOpenProvinces((prev) => ({ ...prev, [name]: !prev[name] }));
  }

  return (
    <div>
      <div className="flex justify-center gap-8 border-b border-ivy-hairline mb-6 pb-3">
        {REGIONS.map((region) => {
          const isActive = region.key === activeRegion;
          return (
            <button
              key={region.key}
              type="button"
              onClick={() => setActiveRegion(region.key)}
              className={cn(
                "pb-2 text-base font-medium text-ivy-text-muted cursor-pointer",
                isActive &&
                  "text-ivy-dark border-b-2 border-ivy-dark font-semibold"
              )}
            >
              {region.label}
            </button>
          );
        })}
      </div>

      <div className="border border-ivy-hairline rounded-2xl p-6">
        <h3 className="text-lg font-semibold text-ivy-dark mb-4">
          {activeRegionMeta.heading}
        </h3>

        <div className="relative">
          <SearchIcon className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 size-4 text-ivy-text-muted" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Tìm cửa hàng"
            className="w-full rounded-full border border-ivy-hairline py-2.5 pl-10 pr-4 text-sm text-ivy-dark placeholder:text-ivy-text-muted focus:outline-none focus:border-ivy-dark"
          />
        </div>

        <ul className="mt-4">
          {provinces.map((province) => {
            const isOpen = !!openProvinces[province.name];
            return (
              <li key={province.name}>
                <button
                  type="button"
                  onClick={() => toggleProvince(province.name)}
                  className="w-full flex justify-between items-center py-3.5 border-b border-[#F7F8F9] text-[15px] text-ivy-dark"
                >
                  <span>{province.name}</span>
                  <ChevronDownIcon
                    className={cn(
                      "size-4 text-ivy-text-muted transition-transform duration-200",
                      isOpen && "rotate-180"
                    )}
                  />
                </button>

                {isOpen && (
                  <div>
                    {province.stores ? (
                      province.stores.map((store) => (
                        <div
                          key={store.name}
                          className="py-3 pl-2 border-b border-[#F7F8F9]"
                        >
                          <p className="text-[13px] leading-5 text-ivy-text font-semibold">
                            {store.name}
                          </p>
                          <p className="text-[13px] leading-5 text-ivy-text flex items-center gap-1.5">
                            <PhoneCallIcon className="size-3.5 shrink-0" />
                            {store.phone}
                          </p>
                          <a
                            href={directionsUrl(store.name)}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-[13px] leading-5 text-ivy-accent inline-block"
                          >
                            Chỉ đường →
                          </a>
                        </div>
                      ))
                    ) : (
                      <div className="py-3 pl-2 border-b border-[#F7F8F9]">
                        <p className="text-[13px] leading-5 text-ivy-text">
                          Đang cập nhật
                        </p>
                      </div>
                    )}
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
}

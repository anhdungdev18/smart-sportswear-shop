"use client";

import { useMemo, useState } from "react";
import { cn } from "@/lib/utils";
import { SearchIcon, PhoneCallIcon, ChevronDownIcon } from "@/components/shared/icons";
import { STORE_REGIONS, PROVINCES_BY_REGION } from "@/modules/content/data/stores";
import type { RegionKey } from "@/modules/content/data/stores";

function directionsUrl(address: string) {
  return `https://www.google.com/maps/dir/Current+Location/${encodeURIComponent(address)}`;
}

export function StoreProvinceList() {
  const [activeRegion, setActiveRegion] = useState<RegionKey>("bac");
  const [openProvinces, setOpenProvinces] = useState<Record<string, boolean>>({ "Hà Nội": true });
  const [query, setQuery] = useState("");

  const activeRegionMeta = STORE_REGIONS.find((r) => r.key === activeRegion)!;

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
      <div className="mb-6 flex justify-center gap-8 border-b border-ivy-hairline pb-3">
        {STORE_REGIONS.map((region) => {
          const isActive = region.key === activeRegion;
          return (
            <button
              key={region.key}
              type="button"
              onClick={() => setActiveRegion(region.key)}
              className={cn(
                "cursor-pointer pb-2 text-base font-medium text-ivy-text-muted",
                isActive && "border-b-2 border-ivy-dark font-semibold text-ivy-dark",
              )}
            >
              {region.label}
            </button>
          );
        })}
      </div>

      <div className="rounded-2xl border border-ivy-hairline p-6">
        <h3 className="mb-4 text-lg font-semibold text-ivy-dark">{activeRegionMeta.heading}</h3>

        <div className="relative">
          <SearchIcon className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 size-4 text-ivy-text-muted" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Tìm cửa hàng"
            className="w-full rounded-full border border-ivy-hairline py-2.5 pl-10 pr-4 text-sm text-ivy-dark placeholder:text-ivy-text-muted focus:border-ivy-dark focus:outline-none"
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
                  className="flex w-full items-center justify-between border-b border-[#F7F8F9] py-3.5 text-[15px] text-ivy-dark"
                >
                  <span>{province.name}</span>
                  <ChevronDownIcon
                    className={cn(
                      "size-4 text-ivy-text-muted transition-transform duration-200",
                      isOpen && "rotate-180",
                    )}
                  />
                </button>

                {isOpen && (
                  <div>
                    {province.stores ? (
                      province.stores.map((store) => (
                        <div key={store.name} className="border-b border-[#F7F8F9] py-3 pl-2">
                          <p className="text-[13px] font-semibold leading-5 text-ivy-text">
                            {store.name}
                          </p>
                          <p className="flex items-center gap-1.5 text-[13px] leading-5 text-ivy-text">
                            <PhoneCallIcon className="size-3.5 shrink-0" />
                            {store.phone}
                          </p>
                          <a
                            href={directionsUrl(store.name)}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-block text-[13px] leading-5 text-ivy-accent"
                          >
                            Chỉ đường →
                          </a>
                        </div>
                      ))
                    ) : (
                      <div className="border-b border-[#F7F8F9] py-3 pl-2">
                        <p className="text-[13px] leading-5 text-ivy-text">Đang cập nhật</p>
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

"use client";

import { useEffect, useState } from "react";

type Province = { code: number; name: string };
type Ward = { code: number; name: string };
type ProvinceDetail = Province & { wards: Ward[] };

const PROVINCES_URL = "https://provinces.open-api.vn/api/v2/p/";
const PROVINCE_DETAIL_URL = (code: number) => `https://provinces.open-api.vn/api/v2/p/${code}?depth=2`;

const DEFAULT_SELECT_CLASSNAME = "h-12 border border-ivy-hairline px-4 text-[15px] outline-none bg-white";

/**
 * Cascading Tỉnh/Thành phố -> Phường/Xã picker (Vietnam's 2025 administrative
 * reform dropped the district tier, so this is 2 levels, not 3). Province and
 * ward are stored/emitted as their display names (matching how Address is
 * persisted - see Address entity), not the API's numeric codes, so existing
 * saved addresses keep working without a lookup table.
 */
export function ProvinceWardSelect({
  provinceValue,
  wardValue,
  onProvinceChange,
  onWardChange,
  selectClassName = DEFAULT_SELECT_CLASSNAME,
  labels
}: {
  provinceValue: string;
  wardValue: string;
  onProvinceChange: (name: string) => void;
  onWardChange: (name: string) => void;
  selectClassName?: string;
  /** When set, each select is wrapped in a <label> with a caption, matching form fields that show one. */
  labels?: { wrapperClassName: string; captionClassName: string };
}) {
  const [provinces, setProvinces] = useState<Province[]>([]);
  const [wards, setWards] = useState<Ward[]>([]);
  const [loadingWards, setLoadingWards] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetch(PROVINCES_URL)
      .then((res) => res.json())
      .then((data: Province[]) => {
        if (cancelled) return;
        setProvinces([...data].sort((a, b) => a.name.localeCompare(b.name, "vi")));
      })
      .catch(() => {
        if (!cancelled) setError("Không tải được danh sách tỉnh/thành.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!provinceValue || provinces.length === 0) {
      setWards([]);
      return;
    }
    const province = provinces.find((p) => p.name === provinceValue);
    if (!province) {
      setWards([]);
      return;
    }
    let cancelled = false;
    setLoadingWards(true);
    fetch(PROVINCE_DETAIL_URL(province.code))
      .then((res) => res.json())
      .then((data: ProvinceDetail) => {
        if (cancelled) return;
        setWards([...(data.wards ?? [])].sort((a, b) => a.name.localeCompare(b.name, "vi")));
      })
      .catch(() => {
        if (!cancelled) setError("Không tải được danh sách phường/xã.");
      })
      .finally(() => {
        if (!cancelled) setLoadingWards(false);
      });
    return () => {
      cancelled = true;
    };
  }, [provinceValue, provinces]);

  // A saved address's ward/province may not match the freshly-fetched list
  // (renamed since, or legacy pre-reform data) - keep it selectable/visible
  // instead of silently showing a blank select.
  const provinceOptions = provinceValue && !provinces.some((p) => p.name === provinceValue)
    ? [{ code: -1, name: provinceValue }, ...provinces]
    : provinces;
  const wardOptions = wardValue && !wards.some((w) => w.name === wardValue)
    ? [{ code: -1, name: wardValue }, ...wards]
    : wards;

  const provinceSelect = (
    <select
      className={selectClassName}
      value={provinceValue}
      required
      onChange={(event) => {
        onProvinceChange(event.target.value);
        onWardChange("");
      }}
    >
      <option value="">{labels ? "Chọn tỉnh / thành phố" : "Tỉnh / Thành phố"}</option>
      {provinceOptions.map((p) => (
        <option value={p.name} key={p.code}>{p.name}</option>
      ))}
    </select>
  );

  const wardSelect = (
    <select
      className={selectClassName}
      value={wardValue}
      required
      onChange={(event) => onWardChange(event.target.value)}
      disabled={!provinceValue || loadingWards}
    >
      <option value="">{loadingWards ? "Đang tải..." : labels ? "Chọn phường / xã" : "Phường / Xã"}</option>
      {wardOptions.map((w) => (
        <option value={w.name} key={w.code}>{w.name}</option>
      ))}
    </select>
  );

  if (labels) {
    return (
      <>
        <label className={labels.wrapperClassName}>
          <span className={labels.captionClassName}>Tỉnh / Thành phố</span>
          {provinceSelect}
        </label>
        <label className={labels.wrapperClassName}>
          <span className={labels.captionClassName}>Phường / Xã</span>
          {wardSelect}
        </label>
        {error ? <p className="md:col-span-2 text-[13px] text-[#C62127]">{error}</p> : null}
      </>
    );
  }

  return (
    <>
      {provinceSelect}
      {wardSelect}
      {error ? <p className="md:col-span-2 text-[13px] text-[#C62127]">{error}</p> : null}
    </>
  );
}

export type RegionKey = "bac" | "trung" | "nam";

export interface Store {
  name: string;
  phone: string;
}

export interface Province {
  name: string;
  stores: Store[] | null;
}

export interface RegionMeta {
  key: RegionKey;
  label: string;
  heading: string;
}

export const STORE_REGIONS: RegionMeta[] = [
  { key: "bac", label: "Miền Bắc", heading: "Cửa hàng Miền Bắc" },
  { key: "trung", label: "Miền Trung", heading: "Cửa hàng Miền Trung" },
  { key: "nam", label: "Miền Nam", heading: "Cửa hàng Miền Nam" },
];

export const PROVINCES_BY_REGION: Record<RegionKey, Province[]> = {
  bac: [
    {
      name: "Hà Nội",
      stores: [
        { name: "Điểm Đến Thể Thao 267 Đ. Quang Trung, P. Quang Trung (Hà Đông), TP. Hà Nội", phone: "0243 834 1002" },
        { name: "Điểm Đến Thể Thao 261-263 Cao Lỗ, Uy Nỗ, Đông Anh, Hà Nội", phone: "0243 834 1003" },
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

import { Headphones, Mail, MapPin, ShoppingBag } from "lucide-react";
import type { LucideIcon } from "lucide-react";

export interface ContactInfoCardData {
  icon: LucideIcon;
  label: string;
  lines: string[];
}

export const CONTACT_INFO_CARDS: ContactInfoCardData[] = [
  {
    icon: MapPin,
    label: "Địa chỉ",
    lines: ["Tầng 14, Toà nhà Hapulico Complex 24T- 85 Vũ Trọng Phụng - Quận Thanh Xuân, HN"],
  },
  {
    icon: Mail,
    label: "Email",
    lines: ["cskh@ivy.com.vn"],
  },
  {
    icon: ShoppingBag,
    label: "Mua hàng online",
    lines: ["02466623434"],
  },
  {
    icon: Headphones,
    label: "Chăm sóc khách hàng",
    lines: [
      "Email: cskh@ivy.com.vn",
      "Hotline: 0905 89 86 83",
      "Thứ Hai đến Thứ Bảy, từ 8:00 đến 17:30",
    ],
  },
];

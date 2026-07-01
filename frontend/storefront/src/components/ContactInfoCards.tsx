import { Headphones, Mail, MapPin, ShoppingBag } from "lucide-react";
import type { LucideIcon } from "lucide-react";

interface ContactInfoCard {
  icon: LucideIcon;
  label: string;
  value: React.ReactNode;
}

const CARDS: ContactInfoCard[] = [
  {
    icon: MapPin,
    label: "Địa chỉ",
    value:
      "Tầng 14, Toà nhà Hapulico Complex 24T- 85 Vũ Trọng Phụng - Quận Thanh Xuân, HN",
  },
  {
    icon: Mail,
    label: "Email",
    value: "cskh@ivy.com.vn",
  },
  {
    icon: ShoppingBag,
    label: "Mua hàng online",
    value: "02466623434",
  },
  {
    icon: Headphones,
    label: "Chăm sóc khách hàng",
    value: (
      <>
        Email: cskh@ivy.com.vn
        <br />
        Hotline: 0905 89 86 83
        <br />
        Thứ Hai đến Thứ Bảy, từ 8:00 đến 17:30
      </>
    ),
  },
];

export function ContactInfoCards() {
  return (
    <div className="flex flex-col gap-4">
      {CARDS.map(({ icon: Icon, label, value }) => (
        <div
          key={label}
          className="flex items-start gap-4 rounded-xl border border-ivy-hairline p-5"
        >
          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-[#F7F8F9] text-ivy-dark">
            <Icon className="h-5 w-5" />
          </div>
          <div>
            <h4 className="mb-1.5 text-base font-semibold text-ivy-dark">
              {label}
            </h4>
            <p className="text-sm leading-[22px] text-ivy-text">{value}</p>
          </div>
        </div>
      ))}
    </div>
  );
}

"use client";

import {
  SearchIcon,
  HeadphonesIcon,
  PhoneCallIcon,
  ChatIcon,
  MessengerIcon,
  EnvelopeIcon,
  OrderLookupIcon,
  AvatarIcon,
  ShoppingBagIcon,
} from "@/components/shared/icons";
import { CUSTOMER_SERVICE_LINKS } from "@/modules/content/data/layout";

const CS_ICON_MAP: Record<string, React.ElementType> = {
  Hotline: PhoneCallIcon,
  "Live Chat": ChatIcon,
  Messenger: MessengerIcon,
  Email: EnvelopeIcon,
  "Tra cứu đơn hàng": OrderLookupIcon,
};

export function HeaderActions() {
  return (
    <div className="right-header flex h-10 items-center gap-6">
      <form
        className="search-form hidden h-[38px] items-center gap-2 rounded-[4px] border border-ivy-hairline bg-white px-4 text-[12px] text-ivy-text md:flex"
        onSubmit={(e) => e.preventDefault()}
      >
        <button
          type="submit"
          className="submit flex items-center justify-center text-ivy-dark"
          aria-label="Tìm kiếm"
        >
          <SearchIcon className="size-[15px]" />
        </button>
        <input
          id="search-quick"
          type="text"
          placeholder="TÌM KIẾM SẢN PHẨM"
          className="w-[300px] bg-transparent text-[12px] tracking-[0.01em] text-ivy-text placeholder:text-[#8b8c91] outline-none lg:w-[330px]"
        />
      </form>

      <div className="icon group relative hidden md:block">
        <button
          type="button"
          className="flex h-10 w-5 items-center justify-center text-ivy-dark"
          aria-label="Hỗ trợ khách hàng"
        >
          <HeadphonesIcon className="size-[18px]" />
        </button>
        <div className="absolute right-0 top-full z-50 hidden w-56 border border-ivy-hairline bg-white p-4 shadow-[0_8px_24px_rgba(34,31,32,0.06)] group-hover:block">
          <ul className="flex flex-col gap-3">
            {CUSTOMER_SERVICE_LINKS.map(({ label, href, ...rest }) => {
              const Icon = CS_ICON_MAP[label] ?? PhoneCallIcon;
              return (
                <li key={label}>
                  <a
                    href={href}
                    {...(rest.external ? { target: "_blank", rel: "nofollow" } : {})}
                    className="flex items-center gap-2 text-[13px] text-ivy-text hover:text-ivy-accent"
                  >
                    <Icon className="size-4 shrink-0" />
                    <span>{label}</span>
                  </a>
                </li>
              );
            })}
          </ul>
        </div>
      </div>

      <a href="/dang-nhap" className="icon flex h-10 w-5 items-center justify-center text-ivy-dark" aria-label="Tài khoản">
        <AvatarIcon className="size-[18px]" />
      </a>

      <a
        href="/gio-hang"
        className="icon relative flex h-10 w-5 items-center justify-center text-ivy-dark"
        aria-label="Giỏ hàng"
      >
        <ShoppingBagIcon className="size-[18px]" />
        <span className="absolute -right-1 top-[2px] flex size-4 items-center justify-center rounded-full bg-ivy-accent text-[10px] text-white">
          0
        </span>
      </a>
    </div>
  );
}

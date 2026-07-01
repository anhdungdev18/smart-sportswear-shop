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
} from "@/components/icons";

const CS_LINKS = [
  {
    icon: PhoneCallIcon,
    label: "Hotline",
    href: "tel:02466623434",
  },
  {
    icon: ChatIcon,
    label: "Live Chat",
    href: "#",
  },
  {
    icon: MessengerIcon,
    label: "Messenger",
    href: "http://messenger.com/t/thoitrangivymoda",
    external: true,
  },
  {
    icon: EnvelopeIcon,
    label: "Email",
    href: "mailto:saleadmin@ivy.com.vn",
  },
  {
    icon: OrderLookupIcon,
    label: "Tra cứu đơn hàng",
    href: "#",
  },
] as const;

export function HeaderActions() {
  return (
    <div className="right-header flex h-10 items-center gap-6">
      <form
        className="search-form hidden items-center gap-2 rounded-full border border-ivy-hairline bg-white px-3 h-9 text-[12px] text-ivy-text md:flex"
        onSubmit={(e) => e.preventDefault()}
      >
        <button
          type="submit"
          className="submit flex items-center justify-center text-ivy-dark"
          aria-label="Tìm kiếm"
        >
          <SearchIcon className="size-4" />
        </button>
        <input
          id="search-quick"
          type="text"
          placeholder="TÌM KIẾM SẢN PHẨM"
          className="w-40 bg-transparent text-[12px] text-ivy-text placeholder:text-ivy-text outline-none lg:w-56"
        />
      </form>

      <a
        href="#"
        className="hidden text-[13px] font-medium text-ivy-dark hover:text-ivy-accent md:inline-block"
      >
        Outlet
      </a>

      <div className="icon group relative hidden md:block">
        <button type="button" className="flex items-center justify-center text-ivy-dark" aria-label="Hỗ trợ khách hàng">
          <HeadphonesIcon className="size-5" />
        </button>
        <div className="absolute right-0 top-full z-50 hidden w-56 rounded-sm border border-ivy-hairline bg-white p-4 shadow-sm group-hover:block">
          <ul className="flex flex-col gap-3">
            {CS_LINKS.map(({ icon: Icon, label, href, ...rest }) => (
              <li key={label}>
                <a
                  href={href}
                  {...("external" in rest && rest.external
                    ? { target: "_blank", rel: "nofollow" }
                    : {})}
                  className="flex items-center gap-2 text-[13px] text-ivy-text hover:text-ivy-accent"
                >
                  <Icon className="size-4 shrink-0" />
                  <span>{label}</span>
                </a>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <a href="#" className="icon flex items-center justify-center text-ivy-dark" aria-label="Tài khoản">
        <AvatarIcon className="size-5" />
      </a>

      <a href="#" className="icon relative flex items-center justify-center text-ivy-dark" aria-label="Giỏ hàng">
        <ShoppingBagIcon className="size-5" />
        <span className="absolute -right-1 -top-1 flex size-4 items-center justify-center rounded-full bg-ivy-accent text-[10px] text-white">
          0
        </span>
      </a>
    </div>
  );
}

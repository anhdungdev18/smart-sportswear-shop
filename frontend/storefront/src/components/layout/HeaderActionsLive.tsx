"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import {
  AvatarIcon,
  ChatIcon,
  EnvelopeIcon,
  HeadphonesIcon,
  HeartIcon,
  MessengerIcon,
  OrderLookupIcon,
  PhoneCallIcon,
  ShoppingBagIcon,
} from "@/components/shared/icons";
import { clearSession, getAccessToken, getRefreshToken, onSessionChange } from "@/lib/session";
import { ApiError } from "@/lib/api";
import { getMe, logout } from "@/modules/auth/api";
import { getWishlist } from "@/modules/account/api";
import { getCart } from "@/modules/cart/api";
import { NotificationBell } from "@/modules/notifications/NotificationBell";
import { CUSTOMER_SERVICE_LINKS } from "@/modules/content/data/layout";
import { ChevronDown, KeyRound, MapPin, Package, UserRound } from "lucide-react";

const CS_ICON_MAP: Record<string, React.ElementType> = {
  Hotline: PhoneCallIcon,
  "Live Chat": ChatIcon,
  Messenger: MessengerIcon,
  Email: EnvelopeIcon,
  "Tra cứu đơn hàng": OrderLookupIcon,
};

export function HeaderActionsLive() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [cartCount, setCartCount] = useState(0);
  const [wishlistCount, setWishlistCount] = useState(0);
  const [accountMenuOpen, setAccountMenuOpen] = useState(false);
  const accountMenuRef = useRef<HTMLDivElement>(null);

  const refresh = useCallback(async () => {
    const accessToken = getAccessToken();
    if (!accessToken) {
      try {
        const cart = await getCart();
        setCartCount(cart.items.reduce((sum, item) => sum + item.quantity, 0));
      } catch {
        setCartCount(0);
      }
      setIsAuthenticated(false);
      setWishlistCount(0);
      return;
    }

    // Keep the authenticated shell stable while parallel header requests run.
    // Navigation can abort in-flight fetches; an abort/network failure is not
    // evidence that the token is invalid and must not log the customer out.
    setIsAuthenticated(true);

    const [cartResult, meResult, wishlistResult] = await Promise.allSettled([
      getCart(),
      getMe(),
      getWishlist(),
    ]);

    if (cartResult.status === "fulfilled") {
      setCartCount(cartResult.value.items.reduce((sum, item) => sum + item.quantity, 0));
    } else {
      setCartCount(0);
    }

    if (meResult.status === "fulfilled") {
      setIsAuthenticated(true);
    } else {
      if (meResult.reason instanceof ApiError && meResult.reason.status === 401) {
        setIsAuthenticated(false);
        setWishlistCount(0);
        clearSession();
      }
      return;
    }

    setWishlistCount(wishlistResult.status === "fulfilled" ? wishlistResult.value.items.length : 0);
  }, []);

  useEffect(() => {
    const initialRefresh = window.setTimeout(() => {
      void refresh();
    }, 0);
    const unsubscribe = onSessionChange(() => {
      void refresh();
    });
    return () => {
      window.clearTimeout(initialRefresh);
      unsubscribe();
    };
  }, [refresh]);

  useEffect(() => {
    const closeMenu = (event: MouseEvent) => {
      if (!accountMenuRef.current?.contains(event.target as Node)) setAccountMenuOpen(false);
    };
    document.addEventListener("mousedown", closeMenu);
    return () => document.removeEventListener("mousedown", closeMenu);
  }, []);

  const handleLogout = () => {
    const refreshToken = getRefreshToken();
    // Start revocation while the access token is still available, but update
    // the interface immediately instead of awaiting the network round-trip.
    const revocation = refreshToken ? logout(refreshToken) : Promise.resolve();
    clearSession();
    setIsAuthenticated(false);
    setWishlistCount(0);
    void revocation.catch(() => {
      // Best effort only; the local session is already cleared.
    });
  };

  return (
    <div className="right-header flex h-10 items-center gap-4 sm:gap-5 md:gap-6">
      <div className="icon group relative hidden md:block">
        <button type="button" className="flex h-10 w-5 items-center justify-center text-ivy-dark" aria-label="Hỗ trợ khách hàng">
          <HeadphonesIcon className="size-4.5" />
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

      <Link href="/yeu-thich" className="icon relative flex h-10 w-5 items-center justify-center text-ivy-dark" aria-label="Yêu thích">
        <HeartIcon className="size-4.5" />
        {wishlistCount > 0 ? (
          <span className="absolute -right-1 top-0.5 flex size-4 items-center justify-center rounded-full bg-ivy-accent text-[10px] text-ivy-dark">
            {wishlistCount > 9 ? "9+" : wishlistCount}
          </span>
        ) : null}
      </Link>

      <NotificationBell />

      <div ref={accountMenuRef} className="relative">
        {isAuthenticated ? (
          <button type="button" onClick={() => setAccountMenuOpen((open) => !open)} className="icon flex h-10 items-center justify-center gap-1 text-ivy-dark" aria-label="Tài khoản" aria-expanded={accountMenuOpen}>
            <AvatarIcon className="size-4.5" />
            <ChevronDown className={`size-3 transition-transform ${accountMenuOpen ? "rotate-180" : ""}`} />
          </button>
        ) : (
          <Link href="/dang-nhap" className="icon flex h-10 w-5 items-center justify-center text-ivy-dark" aria-label="Tài khoản"><AvatarIcon className="size-4.5" /></Link>
        )}
        {isAuthenticated && accountMenuOpen ? (
          <div className="absolute right-0 top-[calc(100%+10px)] z-[70] w-64 overflow-hidden rounded-2xl border border-[#e8e4df] bg-white p-2 shadow-[0_18px_50px_rgba(34,31,32,0.16)]">
            <p className="px-3 pb-2 pt-2 text-[11px] font-semibold uppercase tracking-[0.18em] text-ivy-text-muted">Tài khoản của tôi</p>
            {[
              ["Thông tin", "profile", UserRound], ["Địa chỉ", "addresses", MapPin],
              ["Đơn hàng", "orders", Package], ["Đặt lại mật khẩu", "password", KeyRound],
            ].map(([label, tab, Icon]) => (
              <Link key={tab as string} href={`/tai-khoan?tab=${tab}`} onClick={() => setAccountMenuOpen(false)} className="flex items-center gap-3 rounded-xl px-3 py-3 text-[14px] text-ivy-dark transition hover:bg-[#f6f3ef]">
                <Icon className="size-4 text-[#9b6b45]" /><span>{label as string}</span>
              </Link>
            ))}
          </div>
        ) : null}
      </div>

      <Link href="/gio-hang" className="icon relative flex h-10 w-5 items-center justify-center text-ivy-dark" aria-label="Giỏ hàng">
        <ShoppingBagIcon className="size-4.5" />
        <span className="absolute -right-1 top-0.5 flex size-4 items-center justify-center rounded-full bg-ivy-accent text-[10px] text-ivy-dark">
          {cartCount > 9 ? "9+" : cartCount}
        </span>
      </Link>

      {isAuthenticated ? (
        <button type="button" onClick={handleLogout} className="hidden text-[12px] font-medium uppercase tracking-[0.08em] text-ivy-text lg:block">
          Đăng xuất
        </button>
      ) : null}
    </div>
  );
}

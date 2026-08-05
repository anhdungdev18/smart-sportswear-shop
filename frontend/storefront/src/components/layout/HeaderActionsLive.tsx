"use client";

import { useCallback, useEffect, useState } from "react";
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
import { getMe, logout } from "@/modules/auth/api";
import { getWishlist } from "@/modules/account/api";
import { getCart } from "@/modules/cart/api";
import { NotificationBell } from "@/modules/notifications/NotificationBell";
import { CUSTOMER_SERVICE_LINKS } from "@/modules/content/data/layout";

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

  const refresh = useCallback(async () => {
    if (!getAccessToken()) {
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
      setIsAuthenticated(false);
      setWishlistCount(0);
      clearSession();
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
    <div className="right-header flex h-10 items-center gap-6">
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

      <Link href={isAuthenticated ? "/tai-khoan" : "/dang-nhap"} className="icon flex h-10 w-5 items-center justify-center text-ivy-dark" aria-label="Tài khoản">
        <AvatarIcon className="size-4.5" />
      </Link>

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

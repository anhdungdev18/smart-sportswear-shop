"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { getAccessToken, getRefreshToken, clearSession, onSessionChange } from "@/lib/session";
import { getMe, logout } from "@/modules/auth/api";
import type { AuthUser } from "@/modules/auth/types";
import { getWishlist } from "@/modules/account/api";
import { getCart } from "@/modules/cart/api";

type StorefrontContextValue = {
  user: AuthUser | null;
  cartCount: number;
  wishlistCount: number;
  loading: boolean;
  refreshSession: () => Promise<void>;
  refreshCartCount: () => Promise<void>;
  refreshWishlistCount: () => Promise<void>;
  signOut: () => Promise<void>;
};

const StorefrontContext = createContext<StorefrontContextValue | null>(null);

export function StorefrontProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [cartCount, setCartCount] = useState(0);
  const [wishlistCount, setWishlistCount] = useState(0);
  const [loading, setLoading] = useState(true);

  const refreshCartCount = useCallback(async () => {
    try {
      const cart = await getCart();
      setCartCount(cart.items.reduce((sum, item) => sum + item.quantity, 0));
    } catch {
      setCartCount(0);
    }
  }, []);

  const refreshWishlistCount = useCallback(async () => {
    if (!getAccessToken()) {
      setWishlistCount(0);
      return;
    }
    try {
      const wishlist = await getWishlist();
      setWishlistCount(wishlist.items.length);
    } catch {
      setWishlistCount(0);
    }
  }, []);

  const refreshSession = useCallback(async () => {
    setLoading(true);
    try {
      if (!getAccessToken()) {
        setUser(null);
        await refreshCartCount();
        setWishlistCount(0);
        return;
      }

      const [me] = await Promise.all([getMe(), refreshCartCount(), refreshWishlistCount()]);
      setUser(me);
    } catch {
      setUser(null);
      clearSession();
      await refreshCartCount();
      setWishlistCount(0);
    } finally {
      setLoading(false);
    }
  }, [refreshCartCount, refreshWishlistCount]);

  const signOut = useCallback(async () => {
    const refreshToken = getRefreshToken();
    try {
      if (refreshToken) {
        await logout(refreshToken);
      }
    } catch {
      // Ignore logout network errors; local session must still be cleared.
    } finally {
      clearSession();
      setUser(null);
      setWishlistCount(0);
      await refreshCartCount();
    }
  }, [refreshCartCount]);

  useEffect(() => {
    void refreshSession();
    const unsubscribe = onSessionChange(() => {
      void refreshSession();
    });
    return unsubscribe;
  }, [refreshSession]);

  const value = useMemo<StorefrontContextValue>(
    () => ({
      user,
      cartCount,
      wishlistCount,
      loading,
      refreshSession,
      refreshCartCount,
      refreshWishlistCount,
      signOut,
    }),
    [user, cartCount, wishlistCount, loading, refreshSession, refreshCartCount, refreshWishlistCount, signOut],
  );

  return <StorefrontContext.Provider value={value}>{children}</StorefrontContext.Provider>;
}

export function useStorefront() {
  const context = useContext(StorefrontContext);
  if (!context) {
    throw new Error("useStorefront must be used within StorefrontProvider");
  }
  return context;
}

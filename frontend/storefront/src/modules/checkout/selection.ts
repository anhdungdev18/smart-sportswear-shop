const KEY = "checkout-cart-item-ids";
const BUY_NOW_KEY = "checkout-buy-now";

export interface BuyNowSelection {
  variantId: string;
  quantity: number;
}

export function saveCheckoutSelection(ids: string[]) {
  if (typeof window !== "undefined") {
    sessionStorage.removeItem(BUY_NOW_KEY);
    sessionStorage.setItem(KEY, JSON.stringify(ids));
  }
}

export function saveBuyNowSelection(selection: BuyNowSelection) {
  if (typeof window !== "undefined") {
    sessionStorage.removeItem(KEY);
    sessionStorage.setItem(BUY_NOW_KEY, JSON.stringify(selection));
  }
}

export function loadBuyNowSelection(): BuyNowSelection | null {
  if (typeof window === "undefined") return null;
  try {
    const value = JSON.parse(sessionStorage.getItem(BUY_NOW_KEY) || "null");
    return value && typeof value.variantId === "string" && Number.isInteger(value.quantity) && value.quantity > 0
      ? value
      : null;
  } catch {
    return null;
  }
}

export function loadCheckoutSelection(): string[] {
  if (typeof window === "undefined") return [];
  try {
    const value = JSON.parse(sessionStorage.getItem(KEY) || "[]");
    return Array.isArray(value) ? value.filter((id): id is string => typeof id === "string") : [];
  } catch {
    return [];
  }
}

export function clearCheckoutSelection() {
  if (typeof window !== "undefined") {
    sessionStorage.removeItem(KEY);
    sessionStorage.removeItem(BUY_NOW_KEY);
  }
}

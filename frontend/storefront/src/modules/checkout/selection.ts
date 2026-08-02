const KEY = "checkout-cart-item-ids";

export function saveCheckoutSelection(ids: string[]) {
  if (typeof window !== "undefined") sessionStorage.setItem(KEY, JSON.stringify(ids));
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
  if (typeof window !== "undefined") sessionStorage.removeItem(KEY);
}

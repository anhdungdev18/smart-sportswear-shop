import type { Product } from "@/types/ivy";
import {
  NEW_ARRIVAL_WOMEN,
  FEATURED_CLASSY,
  SALE_WOMEN,
} from "@/data/homeProducts";

// Real "Thời Trang Nữ" category grid content, reusing already-downloaded
// product assets. Mirrors the ribbon pattern observed on the live category
// page: the top discounted item is tagged "Best Seller", non-discounted
// items are tagged "NEW".
export const CATEGORY_NU_PRODUCTS: Product[] = [
  { ...SALE_WOMEN[0], ribbon: "bestseller" },
  ...SALE_WOMEN.slice(1).map((p) => ({ ...p, ribbon: "new" as const })),
  ...NEW_ARRIVAL_WOMEN.map((p) => ({ ...p, ribbon: "new" as const })),
  ...FEATURED_CLASSY,
];

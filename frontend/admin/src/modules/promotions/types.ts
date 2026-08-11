export type PromotionStatus = "ACTIVE" | "INACTIVE" | "DRAFT" | "EXPIRED";

export type PromotionResponse = {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  discountPercent: number;
  startsAt: string | null;
  endsAt: string | null;
  status: PromotionStatus;
  live: boolean;
  productCount: number;
  productIds: string[];
};

export type PromotionInput = {
  name: string;
  description?: string | null;
  discountPercent: number;
  startsAt?: string | null;
  endsAt?: string | null;
  productIds: string[];
};

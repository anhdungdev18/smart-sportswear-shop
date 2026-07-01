export type AdminReviewResponse = {
  id: string;
  productId: string;
  userId: string;
  reviewerName: string;
  rating: number;
  title: string;
  content: string;
  status: string;
  verifiedPurchase: boolean;
  createdAt: string;
};

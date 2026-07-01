export type BannerItemResponse = {
  id: string;
  bannerId: string;
  title: string | null;
  subtitle: string | null;
  imageUrl: string;
  targetUrl: string | null;
  productId: string | null;
  sortOrder: number;
  isActive: boolean;
};

export type BannerResponse = {
  id: string;
  name: string;
  code: string;
  placement: string;
  status: string;
  startsAt: string | null;
  endsAt: string | null;
  items: BannerItemResponse[];
  createdAt: string;
  updatedAt: string;
};

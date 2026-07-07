import { CategoryListingPage } from "@/modules/category/pages/CategoryListingPage";

type CategorySearchParams = {
  page?: string;
  sort?: string;
  sortBy?: string;
  sortOrder?: string;
  size?: string;
  color?: string;
  minPrice?: string;
  maxPrice?: string;
};

export default async function CategoryPage({
  params,
  searchParams,
}: {
  params: Promise<{ slug: string }>;
  searchParams: Promise<CategorySearchParams>;
}) {
  return <CategoryListingPage params={params} searchParams={searchParams} />;
}

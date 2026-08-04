import { SearchResultsPage } from "@/modules/category/pages/SearchResultsPage";

export default async function SearchPage({
  searchParams,
}: {
  searchParams: Promise<{ q?: string; page?: string; discount?: string }>;
}) {
  return <SearchResultsPage searchParams={searchParams} />;
}

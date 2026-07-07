import { ProductDetailScreen } from "@/modules/product/pages/ProductDetailScreen";

export default async function ProductDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  return <ProductDetailScreen params={params} />;
}

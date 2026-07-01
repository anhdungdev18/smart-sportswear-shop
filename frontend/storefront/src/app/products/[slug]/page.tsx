import Link from "next/link";
import { notFound } from "next/navigation";
import { featuredProducts, getProductBySlug } from "@/lib/mock-catalog";

type Props = {
  params: Promise<{ slug: string }>;
};

export function generateStaticParams() {
  return featuredProducts.map((item) => ({ slug: item.slug }));
}

export default async function ProductPage({ params }: Props) {
  const { slug } = await params;
  const product = getProductBySlug(slug);

  if (!product) {
    notFound();
  }

  return (
    <main className="content-page">
      <div className="shell product-page-grid">
        <img className="product-page-image" src={product.image} alt={product.name} />

        <article className="product-page-card">
          <p className="eyebrow">{product.collection}</p>
          <h1>{product.name}</h1>
          <div className="product-price">
            <strong>{product.price}</strong>
            {product.compareAtPrice ? <span>{product.compareAtPrice}</span> : null}
          </div>
          <div className="product-meta">
            <p>{product.description}</p>
            <p>
              Trang chi tiết này đang dùng mock data, nhưng bố cục đã theo kiểu
              PDP premium để sau này map sang API product detail.
            </p>
            <div className="product-meta-list">
              {product.materials.map((material) => (
                <span key={material}>{material}</span>
              ))}
            </div>
          </div>
          <div className="hero-actions">
            <Link href="/search" className="button-primary">
              Xem toàn bộ sản phẩm
            </Link>
            <Link href="/collections/summer-motion" className="button-secondary">
              Về bộ sưu tập
            </Link>
          </div>
        </article>
      </div>
    </main>
  );
}

import Link from "next/link";
import type { ProductSummary } from "@/lib/mock-catalog";

export function ProductCard({ product }: { product: ProductSummary }) {
  return (
    <Link href={`/products/${product.slug}`} className="product-card">
      <img className="product-image" src={product.image} alt={product.name} />
      <div className="product-body">
        {product.badges?.length ? (
          <div className="product-badges">
            {product.badges.map((badge) => (
              <span key={badge}>{badge}</span>
            ))}
          </div>
        ) : null}
        <span className="product-label">{product.category}</span>
        <h3>{product.name}</h3>
        <p>{product.description}</p>
        <div className="product-price">
          <strong>{product.price}</strong>
          {product.compareAtPrice ? <span>{product.compareAtPrice}</span> : null}
        </div>
      </div>
    </Link>
  );
}

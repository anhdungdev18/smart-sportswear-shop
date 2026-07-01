import Link from "next/link";
import { ProductCard } from "@/components/storefront/ProductCard";
import { collections, featuredProducts } from "@/lib/mock-catalog";

const productGroups = [
  "Mới lên kệ",
  "Best Seller",
  "Giày hiệu năng",
  "Phụ kiện đồng bộ",
];

export default function ProductsPage() {
  return (
    <main className="content-page">
      <section className="shell catalog-hero">
        <div className="catalog-copy">
          <p className="eyebrow">Catalogue</p>
          <h1>Toàn bộ sản phẩm</h1>
        </div>

        <div className="catalog-capsules">
          {productGroups.map((group) => (
            <span key={group}>{group}</span>
          ))}
        </div>
      </section>

      <section className="shell catalog-feature-grid">
        {collections.map((collection) => (
          <Link
            key={collection.slug}
            href={`/collections/${collection.slug}`}
            className="catalog-feature-card"
            style={{
              backgroundImage: `linear-gradient(180deg, rgba(0,0,0,0.12), rgba(0,0,0,0.48)), url(${collection.cover})`,
            }}
          >
            <p className="eyebrow">{collection.kicker}</p>
            <strong>{collection.name}</strong>
          </Link>
        ))}
      </section>

      <section className="shell editorial-split">
        <div className="editorial-split-copy">
          <p className="eyebrow">Editorial</p>
          <h2>Sport Fashion Edit</h2>
          <Link href="/lookbook" className="text-link">Xem lookbook</Link>
        </div>

        <img
          src="https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=1200&q=80"
          alt="Catalogue editorial"
        />
      </section>

      <section className="section">
        <div className="shell">
          <div className="section-heading">
            <p className="eyebrow">Product Grid</p>
            <h2>Danh mục biên tập</h2>
          </div>

          <div className="products-grid">
            {featuredProducts.map((product) => (
              <ProductCard key={product.slug} product={product} />
            ))}
          </div>
        </div>
      </section>
    </main>
  );
}

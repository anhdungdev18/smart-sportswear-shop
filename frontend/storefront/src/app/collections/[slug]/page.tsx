import Link from "next/link";
import { notFound } from "next/navigation";
import { ProductCard } from "@/components/storefront/ProductCard";
import { collections, getCollectionBySlug, getProductsForCollection } from "@/lib/mock-catalog";

type Props = {
  params: Promise<{ slug: string }>;
};

export function generateStaticParams() {
  return [
    ...collections.map((item) => ({ slug: item.slug })),
    { slug: "apparel-edit" },
    { slug: "footwear-lab" },
    { slug: "accessory-atelier" },
  ];
}

export default async function CollectionPage({ params }: Props) {
  const { slug } = await params;
  const fallbackName = slug === "apparel-edit" ? "Apparel Edit" : slug === "footwear-lab" ? "Footwear Lab" : slug === "accessory-atelier" ? "Accessory Atelier" : null;
  const collection = getCollectionBySlug(slug) ?? (fallbackName ? { slug, name: fallbackName, kicker: "Danh mục", description: "Landing page dạng collection để sau này nối sản phẩm thật từ backend.", cover: "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?auto=format&fit=crop&w=1200&q=80" } : null);
  if (!collection) notFound();
  const products = getProductsForCollection(slug);

  return (
    <main className="content-page">
      <div className="shell content-grid">
        <img className="content-hero-image" src={collection.cover} alt={collection.name} />
        <article className="product-page-card">
          <p className="eyebrow">{collection.kicker}</p>
          <h1>{collection.name}</h1>
          <p>{collection.description}</p>
          <div className="hero-actions">
            <Link href="/products" className="button-primary">Vào catalogue</Link>
            <Link href="/lookbook" className="button-secondary">Xem lookbook</Link>
          </div>
        </article>
      </div>

      <section className="shell editorial-split collection-story-block">
        <div className="editorial-split-copy">
          <p className="eyebrow">Collection Story</p>
          <h2>Seasonal Edit</h2>
          <Link href="/about" className="text-link">Xem thương hiệu</Link>
        </div>
        <img src="https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=1200&q=80" alt="Editorial collection" />
      </section>

      <section className="section">
        <div className="shell">
          <div className="section-heading">
            <p className="eyebrow">Product Selection</p>
            <h2>Sản phẩm trong bộ này</h2>
          </div>
          <div className="products-grid">
            {products.map((product) => (<ProductCard key={`${slug}-${product.slug}`} product={product} />))}
          </div>
        </div>
      </section>
    </main>
  );
}

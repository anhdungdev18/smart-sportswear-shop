import Link from "next/link";
import { featuredProducts } from "@/lib/mock-catalog";

export default function SearchPage() {
  return (
    <main className="content-page">
      <div className="shell">
        <article className="search-panel">
          <p className="eyebrow">Catalog Preview</p>
          <h1>Danh sách sản phẩm mock</h1>
          <p>
            Tạm thời chưa nối API. Trang này đóng vai trò catalog nhẹ để kiểm tra
            cảm giác typography, spacing và card layout.
          </p>
          <ul>
            {featuredProducts.map((product) => (
              <li key={product.slug}>
                <Link href={`/products/${product.slug}`} className="text-link">
                  {product.name} · {product.price}
                </Link>
              </li>
            ))}
          </ul>
        </article>
      </div>
    </main>
  );
}

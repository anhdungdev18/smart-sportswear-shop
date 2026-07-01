import { lookbooks } from "@/lib/mock-catalog";

export default function LookbookPage() {
  return (
    <main className="content-page">
      <div className="shell">
        <div className="section-heading">
          <p className="eyebrow">Lookbook Archive</p>
          <h2>Không gian kể chuyện bằng hình ảnh</h2>
          <p>
            Đây là kiểu trang hợp với định hướng bạn muốn: website bán hàng nhưng
            vẫn có nhịp trình bày như một thương hiệu thời trang.
          </p>
        </div>
        <div className="lookbook-grid">
          {lookbooks.map((item) => (
            <article key={item.slug} className="lookbook-card">
              <img src={item.image} alt={item.title} />
              <div>
                <p className="eyebrow">{item.kicker}</p>
                <h3>{item.title}</h3>
                <p>{item.description}</p>
              </div>
            </article>
          ))}
        </div>
      </div>
    </main>
  );
}

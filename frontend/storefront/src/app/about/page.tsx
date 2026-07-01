export default function AboutPage() {
  return (
    <main className="content-page">
      <div className="shell content-grid">
        <img
          className="content-hero-image"
          src="https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=1200&q=80"
          alt="Tinh thần thương hiệu thể thao cao cấp"
        />
        <article className="product-page-card">
          <p className="eyebrow">Brand Direction</p>
          <h1>Thể thao, nhưng được trình bày như một thương hiệu thời trang</h1>
          <p>
            Hướng storefront này phù hợp với việc bạn bán cả quần áo, giày và phụ kiện,
            đồng thời muốn mỗi bộ sưu tập có landing page riêng như BST mùa hè,
            BST sân cỏ hoặc BST studio.
          </p>
          <p>
            Kiến trúc này không chôn website vào một layout marketplace phổ thông.
            Nó tạo chỗ cho storytelling, campaign và các đợt ra mắt sản phẩm sau này.
          </p>
        </article>
      </div>
    </main>
  );
}

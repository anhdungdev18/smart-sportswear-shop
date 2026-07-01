import Link from "next/link";

export function SiteFooter() {
  return (
    <footer className="footer-shell">
      <div className="shell">
        <div className="footer-grid">
          <div className="footer-brand">
            <strong>Sporta Atelier</strong>
            <p>
              Nền storefront mới cho web bán trang phục thể thao, giày và phụ kiện
              theo ngôn ngữ giao diện fashion-commerce cao cấp.
            </p>
          </div>

          <div>
            <h3>Danh mục</h3>
            <Link href="/collections/apparel-edit">Trang phục</Link>
            <Link href="/collections/footwear-lab">Giày</Link>
            <Link href="/collections/accessory-atelier">Phụ kiện</Link>
          </div>

          <div>
            <h3>Nội dung</h3>
            <Link href="/collections/summer-motion">Bộ sưu tập</Link>
            <Link href="/lookbook">Lookbook</Link>
            <Link href="/about">Câu chuyện thương hiệu</Link>
          </div>

          <div>
            <h3>Triển khai</h3>
            <p>Giai đoạn này dùng mock data.</p>
            <p>Bước tiếp theo là nối API sản phẩm, review, cart và checkout.</p>
          </div>
        </div>

        <div className="footer-bottom">
          <span>© 2026 Sporta Atelier. Premium storefront prototype.</span>
          <span>Typography lớn, khoảng trắng rộng, cấu trúc sẵn cho e-commerce.</span>
        </div>
      </div>
    </footer>
  );
}

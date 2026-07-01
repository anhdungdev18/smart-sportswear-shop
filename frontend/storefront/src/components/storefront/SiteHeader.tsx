import Link from "next/link";

type MenuGroup = {
  title: string;
  items: { href: string; label: string }[];
};

type MegaMenuItem = {
  href: string;
  label: string;
  featured?: boolean;
  groups?: MenuGroup[];
};

const megaMenu: MegaMenuItem[] = [
  {
    href: "/products?department=nu",
    label: "Nữ",
    groups: [
      { title: "Áo", items: [{ href: "/collections/apparel-edit", label: "Áo tập" }, { href: "/collections/summer-motion", label: "Áo khoác nhẹ" }, { href: "/products", label: "Bra thể thao" }] },
      { title: "Quần", items: [{ href: "/products", label: "Legging" }, { href: "/products", label: "Short training" }, { href: "/products", label: "Jogger" }] },
      { title: "Bộ sưu tập", items: [{ href: "/collections/summer-motion", label: "Summer Motion" }, { href: "/lookbook", label: "Studio Balance" }, { href: "/lookbook", label: "City Training" }] },
    ],
  },
  {
    href: "/products?department=nam",
    label: "Nam",
    groups: [
      { title: "Trang phục", items: [{ href: "/collections/apparel-edit", label: "Áo performance" }, { href: "/products", label: "Áo thun active" }, { href: "/products", label: "Set tập luyện" }] },
      { title: "Match Day", items: [{ href: "/products", label: "Áo sân cỏ" }, { href: "/products", label: "Quần thi đấu" }, { href: "/products", label: "Jacket di chuyển" }] },
      { title: "Phụ trợ", items: [{ href: "/collections/footwear-lab", label: "Giày hiệu năng" }, { href: "/collections/accessory-atelier", label: "Phụ kiện đồng bộ" }, { href: "/lookbook", label: "Court Energy" }] },
    ],
  },
  {
    href: "/collections/footwear-lab",
    label: "Giày",
    featured: true,
    groups: [
      { title: "Theo nhu cầu", items: [{ href: "/collections/footwear-lab", label: "Futsal" }, { href: "/products", label: "Running" }, { href: "/products", label: "Lifestyle" }] },
      { title: "Theo form", items: [{ href: "/products", label: "Đế thấp" }, { href: "/products", label: "Đệm dày" }, { href: "/products", label: "Upper knit" }] },
    ],
  },
  {
    href: "/collections/accessory-atelier",
    label: "Phụ kiện",
    groups: [
      { title: "Túi & Mũ", items: [{ href: "/collections/accessory-atelier", label: "Túi thể thao" }, { href: "/products", label: "Mũ lưỡi trai" }, { href: "/products", label: "Túi đeo chéo" }] },
      { title: "Hoàn thiện outfit", items: [{ href: "/products", label: "Tất thể thao" }, { href: "/products", label: "Băng cổ tay" }, { href: "/products", label: "Bình nước" }] },
    ],
  },
  {
    href: "/collections/summer-motion",
    label: "Bộ sưu tập",
    groups: [
      { title: "Mùa hiện tại", items: [{ href: "/collections/summer-motion", label: "Summer Motion" }, { href: "/lookbook", label: "Studio Balance" }, { href: "/lookbook", label: "City Training" }] },
      { title: "Biên tập", items: [{ href: "/products", label: "New Arrival" }, { href: "/products", label: "Best Seller" }, { href: "/products", label: "Phong cách sân cỏ" }] },
    ],
  },
];

const utilityLinks = [
  { href: "/lookbook", label: "Lookbook" },
  { href: "/about", label: "Câu chuyện" },
  { href: "/search", label: "Tìm kiếm" },
];

export function SiteHeader() {
  return (
    <>
      <div className="site-topbar">
        <div className="shell">
          <span>Miễn phí đổi size trong 14 ngày</span>
          <span>Thiết kế storefront cao cấp trước, nối API sau</span>
          <span>Hotline tư vấn: 1900 26 26</span>
        </div>
      </div>

      <header className="site-header">
        <div className="shell header-row">
          <Link href="/" className="brand-lockup" aria-label="Trang chủ Sporta Atelier">
            <span>Premium Sportswear House</span>
            <strong>Sporta <em>Atelier</em></strong>
          </Link>

          <div className="header-actions">
            {utilityLinks.map((item) => (
              <Link key={item.href} href={item.href} className="header-action-link">
                {item.label}
              </Link>
            ))}
          </div>

          <details className="mobile-menu">
            <summary>Menu</summary>
            <div className="mobile-menu-panel">
              {megaMenu.map((entry) => (
                <details key={entry.label}>
                  <summary>{entry.label}</summary>
                  <div>
                    {entry.groups?.map((group) => (
                      <section key={group.title}>
                        <strong>{group.title}</strong>
                        {group.items.map((item) => (
                          <Link key={item.href + item.label} href={item.href}>{item.label}</Link>
                        ))}
                      </section>
                    ))}
                  </div>
                </details>
              ))}
            </div>
          </details>
        </div>

        <div className="shell nav-shell">
          <nav className="mega-nav" aria-label="Danh mục chính">
            {megaMenu.map((entry) => (
              <div key={entry.label} className="mega-nav-item">
                <Link href={entry.href} className={entry.featured ? "featured-link" : undefined}>{entry.label}</Link>
                {entry.groups ? (
                  <div className="mega-panel">
                    {entry.groups.map((group) => (
                      <div key={group.title} className="mega-group">
                        <p>{group.title}</p>
                        {group.items.map((item) => (
                          <Link key={item.href + item.label} href={item.href}>{item.label}</Link>
                        ))}
                      </div>
                    ))}
                  </div>
                ) : null}
              </div>
            ))}
          </nav>
        </div>
      </header>
    </>
  );
}

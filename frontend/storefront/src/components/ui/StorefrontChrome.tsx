"use client";

import {
  CaretDown,
  List,
  MagnifyingGlass,
  MapPin,
  Phone,
  ShoppingBag,
  UserCircle,
  X
} from "@phosphor-icons/react";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { useCart } from "@/modules/cart/CartContext";
import { getLocalizedProductName, getLocalizedProductTag, mockShoeImage, type Product, products } from "@/modules/catalog/products";
import { languageCookieName, siteCopy, type Language } from "@/modules/i18n";

const fallbackProductImage = mockShoeImage;
type NavKey = keyof typeof siteCopy.vi.nav;
type NavItem = {
  labelKey: NavKey;
  href: string;
  hot?: boolean;
  children?: string[][];
};

function slugify(value: string) {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/đ/g, "d")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "");
}

const navItems: NavItem[] = [
  { labelKey: "home", href: "/" },
  { labelKey: "products", href: "/products" },
  {
    labelKey: "turf",
    href: "/collections/giay-co-nhan-tao",
    children: [
      ["Nike", "Mercurial", "Tiempo", "Phantom"],
      ["Adidas", "F50", "X", "Predator", "Copa"],
      ["Puma", "Ultra", "Future", "King"],
      ["Mizuno", "Alpha", "Morelia", "Monarcida"],
      ["Joma", "Top Flex", "Cancha"],
      ["Zocker", "Winner Energy", "Inspire"]
    ]
  },
  {
    labelKey: "futsal",
    href: "/collections/giay-da-bong-san-futsal-chinh-hang",
    children: [
      ["Nike", "Lunar Gato", "Street Gato", "Tiempo"],
      ["Adidas", "Top Sala", "Samba"],
      ["Joma", "Top Flex", "Cancha", "Regate", "Mundial"],
      ["Asics", "Senda"]
    ]
  },
  {
    labelKey: "kids",
    href: "/collections/giay-da-bong-tre-em"
  },
  {
    labelKey: "hotSales",
    href: "/collections/hot-sales",
    hot: true,
    children: [["Hot sales cỏ nhân tạo"], ["Dưới 2 triệu"], ["Dưới 1 triệu 5"], ["Puma up to 50%"]]
  },
  {
    labelKey: "accessories",
    href: "/collections/phu-kien",
    children: [["Quần áo bóng đá"], ["Joma", "Hummel"], ["Trái bóng"], ["Balo", "Vớ", "Lót giày"], ["Hỗ trợ và phục hồi"]]
  },
  { labelKey: "blog", href: "/blogs/tin-tuc" },
  { labelKey: "customers", href: "/pages/khach-hang-cua-thanhhung-futsal" },
  { labelKey: "stores", href: "/pages/gioi-thieu" },
  { labelKey: "contact", href: "/pages/lien-he" }
] as const;

const navChildTranslations: Record<string, string> = {
  "Hot sales cỏ nhân tạo": "Artificial grass hot sales",
  "Dưới 2 triệu": "Under 2 million",
  "Dưới 1 triệu 5": "Under 1.5 million",
  "Quần áo bóng đá": "Football kits",
  "Trái bóng": "Balls",
  "Vớ": "Socks",
  "Lót giày": "Insoles",
  "Hỗ trợ và phục hồi": "Support and recovery"
};

function localizeNavChild(label: string, language: Language) {
  return language === "en" ? navChildTranslations[label] ?? label : label;
}

function readCookieLanguage(): Language | null {
  const value = document.cookie
    .split("; ")
    .find((part) => part.startsWith(`${languageCookieName}=`))
    ?.split("=")[1];

  return value === "en" || value === "vi" ? value : null;
}

function persistLanguage(language: Language) {
  window.localStorage.setItem(languageCookieName, language);
  document.cookie = `${languageCookieName}=${language}; path=/; max-age=31536000; SameSite=Lax`;
}

export function StorefrontHeader({ initialLanguage = "vi" }: { initialLanguage?: Language } = {}) {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [announcementOpen, setAnnouncementOpen] = useState(true);
  const [query, setQuery] = useState("");
  const [language, setLanguage] = useState<Language>(initialLanguage);
  const pathname = usePathname();
  const { count } = useCart();

  useEffect(() => {
    const stored = readCookieLanguage() ?? window.localStorage.getItem(languageCookieName);
    if (stored === "en" || stored === "vi") {
      setLanguage(stored);
    }
  }, []);

  function toggleLanguage() {
    const next = language === "vi" ? "en" : "vi";
    persistLanguage(next);
    setLanguage(next);
    window.location.reload();
  }

  function isActive(href: string) {
    if (href === "/") {
      return pathname === "/";
    }

    return pathname === href || pathname.startsWith(`${href}/`);
  }

  const t = siteCopy[language];
  const searchPlaceholder = t.searchPlaceholder;

  return (
    <>
      <header className="site-header">
        {announcementOpen ? (
          <div className="utility-bar">
            <div className="shell utility-inner">
              <span>{t.announcement}</span>
              <button className="utility-close" type="button" aria-label={language === "vi" ? "Đóng thông báo" : "Close announcement"} onClick={() => setAnnouncementOpen(false)}>
                <X size={24} />
              </button>
            </div>
          </div>
        ) : null}

        <div className="main-header">
          <div className="shell main-header-inner">
            <button className="mobile-menu-btn" type="button" aria-label={language === "vi" ? "Mở menu" : "Open menu"} onClick={() => setDrawerOpen(true)}>
              <List size={25} weight="bold" />
            </button>

            <a className="store-logo" href="/" aria-label="Thanh Hùng Futsal">
              <img src="https://cdn.hstatic.net/themes/200000278317/1001484753/14/logo.png?v=132" alt="Thanh Hung Futsal" />
            </a>

            <form
              className={searchOpen ? "header-search active" : "header-search"}
              action="/search"
              onSubmit={(event) => {
                if (!query.trim()) {
                  event.preventDefault();
                }
              }}
            >
              <input
                name="q"
                aria-label={language === "vi" ? "Tìm sản phẩm" : "Search products"}
                placeholder={searchPlaceholder}
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                onFocus={() => setSearchOpen(true)}
                onBlur={() => window.setTimeout(() => setSearchOpen(false), 160)}
              />
              <button className="search-submit" type="submit" aria-label={language === "vi" ? "Tìm kiếm" : "Search"}>
                <MagnifyingGlass size={22} />
              </button>
              {searchOpen ? (
                <div className="search-suggest">
                  <strong>{t.quickSuggestions}</strong>
                  {products.slice(0, 4).map((product) => (
                    <a href={`/products/${product.slug}`} key={product.slug}>
                      <img
                        src={product.image}
                        alt=""
                        onError={(event) => {
                          event.currentTarget.src = fallbackProductImage;
                        }}
                      />
                      <span>{getLocalizedProductName(product, language)}</span>
                      <b>{product.price}</b>
                    </a>
                  ))}
                  <a className="search-all-link" href={`/search?q=${encodeURIComponent(query || "giày")}`}>
                    {t.viewAllResults}
                  </a>
                </div>
              ) : null}
            </form>

            <div className="header-icons">
              <a className="header-account-link" href="/account/login">
                <UserCircle size={25} />
                <span>{t.login}</span>
              </a>
              <a className="header-account-link register-link" href="/account/register">
                {t.register}
              </a>
              <a className="header-icon cart-icon" href="/cart" aria-label={t.cart}>
                <ShoppingBag size={25} />
                <span>{count}</span>
              </a>
              <button className="language-switch" type="button" onClick={toggleLanguage} aria-label={language === "vi" ? "Switch to English" : "Chuyển sang tiếng Việt"}>
                {language === "vi" ? "ENG" : "VIE"}
              </button>
            </div>
          </div>
        </div>

        <nav className="nav-bar" aria-label={language === "vi" ? "Danh mục chính" : "Main navigation"}>
          <div className="shell nav-inner">
            {navItems.map((item) => {
              const active = isActive(item.href);

              return (
                <div className={`${active ? "nav-item active" : "nav-item"} ${item.hot && active ? "hot" : ""}`} key={item.labelKey}>
                  <a href={item.href}>
                    {t.nav[item.labelKey]}
                    {item.children ? <CaretDown size={12} weight="bold" /> : null}
                  </a>
                  {item.children ? (
                    <div className="mega-menu">
                      {item.children.map((group) => (
                        <div className="mega-group" key={group.join("-")}>
                          <strong>{localizeNavChild(group[0], language)}</strong>
                          {group.slice(1).map((child) => (
                            <a href={`/collections/${slugify(child)}`} key={child}>
                              {localizeNavChild(child, language)}
                            </a>
                          ))}
                        </div>
                      ))}
                    </div>
                  ) : null}
                </div>
              );
            })}
          </div>
        </nav>
      </header>

      {drawerOpen ? (
        <div className="drawer-layer open">
          <button className="drawer-backdrop" type="button" aria-label={language === "vi" ? "Đóng menu" : "Close menu"} onClick={() => setDrawerOpen(false)} />
          <aside className="mobile-drawer">
            <div className="drawer-head">
              <img src="https://cdn.hstatic.net/themes/200000278317/1001484753/14/logo.png?v=132" alt="Thanh Hung Futsal" />
              <button type="button" aria-label={language === "vi" ? "Đóng menu" : "Close menu"} onClick={() => setDrawerOpen(false)}>
                <X size={22} weight="bold" />
              </button>
            </div>
            <form className="mobile-search" action="/search">
              <MagnifyingGlass size={18} />
              <input name="q" aria-label={language === "vi" ? "Tìm sản phẩm mobile" : "Mobile product search"} placeholder={searchPlaceholder} />
            </form>
            <div className="mobile-nav">
              {navItems.map((item) => (
                <details className={isActive(item.href) ? "active" : ""} key={item.labelKey}>
                  <summary>
                    <a href={item.href}>{t.nav[item.labelKey]}</a>
                    {item.children ? <span>+</span> : null}
                  </summary>
                  {item.children ? (
                    <div>
                          {item.children.flat().map((child) => (
                        <a href={`/collections/${slugify(child)}`} key={child}>
                          {localizeNavChild(child, language)}
                        </a>
                      ))}
                    </div>
                  ) : null}
                </details>
              ))}
            </div>
          </aside>
        </div>
      ) : null}
    </>
  );
}

export function ProductCard({ product, initialLanguage = "vi" }: { product: Product; initialLanguage?: Language }) {
  const installment = Math.round(Number(product.price.replace(/\D/g, "")) / 3).toLocaleString("vi-VN");
  const [language, setLanguage] = useState<Language>(initialLanguage);

  useEffect(() => {
    const stored = readCookieLanguage() ?? window.localStorage.getItem(languageCookieName);
    if (stored === "en" || stored === "vi") {
      setLanguage(stored);
    }
  }, []);

  const t = siteCopy[language].productCard;
  const productName = getLocalizedProductName(product, language);
  const productTag = getLocalizedProductTag(product, language);

  return (
    <article className="product-card">
      <a href={`/products/${product.slug}`} aria-label={`${t.detail} ${productName}`}>
        <div className="product-image-wrap">
          {product.sale ? <span className="sale-badge">{product.sale}</span> : null}
          <span className="status-badge">{productTag}</span>
          <img
            src={product.image}
            alt={productName}
            loading="lazy"
            onError={(event) => {
              event.currentTarget.src = fallbackProductImage;
            }}
          />
          {product.hoverImage ? (
            <img
              className="product-hover-image"
              src={product.hoverImage}
              alt=""
              loading="lazy"
              onError={(event) => {
                event.currentTarget.style.display = "none";
              }}
            />
          ) : null}
        </div>
        <div className="product-info">
          <h3 className="product-title">{productName}</h3>
          <div className="price-line">
            <span className="price">{product.price}</span>
            {product.oldPrice ? <span className="old-price">{product.oldPrice}</span> : null}
          </div>
          <p className="fundiin">{t.installmentPrefix} {installment}₫ {t.installmentSuffix}</p>
        </div>
      </a>
    </article>
  );
}

export function FloatingActions({ initialLanguage = "vi" }: { initialLanguage?: Language } = {}) {
  const [language, setLanguage] = useState<Language>(initialLanguage);

  useEffect(() => {
    const stored = readCookieLanguage() ?? window.localStorage.getItem(languageCookieName);
    if (stored === "en" || stored === "vi") {
      setLanguage(stored);
    }
  }, []);

  const t = siteCopy[language].floating;

  return (
    <div className="floating-actions" aria-label={t.label}>
      <a className="float-btn call" href="tel:0900000000" aria-label={t.call}>
        <Phone size={21} weight="bold" />
        <span>{t.call}</span>
      </a>
      <a className="float-btn zalo" href="#" aria-label={t.zalo}>
        Z
        <span>{t.zalo}</span>
      </a>
      <a className="float-btn map" href="/pages/lien-he" aria-label={t.map}>
        <MapPin size={21} weight="bold" />
        <span>{t.map}</span>
      </a>
    </div>
  );
}
export function StorefrontFooter({ initialLanguage = "vi" }: { initialLanguage?: Language } = {}) {
  const footerImages = products.slice(0, 6).map((product) => product.image);
  const [language, setLanguage] = useState<Language>(initialLanguage);

  useEffect(() => {
    const stored = readCookieLanguage() ?? window.localStorage.getItem(languageCookieName);
    if (stored === "en" || stored === "vi") {
      setLanguage(stored);
    }
  }, []);

  const t = siteCopy[language].footer;

  return (
    <footer className="footer">
      <div className="shell footer-grid">
        <div>
          <h3>{t.policies}</h3>
          <a href="/pages/chinh-sach-bao-hanh">{t.warranty}</a>
          <a href="/pages/chinh-sach-doi-tra">{t.returns}</a>
          <a href="/pages/giao-nhan-hang">{t.shipping}</a>
          <a href="/pages/bao-mat-thong-tin">{t.privacy}</a>
          <a href="/pages/huong-dan-mua-tra-sau-fundiin">{t.fundiin}</a>
        </div>
        <div>
          <h3>{t.about}</h3>
          <a href="/pages/gioi-thieu">{t.aboutUs}</a>
          <a href="/pages/linh-vuc-kinh-doanh">{t.business}</a>
          <a href="/collections/hot-sales">Hot sale</a>
          <p>{t.description}</p>
        </div>
        <div>
          <h3>FACEBOOK</h3>
          <div className="social-box">{t.facebook}</div>
        </div>
        <div>
          <h3>INSTAGRAM</h3>
          <div className="insta-mini">
            {footerImages.map((image, index) => (
              <img src={image} alt="" key={index} />
            ))}
          </div>
        </div>
      </div>
      <div className="shell footer-bottom">
        <img src="https://cdn.hstatic.net/themes/200000278317/1001484753/14/logo-bct.png?v=132" alt="Bộ Công Thương" />
        <span>{t.copyright}</span>
      </div>
    </footer>
  );
}


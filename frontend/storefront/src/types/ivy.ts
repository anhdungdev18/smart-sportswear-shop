export interface NavSubItemGroup {
  heading: string;
  headingHref: string;
  links: { label: string; href: string }[];
}

export interface NavCategoryMenu {
  label: string;
  href: string;
  variant: "category" | "collection" | "about" | "plain";
  highlight?: boolean;
  quickLinks?: { label: string; href: string; highlight?: boolean }[];
  groups?: NavSubItemGroup[];
}

export interface BannerSlide {
  href: string;
  image: string;
  alt: string;
}

export interface ProductColorSwatch {
  id: string;
  image: string;
  label: string;
  active?: boolean;
}

export interface ProductSize {
  id: string;
  label: string;
  disabled?: boolean;
}

export interface Product {
  id: string;
  name: string;
  href: string;
  image: string;
  hoverImage: string;
  price: number;
  oldPrice?: number;
  discountPercent?: number;
  ribbon?: "new" | "bestseller";
  colors: ProductColorSwatch[];
  sizes: ProductSize[];
}

export interface ProductTab {
  id: string;
  label: string;
  categorySlug: string;
  products: Product[];
}

export interface ProductCarouselSectionData {
  title: string;
  tabs?: ProductTab[];
  products?: Product[];
}

export interface PromoBannerItem {
  href: string;
  image: string;
}

export interface GalleryItem {
  href: string;
  image: string;
}

export interface FooterLinkColumn {
  title: string;
  links: { label: string; href: string }[];
}

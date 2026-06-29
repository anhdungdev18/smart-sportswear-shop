export const storefrontEndpoints = {
  home: "/storefront/home",
  products: "/storefront/products",
  productDetail: (slug: string) => `/storefront/products/${slug}`,
  searchProducts: "/storefront/search/products",
  blogs: "/storefront/blogs",
  blogDetail: (slug: string) => `/storefront/blogs/${slug}`,
  cartQuote: "/storefront/cart/quote",
  checkout: "/storefront/checkout"
} as const;

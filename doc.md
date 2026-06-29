# Tài Liệu Kết Nối FE-BE

## Tổng quan

Repo gồm 2 ứng dụng Next.js 15:

- `frontend/storefront`: giao diện bán hàng cho khách.
- `frontend/admin`: giao diện quản trị sản phẩm, dashboard và tồn kho.

Cả 2 app hiện có thể chạy độc lập bằng mock data. Để chuẩn bị kết nối BE, FE đã có lớp API adapter:

- Storefront: `frontend/storefront/src/modules/api/*`, service tại `frontend/storefront/src/modules/catalog/api.ts`.
- Admin: `frontend/admin/src/modules/api/*`, service tại `frontend/admin/src/modules/analytics/api.ts` và `frontend/admin/src/modules/product-management/api.ts`.

Nếu `NEXT_PUBLIC_API_BASE_URL` chưa được cấu hình, FE tự động dùng mock data hiện có. Khi BE sẵn sàng, chỉ cần thêm biến môi trường này và BE trả đúng contract bên dưới.

## Cách chạy FE

Storefront:

```bash
cd frontend/storefront
npm install
npm run dev
```

Mặc định chạy tại `http://localhost:3000`.

Admin:

```bash
cd frontend/admin
npm install
npm run dev
```

Mặc định chạy tại `http://localhost:3001`.

## Biến môi trường

Tạo `.env.local` trong từng app khi kết nối BE:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

Quy ước:

- Không thêm dấu `/` ở cuối base URL.
- Nếu FE gọi API từ browser sau này, BE cần cấu hình CORS cho `localhost:3000` và `localhost:3001`.
- Admin API nên yêu cầu auth token. Hiện FE chưa có login admin thật, nên token/header sẽ cần bổ sung khi BE có auth.

## Response format chung

FE chấp nhận 2 kiểu response:

```json
{
  "data": {},
  "meta": {},
  "message": "OK"
}
```

Hoặc trả trực tiếp payload:

```json
{}
```

Khuyến nghị BE dùng envelope `data/meta/message` để phục vụ pagination, error và audit sau này.

Error response khuyến nghị:

```json
{
  "message": "Validation failed",
  "errors": {
    "field": ["Reason"]
  }
}
```

## Storefront API

Base endpoint đang khai báo tại `frontend/storefront/src/modules/api/endpoints.ts`.

### `GET /storefront/home`

Dùng cho trang chủ.

Response `data`:

```ts
type StorefrontHomeData = {
  heroSlides: HeroSlide[];
  quickCategories: QuickCategory[];
  popularCategories: readonly [string, string][];
  brandTiles: BrandTile[];
  productTabs: readonly [string, Product[]][];
  blogPosts: BlogPostCard[];
  featuredProducts: Product[];
  hotDeals: Product[];
  hotSale: Product[];
  images: {
    store: string;
    instagram: string[];
    [key: string]: unknown;
  };
};
```

### `GET /storefront/products`

Dùng cho danh sách sản phẩm và sản phẩm gợi ý.

Query hỗ trợ:

```ts
{
  q?: string;
  collection?: string;
  brand?: string | string[];
  size?: string | string[];
  minPrice?: number;
  maxPrice?: number;
  sort?: "manual" | "price-ascending" | "price-descending" | "title-ascending" | "title-descending" | "created-ascending" | "created-descending" | "best-selling";
  page?: number;
  limit?: number;
}
```

Response `data`: `Product[]`.

### `GET /storefront/products/:slug`

Dùng cho trang chi tiết sản phẩm.

Response `data`: `Product | null`.

### `GET /storefront/search/products?q=...`

Dùng cho trang search.

Response `data`: `Product[]`.

### `GET /storefront/blogs`

Dùng cho trang tin tức.

Response `data`: `BlogPost[]`.

### `GET /storefront/blogs/:slug`

Dùng cho chi tiết bài viết.

Response `data`: `BlogPost | null`.

### `POST /storefront/cart/quote`

Chưa được UI gọi thật, nhưng nên có để tính tổng tiền, khuyến mãi và phí ship trên BE.

Request:

```json
{
  "lines": [
    {
      "slug": "nike-zoom-mercurial-vapor-16-pro-tf-vjr-io9814-hong-neon-xanh",
      "size": "42",
      "quantity": 1
    }
  ],
  "couponCode": null,
  "provinceCode": null,
  "districtCode": null
}
```

### `POST /storefront/checkout`

Chưa được UI gọi thật. BE nên tạo order từ cart.

Request tối thiểu:

```json
{
  "customer": {
    "name": "Nguyen Van A",
    "phone": "0900000000",
    "email": "a@example.com"
  },
  "shippingAddress": {
    "addressLine": "123 Nguyen Trai",
    "ward": "Ward",
    "district": "District",
    "province": "HCM"
  },
  "lines": [
    {
      "slug": "product-slug",
      "size": "42",
      "quantity": 1
    }
  ],
  "paymentMethod": "cod",
  "note": ""
}
```

## Product DTO

FE hiện đang dùng `Product` tại `frontend/storefront/src/modules/catalog/products.ts`.

```ts
type Product = {
  slug: string;
  name: string;
  brand: string;
  category: string;
  tag: string;
  price: string;
  oldPrice?: string;
  sale?: string;
  image: string;
  hoverImage?: string;
  gallery: string[];
  description: string;
  sizes: string[];
  unavailableSizes?: string[];
};
```

Lưu ý cho BE:

- `slug` là khóa route public.
- `price` đang là string để giữ UI hiện tại, ví dụ `3,350,000đ`. Về lâu dài nên BE trả thêm `priceValue: number` để FE sort/filter chính xác hơn.
- `sizes` là size có thể hiển thị. `unavailableSizes` là size hết hàng.
- `image`, `hoverImage`, `gallery` cần là URL public.

## Blog DTO

```ts
type BlogPost = {
  slug: string;
  title: string;
  titleEn?: string;
  image: string;
  excerpt: string;
  excerptEn?: string;
  sections: string[];
  sectionsEn?: string[];
};
```

## Storefront cart hiện tại

Cart đang lưu localStorage key `thf-cart`.

```ts
type CartLine = {
  slug: string;
  name: string;
  image: string;
  price: string;
  size?: string;
  quantity: number;
};
```

Khi BE sẵn sàng, nên đổi cart client sang:

- FE chỉ lưu `slug`, `size`, `quantity`.
- BE tính lại giá, tồn kho, khuyến mãi qua `/storefront/cart/quote`.

## Admin API

Base endpoint đang khai báo tại `frontend/admin/src/modules/api/endpoints.ts`.

### `GET /admin/dashboard`

Dùng cho dashboard tổng quan.

Response `data`:

```ts
{
  revenue: RevenuePoint[];
  topProducts: TopProductPoint[];
  stockAlerts: StockAlert[];
}
```

### `GET /admin/analytics/revenue`

Có thể tách riêng nếu dashboard cần lazy load.

Response `data`: `RevenuePoint[]`.

```ts
type RevenuePoint = {
  month: string;
  revenue: number;
  orders: number;
};
```

### `GET /admin/analytics/top-products`

Response `data`: `TopProductPoint[]`.

```ts
type TopProductPoint = {
  name: string;
  sales: number;
};
```

### `GET /admin/inventory/stock-alerts`

Response `data`: `StockAlert[]`.

```ts
type StockAlert = {
  sku: string;
  product: string;
  stock: number;
  forecast: number;
  status: "critical" | "low";
};
```

### `GET /admin/products`

Query hỗ trợ:

```ts
{
  q?: string;
  status?: "active" | "draft" | "low" | "out";
  brand?: string;
  category?: string;
  sort?: "newest" | "best-selling" | "low-stock";
  page?: number;
  limit?: number;
}
```

Response `data`: `AdminProduct[]`.

### `GET /admin/products/stats`

Response `data`:

```ts
type ProductStat = {
  label: string;
  value: string;
  tone: "neutral" | "success" | "warning" | "danger";
};
```

### `GET /admin/products/:sku`

Chưa được UI gọi, nên có để phục vụ trang edit sau này.

Response `data`: `AdminProduct | null`.

## AdminProduct DTO

FE hiện đang dùng type tại `frontend/admin/src/modules/product-management/products.ts`.

```ts
type AdminProduct = {
  sku: string;
  name: string;
  category: string;
  brand: string;
  price: string;
  stock: number;
  sold: number;
  status: "active" | "draft" | "low" | "out";
  image: string;
};
```

Khuyến nghị BE nên mở rộng về sau:

```ts
{
  id: string;
  sku: string;
  slug: string;
  name: string;
  brandId: string;
  categoryId: string;
  priceValue: number;
  compareAtPriceValue?: number;
  stockByVariant: Array<{ size: string; stock: number }>;
  status: "active" | "draft" | "low" | "out";
  images: string[];
  seoTitle?: string;
  seoDescription?: string;
}
```

## Trang FE đang dùng API adapter

Storefront:

- `src/app/page.tsx`: `getStorefrontHomeData`.
- `src/app/search/page.tsx`: `searchStorefrontProducts`.
- `src/app/products/[slug]/page.tsx`: `getStorefrontProduct`, `listStorefrontProducts`, `listStorefrontProductSlugs`.

Admin:

- `src/app/page.tsx`: `getDashboardData`.
- `src/app/products/page.tsx`: `listAdminProducts`, `getAdminProductStats`.
- `src/components/ui/AdminCharts.tsx`: nhận data qua props.

Một số component vẫn dùng mock trực tiếp để giữ UI hiện tại:

- Storefront header gọi `products.slice(0, 4)` cho search suggestion.
- Catalog filter page `AllProductsCatalog` còn build nhiều mock variants trên client.
- Cart vẫn dùng localStorage, chưa gọi BE.

## Việc BE cần làm trước để kết nối nhanh

1. Implement các endpoint GET chính: `/storefront/home`, `/storefront/products`, `/storefront/products/:slug`, `/storefront/search/products`, `/admin/dashboard`, `/admin/products`, `/admin/products/stats`.
2. Trả response theo envelope `{ data, meta, message }`.
3. Đảm bảo URL ảnh là public và có thể load từ Next Image/browser.
4. Thêm CORS cho 2 origin local.
5. Chuẩn hóa tiền tệ: trước mắt trả `price` string đúng UI, về sau bổ sung `priceValue`.
6. Chuẩn hóa auth admin: bearer token hoặc cookie session, sau đó FE sẽ thêm header vào `apiRequest`.
7. Sau khi BE có pagination, FE có thể đọc `meta.total`, `meta.page`, `meta.limit` để thay thế pagination mock.

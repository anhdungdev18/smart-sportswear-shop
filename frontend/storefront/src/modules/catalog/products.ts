export type Product = {
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

const cdn = (path: string) => `https:${path}`;

function svgImage(title: string, accent = "#d9121f", bg = "#f4f4f4") {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 900">
      <defs>
        <linearGradient id="bg" x1="0" x2="1" y1="0" y2="1">
          <stop offset="0" stop-color="${bg}"/>
          <stop offset="1" stop-color="#ffffff"/>
        </linearGradient>
      </defs>
      <rect width="900" height="900" fill="url(#bg)"/>
      <circle cx="160" cy="145" r="70" fill="${accent}" opacity=".12"/>
      <circle cx="760" cy="760" r="120" fill="${accent}" opacity=".08"/>
      <path d="M182 548c91-69 190-112 298-130 68-11 145-42 222-94 23-15 52-7 64 18l55 114c11 23 1 51-22 62-87 40-196 65-325 76-92 8-185 32-277 72-33 14-70-6-78-41l-8-34c-3-18 3-34 16-45z" fill="#111820"/>
      <path d="M280 558c74-44 152-72 235-84 70-10 136-32 197-66l31 64c-74 31-160 52-258 61-72 7-144 24-217 55z" fill="${accent}"/>
      <text x="450" y="742" text-anchor="middle" font-family="Arial, sans-serif" font-size="32" font-weight="700" fill="#0e1c22">${title}</text>
    </svg>
  `;

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
}

export const mockShoeImage = svgImage("GIAY DA BONG", "#d9121f");
export const mockShoeImageBlue = svgImage("GIAY FUTSAL", "#288ad6");
export const mockShoeImageOrange = svgImage("HOT SALE", "#f76b1c");
export const mockAccessoryImage = svgImage("PHU KIEN", "#f89406");

const productImages = [
  cdn("//cdn.hstatic.net/products/200000278317/a-bong-nike-zoom-mercurial-vapor-16-pro-tf-vjr-io9814-hong-neon-xanh-1_6ec28c52f6544bb5913d3081df42361b_large.jpg"),
  cdn("//cdn.hstatic.net/products/200000278317/ay-da-bong-nike-zoom-mercurial-vapor-16-pro-tf-fq8687-446-xanh-trang-1_43e9da77b89a4cdc8bbe9c0033b9fee4_large.jpg"),
  cdn("//cdn.hstatic.net/products/200000278317/al-giay-da-bong-mizuno-alpha-3-pro-as-p1gd266464-hong-canh-sen-trang-1_714c01aa5f6f4606885041ccaf4c12dd_large.jpg"),
  cdn("//cdn.hstatic.net/products/200000278317/tsal-giay-da-bong-mizuno-morelia-sala-elite-tf-q1gb261225-xanh-trang-1_5ec687fffb6b478699b7d55e09ed68c2_large.jpg"),
  cdn("//cdn.hstatic.net/products/200000278317/z7982451354781_962d306292147f9baa344c4619e65151_eb5f6a1abe144747be2437641ab87adb_large.jpg"),
  cdn("//cdn.hstatic.net/products/200000278317/tsal-giay-da-bong-kamito-artista-2-kl-tf-limited-kmtf260730-vang-den-1_587246e530954189b6ee360cc8812063_large.jpg"),
  cdn("//cdn.hstatic.net/products/200000278317/h-hung-futsal-giay-da-bong-kelme-flash-6-0-4-tf-8621zx1406-trang-tim-1_ced5603e598a4ab59ea271fa55902656_large.jpg"),
  cdn("//cdn.hstatic.net/products/200000278317/thanh-hung-futsal-giay-da-bong-nike-gato-ic-ib3566-001-bac-1_c7960bdaccbe4adca66c445547964950_large.jpg"),
  cdn("//cdn.hstatic.net/products/200000278317/g-futsal-giay-da-bong-mizuno-morelia-ii-pro-as-p1gd260650-trang-vang-1_d3621e01384b45adb5e16afa1773ca26_large.jpg"),
  cdn("//product.hstatic.net/200000278317/product/ng-futsal-giay-da-bong-nike-phantom-gx-2-pro-tf-fj2583-400-xanh-bien-1_b7e59f2e57894ea29f3d1a938ebc8eb2_large.jpg")
];

export const products: Product[] = [
  {
    slug: "nike-zoom-mercurial-vapor-16-pro-tf-vjr-io9814-hong-neon-xanh",
    name: "NIKE ZOOM MERCURIAL VAPOR 16 PRO TF VJR - IO9814-640 - HỒNG NEON/XANH",
    brand: "Nike",
    category: "Giày cỏ nhân tạo",
    tag: "Mới ra mắt",
    price: "3,350,000₫",
    sale: "-8%",
    image: productImages[0],
    hoverImage: productImages[1],
    gallery: [productImages[0], productImages[1], productImages[2]],
    description: "Phiên bản Vapor 16 Pro TF có upper mỏng, đế TF bám sân tốt và form ôm chân cho người chơi tốc độ.",
    sizes: ["39", "40", "40.5", "41", "42", "42.5", "43"],
    unavailableSizes: ["39"]
  },
  {
    slug: "nike-zoom-mercurial-vapor-16-pro-tf-fq8687-446-xanh-trang",
    name: "NIKE ZOOM MERCURIAL VAPOR 16 PRO TF - FQ8687-446 - XANH/TRẮNG",
    brand: "Nike",
    category: "Giày cỏ nhân tạo",
    tag: "Giá tốt",
    price: "2,790,000₫",
    oldPrice: "3,042,700₫",
    sale: "-8%",
    image: productImages[1],
    hoverImage: productImages[0],
    gallery: [productImages[1], productImages[0], productImages[2]],
    description: "Giày đá bóng sân cỏ nhân tạo form tốc độ, phù hợp cầu thủ đá cánh và tiền đạo.",
    sizes: ["39", "40", "40.5", "41", "42", "43"]
  },
  {
    slug: "mizuno-alpha-3-pro-as-p1gd266464-trang-hong-canh-sen",
    name: "MIZUNO ALPHA 3 PRO AS - P1GD266464 - TRẮNG/HỒNG CÁNH SEN",
    brand: "Mizuno",
    category: "Giày cỏ nhân tạo",
    tag: "Mới ra mắt",
    price: "2,700,000₫",
    oldPrice: "3,000,000₫",
    sale: "-10%",
    image: productImages[2],
    hoverImage: productImages[3],
    gallery: [productImages[2], productImages[3], productImages[0]],
    description: "Mizuno Alpha 3 Pro AS nhẹ, đế bám sân cỏ nhân tạo và cảm giác bóng gọn.",
    sizes: ["39", "40", "41", "42", "43", "44"]
  },
  {
    slug: "mizuno-morelia-sala-elite-tf-q1gb261225-xanh-trang",
    name: "MIZUNO MORELIA SALA ELITE TF - Q1GB261225 - XANH/TRẮNG",
    brand: "Mizuno",
    category: "Giày futsal",
    tag: "Sắp hết hàng",
    price: "2,970,000₫",
    oldPrice: "3,300,000₫",
    sale: "-10%",
    image: productImages[3],
    hoverImage: productImages[2],
    gallery: [productImages[3], productImages[2], productImages[1]],
    description: "Morelia Sala Elite TF ưu tiên cảm giác bóng mềm và thân giày linh hoạt.",
    sizes: ["39", "40", "40.5", "41", "42", "42.5"]
  },
  {
    slug: "adidas-predator-pro-luoi-ga-lat-tf-hq2254-hong-den",
    name: "ADIDAS PREDATOR PRO LƯỠI GÀ LẬT TF - HQ2254 - HỒNG/ĐEN",
    brand: "Adidas",
    category: "Giày cỏ nhân tạo",
    tag: "Giá tốt",
    price: "3,050,000₫",
    oldPrice: "3,500,000₫",
    sale: "-13%",
    image: productImages[4],
    hoverImage: productImages[9],
    gallery: [productImages[4], productImages[9], productImages[5]],
    description: "Predator Pro lưỡi gà lật cho cảm giác sút bóng chắc và hỗ trợ kiểm soát bóng.",
    sizes: ["39", "40", "41", "42", "43"]
  },
  {
    slug: "kamito-artista-2-kl-tf-limited-kmtf260730-vang-den",
    name: "KAMITO ARTISTA 2 KL TF LIMITED - KMTF260730 - VÀNG/ĐEN",
    brand: "Kamito",
    category: "Giày cỏ nhân tạo",
    tag: "Mới ra mắt",
    price: "1,799,000₫",
    image: productImages[5],
    hoverImage: productImages[6],
    gallery: [productImages[5], productImages[4], productImages[6]],
    description: "Mẫu limited nổi bật, đế TF phù hợp mặt sân cỏ nhân tạo phổ biến tại Việt Nam.",
    sizes: ["38", "39", "40", "41", "42", "43"]
  },
  {
    slug: "kelme-flash-604-tf-8621zx1406-trang-tim",
    name: "KELME FLASH 6.0.4 TF - 8621ZX1406 - TRẮNG/TÍM",
    brand: "Kelme",
    category: "Giày cỏ nhân tạo",
    tag: "Giá tốt",
    price: "2,052,000₫",
    oldPrice: "2,280,000₫",
    sale: "-10%",
    image: productImages[6],
    hoverImage: productImages[5],
    gallery: [productImages[6], productImages[7], productImages[2]],
    description: "Kelme Flash 6.0.4 TF cân bằng giữa độ bền, đệm êm và mức giá dễ tiếp cận.",
    sizes: ["39", "40", "41", "42", "43", "44"]
  },
  {
    slug: "nike-gato-ic-ib3566-001-bac",
    name: "NIKE GATO IC - IB3566-001 - BẠC",
    brand: "Nike",
    category: "Giày futsal",
    tag: "Hot sale",
    price: "1,590,000₫",
    oldPrice: "2,250,000₫",
    sale: "-29%",
    image: productImages[7],
    hoverImage: productImages[8],
    gallery: [productImages[7], productImages[3], productImages[1]],
    description: "Nike Gato IC đế bằng trong nhà, hợp futsal và street football.",
    sizes: ["39", "40", "41", "42", "43"]
  }
];

export const heroSlides = [
  {
    desktop: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_2.webp?v=132"),
    mobile: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_mb_2.webp?v=132"),
    alt: "Giày sân cỏ nhân tạo",
    href: "/collections/giay-co-nhan-tao"
  },
  {
    desktop: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_3.webp?v=132"),
    mobile: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_mb_3.webp?v=132"),
    alt: "Giày sân futsal",
    href: "/collections/giay-da-bong-san-futsal-chinh-hang"
  },
  {
    desktop: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_4.webp?v=132"),
    mobile: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_mb_4.webp?v=132"),
    alt: "Attack Pack",
    href: "/collections/bst-giay-da-bong-nike-attack"
  },
  {
    desktop: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_5.webp?v=132"),
    mobile: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_mb_5.webp?v=132"),
    alt: "Ice Cold Precision Pack",
    href: "/collections/ice-cold-precision"
  },
  {
    desktop: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_8.webp?v=132"),
    mobile: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_mb_8.webp?v=132"),
    alt: "Mizuno Unity Sky Pack",
    href: "/collections/mizuno-unity-sky-pack"
  },
  {
    desktop: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_9.webp?v=132"),
    mobile: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_mb_9.webp?v=132"),
    alt: "Giày futsal Joma",
    href: "/collections/joma-futsal"
  },
  {
    desktop: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_10.webp?v=132"),
    mobile: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_mb_10.webp?v=132"),
    alt: "Trang phục thể thao chính hãng",
    href: "/collections/trang-phuc-the-thao-chinh-hang"
  }
];

export const quickCategories = [
  {
    title: "GIÀY ĐÁ BÓNG SÂN CỎ NHÂN TẠO",
    image: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_1_img.webp?v=132"),
    href: "/collections/giay-co-nhan-tao"
  },
  {
    title: "GIÀY ĐÁ BÓNG SÂN FUTSAL",
    image: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_2_img.webp?v=132"),
    href: "/collections/giay-da-bong-san-futsal-chinh-hang"
  },
  {
    title: "BỘ QUẦN ÁO THI ĐẤU",
    image: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132"),
    href: "/collections/phu-kien"
  },
  {
    title: "TRÁI BÓNG THI ĐẤU",
    image: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132"),
    href: "/collections/phu-kien"
  }
];

export const popularCategories = [
  ["NIKE TIEMPO LIGERA", productImages[0]],
  ["MIZUNO ALPHA 3", productImages[2]],
  ["MERCURIAL VAPOR 16 PRO TF", productImages[1]],
  ["JOMA TOP FLEX", productImages[3]],
  ["SIGNATURE BOOTS", productImages[8]],
  ["ZOCKER", productImages[9]]
] as const;

export const blogPosts = [
  {
    title: "REVIEW F50 HYPERFAST LEAGUE TF 'ROAD TO GLORY': CHÂN ÁI CHO ANH EM CHÂN BÈ ÍT",
    image: cdn("//cdn.hstatic.net/files/200000278317/article/review-giay-da-bong-adidas-f50-hyperfast-league-tf-1_09bf0a2a30ce4203a959f383f43419ff_large.jpg")
  },
  {
    title: "NHỮNG ĐÔI GIÀY ĐÁ BÓNG KHÁC BIỆT TẠI WORLD CUP 2026",
    image: cdn("//cdn.hstatic.net/files/200000278317/article/nhung-doi-giay-da-bong-khac-biet-tai-world-cup-2026-1_24ffcc6bb7f04283a4b7b3c73a486a90_large.jpg")
  },
  {
    title: "CÁCH RONALDO DE LIMA VÀ NIKE MERCURIAL THAY ĐỔI LỊCH SỬ GIÀY ĐÁ BÓNG",
    image: cdn("//cdn.hstatic.net/files/200000278317/article/ronaldo-va-mercurial-da-thay-doi-lich-su-giay-da-bong-1_c9a14e10331046f5af3630be23516a79_large.jpg")
  }
];

export const serviceHighlights = [
  ["CAM KẾT CHÍNH HÃNG", "100% sản phẩm chính hãng, bảo hành theo chính sách hãng."],
  ["GIAO HÀNG TOÀN QUỐC", "Đóng gói kỹ, hỗ trợ giao nhanh nội thành trong ngày."],
  ["ĐỔI SIZE LINH HOẠT", "Hỗ trợ đổi size khi sản phẩm còn tem và chưa qua sử dụng."],
  ["TRẢ GÓP 0%", "Thanh toán linh hoạt qua Fundiin với 3 kỳ thanh toán."]
] as const;

export const brandTiles = [
  {
    title: "THFC x JOMA",
    image: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/home_banner_img_larges.webp?v=132"),
    href: "/collections/joma-futsal"
  },
  {
    title: "NIKE FOOTBALL",
    image: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_4.webp?v=132"),
    href: "/collections/nike"
  },
  {
    title: "MIZUNO SPEED",
    image: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/slideshow_8.webp?v=132"),
    href: "/collections/mizuno"
  }
] as const;

export const productTabs = [
  ["SẢN PHẨM MỚI", products.slice(0, 8)],
  ["BÁN CHẠY", [products[7], products[0], products[3], products[5], products[2], products[4], products[1], products[6]]],
  ["GIÀY FUTSAL", products.filter((product) => product.category.toLowerCase().includes("futsal")).concat(products.slice(0, 6)).slice(0, 8)],
  ["GIÀY CỎ NHÂN TẠO", products.filter((product) => product.category.toLowerCase().includes("nhân tạo")).concat(products).slice(0, 8)]
] as const;

export function getProductBySlug(slug: string) {
  return products.find((product) => product.slug === slug);
}

const productNameEnglishReplacements: Record<string, string> = {
  "HỒNG NEON": "NEON PINK",
  "HỒNG CÁNH SEN": "MAGENTA",
  "HỒNG": "PINK",
  "XANH BIỂN": "OCEAN BLUE",
  "XANH": "BLUE",
  "TRẮNG": "WHITE",
  "ĐEN": "BLACK",
  "VÀNG": "YELLOW",
  "TÍM": "PURPLE",
  "BẠC": "SILVER",
  "LƯỠI GÀ LẬT": "FOLD-OVER TONGUE",
  "TRÁI BÓNG": "BALL",
  "BALO": "BACKPACK",
  "LÓT GIÀY": "INSOLE"
};

const productTagEnglishReplacements: Record<string, string> = {
  "Mới ra mắt": "New arrival",
  "Giá tốt": "Good price",
  "Sắp hết hàng": "Low stock",
  "Hot sale": "Hot sale",
  "Độc quyền": "Exclusive",
  "Hỗ trợ chân": "Foot support",
  "Phục hồi": "Recovery",
  "Bán chạy": "Best seller",
  "Hàng mới": "New arrival"
};

export function getLocalizedProductName(product: Product, language: "vi" | "en") {
  if (language === "vi") {
    return product.name;
  }

  return Object.entries(productNameEnglishReplacements).reduce(
    (name, [source, replacement]) => name.replaceAll(source, replacement),
    product.name
  );
}

export function getLocalizedProductTag(product: Product, language: "vi" | "en") {
  return language === "vi" ? product.tag : productTagEnglishReplacements[product.tag] ?? product.tag;
}

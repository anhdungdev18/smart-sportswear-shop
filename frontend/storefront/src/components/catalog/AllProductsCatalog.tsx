"use client";

import { SlidersHorizontal, X } from "@phosphor-icons/react";
import { useMemo, useState } from "react";
import { ProductCard } from "@/components/ui/StorefrontChrome";
import { type Product, products } from "@/modules/catalog/products";
import { commonPageCopy, type Language } from "@/modules/i18n";

type PriceFilter = {
  label: string;
  min: number;
  max: number;
};

export type CatalogPreset = "all" | "artificial-turf" | "futsal" | "kids" | "hot-sales" | "accessories";

type CatalogConfig = {
  title: string;
  bannerAlt: string;
  bannerUrl: string;
  descriptions: string[];
  sourceProducts: Product[];
  brands: string[];
  categoryLinks: readonly (readonly [string, string])[];
  filterLinkTitle: string;
  productMultiplier: number;
};

const allProductsBanner =
  "https://cdn.hstatic.net/files/200000278317/collection/main-category-banner-2026-tatcasanpham_28f62a2fae2443e2b3ecdef5fa8bb043_master.jpg";

const artificialTurfBanner =
  "https://cdn.hstatic.net/files/200000278317/collection/main-category-banner-2026-giay-co-nhan-tao_4d92bb7d259946a9a62ae4c81083c916_master.jpg";

const futsalBanner =
  "https://cdn.hstatic.net/files/200000278317/collection/main-category-banner-2026-giayic_d63747f6eb184bb0b9e7847ae4e93a32_master.jpg";

const kidsBanner =
  "https://file.hstatic.net/200000278317/collection/main-category-banner-giaytrem_50c2edd6ed414940824ad609d13f9d63.jpg";

const hotSalesBanner =
  "https://file.hstatic.net/200000278317/collection/main-category-banner-sale_ab4b5c3423964961bc18f6d84be464c1_master.jpg";

const accessoriesBanner =
  "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132";

const categoryLinks = [
  ["SUPERFLY 9 - ĐỒNG GIÁ 1690K", "/collections/superfly-9-dong-gia-1690k"],
  ["VAPOR PRO TF", "/collections/giay-da-bong-nike-mercurial-vapor-pro-tf"],
  ["MID-SEASON SALE", "/collections/mid-season-sale-2025"],
  ["Mizuno Ruby Red Pack", "/collections/giay-da-bong-mizuno-ruby-red-pack"],
  ["Nike Mad Energy Pack", "/collections/giay-da-bong-nike-mad-energy-pack"],
  ["adidas Pure Victory Pack", "/collections/giay-da-bong-adidas-pure-victory-pack"],
  ["adidas F50", "/collections/giay-da-banh-adidas-f50"],
  ["Nike Mercurial", "/collections/giay-da-banh-nike-mercurial"],
  ["Sản phẩm Hot Deals", "/collections/hot-sales"],
  ["Giày sân Cỏ Nhân Tạo", "/collections/giay-co-nhan-tao"],
  ["Giày sân Futsal", "/collections/giay-da-bong-san-futsal-chinh-hang"]
] as const;

const hotSalesLinks = [
  ["HOT SALES - GIÀY CỎ NHÂN TẠO", "/collections/hot-sales-tf-turf"],
  ["HOT DEAL - NIKE MERCURIAL VAPOR 15 PRO TF", "/collections/giay-da-bong-nike-mercurial-vapor-15-pro-tf"],
  ["HOT DEAL - ADIDAS X CRAZYFAST.3/LEAGUE TF", "/collections/hot-deal-adidas-x-crazyfast-3-league-tf"],
  ["HOT DEALS GIÀY ĐẾ TF TRÊN 2 TRIỆU", "/collections/hot-sales-giay-co-nhan-tao-tren-2trieu"],
  ["HOT DEALS GIÀY ĐẾ TF DƯỚI 2 TRIỆU", "/collections/hot-deals-giay-co-nhan-tao-duoi-2trieu"],
  ["HOT DEALS GIÀY ĐẾ TF DƯỚI 1 TRIỆU 5", "/collections/hot-deals-giay-co-nhan-tao-duoi-1-trieu-5"],
  ["HOT SALES - GIÀY FUTSAL", "/collections/hot-sales-ic"],
  ["HOT DEALS GIÀY FUTSAL TRÊN 2 TRIỆU", "/collections/hot-deals-giay-futsal-tren-2trieu"],
  ["HOT DEALS GIÀY FUTSAL DƯỚI 2 TRIỆU", "/collections/hot-deals-giay-futsal-duoi-2-trieu"],
  ["HOT DEALS GIÀY FUTSAL DƯỚI 1 TRIỆU 5", "/collections/hot-deals-giay-futsal-duoi-1-trieu-5"],
  ["PUMA UP TO 50%", "/collections/puma-up-to-50"]
] as const;

const accessoriesLinks = [
  ["Bộ quần áo thi đấu", "/collections/bo-quan-ao-thi-dau"],
  ["Áo bóng đá chính hãng", "/collections/ao-bong-da-chinh-hang"],
  ["Quần bóng đá", "/collections/quan-bong-da"],
  ["Vớ bóng đá", "/collections/vo-bong-da"],
  ["Balo và túi thể thao", "/collections/balo-tui-the-thao"],
  ["Trái bóng thi đấu", "/collections/trai-bong-thi-dau"],
  ["Lót giày và băng keo", "/collections/lot-giay-bang-keo"],
  ["Găng tay thủ môn", "/collections/gang-tay-thu-mon"],
  ["Bó gối, bó cổ chân", "/collections/phu-kien-bao-ve"],
  ["Dầu nóng và phục hồi", "/collections/ho-tro-phuc-hoi"]
] as const;

const priceFilters: PriceFilter[] = [
  { label: "Tất cả", min: 0, max: Number.POSITIVE_INFINITY },
  { label: "Dưới 1,000,000đ", min: 0, max: 1_000_000 },
  { label: "1,000,000đ - 2,000,000đ", min: 1_000_000, max: 2_000_000 },
  { label: "2,000,000đ - 3,000,000đ", min: 2_000_000, max: 3_000_000 },
  { label: "3,000,000đ - 4,000,000đ", min: 3_000_000, max: 4_000_000 },
  { label: "Trên 4,000,000đ", min: 4_000_000, max: Number.POSITIVE_INFINITY }
];

const allBrands = [
  "ADIDAS",
  "ATHLETA",
  "KELME",
  "PUMA",
  "JOMA",
  "STARBALM",
  "ASICS",
  "NIKE",
  "GRAND SPORT",
  "DESPORTE",
  "ACTIVITAL",
  "X-MUNICH",
  "UMBRO",
  "ĐỘNG LỰC",
  "KAMITO",
  "THANH HÙNG FUTSAL",
  "GERUSTAR",
  "LIGPRO",
  "LHGoalkeeping",
  "ZOCKER",
  "UNBOX",
  "MIZUNO",
  "HUMMEL",
  "SENDA"
];

const turfBrands = ["ADIDAS", "NIKE", "MIZUNO", "PUMA", "JOMA", "KELME", "KAMITO", "ZOCKER", "ASICS", "UMBRO"];

const futsalBrands = ["DESPORTE", "JOMA", "ADIDAS", "ATHLETA", "ASICS", "NIKE", "KAMITO", "MIZUNO", "SENDA"];

const kidsBrands = ["NIKE", "ADIDAS", "ZOCKER"];

const hotSalesBrands = ["NIKE", "PUMA", "UMBRO", "ATHLETA", "ADIDAS", "DESPORTE", "JOMA", "MIZUNO"];

const accessoriesBrands = [
  "JOMA",
  "HUMMEL",
  "NIKE",
  "ADIDAS",
  "PUMA",
  "GRAND SPORT",
  "KAMITO",
  "ĐỘNG LỰC",
  "ACTIVITAL",
  "STARBALM",
  "THANH HÙNG FUTSAL"
];

const futsalLinks = [
  ["DESPORTE", "/collections/desporte"],
  ["JOMA", "/collections/joma-futsal"],
  ["ADIDAS", "/collections/adidas-futsal"],
  ["ATHLETA", "/collections/athleta"],
  ["ASICS", "/collections/asics"],
  ["NIKE", "/collections/nike-futsal"],
  ["KAMITO", "/collections/kamito"],
  ["MIZUNO", "/collections/mizuno-futsal"],
  ["SENDA", "/collections/senda"]
] as const;

const sizes = [
  "37",
  "37.5",
  "38",
  "38 2/3",
  "38.5",
  "39",
  "39 1/3",
  "39.5",
  "40",
  "40.5",
  "40 2/3",
  "41",
  "41 1/3",
  "41.5",
  "42",
  "42 2/3",
  "42.5",
  "43",
  "43 1/3",
  "43.5",
  "44",
  "44.5",
  "44 2/3",
  "45",
  "45 1/3",
  "45.5",
  "46",
  "S",
  "M",
  "L",
  "XL",
  "XXL",
  "L-LL [26.0cm-29.0cm]",
  "S-M [22.5cm-25.5cm]",
  "S [23cm-25cm]",
  "M [25cm-27cm]",
  "L [27cm-29cm]",
  "39 (23-24cm)",
  "40 1/2 (24-25cm)",
  "42 (25-26cm)",
  "44 1/2 (27-28cm)"
];

const sortOptions = [
  ["manual", "Sản phẩm nổi bật"],
  ["price-ascending", "Giá: Tăng dần"],
  ["price-descending", "Giá: Giảm dần"],
  ["title-ascending", "Tên: A-Z"],
  ["title-descending", "Tên: Z-A"],
  ["created-ascending", "Cũ nhất"],
  ["created-descending", "Mới nhất"],
  ["best-selling", "Bán chạy nhất"]
] as const;

const catalogUiCopy = {
  vi: {
    filterTitle: "BỘ LỌC",
    closeFilter: "Đóng bộ lọc",
    price: "GIÁ",
    brand: "THƯƠNG HIỆU",
    size: "SIZE",
    clearFilters: "Xóa tất cả bộ lọc",
    empty: "Không tìm thấy sản phẩm phù hợp. Hãy thử bỏ bớt bộ lọc hoặc chọn khoảng giá khác.",
    priceLabels: {
      "Tất cả": "Tất cả",
      "Dưới 1,000,000đ": "Dưới 1,000,000đ",
      "1,000,000đ - 2,000,000đ": "1,000,000đ - 2,000,000đ",
      "2,000,000đ - 3,000,000đ": "2,000,000đ - 3,000,000đ",
      "3,000,000đ - 4,000,000đ": "3,000,000đ - 4,000,000đ",
      "Trên 4,000,000đ": "Trên 4,000,000đ"
    },
    sortLabels: {
      manual: "Sản phẩm nổi bật",
      "price-ascending": "Giá: Tăng dần",
      "price-descending": "Giá: Giảm dần",
      "title-ascending": "Tên: A-Z",
      "title-descending": "Tên: Z-A",
      "created-ascending": "Cũ nhất",
      "created-descending": "Mới nhất",
      "best-selling": "Bán chạy nhất"
    }
  },
  en: {
    filterTitle: "FILTER",
    closeFilter: "Close filter",
    price: "PRICE",
    brand: "BRAND",
    size: "SIZE",
    clearFilters: "Clear all filters",
    empty: "No matching products found. Try removing some filters or choosing another price range.",
    priceLabels: {
      "Tất cả": "All",
      "Dưới 1,000,000đ": "Under 1,000,000đ",
      "1,000,000đ - 2,000,000đ": "1,000,000đ - 2,000,000đ",
      "2,000,000đ - 3,000,000đ": "2,000,000đ - 3,000,000đ",
      "3,000,000đ - 4,000,000đ": "3,000,000đ - 4,000,000đ",
      "Trên 4,000,000đ": "Over 4,000,000đ"
    },
    sortLabels: {
      manual: "Featured products",
      "price-ascending": "Price: Low to high",
      "price-descending": "Price: High to low",
      "title-ascending": "Name: A-Z",
      "title-descending": "Name: Z-A",
      "created-ascending": "Oldest",
      "created-descending": "Newest",
      "best-selling": "Best selling"
    }
  }
} as const;

const categoryLinkTranslations: Record<string, string> = {
  "Sản phẩm Hot Deals": "Hot Deals products",
  "Giày sân Cỏ Nhân Tạo": "Artificial grass boots",
  "Giày sân Futsal": "Futsal shoes",
  "HOT SALES - GIÀY CỎ NHÂN TẠO": "HOT SALES - ARTIFICIAL GRASS BOOTS",
  "HOT DEALS GIÀY ĐẾ TF TRÊN 2 TRIỆU": "HOT DEALS TF BOOTS OVER 2 MILLION",
  "HOT DEALS GIÀY ĐẾ TF DƯỚI 2 TRIỆU": "HOT DEALS TF BOOTS UNDER 2 MILLION",
  "HOT DEALS GIÀY ĐẾ TF DƯỚI 1 TRIỆU 5": "HOT DEALS TF BOOTS UNDER 1.5 MILLION",
  "HOT SALES - GIÀY FUTSAL": "HOT SALES - FUTSAL SHOES",
  "HOT DEALS GIÀY FUTSAL TRÊN 2 TRIỆU": "HOT DEALS FUTSAL SHOES OVER 2 MILLION",
  "HOT DEALS GIÀY FUTSAL DƯỚI 2 TRIỆU": "HOT DEALS FUTSAL SHOES UNDER 2 MILLION",
  "HOT DEALS GIÀY FUTSAL DƯỚI 1 TRIỆU 5": "HOT DEALS FUTSAL SHOES UNDER 1.5 MILLION",
  "Bộ quần áo thi đấu": "Match kits",
  "Áo bóng đá chính hãng": "Authentic football shirts",
  "Quần bóng đá": "Football shorts",
  "Vớ bóng đá": "Football socks",
  "Balo và túi thể thao": "Backpacks and sport bags",
  "Trái bóng thi đấu": "Match balls",
  "Lót giày và băng keo": "Insoles and athletic tape",
  "Găng tay thủ môn": "Goalkeeper gloves",
  "Bó gối, bó cổ chân": "Knee and ankle supports",
  "Dầu nóng và phục hồi": "Warm-up and recovery"
};

function localizeCategoryLink(label: string, language: Language) {
  return language === "en" ? categoryLinkTranslations[label] ?? label : label;
}

function localizeFilterChip(label: string, language: Language) {
  if (language === "vi") {
    return label;
  }

  const ui = catalogUiCopy.en;
  return ui.priceLabels[label as keyof typeof ui.priceLabels] ?? categoryLinkTranslations[label] ?? label;
}

function getCatalogPresentation(language: Language, preset: CatalogPreset) {
  const catalogPresentation = {
  vi: {
    all: {
      title: "TẤT CẢ SẢN PHẨM",
      bannerAlt: "Tất cả sản phẩm Thanh Hùng Futsal",
      filterLinkTitle: "DANH MỤC",
      descriptions: allCopy
    },
    "artificial-turf": {
      title: "GIÀY SÂN CỎ NHÂN TẠO",
      bannerAlt: "Giày đá bóng sân cỏ nhân tạo Thanh Hùng Futsal",
      filterLinkTitle: "DANH MỤC",
      descriptions: turfCopy
    },
    futsal: {
      title: "GIÀY ĐÁ BÓNG SÂN FUTSAL",
      bannerAlt: "Giày đá bóng sân futsal chính hãng Thanh Hùng Futsal",
      filterLinkTitle: "THƯƠNG HIỆU",
      descriptions: futsalCopy
    },
    kids: {
      title: "GIÀY ĐÁ BÓNG TRẺ EM",
      bannerAlt: "Giày đá bóng trẻ em Thanh Hùng Futsal",
      filterLinkTitle: "DANH MỤC",
      descriptions: kidsCopy
    },
    "hot-sales": {
      title: "HOT SALES",
      bannerAlt: "Hot Sales Thanh Hung Futsal",
      filterLinkTitle: "HOT SALES",
      descriptions: hotSalesCopy
    },
    accessories: {
      title: "PHỤ KIỆN CHÍNH HÃNG",
      bannerAlt: "Phụ kiện bóng đá chính hãng Thanh Hùng Futsal",
      filterLinkTitle: "PHỤ KIỆN",
      descriptions: accessoriesCopy
    }
  },
  en: {
    all: {
      title: "ALL PRODUCTS",
      bannerAlt: "All products at Thanh Hung Futsal",
      filterLinkTitle: "CATEGORIES",
      descriptions: [
        "Choosing the right football boots helps you play with more confidence every time you step onto the pitch. For local players, authentic boots bring comfort, durability and better safety in every move.",
        "To find the right pair, consider three things: playing surface, foot shape and budget. A suitable boot improves control, traction and reduces injury risk.",
        "At Thanh Hung Futsal, you can browse new models from Nike, adidas, PUMA, Mizuno, Joma, Asics and essential football accessories.",
        "Each boot order can include campaign gifts, 0% installment support through Fundiin and warranty support under store policy."
      ]
    },
    "artificial-turf": {
      title: "ARTIFICIAL GRASS BOOTS",
      bannerAlt: "Artificial grass football boots at Thanh Hung Futsal",
      filterLinkTitle: "CATEGORIES",
      descriptions: [
        "Artificial grass boots are a popular choice for local football thanks to their traction, cushioning and suitability for 5-a-side and 7-a-side surfaces.",
        "This mock catalog includes Nike Mercurial, Phantom, Tiempo, adidas F50, Predator, Mizuno Alpha, Morelia, Joma, Kamito and Kelme lines.",
        "You can filter by price, brand and size, then sort by price or product name. The desktop layout uses a sidebar while mobile opens filters in a drawer.",
        "The display includes key shopping signals such as installment support, flexible size exchange and warranty information."
      ]
    },
    futsal: {
      title: "FUTSAL SHOES",
      bannerAlt: "Authentic futsal shoes at Thanh Hung Futsal",
      filterLinkTitle: "BRANDS",
      descriptions: [
        "Futsal shoes are designed for indoor courts, hard ground or concrete with non-marking IC outsoles and grooves for grip and control.",
        "Thanh Hung Futsal carries popular futsal models from Nike, Joma, adidas, Mizuno, Asics, Desporte, Athleta, Senda and Kamito.",
        "Campaign gifts, 0% installment support and warranty information are shown so the buying flow can be tested end to end.",
        "Filters mirror the reference collection page with brands, price ranges, sizes and sorting."
      ]
    },
    kids: {
      title: "KIDS FOOTBALL BOOTS",
      bannerAlt: "Kids football boots at Thanh Hung Futsal",
      filterLinkTitle: "CATEGORIES",
      descriptions: [
        "Finding authentic, comfortable and durable kids football boots helps protect young players and support their passion for the game.",
        "The collection includes TF and IC outsole options from leading brands so children can choose a pair that fits both their surface and style.",
        "Exchange, installment and warranty policies are shown to make the purchase flow easier to review.",
        "Visit Thanh Hung Futsal to find a suitable pair for young players."
      ]
    },
    "hot-sales": {
      title: "HOT SALES",
      bannerAlt: "Hot Sales at Thanh Hung Futsal",
      filterLinkTitle: "HOT SALES",
      descriptions: [
        "Hot Sales gathers authentic football boots with strong prices across turf, futsal and selected accessory deals.",
        "The page uses mock data but keeps the structure ready for real APIs: listed price, old price, discount, brand, sizes and hover images.",
        "Filters include sale categories, price ranges, brands, sizes and sorting by featured, price, name, newest and best selling.",
        "Key buying signals stay visible, including discounts, Fundiin installment support, pagination and quick contact actions."
      ]
    },
    accessories: {
      title: "AUTHENTIC ACCESSORIES",
      bannerAlt: "Authentic football accessories at Thanh Hung Futsal",
      filterLinkTitle: "ACCESSORIES",
      descriptions: [
        "Football accessories at Thanh Hung Futsal include matchwear, socks, backpacks, balls, insoles, athletic tape and recovery products.",
        "This page follows the collection layout with a banner, SEO copy, filter sidebar, sorting bar and product grid.",
        "The data is currently mocked for interface testing, but each item already includes brand, price, old price, discount labels, hover images and available sizes.",
        "Customers can quickly filter socks, kits, backpacks, balls, gloves or protective accessories by price and brand."
      ]
    }
  }
  } as const;

  return catalogPresentation[language][preset];
}

const turfCopy = [
  "Giày đá bóng sân cỏ nhân tạo là lựa chọn phổ biến nhất với người chơi phong trào tại Việt Nam nhờ độ bám tốt, đệm êm và phù hợp nhiều mặt sân 5 người, 7 người. Các mẫu TF/AS chính hãng giúp bạn xoay trở linh hoạt, kiểm soát bóng ổn định và hạn chế trơn trượt khi sân khô hoặc hơi ẩm.",
  "Thanh Hùng Futsal mock đầy đủ các dòng Nike Mercurial, Phantom, Tiempo, adidas F50, Predator, Mizuno Alpha, Morelia, Joma, Kamito và Kelme để bạn kiểm thử giao diện category. Dữ liệu có thể thay bằng API thật mà không đổi bố cục.",
  "Bạn có thể lọc theo khoảng giá, thương hiệu, size còn hàng, sắp xếp theo giá hoặc tên sản phẩm. Bố cục desktop dùng sidebar như trang tham chiếu, còn mobile mở bộ lọc dạng drawer trượt từ trái.",
  "Chính sách hiển thị đi kèm gồm trả góp Fundiin 0%, đổi size linh hoạt và bảo hành theo từng dòng sản phẩm. Đây là phần mô tả SEO thường nằm phía trên danh sách hàng của trang collection."
];

const allCopy = [
  "Chọn một đôi giày đá bóng thích hợp sẽ giúp bạn tự tin thể hiện hết khả năng mỗi khi ra sân. Đặc biệt với người chơi bóng phong trào, việc sở hữu một đôi giày đá bóng chính hãng không chỉ mang đến cảm giác thoải mái, bền bỉ mà còn đảm bảo an toàn trong từng pha xử lý.",
  "Để tìm được chân ái thực sự, có 3 yếu tố bạn nên cân nhắc: mặt sân thi đấu, form chân và ngân sách cá nhân. Một đôi giày phù hợp sẽ giúp bạn xử lý bóng linh hoạt, tăng độ bám sân và hạn chế tối đa rủi ro chấn thương.",
  "Tại Thanh Hùng Futsal, bạn sẽ tìm thấy các mẫu mới nhất từ Nike, adidas, PUMA, Mizuno, Joma, Asics cùng nhiều phụ kiện cần có trên sân bóng.",
  "Tặng kèm 1 đôi vớ và 1 balo THF cho mỗi đơn hàng giày đá bóng, hỗ trợ trả góp 0% qua Fundiin và bảo hành miễn phí 6 tháng."
];

const futsalCopy = [
  "Là dòng giày đá bóng được thiết kế đặc biệt dành cho mặt sân sàn gỗ hoặc bê tông, với bề mặt đế IC Non-Marking bằng phẳng cùng các rãnh nhỏ giúp tăng độ bám, hỗ trợ kiểm soát bóng bằng gầm giày, xoay trở linh hoạt và thực hiện các động tác kỹ thuật.",
  "Tại Thanh Hùng Futsal, bạn có thể dễ dàng trải nghiệm những mẫu giày Futsal mới nhất và được săn đón nhiều nhất từ các thương hiệu hàng đầu như Nike, Joma, adidas, Mizuno, Asics, Desporte, Athleta, Senda và Kamito.",
  "Tặng kèm 1 đôi vớ + 1 balo THF cho mỗi đơn hàng giày đá bóng, hỗ trợ trả góp 0% lãi suất qua Fundiin và bảo hành miễn phí 6 tháng.",
  "Bộ lọc mô phỏng trang gốc với nhóm thương hiệu, khoảng giá, size còn hàng, sắp xếp theo giá, tên, mới nhất và bán chạy. Dữ liệu đang là mock nhưng hành vi đã sẵn sàng thay bằng API thật."
];

const kidsCopy = [
  "Việc tìm kiếm một đôi giày đá bóng trẻ em chính hãng, vừa vặn và bền bỉ là điều cực kỳ quan trọng để bảo vệ đôi chân non nớt và nuôi dưỡng đam mê cho các cầu thủ nhí. Hiểu được điều đó, Thanh Hùng Futsal mang đến bộ sưu tập giày đá bóng trẻ em và giày bóng đá dành cho nữ, với thiết kế form và size được nghiên cứu kỹ lưỡng, đảm bảo sự thoải mái và an toàn tối đa.",
  "Các mẫu giày tại cửa hàng đều là hàng chính hãng, có sẵn các phiên bản đế phù hợp với mọi mặt sân, từ sân cỏ nhân tạo (đế TF) cho đến sân futsal (đế IC). Tại đây, bạn sẽ tìm thấy nhiều mẫu mã đa dạng đến từ các thương hiệu hàng đầu, giúp các bé thỏa sức lựa chọn đôi giày ưng ý nhất.",
  "Để việc mua sắm trở nên dễ dàng hơn, chúng tôi còn có chính sách đổi/trả hợp lý, hỗ trợ trả góp 0% lãi suất qua Fundiin và bảo hành miễn phí lên đến 6 tháng.",
  "Tặng kèm 1 đôi vớ + 1 balo THF cho mỗi đơn hàng giày. Hỗ trợ trả góp 0% qua Fundiin. Bảo hành miễn phí 6 tháng.",
  "Hãy ghé thăm Thanh Hùng Futsal để chọn cho bé một đôi giày đá bóng trẻ em phù hợp nhất, giúp bé tự tin tỏa sáng trên sân cỏ!"
];

const kidsProducts: Product[] = [
  {
    ...products[1],
    slug: "nike-zoom-mercurial-vapor-16-pro-tf-fq8687-400-xanh-lo-kids",
    name: "NIKE ZOOM MERCURIAL VAPOR 16 PRO TF - FQ8687-400 - XANH LƠ",
    brand: "Nike",
    category: "Giày đá bóng trẻ em",
    tag: "Bán chạy",
    price: "2,490,000đ",
    oldPrice: "3,650,000đ",
    sale: "-32%",
    sizes: ["37", "37.5", "38", "38.5", "39", "39.5", "40"]
  },
  {
    ...products[0],
    slug: "nike-zoom-mercurial-vapor-16-academy-tf-hong-neon-kids",
    name: "NIKE ZOOM MERCURIAL VAPOR 16 ACADEMY TF JR - HỒNG NEON/XANH",
    brand: "Nike",
    category: "Giày đá bóng trẻ em",
    tag: "Hàng mới",
    price: "1,790,000đ",
    oldPrice: "2,150,000đ",
    sale: "-17%",
    sizes: ["36", "36.5", "37", "37.5", "38", "38.5"]
  },
  {
    ...products[4],
    slug: "adidas-predator-league-tf-jr-hong-den-kids",
    name: "ADIDAS PREDATOR LEAGUE TF JR - HỒNG/ĐEN",
    brand: "Adidas",
    category: "Giày đá bóng trẻ em",
    tag: "Giá tốt",
    price: "1,550,000đ",
    oldPrice: "1,950,000đ",
    sale: "-21%",
    sizes: ["36", "37", "38", "38 2/3", "39 1/3", "40"]
  },
  {
    ...products[5],
    slug: "zocker-inspire-pro-jr-tf-vang-den-kids",
    name: "ZOCKER INSPIRE PRO JR TF - VÀNG/ĐEN",
    brand: "Zocker",
    category: "Giày đá bóng trẻ em",
    tag: "Hot deal",
    price: "890,000đ",
    oldPrice: "1,190,000đ",
    sale: "-25%",
    sizes: ["35", "36", "37", "38", "39", "40"]
  },
  {
    ...products[7],
    slug: "nike-gato-ic-jr-bac-kids",
    name: "NIKE GATO IC JR - BẠC",
    brand: "Nike",
    category: "Giày đá bóng trẻ em",
    tag: "Futsal",
    price: "1,390,000đ",
    oldPrice: "1,850,000đ",
    sale: "-25%",
    sizes: ["37", "38", "39", "40", "40.5", "41"]
  },
  {
    ...products[2],
    slug: "adidas-f50-league-tf-jr-trang-hong-kids",
    name: "ADIDAS F50 LEAGUE TF JR - TRẮNG/HỒNG",
    brand: "Adidas",
    category: "Giày đá bóng trẻ em",
    tag: "Bán chạy",
    price: "1,690,000đ",
    oldPrice: "2,050,000đ",
    sale: "-18%",
    sizes: ["36", "37", "38", "38 2/3", "39 1/3", "40 2/3"]
  }
];

const hotSalesCopy = [
  "Hot Sales tap hop cac mau giay da bong chinh hang dang co muc gia tot tai Thanh Hung Futsal. Nhom san pham nay duoc chia theo giay co nhan tao TF, giay futsal IC va cac deal theo tam gia de nguoi mua loc nhanh hon.",
  "Du lieu tren trang dang la mock de phuc vu giao dien, nhung cau truc da san sang thay bang API that: san pham co gia niem yet, gia cu, phan tram giam, thuong hieu, size con hang va hinh anh hover.",
  "Bo loc mo phong trang tham chieu voi danh muc Hot Sales, khoang gia, thuong hieu, size, sap xep theo san pham noi bat, gia tang/giam, ten A-Z, moi nhat va ban chay.",
  "Khach hang van thay day du cac tin hieu mua hang quan trong nhu tag giam gia, tra gop Fundiin 0%, pagination, nut loc mobile dang drawer va cac nut lien he noi o goc man hinh."
];

const accessoriesCopy = [
  "Phụ kiện bóng đá chính hãng tại Thanh Hùng Futsal gồm quần áo thi đấu, vớ, balo, túi thể thao, trái bóng, lót giày, băng keo thể thao và các sản phẩm bảo vệ khi ra sân. Nhóm sản phẩm này giúp người chơi hoàn thiện set đồ thi đấu và chăm sóc cơ thể sau mỗi trận.",
  "Trang Phụ Kiện được mô phỏng theo bố cục collection của website tham chiếu: phía trên có banner danh mục, tiêu đề, mô tả SEO; phía dưới là sidebar bộ lọc theo danh mục, giá, thương hiệu, size, kèm thanh sắp xếp và phân trang sản phẩm.",
  "Dữ liệu hiện là mock để kiểm thử giao diện, nhưng mỗi sản phẩm đã có thương hiệu, giá, giá cũ, nhãn giảm giá, ảnh hover và size còn hàng. Khi nối API thật, cấu trúc hiển thị và hành vi lọc có thể giữ nguyên.",
  "Khách hàng có thể lọc nhanh vớ bóng đá, bộ quần áo, balo, trái bóng, găng tay hoặc phụ kiện bảo vệ theo tầm giá và thương hiệu. Trên mobile, bộ lọc mở dạng drawer giống các trang collection còn lại."
];

const accessoryProducts: Product[] = [
  {
    slug: "joma-bo-quan-ao-thi-dau-copa-trang-do",
    name: "JOMA BỘ QUẦN ÁO THI ĐẤU COPA - TRẮNG/ĐỎ",
    brand: "Joma",
    category: "Bộ quần áo thi đấu",
    tag: "Hàng mới",
    price: "690,000đ",
    oldPrice: "790,000đ",
    sale: "-13%",
    image: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132",
    hoverImage: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_10_img.webp?v=132",
    gallery: [
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132",
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_10_img.webp?v=132"
    ],
    description: "Bộ quần áo thi đấu chất liệu nhẹ, thoáng khí, phù hợp đá phong trào và đặt đồng phục đội bóng.",
    sizes: ["S", "M", "L", "XL", "XXL"]
  },
  {
    slug: "hummel-ao-training-core-xanh-navy",
    name: "HUMMEL ÁO TRAINING CORE - XANH NAVY",
    brand: "Hummel",
    category: "Áo bóng đá chính hãng",
    tag: "Bán chạy",
    price: "520,000đ",
    oldPrice: "650,000đ",
    sale: "-20%",
    image: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132",
    hoverImage: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_10_img.webp?v=132",
    gallery: [
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132",
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_10_img.webp?v=132"
    ],
    description: "Áo training form thể thao, cổ tròn, thấm hút nhanh cho tập luyện hằng tuần.",
    sizes: ["S", "M", "L", "XL"]
  },
  {
    slug: "nike-classic-football-socks-den-trang",
    name: "NIKE CLASSIC FOOTBALL SOCKS - ĐEN/TRẮNG",
    brand: "Nike",
    category: "Vớ bóng đá",
    tag: "Có sẵn",
    price: "259,000đ",
    image: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132",
    hoverImage: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132",
    gallery: [
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132",
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132"
    ],
    description: "Vớ bóng đá cổ cao, hỗ trợ ôm chân và giảm ma sát khi mang giày đá bóng.",
    sizes: ["S [23cm-25cm]", "M [25cm-27cm]", "L [27cm-29cm]"]
  },
  {
    slug: "adidas-milano-23-socks-trang",
    name: "ADIDAS MILANO 23 SOCKS - TRẮNG",
    brand: "Adidas",
    category: "Vớ bóng đá",
    tag: "Giá tốt",
    price: "229,000đ",
    oldPrice: "290,000đ",
    sale: "-21%",
    image: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132",
    hoverImage: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132",
    gallery: [
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132",
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132"
    ],
    description: "Vớ đá bóng adidas cổ dài, phù hợp thi đấu và tập luyện trên sân cỏ nhân tạo.",
    sizes: ["S-M [22.5cm-25.5cm]", "L-LL [26.0cm-29.0cm]"]
  },
  {
    slug: "dong-luc-trai-bong-futsal-ufs-2-05",
    name: "ĐỘNG LỰC TRÁI BÓNG FUTSAL UFS 2.05",
    brand: "Động Lực",
    category: "Trái bóng thi đấu",
    tag: "Futsal",
    price: "550,000đ",
    oldPrice: "650,000đ",
    sale: "-15%",
    image: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132",
    hoverImage: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132",
    gallery: [
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132",
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132"
    ],
    description: "Bóng futsal độ nảy thấp, bề mặt bền, phù hợp sân trong nhà và sân bê tông.",
    sizes: ["4"]
  },
  {
    slug: "thanh-hung-futsal-balo-training-den",
    name: "THANH HÙNG FUTSAL BALO TRAINING - ĐEN",
    brand: "Thanh Hùng Futsal",
    category: "Balo và túi thể thao",
    tag: "Độc quyền",
    price: "390,000đ",
    image: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132",
    hoverImage: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_10_img.webp?v=132",
    gallery: [
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132",
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_10_img.webp?v=132"
    ],
    description: "Balo thể thao ngăn rộng, có khoang giày riêng, phù hợp đi sân và di chuyển hằng ngày.",
    sizes: ["M", "L"]
  },
  {
    slug: "activital-insole-performance-yellow",
    name: "ACTIVITAL INSOLE PERFORMANCE - YELLOW",
    brand: "Activital",
    category: "Lót giày",
    tag: "Hỗ trợ chân",
    price: "690,000đ",
    image: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132",
    hoverImage: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132",
    gallery: [
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132",
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132"
    ],
    description: "Lót giày hỗ trợ vòm chân, tăng độ ổn định khi đổi hướng và giảm áp lực bàn chân.",
    sizes: ["39 (23-24cm)", "40 1/2 (24-25cm)", "42 (25-26cm)", "44 1/2 (27-28cm)"]
  },
  {
    slug: "starbalm-warm-spray-150ml",
    name: "STARBALM WARM SPRAY 150ML",
    brand: "Starbalm",
    category: "Hỗ trợ và phục hồi",
    tag: "Phục hồi",
    price: "320,000đ",
    image: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132",
    hoverImage: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132",
    gallery: [
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_6_img.webp?v=132",
      "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132"
    ],
    description: "Xịt làm nóng trước vận động, hỗ trợ làm ấm vùng cơ khi tập luyện và thi đấu.",
    sizes: ["M"]
  }
];

function toPriceValue(price: string) {
  return Number(price.replace(/\D/g, ""));
}

function toPriceText(value: number) {
  return `${value.toLocaleString("vi-VN")}đ`;
}

function normalize(value: string) {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
}

function isArtificialTurf(product: Product) {
  return normalize(product.category).includes("co nhan tao") || /\b(tf|as)\b/i.test(product.name);
}

function isFutsal(product: Product) {
  return normalize(product.category).includes("futsal") || /\b(ic|sala|gato|top flex|cancha|regate|mundial)\b/i.test(product.name);
}

function buildCatalogProducts(sourceProducts: Product[], multiplier: number): Product[] {
  const variants = [
    { tag: "", priceOffset: 0, nameSuffix: "" },
    { tag: "Hàng mới", priceOffset: 120_000, nameSuffix: " NEW ARRIVAL" },
    { tag: "Hot deal", priceOffset: -180_000, nameSuffix: " HOT DEAL" },
    { tag: "Bán chạy", priceOffset: 260_000, nameSuffix: " BEST SELLER" },
    { tag: "Limited", priceOffset: 430_000, nameSuffix: " LIMITED" },
    { tag: "Giá tốt", priceOffset: -320_000, nameSuffix: " SALE" }
  ].slice(0, multiplier);

  return variants.flatMap((variant, variantIndex) =>
    sourceProducts.map((product, productIndex) => {
      const basePrice = toPriceValue(product.price);
      const nextPrice = Math.max(790_000, basePrice + variant.priceOffset + productIndex * 35_000);

      return {
        ...product,
        name: `${product.name}${variant.nameSuffix}`,
        tag: variant.tag || product.tag,
        price: toPriceText(nextPrice),
        oldPrice: product.oldPrice ?? (variantIndex > 0 ? toPriceText(nextPrice + 320_000) : undefined),
        sale: product.sale ?? (variantIndex === 2 ? "-15%" : undefined)
      };
    })
  );
}

function getCatalogConfig(preset: CatalogPreset): CatalogConfig {
  if (preset === "kids") {
    return {
      title: "GIÀY ĐÁ BÓNG TRẺ EM",
      bannerAlt: "Giày đá bóng trẻ em - Giày đá banh cho nữ - Thanh Hùng Futsal",
      bannerUrl: kidsBanner,
      descriptions: kidsCopy,
      sourceProducts: kidsProducts,
      brands: kidsBrands,
      categoryLinks,
      filterLinkTitle: "DANH MỤC",
      productMultiplier: 4
    };
  }

  if (preset === "futsal") {
    const futsalProducts = products.filter(isFutsal);

    return {
      title: "GIÀY ĐÁ BÓNG SÂN FUTSAL",
      bannerAlt: "GIÀY ĐÁ BÓNG SÂN FUTSAL CHÍNH HÃNG - THANH HÙNG FUTSAL",
      bannerUrl: futsalBanner,
      descriptions: futsalCopy,
      sourceProducts: futsalProducts.length ? futsalProducts : products,
      brands: futsalBrands,
      categoryLinks: futsalLinks,
      filterLinkTitle: "THƯƠNG HIỆU",
      productMultiplier: 10
    };
  }

  if (preset === "artificial-turf") {
    const turfProducts = products.filter(isArtificialTurf);

    return {
      title: "GIÀY SÂN CỎ NHÂN TẠO",
      bannerAlt: "Giày đá bóng sân cỏ nhân tạo Thanh Hùng Futsal",
      bannerUrl: artificialTurfBanner,
      descriptions: turfCopy,
      sourceProducts: turfProducts.length ? turfProducts : products,
      brands: turfBrands,
      categoryLinks,
      filterLinkTitle: "DANH MỤC",
      productMultiplier: 6
    };
  }

  if (preset === "hot-sales") {
    const saleProducts = products.filter((product) => product.sale || product.oldPrice);

    return {
      title: "HOT SALES",
      bannerAlt: "Hot Sales Thanh Hung Futsal",
      bannerUrl: hotSalesBanner,
      descriptions: hotSalesCopy,
      sourceProducts: saleProducts.length ? saleProducts : products,
      brands: hotSalesBrands,
      categoryLinks: hotSalesLinks,
      filterLinkTitle: "HOT SALES",
      productMultiplier: 8
    };
  }

  if (preset === "accessories") {
    return {
      title: "PHỤ KIỆN CHÍNH HÃNG",
      bannerAlt: "Phụ kiện bóng đá chính hãng Thanh Hùng Futsal",
      bannerUrl: accessoriesBanner,
      descriptions: accessoriesCopy,
      sourceProducts: accessoryProducts,
      brands: accessoriesBrands,
      categoryLinks: accessoriesLinks,
      filterLinkTitle: "PHỤ KIỆN",
      productMultiplier: 5
    };
  }

  return {
    title: "TẤT CẢ SẢN PHẨM",
    bannerAlt: "Tất cả sản phẩm Thanh Hùng Futsal",
    bannerUrl: allProductsBanner,
    descriptions: allCopy,
    sourceProducts: products,
    brands: allBrands,
    categoryLinks,
    filterLinkTitle: "DANH MỤC",
    productMultiplier: 3
  };
}

function toggleValue(values: string[], value: string) {
  return values.includes(value) ? values.filter((item) => item !== value) : [...values, value];
}

function FilterPanel({
  categoryLinks,
  filterLinkTitle,
  brands,
  selectedBrands,
  selectedSizes,
  selectedPrice,
  onBrandToggle,
  onSizeToggle,
  onPriceSelect,
  onClear,
  onClose,
  language = "vi"
}: {
  categoryLinks: readonly (readonly [string, string])[];
  filterLinkTitle: string;
  brands: string[];
  selectedBrands: string[];
  selectedSizes: string[];
  selectedPrice: string;
  onBrandToggle: (brand: string) => void;
  onSizeToggle: (size: string) => void;
  onPriceSelect: (price: string) => void;
  onClear: () => void;
  onClose?: () => void;
  language?: Language;
}) {
  const ui = catalogUiCopy[language];
  const activeCount = selectedBrands.length + selectedSizes.length + (selectedPrice === "Tất cả" ? 0 : 1);

  return (
    <aside className="catalog-filter" aria-label={ui.filterTitle}>
      <div className="catalog-filter-heading">
        <h2>{ui.filterTitle}</h2>
        {onClose ? (
          <button type="button" aria-label={ui.closeFilter} onClick={onClose}>
            <X size={20} weight="bold" />
          </button>
        ) : null}
      </div>

      <div className="filter-category-links">
        <h3>{filterLinkTitle}</h3>
        {categoryLinks.map(([label, href]) => (
          <a href={href} key={href}>
            {localizeCategoryLink(label, language)}
          </a>
        ))}
      </div>

      <details className="filter-group" open>
        <summary>{ui.price}</summary>
        <div>
          {priceFilters.map((price) => (
            <label key={price.label}>
              <input type="checkbox" checked={selectedPrice === price.label} onChange={() => onPriceSelect(price.label)} />
              <span>{ui.priceLabels[price.label as keyof typeof ui.priceLabels] ?? price.label}</span>
            </label>
          ))}
        </div>
      </details>

      <details className="filter-group" open>
        <summary>{ui.brand}</summary>
        <div>
          {brands.map((brand) => (
            <label key={brand}>
              <input type="checkbox" checked={selectedBrands.includes(brand)} onChange={() => onBrandToggle(brand)} />
              <span>{brand}</span>
            </label>
          ))}
        </div>
      </details>

      <details className="filter-group" open>
        <summary>{ui.size}</summary>
        <div className="size-filter-list">
          {sizes.map((size) => (
            <label key={size}>
              <input type="checkbox" checked={selectedSizes.includes(size)} onChange={() => onSizeToggle(size)} />
              <span>{size}</span>
            </label>
          ))}
        </div>
      </details>

      {activeCount > 0 ? (
        <button className="filter-clear" type="button" onClick={onClear}>
          {ui.clearFilters} ({activeCount})
        </button>
      ) : null}
    </aside>
  );
}

export function AllProductsCatalog({ preset = "all", language = "vi" }: { preset?: CatalogPreset; language?: Language }) {
  const config = useMemo(() => getCatalogConfig(preset), [preset]);
  const presentation = getCatalogPresentation(language, preset);
  const t = commonPageCopy[language];
  const ui = catalogUiCopy[language];
  const [selectedBrands, setSelectedBrands] = useState<string[]>([]);
  const [selectedSizes, setSelectedSizes] = useState<string[]>([]);
  const [selectedPrice, setSelectedPrice] = useState("Tất cả");
  const [sortBy, setSortBy] = useState<(typeof sortOptions)[number][0]>("manual");
  const [page, setPage] = useState(1);
  const [filterOpen, setFilterOpen] = useState(false);
  const allProducts = useMemo(() => buildCatalogProducts(config.sourceProducts, config.productMultiplier), [config]);
  const pageSize = 24;

  const filteredProducts = useMemo(() => {
    const priceFilter = priceFilters.find((item) => item.label === selectedPrice) ?? priceFilters[0];
    const selectedBrandSet = new Set(selectedBrands.map(normalize));
    const selectedSizeSet = new Set(selectedSizes);

    const nextProducts = allProducts.filter((product) => {
      const productPrice = toPriceValue(product.price);
      const brandMatches = selectedBrands.length === 0 || selectedBrandSet.has(normalize(product.brand));
      const sizeMatches = selectedSizes.length === 0 || product.sizes.some((size) => selectedSizeSet.has(size));
      const priceMatches = productPrice >= priceFilter.min && productPrice <= priceFilter.max;

      return brandMatches && sizeMatches && priceMatches;
    });

    return [...nextProducts].sort((a, b) => {
      if (sortBy === "price-ascending") return toPriceValue(a.price) - toPriceValue(b.price);
      if (sortBy === "price-descending") return toPriceValue(b.price) - toPriceValue(a.price);
      if (sortBy === "title-ascending") return a.name.localeCompare(b.name, "vi");
      if (sortBy === "title-descending") return b.name.localeCompare(a.name, "vi");
      if (sortBy === "created-ascending") return allProducts.indexOf(a) - allProducts.indexOf(b);
      if (sortBy === "created-descending") return allProducts.indexOf(b) - allProducts.indexOf(a);
      if (sortBy === "best-selling") return (b.sale ? 1 : 0) - (a.sale ? 1 : 0);
      return 0;
    });
  }, [allProducts, selectedBrands, selectedPrice, selectedSizes, sortBy]);

  const pageCount = Math.max(1, Math.ceil(filteredProducts.length / pageSize));
  const currentPage = Math.min(page, pageCount);
  const visibleProducts = filteredProducts.slice((currentPage - 1) * pageSize, currentPage * pageSize);
  const activeFilters = [...(selectedPrice === "Tất cả" ? [] : [selectedPrice]), ...selectedBrands, ...selectedSizes];

  function resetPage(callback: () => void) {
    callback();
    setPage(1);
  }

  function clearFilters() {
    setSelectedBrands([]);
    setSelectedSizes([]);
    setSelectedPrice("Tất cả");
    setPage(1);
  }

  const filterPanel = (
    <FilterPanel
      categoryLinks={config.categoryLinks}
      filterLinkTitle={presentation.filterLinkTitle}
      brands={config.brands}
      selectedBrands={selectedBrands}
      selectedSizes={selectedSizes}
      selectedPrice={selectedPrice}
      onBrandToggle={(brand) => resetPage(() => setSelectedBrands((current) => toggleValue(current, brand)))}
      onSizeToggle={(size) => resetPage(() => setSelectedSizes((current) => toggleValue(current, size)))}
      onPriceSelect={(price) => resetPage(() => setSelectedPrice(price))}
      onClear={clearFilters}
      language={language}
    />
  );

  return (
    <>
      <section className="shell collection-shop-head">
        <div className="collection-banner-image">
          <img src={config.bannerUrl} alt={presentation.bannerAlt} />
        </div>
        <h1>{presentation.title}</h1>
        <div className="collection-description">
          {presentation.descriptions.map((paragraph) => (
            <p key={paragraph}>{paragraph}</p>
          ))}
        </div>
      </section>

      <section className="shell catalog-layout enhanced-catalog">
        {filterPanel}

        <div className="catalog-content">
          <div className="catalog-toolbar" id="sort-wrap">
            <div className="catalog-result-count">
              <button className="mobile-filter-btn" type="button" onClick={() => setFilterOpen(true)}>
                <SlidersHorizontal size={18} weight="bold" />
                {t.productFilter}
              </button>
              <span>{filteredProducts.length} {t.productsFound}</span>
            </div>

            <label>
              {t.sortBy}
              <select
                aria-label={t.sortBy}
                value={sortBy}
                onChange={(event) => resetPage(() => setSortBy(event.target.value as typeof sortBy))}
              >
                {sortOptions.map(([value]) => (
                  <option value={value} key={value}>
                    {ui.sortLabels[value as keyof typeof ui.sortLabels]}
                  </option>
                ))}
              </select>
            </label>
          </div>

          {activeFilters.length > 0 ? (
            <div className="active-filter-row" aria-label={t.activeFilters}>
              {activeFilters.map((filter) => (
                <button
                  type="button"
                  key={filter}
                  onClick={() => {
                    if (filter === selectedPrice) setSelectedPrice("Tất cả");
                    setSelectedBrands((current) => current.filter((item) => item !== filter));
                    setSelectedSizes((current) => current.filter((item) => item !== filter));
                    setPage(1);
                  }}
                >
                  <span>{localizeFilterChip(filter, language)}</span>
                  <X size={14} weight="bold" />
                </button>
              ))}
              <button className="clear-all-chip" type="button" onClick={clearFilters}>
                {t.clearAll}
              </button>
            </div>
          ) : null}

          {visibleProducts.length > 0 ? (
            <div className="product-grid" id="result">
              {visibleProducts.map((product, index) => (
                <ProductCard product={product} initialLanguage={language} key={`${product.slug}-${currentPage}-${index}`} />
              ))}
            </div>
          ) : (
            <div className="empty-state catalog-empty">
              {ui.empty}
            </div>
          )}

          {pageCount > 1 ? (
            <div className="pagination" id="pagination">
              {Array.from({ length: pageCount }).map((_, index) => {
                const pageNumber = index + 1;

                return pageNumber === currentPage ? (
                  <span className="active" key={pageNumber}>
                    {pageNumber}
                  </span>
                ) : (
                  <button type="button" key={pageNumber} onClick={() => setPage(pageNumber)}>
                    {pageNumber}
                  </button>
                );
              })}
            </div>
          ) : null}
        </div>
      </section>

      {filterOpen ? (
        <div className="catalog-filter-drawer open">
          <button className="catalog-filter-backdrop" type="button" aria-label={ui.closeFilter} onClick={() => setFilterOpen(false)} />
          <FilterPanel
            categoryLinks={config.categoryLinks}
            filterLinkTitle={presentation.filterLinkTitle}
            brands={config.brands}
            selectedBrands={selectedBrands}
            selectedSizes={selectedSizes}
            selectedPrice={selectedPrice}
            onBrandToggle={(brand) => resetPage(() => setSelectedBrands((current) => toggleValue(current, brand)))}
            onSizeToggle={(size) => resetPage(() => setSelectedSizes((current) => toggleValue(current, size)))}
            onPriceSelect={(price) => resetPage(() => setSelectedPrice(price))}
            onClear={clearFilters}
            onClose={() => setFilterOpen(false)}
            language={language}
          />
        </div>
      ) : null}
    </>
  );
}

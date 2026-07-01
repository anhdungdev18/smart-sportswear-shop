export type ProductSummary = {
  slug: string;
  name: string;
  category: string;
  department: string;
  collection: string;
  description: string;
  price: string;
  compareAtPrice?: string;
  image: string;
  materials: string[];
  badges?: string[];
};

export const collections = [
  {
    slug: "summer-motion",
    name: "Summer Motion",
    kicker: "BST mùa hè",
    description: "Nhịp phối đồ nhẹ, thoáng, thiên về chuyển động ngoài trời và training cường độ cao.",
    cover: "https://images.unsplash.com/photo-1506629905607-c28f0c4f98a5?auto=format&fit=crop&w=1200&q=80",
  },
  {
    slug: "footwear-lab",
    name: "Footwear Lab",
    kicker: "Giày hiệu năng",
    description: "Tách riêng khu giày để sau này bạn có thể đẩy mạnh futsal, running và lifestyle.",
    cover: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=80",
  },
  {
    slug: "accessory-atelier",
    name: "Accessory Atelier",
    kicker: "Phụ kiện cao cấp",
    description: "Các sản phẩm hoàn thiện outfit như mũ, túi, tất, bình nước và đồ hỗ trợ tập luyện.",
    cover: "https://images.unsplash.com/photo-1523398002811-999ca8dec234?auto=format&fit=crop&w=1200&q=80",
  },
];

export const featuredProducts: ProductSummary[] = [
  {
    slug: "ao-khoac-wind-shell-pro",
    name: "Áo khoác Wind Shell Pro",
    category: "Outerwear",
    department: "Nữ",
    collection: "Summer Motion",
    description: "Form gọn, chống gió nhẹ, hợp cả chạy bộ sáng sớm lẫn street training.",
    price: "2.190.000đ",
    compareAtPrice: "2.650.000đ",
    image: "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=80",
    materials: ["Nylon chống gió", "Lót lưới thoáng", "Khóa chống trượt"],
    badges: ["New Arrival"],
  },
  {
    slug: "set-training-flow",
    name: "Set Training Flow",
    category: "Apparel Set",
    department: "Nam",
    collection: "Summer Motion",
    description: "Bộ phối áo và quần ngắn mang tinh thần ready-to-train nhưng vẫn thời trang.",
    price: "1.780.000đ",
    image: "https://images.unsplash.com/photo-1518310383802-640c2de311b2?auto=format&fit=crop&w=900&q=80",
    materials: ["Poly co giãn 4 chiều", "Thoát ẩm nhanh", "Cắt may tối giản"],
    badges: ["Best Seller"],
  },
  {
    slug: "futsal-velocity-elite",
    name: "Futsal Velocity Elite",
    category: "Footwear",
    department: "Nam",
    collection: "Footwear Lab",
    description: "Giày futsal đế thấp, upper mềm, thiên về cảm giác bóng và đổi hướng nhanh.",
    price: "2.950.000đ",
    compareAtPrice: "3.400.000đ",
    image: "https://images.unsplash.com/photo-1543508282-6319a3e2621f?auto=format&fit=crop&w=900&q=80",
    materials: ["Microfiber upper", "Đế gum traction", "Form ôm chân"],
    badges: ["Signature"],
  },
  {
    slug: "tui-duffle-weekend-team",
    name: "Túi Duffle Weekend Team",
    category: "Accessory",
    department: "Unisex",
    collection: "Accessory Atelier",
    description: "Túi thể thao cỡ vừa cho lịch tập, đi sân hoặc du lịch ngắn ngày.",
    price: "1.390.000đ",
    image: "https://images.unsplash.com/photo-1547949003-9792a18a2601?auto=format&fit=crop&w=900&q=80",
    materials: ["Canvas phủ chống bám", "Ngăn giày riêng", "Dây đeo bản lớn"],
    badges: ["Limited"],
  },
  {
    slug: "legging-aero-sculpt",
    name: "Legging Aero Sculpt",
    category: "Bottom",
    department: "Nữ",
    collection: "Summer Motion",
    description: "Dáng ôm dài, chất liệu nâng đỡ tốt và bảng màu tối giản dễ phối.",
    price: "1.290.000đ",
    image: "https://images.unsplash.com/photo-1506629905607-c28f0c4f98a5?auto=format&fit=crop&w=900&q=80",
    materials: ["Knit nén nhẹ", "Khô nhanh", "Co giãn bốn chiều"],
  },
  {
    slug: "cap-core-runner",
    name: "Cap Core Runner",
    category: "Accessory",
    department: "Unisex",
    collection: "Accessory Atelier",
    description: "Mũ form gọn với lưới thoáng phía sau, phù hợp chạy bộ và di chuyển hằng ngày.",
    price: "590.000đ",
    image: "https://images.unsplash.com/photo-1521369909029-2afed882baee?auto=format&fit=crop&w=900&q=80",
    materials: ["Canvas nhẹ", "Lưới thoáng", "Khóa chỉnh kim loại"],
  },
  {
    slug: "bra-studio-line",
    name: "Bra Studio Line",
    category: "Top",
    department: "Nữ",
    collection: "Summer Motion",
    description: "Phom tinh gọn cho yoga, pilates và lớp chuyển động cường độ vừa.",
    price: "890.000đ",
    image: "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?auto=format&fit=crop&w=900&q=80",
    materials: ["Vải mềm mịn", "Đệm tháo rời", "Viền ôm chắc"],
  },
  {
    slug: "jacket-court-transit",
    name: "Jacket Court Transit",
    category: "Outerwear",
    department: "Nam",
    collection: "Footwear Lab",
    description: "Áo khoác di chuyển trước và sau trận, phom rộng nhẹ và hiện đại.",
    price: "2.490.000đ",
    image: "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=900&q=80",
    materials: ["Dệt chống nhăn", "Lót mỏng", "Khóa kéo hai chiều"],
  },
];

export const lookbooks = [
  {
    slug: "city-training",
    kicker: "Lookbook 01",
    title: "City Training",
    description: "Những set đồ dùng bảng màu trung tính cho vận động và đời sống hằng ngày.",
    image: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=1000&q=80",
  },
  {
    slug: "court-energy",
    kicker: "Lookbook 02",
    title: "Court Energy",
    description: "Tập trung vào giày, tất, áo khoác mỏng và phụ kiện sân cỏ trong ngôn ngữ cao cấp.",
    image: "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=1000&q=80",
  },
  {
    slug: "studio-balance",
    kicker: "Lookbook 03",
    title: "Studio Balance",
    description: "Một hướng mềm hơn cho yoga, pilates và athleisure hiện đại.",
    image: "https://images.unsplash.com/photo-1487412912498-0447578fcca8?auto=format&fit=crop&w=1000&q=80",
  },
];

export const storyPillars = [
  {
    kicker: "Danh mục",
    title: "Một menu đủ lớn để mở rộng sản phẩm",
    description: "Cấu trúc mới cho phép bạn kể riêng từng nhóm: quần áo, giày, phụ kiện, lookbook, bộ sưu tập.",
    points: [
      "Không còn cảm giác chỉ là một shop giày futsal.",
      "Dễ mở thêm seasonal collection hoặc collab.",
      "Hợp để xây storefront giống một nhà bán lẻ thời trang thể thao.",
    ],
  },
  {
    kicker: "Trải nghiệm",
    title: "Typography thiên fashion như IVY",
    description: "Heading lớn, chữ serif sang, khoảng trắng dài và cấu trúc visual mang tinh thần editorial.",
    points: [
      "Tập trung cảm giác thương hiệu trước khi nối chức năng.",
      "Hợp với web bán outfit thay vì chỉ bán SKU.",
      "Giúp homepage nhìn cao cấp ngay cả khi đang dùng mock data.",
    ],
  },
  {
    kicker: "Kỹ thuật",
    title: "Sẵn chỗ để nối backend sau",
    description: "Storefront này chỉ thay mặt tiền. Khi cần, bạn có thể map lần lượt products, collections, promotions, reviews và checkout.",
    points: [
      "Không đụng admin app.",
      "Không chặn tiến độ backend đã làm.",
      "Có thể nối API theo từng cụm sau khi chốt giao diện.",
    ],
  },
];

export function getCollectionBySlug(slug: string) {
  return collections.find((item) => item.slug === slug);
}

export function getProductsForCollection(slug: string) {
  if (slug === "apparel-edit") {
    return featuredProducts.filter((item) => item.category !== "Accessory" && item.category !== "Footwear");
  }
  if (slug === "footwear-lab") {
    return featuredProducts.filter((item) => item.category === "Footwear");
  }
  if (slug === "accessory-atelier") {
    return featuredProducts.filter((item) => item.category === "Accessory");
  }
  return featuredProducts.filter((item) => item.collection.toLowerCase().replace(/\s+/g, "-") === slug);
}

export function getProductBySlug(slug: string) {
  return featuredProducts.find((item) => item.slug === slug);
}

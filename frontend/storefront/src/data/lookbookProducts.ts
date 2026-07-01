import type { Product } from "@/types/ivy";

const IMG = "/images/ivymoda/products";

function swatch(id: string): Product["colors"] {
  return [{ id, image: "/images/ivymoda/colors/001.png", label: "default", active: true }];
}

const SIZES: Product["sizes"] = [
  { id: "s", label: "s" },
  { id: "m", label: "m" },
  { id: "l", label: "l" },
  { id: "xl", label: "xl" },
  { id: "xxl", label: "xxl" },
];

export const DAILY_MOOD_PRODUCTS: Product[] = [
  { id: "11709", name: "Chân váy bút chì Verse", href: "/sanpham/chan-vay-but-chi-verse-ms-31m9380-11709", image: `${IMG}/84886754e0482919c9469f0df37ef6df.webp`, hoverImage: `${IMG}/84886754e0482919c9469f0df37ef6df.webp`, price: 990000, ribbon: "new", colors: swatch("11709"), sizes: SIZES },
  { id: "44652", name: "Áo kiểu Day Dream", href: "/sanpham/ao-kieu-day-dream-ms-16b0570-44652", image: `${IMG}/2cf9b85228b3e78f22cfc3718f6e24b6.webp`, hoverImage: `${IMG}/15562de9138d1c5927ee7f195c512e06.webp`, price: 1390000, colors: swatch("44652"), sizes: SIZES },
  { id: "44663", name: "Chân váy xòe Day Dream", href: "/sanpham/chan-vay-xoe-day-dream-ms-31b0570-44663", image: `${IMG}/2c59162644d025dc6bb44ba2484123c1.webp`, hoverImage: `${IMG}/2c59162644d025dc6bb44ba2484123c1.webp`, price: 1590000, colors: swatch("44663"), sizes: SIZES },
  { id: "44667", name: "Đầm xòe Silk Touch", href: "/sanpham/dam-xoe-silk-touch-ms-48b0727-44667", image: `${IMG}/ac6ca29438fd3746a3c2e84b75b2a46a.webp`, hoverImage: `${IMG}/ffef76478137d3b4273a125121c96be7.webp`, price: 1790000, colors: swatch("44667"), sizes: SIZES },
  { id: "44735", name: "Áo lụa Art Elegance", href: "/sanpham/ao-lua-art-elegance-ms-16b0618-44735", image: `${IMG}/5877f3534b232631d1cf7fdd8277859b.webp`, hoverImage: `${IMG}/7b92778cdf3ca6ecdda23623e14c73dd.webp`, price: 890000, ribbon: "new", colors: swatch("44735"), sizes: SIZES },
  { id: "44757", name: "Áo kiểu White Verse", href: "/sanpham/ao-kieu-white-verse-ms-16m9387-44757", image: `${IMG}/112d32ee60e740c7e170c854621c1994.webp`, hoverImage: `${IMG}/112d32ee60e740c7e170c854621c1994.webp`, price: 1290000, colors: swatch("44757"), sizes: SIZES },
  { id: "44760", name: "Đầm xòe Soft Glow", href: "/sanpham/dam-xoe-soft-glow-ms-48b0640-44760", image: `${IMG}/7ffdeee9bfeaff97eabd2e6aa3f96244.webp`, hoverImage: `${IMG}/7ffdeee9bfeaff97eabd2e6aa3f96244.webp`, price: 1890000, ribbon: "new", colors: swatch("44760"), sizes: SIZES },
  { id: "44762", name: "Áo sơ mi Deep Muse", href: "/sanpham/ao-so-mi-deep-muse-ms-16b0620-44762", image: `${IMG}/1c576e917832620b2d4b0f3cb78642a5.webp`, hoverImage: `${IMG}/b8b5eaf1a0640756809e0bdfeb2039ad.webp`, price: 850000, ribbon: "new", colors: swatch("44762"), sizes: SIZES },
  { id: "44764", name: "Áo lụa Pure Daily", href: "/sanpham/ao-lua-pure-daily-ms-16m9377-44764", image: `${IMG}/a9f9d92197126efdabe0be59ef2006e7.webp`, hoverImage: `${IMG}/049ba3d1ae8ce2ec91c7cdf00d01e0d7.webp`, price: 990000, ribbon: "new", colors: swatch("44764"), sizes: SIZES },
  { id: "44767", name: "Đầm xòe Soft Vibes", href: "/sanpham/dam-xoe-soft-vibes-ms-48b0651-44767", image: `${IMG}/84ceec2e174853ef07c4201f1c60aee3.webp`, hoverImage: `${IMG}/9b5bf42f1df7ad28f286fab5b2cd6622.webp`, price: 1690000, ribbon: "new", colors: swatch("44767"), sizes: SIZES },
  { id: "44771", name: "Chân váy A Dewy", href: "/sanpham/chan-vay-a-dewy-ms-31b0612-44771", image: `${IMG}/b10d17283df624309e40f049630a8672.webp`, hoverImage: `${IMG}/b6264126391943ea38c630483f9956a1.webp`, price: 1090000, ribbon: "new", colors: swatch("44771"), sizes: SIZES },
  { id: "44774", name: "Áo vest Dewy White", href: "/sanpham/ao-vest-dewy-white-ms-67b0723-44774", image: `${IMG}/b6264126391943ea38c630483f9956a1.webp`, hoverImage: `${IMG}/b6264126391943ea38c630483f9956a1.webp`, price: 1390000, ribbon: "new", colors: swatch("44774"), sizes: SIZES },
  { id: "44782", name: "Chân váy đuôi cá Elegance", href: "/sanpham/chan-vay-duoi-ca-elegance-ms-31b0704-44782", image: `${IMG}/c45c97be4ba6a6bfee63efac1f2b6ab1.webp`, hoverImage: `${IMG}/c45c97be4ba6a6bfee63efac1f2b6ab1.webp`, price: 990000, ribbon: "new", colors: swatch("44782"), sizes: SIZES },
  { id: "44783", name: "Chân váy bút chì Verse", href: "/sanpham/chan-vay-but-chi-verse-ms-31m9380-44783", image: `${IMG}/e30b9c0a0dd23cbc40aacdb3e0cd47f4.webp`, hoverImage: `${IMG}/8c6fdb7b299f90db3971b659f19b5ad6.webp`, price: 990000, ribbon: "new", colors: swatch("44783"), sizes: SIZES },
  { id: "44784", name: "Đầm công sở Black Charm", href: "/sanpham/dam-cong-so-black-charm-ms-48b0613-44784", image: `${IMG}/ba427d7547f973c1c0b1a3aac2cec1c3.webp`, hoverImage: `${IMG}/183ee7c3ea75e886fdc61cfbe9a1ffd3.webp`, price: 1990000, ribbon: "new", colors: swatch("44784"), sizes: SIZES },
  { id: "44801", name: "Quần Tây suông Deep Muse", href: "/sanpham/quan-tay-suong-deep-muse-ms-22b0721-44801", image: `${IMG}/61e87a6b08767912bbfbbcdfd4eabac4.webp`, hoverImage: `${IMG}/61e87a6b08767912bbfbbcdfd4eabac4.webp`, price: 1750000, ribbon: "new", colors: swatch("44801"), sizes: SIZES },
];

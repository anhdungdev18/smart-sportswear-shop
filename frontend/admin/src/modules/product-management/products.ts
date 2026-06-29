export type AdminProduct = {
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

export const adminProducts: AdminProduct[] = [
  {
    sku: "NIKE-V16-PRO-TF-42",
    name: "Nike Zoom Mercurial Vapor 16 Pro TF",
    category: "Giày cỏ nhân tạo",
    brand: "Nike",
    price: "3.350.000 đ",
    stock: 84,
    sold: 318,
    status: "active",
    image: "https://cdn.hstatic.net/products/200000278317/a-bong-nike-zoom-mercurial-vapor-16-pro-tf-vjr-io9814-hong-neon-xanh-1_6ec28c52f6544bb5913d3081df42361b_large.jpg"
  },
  {
    sku: "MIZ-ALPHA-AS-43",
    name: "Mizuno Alpha 3 Pro AS",
    category: "Giày cỏ nhân tạo",
    brand: "Mizuno",
    price: "2.700.000 đ",
    stock: 6,
    sold: 185,
    status: "low",
    image: "https://cdn.hstatic.net/products/200000278317/al-giay-da-bong-mizuno-alpha-3-pro-as-p1gd266464-hong-canh-sen-trang-1_714c01aa5f6f4606885041ccaf4c12dd_large.jpg"
  },
  {
    sku: "MIZ-MORELIA-SALA-41",
    name: "Mizuno Morelia Sala Elite TF",
    category: "Giày futsal",
    brand: "Mizuno",
    price: "2.970.000 đ",
    stock: 14,
    sold: 242,
    status: "low",
    image: "https://cdn.hstatic.net/products/200000278317/tsal-giay-da-bong-mizuno-morelia-sala-elite-tf-q1gb261225-xanh-trang-1_5ec687fffb6b478699b7d55e09ed68c2_large.jpg"
  },
  {
    sku: "ADI-PRED-PRO-TF-40",
    name: "Adidas Predator Pro lưỡi gà lật TF",
    category: "Giày cỏ nhân tạo",
    brand: "Adidas",
    price: "3.050.000 đ",
    stock: 42,
    sold: 211,
    status: "active",
    image: "https://cdn.hstatic.net/products/200000278317/z7982451354781_962d306292147f9baa344c4619e65151_eb5f6a1abe144747be2437641ab87adb_large.jpg"
  },
  {
    sku: "NIKE-GATO-IC-43",
    name: "Nike Gato IC",
    category: "Giày futsal",
    brand: "Nike",
    price: "1.590.000 đ",
    stock: 0,
    sold: 97,
    status: "out",
    image: "https://cdn.hstatic.net/products/200000278317/thanh-hung-futsal-giay-da-bong-nike-gato-ic-ib3566-001-bac-1_c7960bdaccbe4adca66c445547964950_large.jpg"
  },
  {
    sku: "THF-BALO-TRAINING",
    name: "Balo Thanh Hùng Futsal Training",
    category: "Phụ kiện",
    brand: "Thanh Hùng Futsal",
    price: "390.000 đ",
    stock: 120,
    sold: 146,
    status: "draft",
    image: "https://cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132"
  }
];

export const productStats = [
  { label: "Tổng sản phẩm", value: "6", tone: "neutral" },
  { label: "Đang bán", value: "4", tone: "success" },
  { label: "Sắp hết hàng", value: "2", tone: "warning" },
  { label: "Hết hàng", value: "1", tone: "danger" }
];

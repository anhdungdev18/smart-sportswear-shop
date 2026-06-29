import { mockAccessoryImage, mockShoeImage, mockShoeImageBlue } from "./products";

const cdn = (path: string) => `https:${path}`;

function mockPanel(title: string, accent = "#d9121f") {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 760">
      <rect width="1200" height="760" fill="#f4f4f4"/>
      <rect x="70" y="70" width="1060" height="620" fill="#fff" stroke="#e1e3e8"/>
      <circle cx="220" cy="190" r="84" fill="${accent}" opacity=".14"/>
      <circle cx="980" cy="570" r="140" fill="${accent}" opacity=".1"/>
      <rect x="265" y="260" width="670" height="180" fill="#111820"/>
      <rect x="300" y="302" width="600" height="28" fill="${accent}"/>
      <rect x="300" y="356" width="410" height="18" fill="#fff" opacity=".82"/>
      <text x="600" y="595" text-anchor="middle" font-family="Arial, sans-serif" font-size="46" font-weight="700" fill="#0e1c22">${title}</text>
    </svg>
  `;

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
}

export const mockImages = {
  artificialTurf: mockShoeImage,
  futsal: mockShoeImageBlue,
  accessories: mockAccessoryImage,
  jersey: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_3_img.webp?v=132"),
  ball: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/home_collection_4_img.webp?v=132"),
  store: cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/index_blog_review_image.webp?v=132"),
  map: mockPanel("BAN DO CUA HANG", "#288ad6"),
  instagram: [
    cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/instagram_1.jpg?v=132"),
    cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/instagram_2.jpg?v=132"),
    cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/instagram_3.jpg?v=132"),
    cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/instagram_4.jpg?v=132"),
    cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/instagram_5.jpg?v=132"),
    cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/instagram_6.jpg?v=132"),
    cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/instagram_7.jpg?v=132"),
    cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/instagram_8.jpg?v=132"),
    cdn("//cdn.hstatic.net/themes/200000278317/1001484753/14/instagram_9.jpg?v=132")
  ]
};

export const accessoryCategories = [
  {
    title: "Quần áo bóng đá",
    desc: "Áo thi đấu, quần short, đồ tập đội bóng",
    image: mockImages.jersey
  },
  {
    title: "Balo, vớ, lót giày",
    desc: "Phụ kiện dùng hằng tuần cho sân cỏ nhân tạo",
    image: mockImages.accessories
  },
  {
    title: "Trái bóng",
    desc: "Bóng thi đấu, bóng tập luyện và phụ kiện bơm bóng",
    image: mockImages.ball
  },
  {
    title: "Hỗ trợ và phục hồi",
    desc: "Băng cổ chân, bó gối, phụ kiện bảo vệ",
    image: mockPanel("HO TRO PHUC HOI", "#18744b")
  }
];

export const blogDetails = [
  {
    slug: "bai-viet-1",
    title: "REVIEW F50 HYPERFAST LEAGUE TF 'ROAD TO GLORY': CHÂN ÁI CHO ANH EM CHÂN BÈ ÍT",
    titleEn: "F50 HYPERFAST LEAGUE TF 'ROAD TO GLORY' REVIEW: A SPEED BOOT FOR SLIGHTLY WIDE FEET",
    image: "https://cdn.hstatic.net/files/200000278317/article/review-giay-da-bong-adidas-f50-hyperfast-league-tf-1_09bf0a2a30ce4203a959f383f43419ff_large.jpg",
    excerpt:
      "Một mẫu giày tốc độ dễ tiếp cận, form gọn và hợp với người chơi cần cảm giác nhẹ khi xử lý bóng trên sân cỏ nhân tạo.",
    excerptEn:
      "An accessible speed boot with a compact fit for players who want a light touch on artificial turf.",
    sections: [
      "Upper mỏng, ôm chân vừa phải, phù hợp người chơi thích tốc độ nhưng không muốn form quá bó.",
      "Đế TF nhiều đinh nhỏ, bám tốt trên sân phủi phổ biến, chuyển hướng ổn trong các pha tăng tốc ngắn.",
      "Nên thử trực tiếp nếu bạn có bàn chân bè nhiều, vì dòng tốc độ thường cho cảm giác ôm ở phần mu bàn chân."
    ],
    sectionsEn: [
      "The thin upper wraps the foot without feeling overly tight, making it suitable for players who like speed boots but need comfort.",
      "The TF outsole uses many small studs for reliable grip on common local turf and stable short accelerations.",
      "Try it in store if your feet are very wide, because speed boots often feel snug around the instep."
    ]
  },
  {
    slug: "bai-viet-2",
    title: "NHỮNG ĐÔI GIÀY ĐÁ BÓNG KHÁC BIỆT TẠI WORLD CUP 2026",
    titleEn: "STANDOUT FOOTBALL BOOTS AT WORLD CUP 2026",
    image: "https://cdn.hstatic.net/files/200000278317/article/nhung-doi-giay-da-bong-khac-biet-tai-world-cup-2026-1_24ffcc6bb7f04283a4b7b3c73a486a90_large.jpg",
    excerpt:
      "Các phối màu nổi bật, lựa chọn cá nhân hóa và xu hướng boot được cầu thủ chuyên nghiệp sử dụng trên sân khấu lớn.",
    excerptEn:
      "Standout colorways, personal choices and boot trends used by professional players on the biggest stage.",
    sections: [
      "World Cup luôn là nơi các hãng ra mắt phối màu dễ nhận diện nhất, từ neon tốc độ đến trắng bạc tối giản.",
      "Cầu thủ tấn công thường ưu tiên giày nhẹ, còn tiền vệ chuộng form cân bằng giữa kiểm soát và độ ổn định.",
      "Nếu mua để đá sân phủi, nên ưu tiên phiên bản TF/AS thay vì bê nguyên đế FG/SG từ sân cỏ tự nhiên."
    ],
    sectionsEn: [
      "The World Cup is where brands release their most recognizable colorways, from speed-focused neon to clean silver white packs.",
      "Attackers often prefer lightweight boots, while midfielders usually choose a balanced fit for control and stability.",
      "For local turf, prioritize TF or AS versions instead of directly copying FG or SG boots from natural grass."
    ]
  },
  {
    slug: "bai-viet-3",
    title: "CÁCH RONALDO DE LIMA VÀ NIKE MERCURIAL THAY ĐỔI LỊCH SỬ GIÀY ĐÁ BÓNG",
    titleEn: "HOW RONALDO DE LIMA AND NIKE MERCURIAL CHANGED FOOTBALL BOOT HISTORY",
    image: "https://cdn.hstatic.net/files/200000278317/article/ronaldo-va-mercurial-da-thay-doi-lich-su-giay-da-bong-1_c9a14e10331046f5af3630be23516a79_large.jpg",
    excerpt:
      "Mercurial đưa khái niệm giày tốc độ trở thành một biểu tượng, gắn liền với lối chơi bùng nổ của Ronaldo de Lima.",
    excerptEn:
      "Mercurial turned the idea of a speed boot into an icon connected with Ronaldo de Lima's explosive style.",
    sections: [
      "Mercurial tạo khác biệt bằng trọng lượng nhẹ, màu sắc nổi bật và cảm giác tăng tốc rất đặc trưng.",
      "Từ hình ảnh Ronaldo de Lima, dòng giày tốc độ trở thành lựa chọn quen thuộc của tiền đạo và cầu thủ đá cánh.",
      "Ở sân cỏ nhân tạo, các phiên bản Vapor TF hiện đại vẫn giữ tinh thần nhẹ, gọn và phản hồi nhanh."
    ],
    sectionsEn: [
      "Mercurial stood apart through low weight, bold colors and a distinct feeling of acceleration.",
      "Through Ronaldo de Lima, speed boots became a familiar choice for strikers and wingers.",
      "On artificial turf, modern Vapor TF versions still keep that light, compact and responsive spirit."
    ]
  }
];

export function getBlogDetail(slug: string) {
  return blogDetails.find((post) => post.slug === slug);
}

export function getLocalizedBlogPost(post: (typeof blogDetails)[number], language: "vi" | "en") {
  if (language === "vi") {
    return {
      ...post,
      displayTitle: post.title,
      displayExcerpt: post.excerpt,
      displaySections: post.sections
    };
  }

  return {
    ...post,
    displayTitle: post.titleEn,
    displayExcerpt: post.excerptEn,
    displaySections: post.sectionsEn
  };
}

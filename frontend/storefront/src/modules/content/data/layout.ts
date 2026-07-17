import type {
  CustomerServiceLink,
  FooterLinkItem,
  HeaderNavItem,
  SocialLinkItem,
} from "@/modules/content/types";

const SITE_ORIGIN = "";

export const CUSTOMER_SERVICE_LINKS: CustomerServiceLink[] = [
  { label: "Hotline", href: "tel:02466623434" },
  { label: "Live Chat", href: "#" },
  { label: "Messenger", href: "http://messenger.com/t/thoitrangivymoda", external: true },
  { label: "Email", href: "mailto:saleadmin@ivy.com.vn" },
  { label: "Tra cứu đơn hàng", href: "/tra-cuu-don-hang" },
];

export const HEADER_NAV_ITEMS: HeaderNavItem[] = [
  {
    label: "ĐÁ BÓNG",
    href: `${SITE_ORIGIN}/danh-muc/ao-da-bong`,
    variant: "category",
    quickLinks: [
      { label: "TẤT CẢ SẢN PHẨM", href: `${SITE_ORIGIN}/danh-muc/ao-da-bong` },
      { label: "MÙA GIẢI 2024/25 - HÀNG MỚI VỀ", href: `${SITE_ORIGIN}/danh-muc/ao-da-bong`, highlight: true },
    ],
    groups: [
      {
        heading: "TRANG PHỤC",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-da-bong`,
        links: [
          { label: "Áo đấu CLB & ĐTQG", href: `${SITE_ORIGIN}/danh-muc/ao-da-bong` },
          { label: "Quần đá bóng", href: `${SITE_ORIGIN}/danh-muc/quan-da-bong` },
          { label: "Áo tập luyện", href: `${SITE_ORIGIN}/danh-muc/ao-da-bong` },
        ],
      },
      {
        heading: "GIÀY ĐÁ BÓNG",
        headingHref: `${SITE_ORIGIN}/danh-muc/giay-da-bong-fg`,
        links: [
          { label: "Giày cỏ thật (FG)", href: `${SITE_ORIGIN}/danh-muc/giay-da-bong-fg` },
          { label: "Giày cỏ nhân tạo (TF)", href: `${SITE_ORIGIN}/danh-muc/giay-da-bong-tf` },
          { label: "Giày futsal (IC)", href: `${SITE_ORIGIN}/danh-muc/giay-futsal` },
        ],
      },
      {
        heading: "PHỤ KIỆN",
        headingHref: `${SITE_ORIGIN}/danh-muc/phu-kien-da-bong`,
        links: [
          { label: "Phụ kiện đá bóng", href: `${SITE_ORIGIN}/danh-muc/phu-kien-da-bong` },
          { label: "Găng tay thủ môn", href: `${SITE_ORIGIN}/danh-muc/gang-tay-thu-mon` },
          { label: "Bóng thể thao", href: `${SITE_ORIGIN}/danh-muc/bong-the-thao` },
        ],
      },
    ],
  },
  {
    label: "CHẠY BỘ",
    href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo`,
    variant: "category",
    quickLinks: [
      { label: "TẤT CẢ SẢN PHẨM", href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo` },
      { label: "SUMMER RUN 2024", href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo`, highlight: true },
    ],
    groups: [
      {
        heading: "TRANG PHỤC CHẠY BỘ",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-chay-bo`,
        links: [
          { label: "Áo chạy bộ nam", href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo` },
          { label: "Áo chạy bộ nữ", href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo` },
          { label: "Quần chạy bộ", href: `${SITE_ORIGIN}/danh-muc/quan-chay-bo` },
        ],
      },
      {
        heading: "GIÀY CHẠY BỘ",
        headingHref: `${SITE_ORIGIN}/danh-muc/giay-chay-bo`,
        links: [
          { label: "Nike Running", href: `${SITE_ORIGIN}/danh-muc/giay-chay-bo` },
          { label: "Adidas Running", href: `${SITE_ORIGIN}/danh-muc/giay-chay-bo` },
          { label: "Tất cả giày chạy bộ", href: `${SITE_ORIGIN}/danh-muc/giay-chay-bo` },
        ],
      },
    ],
  },
  {
    label: "BÓNG RỔ",
    href: `${SITE_ORIGIN}/danh-muc/ao-bong-ro`,
    variant: "category",
    quickLinks: [{ label: "BASKETBALL COLLECTION", href: `${SITE_ORIGIN}/danh-muc/ao-bong-ro`, highlight: true }],
    groups: [
      {
        heading: "TRANG PHỤC",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-bong-ro`,
        links: [
          { label: "Áo bóng rổ", href: `${SITE_ORIGIN}/danh-muc/ao-bong-ro` },
          { label: "Quần bóng rổ", href: `${SITE_ORIGIN}/danh-muc/ao-bong-ro` },
        ],
      },
      {
        heading: "GIÀY BÓNG RỔ",
        headingHref: `${SITE_ORIGIN}/danh-muc/giay-bong-ro`,
        links: [
          { label: "Nike Basketball", href: `${SITE_ORIGIN}/danh-muc/giay-bong-ro` },
          { label: "Adidas Basketball", href: `${SITE_ORIGIN}/danh-muc/giay-bong-ro` },
          { label: "Puma Basketball", href: `${SITE_ORIGIN}/danh-muc/giay-bong-ro` },
        ],
      },
    ],
  },
  {
    label: "GYM & FITNESS",
    href: `${SITE_ORIGIN}/danh-muc/do-gym-nam`,
    variant: "category",
    quickLinks: [
      { label: "ĐỒ GYM NAM", href: `${SITE_ORIGIN}/danh-muc/do-gym-nam` },
      { label: "ĐỒ GYM NỮ", href: `${SITE_ORIGIN}/danh-muc/do-gym-nu` },
    ],
    groups: [
      {
        heading: "GYM NAM",
        headingHref: `${SITE_ORIGIN}/danh-muc/do-gym-nam`,
        links: [
          { label: "Áo tập gym nam", href: `${SITE_ORIGIN}/danh-muc/do-gym-nam` },
          { label: "Quần tập gym nam", href: `${SITE_ORIGIN}/danh-muc/do-gym-nam` },
          { label: "Compression nam", href: `${SITE_ORIGIN}/danh-muc/do-gym-nam` },
        ],
      },
      {
        heading: "GYM NỮ",
        headingHref: `${SITE_ORIGIN}/danh-muc/do-gym-nu`,
        links: [
          { label: "Sports bra", href: `${SITE_ORIGIN}/danh-muc/do-gym-nu` },
          { label: "Legging nữ", href: `${SITE_ORIGIN}/danh-muc/do-gym-nu` },
          { label: "Áo tank top nữ", href: `${SITE_ORIGIN}/danh-muc/do-gym-nu` },
        ],
      },
    ],
  },
  {
    label: "CẦU LÔNG & TENNIS",
    href: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis`,
    variant: "category",
    quickLinks: [{ label: "TẤT CẢ SẢN PHẨM", href: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis` }],
    groups: [
      {
        heading: "TRANG PHỤC",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis`,
        links: [
          { label: "Áo cầu lông & tennis nam", href: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis` },
          { label: "Áo cầu lông & tennis nữ", href: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis` },
        ],
      },
      {
        heading: "GIÀY CẦU LÔNG & TENNIS",
        headingHref: `${SITE_ORIGIN}/danh-muc/giay-cau-long`,
        links: [
          { label: "Giày cầu lông", href: `${SITE_ORIGIN}/danh-muc/giay-cau-long` },
          { label: "Giày tennis", href: `${SITE_ORIGIN}/danh-muc/giay-cau-long` },
        ],
      },
    ],
  },
  {
    label: "BỘ SƯU TẬP",
    href: `${SITE_ORIGIN}/bo-suu-tap`,
    variant: "collection",
    groups: [
      {
        heading: "Bộ sưu tập nổi bật",
        headingHref: `${SITE_ORIGIN}/bo-suu-tap`,
        links: [
          { label: "MÙA GIẢI 2024/25", href: `${SITE_ORIGIN}/lookbook/mua-giai-2024-25` },
          { label: "ĐỘI TUYỂN VIỆT NAM 2024", href: `${SITE_ORIGIN}/lookbook/doi-tuyen-viet-nam-2024` },
          { label: "SUMMER RUN COLLECTION", href: `${SITE_ORIGIN}/lookbook/summer-run-collection` },
          { label: "BASKETBALL COLLECTION", href: `${SITE_ORIGIN}/lookbook/basketball-collection` },
          { label: "Xem tất cả →", href: `${SITE_ORIGIN}/bo-suu-tap` },
        ],
      },
    ],
  },
  {
    label: "VỀ CHÚNG TÔI",
    href: "#",
    variant: "about",
    groups: [
      {
        heading: "John's Sport Shop",
        headingHref: `${SITE_ORIGIN}/about/gioi-thieu`,
        links: [
          { label: "Giới thiệu cửa hàng", href: `${SITE_ORIGIN}/about/gioi-thieu` },
          { label: "Hệ thống cửa hàng", href: `${SITE_ORIGIN}/lien-he` },
          { label: "Tin tức thể thao", href: `${SITE_ORIGIN}/tin-tuc/tin-chinh` },
          { label: "Liên hệ", href: `${SITE_ORIGIN}/lien-he` },
        ],
      },
    ],
  },
];

export const FOOTER_INTRO_LINKS: FooterLinkItem[] = [
  { label: "Về IVY moda", href: "/about/gioi-thieu" },
  { label: "Tuyển dụng", href: "https://tuyendung.ivy.com.vn", external: true },
  { label: "Hệ thống cửa hàng", href: "/cua-hang" },
];

export const FOOTER_CUSTOMER_SERVICE_LINKS: FooterLinkItem[] = [
  { label: "Chính sách điều khoản", href: "/about/gioi-thieu" },
  { label: "Hướng dẫn mua hàng", href: "/about/gioi-thieu" },
  { label: "Chính sách thanh toán", href: "/about/gioi-thieu" },
  { label: "Chính sách đổi trả", href: "/about/gioi-thieu" },
  { label: "Chính sách bảo hành", href: "/about/gioi-thieu" },
  { label: "Chính sách thẻ thành viên", href: "/about/gioi-thieu" },
  { label: "Q&A", href: "/lien-he" },
];

export const FOOTER_CONTACT_LINKS: FooterLinkItem[] = [
  { label: "Hotline", href: "tel:02466623434" },
  { label: "Email", href: "mailto:saleadmin@ivy.com.vn" },
  { label: "Live Chat", href: "#" },
  { label: "Messenger", href: "http://messenger.com/t/thoitrangivymoda", external: true },
  { label: "Liên hệ", href: "/lien-he" },
];

export const FOOTER_SOCIAL_LINKS: SocialLinkItem[] = [
  {
    label: "Facebook",
    href: "https://www.facebook.com/thoitrangivymoda/",
    src: "/images/ivymoda/common/ic_fb.svg",
    width: 12,
    height: 24,
  },
  {
    label: "Google",
    href: "/",
    src: "/images/ivymoda/common/ic_gg.svg",
    width: 22,
    height: 22,
  },
  {
    label: "Instagram",
    href: "https://www.instagram.com/ivy_moda/",
    src: "/images/ivymoda/common/ic_instagram.svg",
    width: 28,
    height: 28,
  },
];

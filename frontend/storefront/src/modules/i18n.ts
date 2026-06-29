export type Language = "vi" | "en";

export const languageCookieName = "thf-language";

export function normalizeLanguage(value: string | undefined | null): Language {
  return value === "en" || value === "vi" ? value : "vi";
}

export const siteCopy = {
  vi: {
    announcement: "Thanh Hùng Futsal - Giày Đá Bóng Chính Hãng - 2013",
    searchPlaceholder: "Bạn đang tìm kiếm ...",
    quickSuggestions: "Gợi ý nhanh",
    viewAllResults: "Xem tất cả kết quả",
    login: "Đăng nhập",
    register: "Đăng ký",
    cart: "Giỏ hàng",
    nav: {
      home: "TRANG CHỦ",
      products: "TẤT CẢ SẢN PHẨM",
      turf: "GIÀY SÂN CỎ NHÂN TẠO",
      futsal: "GIÀY FUTSAL",
      kids: "GIÀY ĐÁ BÓNG TRẺ EM",
      hotSales: "HOT SALES",
      accessories: "PHỤ KIỆN",
      blog: "TIN TỨC GIÀY",
      customers: "KHÁCH HÀNG",
      stores: "CỬA HÀNG",
      contact: "LIÊN HỆ"
    },
    footer: {
      policies: "CHÍNH SÁCH",
      warranty: "Chính sách bảo hành",
      returns: "Chính sách đổi trả",
      shipping: "Giao nhận hàng",
      privacy: "Bảo mật thông tin",
      fundiin: "Hướng dẫn mua trả sau Fundiin",
      about: "VỀ THF",
      aboutUs: "Về chúng tôi",
      business: "Lĩnh vực kinh doanh",
      description: "Thanh Hùng Futsal cung cấp giày đá bóng, giày futsal và phụ kiện chính hãng từ năm 2013.",
      facebook: "Thanh Hùng Futsal fanpage",
      copyright: "Copyright Thanh Hùng Futsal. Địa chỉ cửa hàng, hotline và thông tin liên hệ được hiển thị tại trang liên hệ."
    },
    productCard: {
      detail: "Xem chi tiết",
      installmentPrefix: "hoặc",
      installmentSuffix: "x3 kỳ với Fundiin"
    },
    floating: {
      label: "Liên hệ nhanh",
      call: "Gọi ngay",
      zalo: "Chat Zalo",
      map: "Xem địa chỉ cửa hàng"
    }
  },
  en: {
    announcement: "Thanh Hung Futsal - Authentic Football Boots - 2013",
    searchPlaceholder: "Search products ...",
    quickSuggestions: "Quick suggestions",
    viewAllResults: "View all results",
    login: "Login",
    register: "Register",
    cart: "Cart",
    nav: {
      home: "HOME",
      products: "ALL PRODUCTS",
      turf: "ARTIFICIAL GRASS BOOTS",
      futsal: "FUTSAL SHOES",
      kids: "KIDS FOOTBALL BOOTS",
      hotSales: "HOT SALES",
      accessories: "ACCESSORIES",
      blog: "BOOT NEWS",
      customers: "CUSTOMERS",
      stores: "STORES",
      contact: "CONTACT"
    },
    footer: {
      policies: "POLICIES",
      warranty: "Warranty policy",
      returns: "Return and exchange policy",
      shipping: "Shipping and delivery",
      privacy: "Privacy policy",
      fundiin: "Fundiin installment guide",
      about: "ABOUT THF",
      aboutUs: "About us",
      business: "Business areas",
      description: "Thanh Hung Futsal has supplied authentic football boots, futsal shoes and accessories since 2013.",
      facebook: "Thanh Hung Futsal fanpage",
      copyright: "Copyright Thanh Hung Futsal. Store address, hotline and contact information are shown on the contact page."
    },
    productCard: {
      detail: "View details",
      installmentPrefix: "or",
      installmentSuffix: "x3 payments with Fundiin"
    },
    floating: {
      label: "Quick contact",
      call: "Call us now",
      zalo: "Chat with us via Zalo",
      map: "View business address"
    }
  }
} as const;

export const homeCopy = {
  vi: {
    joma: "THFC x JOMA",
    hotDeals: "HOT DEALS",
    need: "BẠN ĐANG CẦN TÌM?",
    popular: "GIÀY ĐÁ BÓNG ĐƯỢC ƯA CHUỘNG",
    storeExperience: "TRẢI NGHIỆM MUA SẮM TẠI CỬA HÀNG",
    storeChecks: [
      "Tư vấn chọn giày theo vị trí và mặt sân",
      "Đo chân chuyên dụng tại cửa hàng",
      "Quà tặng vớ, balo và phụ kiện theo chương trình",
      "Thanh toán linh hoạt, hỗ trợ trả góp 0%",
      "Giao hàng Grab và GHTK nhanh trong ngày"
    ],
    storeCount: "2 cửa hàng",
    storeCountDetail: "Thử size trực tiếp",
    experienceYears: "Kinh nghiệm tư vấn",
    featuredProducts: "SẢN PHẨM NỔI BẬT",
    hotSale: "HOTSALE",
    customers: "KHÁCH HÀNG CỦA THF",
    news: "TIN TỨC GIÀY",
    channel: "THANH HUNG FUTSAL 'S CHANNEL",
    seeAll: "XEM TẤT CẢ",
    seeMore: "Xem thêm"
  },
  en: {
    joma: "THFC x JOMA",
    hotDeals: "HOT DEALS",
    need: "WHAT ARE YOU LOOKING FOR?",
    popular: "POPULAR FOOTBALL BOOTS",
    storeExperience: "SHOPPING EXPERIENCE AT STORE",
    storeChecks: [
      "Boot consultation by position and surface",
      "In-store foot measurement support",
      "Socks, bag and accessory gifts by campaign",
      "Flexible payment with 0% installment support",
      "Fast same-day delivery in Ho Chi Minh City"
    ],
    storeCount: "2 stores",
    storeCountDetail: "Try sizes in person",
    experienceYears: "Consulting experience",
    featuredProducts: "FEATURED PRODUCTS",
    hotSale: "HOTSALE",
    customers: "THF CUSTOMERS",
    news: "BOOT NEWS",
    channel: "THANH HUNG FUTSAL 'S CHANNEL",
    seeAll: "SEE ALL",
    seeMore: "See more"
  }
} as const;

export const commonPageCopy = {
  vi: {
    home: "Trang chủ",
    category: "Danh mục",
    allProducts: "Tất cả sản phẩm",
    products: "Sản phẩm",
    cart: "Giỏ hàng",
    search: "Tìm kiếm",
    productsFound: "sản phẩm",
    productFilter: "Lọc sản phẩm",
    sortBy: "Sắp xếp theo:",
    activeFilters: "Bộ lọc đang chọn",
    clearAll: "Xóa hết",
    readMore: "Đọc thêm",
    notFoundProduct: "Không tìm thấy sản phẩm",
    notFoundPost: "Không tìm thấy bài viết"
  },
  en: {
    home: "Home",
    category: "Category",
    allProducts: "All products",
    products: "Products",
    cart: "Cart",
    search: "Search",
    productsFound: "products",
    productFilter: "Filter products",
    sortBy: "Sort by:",
    activeFilters: "Active filters",
    clearAll: "Clear all",
    readMore: "Read more",
    notFoundProduct: "Product not found",
    notFoundPost: "Post not found"
  }
} as const;

export const contactCopy = {
  vi: {
    metadataTitle: "Liên hệ | Thanh Hùng Futsal",
    breadcrumbHome: "Trang chủ",
    breadcrumbCurrent: "Liên hệ",
    kicker: "Liên hệ cửa hàng",
    title: "LIÊN HỆ THANH HÙNG FUTSAL",
    intro: "Gửi thông tin cần tư vấn size, tình trạng hàng hoặc đặt lịch ghé cửa hàng. Đội ngũ THF sẽ phản hồi theo khung giờ làm việc.",
    hotline: "Hotline: 0900 000 000",
    email: "Email: support@thanhhungfutsal.vn",
    storeSystem: "Hệ thống cửa hàng Thanh Hùng Futsal",
    mapAlt: "Bản đồ cửa hàng Thanh Hùng Futsal",
    mapLabel: "BẢN ĐỒ CỬA HÀNG",
    fullName: "Họ tên",
    fullNamePlaceholder: "Nhập họ tên",
    phone: "Số điện thoại",
    phonePlaceholder: "Nhập số điện thoại",
    message: "Nội dung",
    messagePlaceholder: "Bạn cần tư vấn sản phẩm, size hay đơn hàng?",
    submit: "GỬI LIÊN HỆ"
  },
  en: {
    metadataTitle: "Contact | Thanh Hung Futsal",
    breadcrumbHome: "Home",
    breadcrumbCurrent: "Contact",
    kicker: "Store contact",
    title: "CONTACT THANH HUNG FUTSAL",
    intro: "Send your size request, stock question or store visit booking. The THF team will reply during business hours.",
    hotline: "Hotline: 0900 000 000",
    email: "Email: support@thanhhungfutsal.vn",
    storeSystem: "Thanh Hung Futsal store system",
    mapAlt: "Thanh Hung Futsal store map",
    mapLabel: "STORE MAP",
    fullName: "Full name",
    fullNamePlaceholder: "Enter your full name",
    phone: "Phone number",
    phonePlaceholder: "Enter your phone number",
    message: "Message",
    messagePlaceholder: "Need product, size or order support?",
    submit: "SEND MESSAGE"
  }
} as const;

export const accountCopy = {
  vi: {
    loginTitle: "ĐĂNG NHẬP",
    loginMetadata: "Đăng nhập | Thanh Hùng Futsal",
    password: "Mật khẩu",
    passwordPlaceholder: "Nhập mật khẩu",
    loginButton: "Đăng nhập",
    createAccount: "Tạo tài khoản mới",
    registerTitle: "ĐĂNG KÝ",
    registerMetadata: "Đăng ký | Thanh Hùng Futsal",
    fullName: "Họ tên",
    fullNamePlaceholder: "Nhập họ tên",
    createPassword: "Tạo mật khẩu",
    registerButton: "Tạo tài khoản",
    haveAccount: "Đã có tài khoản"
  },
  en: {
    loginTitle: "LOGIN",
    loginMetadata: "Login | Thanh Hung Futsal",
    password: "Password",
    passwordPlaceholder: "Enter your password",
    loginButton: "Login",
    createAccount: "Create a new account",
    registerTitle: "REGISTER",
    registerMetadata: "Register | Thanh Hung Futsal",
    fullName: "Full name",
    fullNamePlaceholder: "Enter your full name",
    createPassword: "Create a password",
    registerButton: "Create account",
    haveAccount: "Already have an account"
  }
} as const;

export const blogCopy = {
  vi: {
    metadataTitle: "Tin tức giày | Thanh Hùng Futsal",
    breadcrumb: "Tin tức giày",
    title: "TIN TỨC GIÀY",
    intro: "Review giày đá bóng, cập nhật phối màu mới, kinh nghiệm chọn form và câu chuyện phía sau các dòng giày được anh em sân phủ quan tâm.",
    kicker: "Review giày",
    noteHeading: "Ghi chú kiểm kê"
  },
  en: {
    metadataTitle: "Boot news | Thanh Hung Futsal",
    breadcrumb: "Boot news",
    title: "BOOT NEWS",
    intro: "Football boot reviews, new colorway updates, fit guidance and stories behind the models that local players care about.",
    kicker: "Boot review",
    noteHeading: "Review note"
  }
} as const;

export const productDetailCopy = {
  vi: {
    warrantyTitles: [
      "1. Thời hạn bảo hành",
      "2. Trường hợp được bảo hành miễn phí",
      "3. Thông tin cần có để được hỗ trợ bảo hành",
      "4. Trường hợp không áp dụng bảo hành miễn phí",
      "5. Hỗ trợ sửa chữa có tính phí",
      "6. Thời gian xử lý",
      "7. Sau thời gian bảo hành"
    ],
    warrantyBody: "Sản phẩm được hỗ trợ theo chính sách của Thanh Hùng Futsal. Vui lòng giữ hóa đơn, hình ảnh sản phẩm và liên hệ cửa hàng để được kiểm tra tình trạng thực tế.",
    related: "SẢN PHẨM LIÊN QUAN"
  },
  en: {
    warrantyTitles: [
      "1. Warranty period",
      "2. Cases covered by free warranty",
      "3. Information required for warranty support",
      "4. Cases not covered by free warranty",
      "5. Paid repair support",
      "6. Processing time",
      "7. After the warranty period"
    ],
    warrantyBody: "Products are supported under Thanh Hung Futsal policies. Please keep your invoice, product photos and contact the store so the team can inspect the actual condition.",
    related: "RELATED PRODUCTS"
  }
} as const;

export const buyBoxCopy = {
  vi: {
    brand: "Thương hiệu",
    status: "Tình trạng",
    inStock: "Còn hàng",
    chooseSize: "Chọn size",
    quantity: "Số lượng",
    decrease: "Giảm số lượng",
    increase: "Tăng số lượng",
    addToCart: "Thêm vào giỏ",
    consult: "Gọi tư vấn size và tình trạng hàng",
    policyDetail: "Thông tin chi tiết được áp dụng theo từng chương trình.",
    policies: ["ƯU ĐÃI TẶNG KÈM", "ĐỔI HÀNG DỄ DÀNG", "CHÍNH SÁCH GIAO HÀNG", "THANH TOÁN TIỆN LỢI"],
    added: "Đã thêm vào giỏ hàng",
    closePopup: "Đóng popup",
    viewCart: "Xem giỏ hàng",
    continueShopping: "Tiếp tục mua",
    size: "Size",
    quantityShort: "Số lượng"
  },
  en: {
    brand: "Brand",
    status: "Status",
    inStock: "In stock",
    chooseSize: "Choose size",
    quantity: "Quantity",
    decrease: "Decrease quantity",
    increase: "Increase quantity",
    addToCart: "Add to cart",
    consult: "Call for size and stock advice",
    policyDetail: "Details apply according to each campaign.",
    policies: ["BUNDLE OFFERS", "EASY EXCHANGE", "SHIPPING POLICY", "CONVENIENT PAYMENT"],
    added: "Added to cart",
    closePopup: "Close popup",
    viewCart: "View cart",
    continueShopping: "Continue shopping",
    size: "Size",
    quantityShort: "Quantity"
  }
} as const;

export const collectionFallbackCopy = {
  vi: {
    description: "Trang collection mock dùng chung cho các danh mục con trong menu. Dữ liệu có thể nối API thật sau khi kiểm kê xong giao diện.",
    suggested: "SẢN PHẨM GỢI Ý"
  },
  en: {
    description: "Shared mock collection page for submenu categories. The data can be connected to real APIs after the interface review.",
    suggested: "SUGGESTED PRODUCTS"
  }
} as const;

export const aboutStoreCopy = {
  vi: {
    metadataTitle: "Cửa hàng | Thanh Hùng Futsal",
    breadcrumb: "Cửa hàng",
    kicker: "Từ 2013",
    title: "CỬA HÀNG THANH HÙNG FUTSAL",
    intro: "Thanh Hùng Futsal là địa chỉ chuyên giày đá bóng, giày futsal và phụ kiện thi đấu chính hãng. Trải nghiệm cửa hàng được thiết kế để khách thử form, chọn size và nhận tư vấn nhanh.",
    alt: "Cửa hàng Thanh Hùng Futsal",
    values: [
      "Bán lẻ giày đá bóng chính hãng từ 2013",
      "Tư vấn theo mặt sân, vị trí và form chân",
      "Hỗ trợ đổi size theo chính sách cửa hàng",
      "Giao hàng nhanh nội thành và toàn quốc"
    ]
  },
  en: {
    metadataTitle: "Stores | Thanh Hung Futsal",
    breadcrumb: "Stores",
    kicker: "Since 2013",
    title: "THANH HUNG FUTSAL STORES",
    intro: "Thanh Hung Futsal specializes in authentic football boots, futsal shoes and match accessories. The in-store experience helps customers try fit, choose size and get fast advice.",
    alt: "Thanh Hung Futsal store",
    values: [
      "Retailing authentic football boots since 2013",
      "Advice by surface, position and foot shape",
      "Size exchange support under store policy",
      "Fast local and nationwide delivery"
    ]
  }
} as const;

export const customerCopy = {
  vi: {
    metadataTitle: "Khách hàng của THF | Thanh Hùng Futsal",
    breadcrumb: "Khách hàng",
    kicker: "THF Community",
    title: "KHÁCH HÀNG CỦA THANH HÙNG FUTSAL",
    intro: "Không gian ghi lại hình ảnh khách hàng, đội bóng và những khoảnh khắc thử giày, chọn size, nhận tư vấn tại cửa hàng.",
    imageAlt: "Khách hàng Thanh Hùng Futsal",
    caption: "Khách hàng THF"
  },
  en: {
    metadataTitle: "THF customers | Thanh Hung Futsal",
    breadcrumb: "Customers",
    kicker: "THF Community",
    title: "THANH HUNG FUTSAL CUSTOMERS",
    intro: "A space for customer photos, teams and in-store moments while trying boots, choosing sizes and getting advice.",
    imageAlt: "Thanh Hung Futsal customer",
    caption: "THF customer"
  }
} as const;

export const staticFallbackCopy = {
  vi: {
    kicker: "Thông tin THF",
    fallbackBody: ["Nội dung mock dùng để hoàn thiện kiểm kê giao diện.", "Có thể thay bằng nội dung CMS/API thật trong giai đoạn tích hợp."],
    pages: {
      "chinh-sach-bao-hanh": {
        title: "CHÍNH SÁCH BẢO HÀNH",
        body: ["Hỗ trợ kiểm tra lỗi sản phẩm theo điều kiện thực tế.", "Khách hàng giữ hóa đơn và hình ảnh sản phẩm để được hỗ trợ nhanh."]
      },
      "chinh-sach-doi-tra": {
        title: "CHÍNH SÁCH ĐỔI TRẢ",
        body: ["Hỗ trợ đổi size khi sản phẩm còn nguyên tình trạng sử dụng.", "Thời gian và điều kiện đổi trả áp dụng theo từng chương trình."]
      },
      "giao-nhan-hang": {
        title: "GIAO NHẬN HÀNG",
        body: ["Giao hàng nội thành qua Grab và giao toàn quốc qua đơn vị vận chuyển.", "Khách hàng có thể liên hệ để kiểm tra tình trạng giao hàng."]
      },
      "bao-mat-thong-tin": {
        title: "BẢO MẬT THÔNG TIN",
        body: ["Thông tin khách hàng chỉ dùng cho tư vấn, giao hàng và chăm sóc sau mua.", "Không chia sẻ dữ liệu cá nhân cho bên thứ ba ngoài mục đích xử lý đơn hàng."]
      },
      "huong-dan-mua-tra-sau-fundiin": {
        title: "HƯỚNG DẪN MUA TRẢ SAU FUNDIIN",
        body: ["Chọn sản phẩm, xác nhận thông tin và làm theo hướng dẫn thanh toán trả sau.", "Điều kiện duyệt phụ thuộc vào đơn vị thanh toán."]
      },
      "linh-vuc-kinh-doanh": {
        title: "LĨNH VỰC KINH DOANH",
        body: ["Giày đá bóng, giày futsal, phụ kiện thi đấu và trang phục đội bóng.", "Tập trung vào sản phẩm chính hãng và tư vấn chọn giày theo nhu cầu."]
      }
    }
  },
  en: {
    kicker: "THF information",
    fallbackBody: ["Mock content used to complete the interface review.", "This can be replaced with real CMS/API content during integration."],
    pages: {
      "chinh-sach-bao-hanh": {
        title: "WARRANTY POLICY",
        body: ["Support for checking product issues based on actual condition.", "Customers should keep invoices and product photos for faster support."]
      },
      "chinh-sach-doi-tra": {
        title: "RETURN AND EXCHANGE POLICY",
        body: ["Size exchange is supported when products remain in eligible condition.", "Timing and conditions apply according to each campaign."]
      },
      "giao-nhan-hang": {
        title: "SHIPPING AND DELIVERY",
        body: ["Local delivery is handled by Grab and nationwide delivery by shipping partners.", "Customers can contact the store to check delivery status."]
      },
      "bao-mat-thong-tin": {
        title: "PRIVACY POLICY",
        body: ["Customer information is used only for consultation, delivery and after-sales care.", "Personal data is not shared with third parties except for order processing."]
      },
      "huong-dan-mua-tra-sau-fundiin": {
        title: "FUNDIIN INSTALLMENT GUIDE",
        body: ["Choose a product, confirm information and follow the installment payment instructions.", "Approval conditions depend on the payment provider."]
      },
      "linh-vuc-kinh-doanh": {
        title: "BUSINESS AREAS",
        body: ["Football boots, futsal shoes, match accessories and team apparel.", "Focused on authentic products and advice by customer needs."]
      }
    }
  }
} as const;

export const galleryCopy = {
  vi: {
    imageAlt: (name: string, index: number) => `${name} ảnh ${index + 1}`,
    wheelHint: "Cuộn để đổi ảnh",
    zoomLabel: "Phóng to ảnh",
    thumbnailsLabel: "Chọn ảnh sản phẩm",
    viewImage: (index: number, name: string) => `Xem ảnh ${index + 1} của ${name}`
  },
  en: {
    imageAlt: (name: string, index: number) => `${name} image ${index + 1}`,
    wheelHint: "Scroll to change image",
    zoomLabel: "Zoom image",
    thumbnailsLabel: "Choose product image",
    viewImage: (index: number, name: string) => `View image ${index + 1} of ${name}`
  }
} as const;

export const homeDataCopy = {
  vi: {
    serviceStrip: "Cam kết dịch vụ",
    serviceHighlights: [
      ["CAM KẾT CHÍNH HÃNG", "100% sản phẩm chính hãng, bảo hành theo chính sách hãng."],
      ["GIAO HÀNG TOÀN QUỐC", "Đóng gói kỹ, hỗ trợ giao nhanh nội thành trong ngày."],
      ["ĐỔI SIZE LINH HOẠT", "Hỗ trợ đổi size khi sản phẩm còn tem và chưa qua sử dụng."],
      ["TRẢ GÓP 0%", "Thanh toán linh hoạt qua Fundiin với 3 kỳ thanh toán."]
    ],
    quickCategories: [
      "GIÀY ĐÁ BÓNG SÂN CỎ NHÂN TẠO",
      "GIÀY ĐÁ BÓNG SÂN FUTSAL",
      "BỘ QUẦN ÁO THI ĐẤU",
      "TRÁI BÓNG THI ĐẤU"
    ],
    popularCategories: [
      "NIKE TIEMPO LIGERA",
      "MIZUNO ALPHA 3",
      "MERCURIAL VAPOR 16 PRO TF",
      "JOMA TOP FLEX",
      "SIGNATURE BOOTS",
      "ZOCKER"
    ],
    productTabLabels: ["SẢN PHẨM MỚI", "BÁN CHẠY", "GIÀY FUTSAL", "GIÀY CỎ NHÂN TẠO"]
  },
  en: {
    serviceStrip: "Service commitments",
    serviceHighlights: [
      ["AUTHENTIC GUARANTEE", "100% authentic products, warranty per brand policy."],
      ["NATIONWIDE DELIVERY", "Careful packing, same-day express within the city."],
      ["FLEXIBLE SIZE EXCHANGE", "Size exchange supported when product is unused with tag."],
      ["0% INSTALLMENT", "Flexible payment via Fundiin with 3 installments."]
    ],
    quickCategories: [
      "ARTIFICIAL TURF BOOTS",
      "FUTSAL SHOES",
      "MATCH KITS",
      "MATCH BALLS"
    ],
    popularCategories: [
      "NIKE TIEMPO LIGERA",
      "MIZUNO ALPHA 3",
      "MERCURIAL VAPOR 16 PRO TF",
      "JOMA TOP FLEX",
      "SIGNATURE BOOTS",
      "ZOCKER"
    ],
    productTabLabels: ["NEW ARRIVALS", "BEST SELLERS", "FUTSAL SHOES", "ARTIFICIAL TURF BOOTS"]
  }
} as const;

export const cartPageCopy = {
  vi: {
    metadataTitle: "Giỏ hàng | Thanh Hùng Futsal"
  },
  en: {
    metadataTitle: "Cart | Thanh Hung Futsal"
  }
} as const;

export const searchPageCopy = {
  vi: {
    metadataTitle: "Tìm kiếm | Thanh Hùng Futsal",
    heading: "TÌM KIẾM SẢN PHẨM",
    resultsFor: (count: number, q: string) => `Có ${count} kết quả cho "${q}".`,
    noKeyword: "Nhập từ khóa để tìm nhanh giày, thương hiệu hoặc danh mục.",
    emptyResult: "Không tìm thấy sản phẩm phù hợp."
  },
  en: {
    metadataTitle: "Search | Thanh Hung Futsal",
    heading: "SEARCH PRODUCTS",
    resultsFor: (count: number, q: string) => `${count} results for "${q}".`,
    noKeyword: "Enter a keyword to quickly find shoes, brands or categories.",
    emptyResult: "No matching products found."
  }
} as const;

export const productsPageCopy = {
  vi: {
    metadataTitle: "Tất cả sản phẩm | Thanh Hùng Futsal"
  },
  en: {
    metadataTitle: "All products | Thanh Hung Futsal"
  }
} as const;

export const collectionMetaCopy = {
  vi: {
    artificialTurf: "Giày sân cỏ nhân tạo | Thanh Hùng Futsal",
    futsal: "Giày đá bóng sân futsal chính hãng | Thanh Hùng Futsal",
    kids: "Giày đá bóng trẻ em | Thanh Hùng Futsal",
    hotSales: "Hot Sales | Thanh Hùng Futsal",
    accessories: "Phụ kiện chính hãng | Thanh Hùng Futsal",
    artificialTurfBreadcrumb: "Giày sân cỏ nhân tạo",
    futsalBreadcrumb: "GIÀY ĐÁ BÓNG SÂN FUTSAL",
    kidsBreadcrumb: "GIÀY ĐÁ BÓNG TRẺ EM",
    accessoriesBreadcrumb: "Phụ kiện"
  },
  en: {
    artificialTurf: "Artificial grass boots | Thanh Hung Futsal",
    futsal: "Authentic futsal shoes | Thanh Hung Futsal",
    kids: "Kids football boots | Thanh Hung Futsal",
    hotSales: "Hot Sales | Thanh Hung Futsal",
    accessories: "Authentic accessories | Thanh Hung Futsal",
    artificialTurfBreadcrumb: "Artificial grass boots",
    futsalBreadcrumb: "FUTSAL SHOES",
    kidsBreadcrumb: "KIDS FOOTBALL BOOTS",
    accessoriesBreadcrumb: "Accessories"
  }
} as const;

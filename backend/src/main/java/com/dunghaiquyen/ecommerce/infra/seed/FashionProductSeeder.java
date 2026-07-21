package com.dunghaiquyen.ecommerce.infra.seed;

import com.dunghaiquyen.ecommerce.modules.banner.entity.Banner;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerItem;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerStatus;
import com.dunghaiquyen.ecommerce.modules.banner.repository.BannerItemRepository;
import com.dunghaiquyen.ecommerce.modules.banner.repository.BannerRepository;
import com.dunghaiquyen.ecommerce.modules.brand.entity.Brand;
import com.dunghaiquyen.ecommerce.modules.brand.entity.BrandStatus;
import com.dunghaiquyen.ecommerce.modules.brand.repository.BrandRepository;
import com.dunghaiquyen.ecommerce.modules.category.entity.Category;
import com.dunghaiquyen.ecommerce.modules.category.entity.CategoryNodeType;
import com.dunghaiquyen.ecommerce.modules.category.entity.CategoryStatus;
import com.dunghaiquyen.ecommerce.modules.category.repository.CategoryRepository;
import com.dunghaiquyen.ecommerce.modules.collection.entity.Collection;
import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionStatus;
import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionType;
import com.dunghaiquyen.ecommerce.modules.collection.entity.ProductCollection;
import com.dunghaiquyen.ecommerce.modules.collection.repository.CollectionRepository;
import com.dunghaiquyen.ecommerce.modules.collection.repository.ProductCollectionRepository;
import com.dunghaiquyen.ecommerce.modules.product.entity.Gender;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductImage;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductType;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductImageRepository;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds ~100 sports products (football, running, basketball, gym, badminton)
 * for John's Sport Shop. Run once with APP_SEED_ENABLED=true.
 *
 * Idempotent: no-op if the "nike" brand already exists.
 * On first run, clears any previous seed data before inserting.
 */
@Service
public class FashionProductSeeder {

    private static final Logger log = LoggerFactory.getLogger(FashionProductSeeder.class);
    private static final String IMG = "/images/ivymoda/products/";
    private static final String LOOKBOOK_IMG = "/images/ivymoda/lookbook/";

    private static final String[] IMGS = {
        IMG + "005b1d8bcd067343383e7477f47ee74a.webp",
        IMG + "03a90dba7165fd770b5ee22d5a4460a0.jpg",
        IMG + "03b041ffa59ceab4ede5c065b47dde0b.jpg",
        IMG + "049ba3d1ae8ce2ec91c7cdf00d01e0d7.webp",
        IMG + "06ae1f0d2157a7b14479f3e01b18c3bc.webp",
        IMG + "0a15a4f09c111bfb2f2cff383d897df6.jpg",
        IMG + "0b3471fa4a675f6f098c0c4c1e310076.webp",
        IMG + "0bdfa4229827fcdf3d0da8b2b6c8ed02.webp",
        IMG + "0d52bbb2915f7fcfaf1cc56e87c92671.jpg",
        IMG + "112d32ee60e740c7e170c854621c1994.webp",
        IMG + "11e658978c71fd09d16ede2679404ea5.jpg",
        IMG + "145e5c1c63d03a76d10f23d4c89b7723.webp",
        IMG + "15562de9138d1c5927ee7f195c512e06.webp",
        IMG + "183ee7c3ea75e886fdc61cfbe9a1ffd3.webp",
        IMG + "187bf4a79415058a70d5c3a59ec0aba3.webp",
        IMG + "1b87780e1a7cb8bfed979da5cfa0ce59.webp",
        IMG + "1c576e917832620b2d4b0f3cb78642a5.webp",
        IMG + "20dff4ffed5fafd3613bd8258e64d03e.webp",
        IMG + "23b930813fdb472c25dd09fe6f6fdf66.jpg",
        IMG + "27c9d3f6b05529b2717358b49a9a3506.jpg",
        IMG + "28a6ba501f9b7a487b8de665b0cefba3.webp",
        IMG + "2c59162644d025dc6bb44ba2484123c1.webp",
        IMG + "2cf9b85228b3e78f22cfc3718f6e24b6.webp",
        IMG + "31a49a220313401e83d40be5d328f558.jpg",
        IMG + "3207e9df533a53bf889a9f97aa7b72ab.jpg",
        IMG + "3cea5000260703bb54bdd346b6365704.webp",
        IMG + "40146f76a13c6b530ecd18f05ef25684.jpg",
        IMG + "4120c25ce0ebf4a93b936e06f6a502da.webp",
        IMG + "4611f30351e0b708a3887c6a03091f0c.jpg",
        IMG + "47df0db8ebdef29d758b7523ce8881be.jpg",
        IMG + "47ff9628191a3696f1f4d20ba26a0f6c.jpg",
        IMG + "4cbd9707ef4e42edc60c05efb73da645.jpg",
        IMG + "51651e3abc6fa445c74bf7cd4c2aa581.jpg",
        IMG + "5877f3534b232631d1cf7fdd8277859b.webp",
        IMG + "58963bb1a8709d6fb2fbc0df4c95cac3.jpg",
        IMG + "58b0bbc0b98b112e263a1d7efa9dc209.webp",
        IMG + "5b03e8a2f3a8fb3e0b61c82bdccf9bbf.jpg",
        IMG + "608ff62c918cfef47d372e1e4eaddcd7.jpg",
        IMG + "60aad941648b59197eac563b91c400ea.webp",
        IMG + "61e87a6b08767912bbfbbcdfd4eabac4.webp",
        IMG + "636d0ca04b95d3d87c2e2f0d49400063.webp",
        IMG + "636e80d7ccdd48a9e423ab1f92681f35.webp",
        IMG + "69ed1eb33d3be7e0a272c6620db4495e.webp",
        IMG + "6c11f0f699cbf42f24f64c95c7923276.webp",
        IMG + "6d99f9f974702e2ea3607b0bd6594d6c.webp",
        IMG + "708815ca100964edf6068baf88e0a6d1.webp",
        IMG + "78524582f9518368d095df8d1602b780.webp",
        IMG + "7b92778cdf3ca6ecdda23623e14c73dd.webp",
        IMG + "7ffdeee9bfeaff97eabd2e6aa3f96244.webp",
        IMG + "8139ba69ca55e890e4d6da3ccd859d34.webp",
        IMG + "8228728a826d3a5e95b9b785550b471c.jpg",
        IMG + "82a734bb9824fceebe0bccb1052e2221.webp",
        IMG + "8375bf5e76b75eb981645f18ac10bfa9.jpg",
        IMG + "84886754e0482919c9469f0df37ef6df.webp",
        IMG + "84ceec2e174853ef07c4201f1c60aee3.webp",
        IMG + "8649c48c30e74fa673fe744fa8409d2b.jpg",
        IMG + "86e4822d0199db1c3f1e0cb46d6f5d19.webp",
        IMG + "8c6fdb7b299f90db3971b659f19b5ad6.webp",
        IMG + "8f4eb4a124198497b85bf2efdaa811a5.jpg",
        IMG + "97e8ac59e39e7049f36aa04a17770521.webp",
        IMG + "9998531dfa3609f3f661088b76f387eb.webp",
        IMG + "9b5bf42f1df7ad28f286fab5b2cd6622.webp",
        IMG + "a83bc666879e970ebfa39facf7e4ef4f.webp",
        IMG + "a9ce6b5f673854c421ceeedbf6b53b34.webp",
        IMG + "a9f9d92197126efdabe0be59ef2006e7.webp",
        IMG + "aba820e5aca51542c7fea0a4da78854e.jpg",
        IMG + "ac6ca29438fd3746a3c2e84b75b2a46a.webp",
        IMG + "ae04423ceee236859f755a7e2f066700.webp",
        IMG + "b10d17283df624309e40f049630a8672.webp",
        IMG + "b2bda0b9f83a7591bf3ec4b05b8e7ce1.jpg",
        IMG + "b5c2adb3a6a52d3abd2d6f91de8c0498.webp",
        IMG + "b6264126391943ea38c630483f9956a1.webp",
        IMG + "b8b5eaf1a0640756809e0bdfeb2039ad.webp",
        IMG + "ba427d7547f973c1c0b1a3aac2cec1c3.webp",
        IMG + "bf5ddde9bd5745bd01e69c3280568c49.webp",
        IMG + "c45c97be4ba6a6bfee63efac1f2b6ab1.webp",
        IMG + "c474e1583d40e88fd509fcd844100e19.webp",
        IMG + "c507ceb51e306d4a91f01a6a5dca1f0c.jpg",
        IMG + "c6ca0a664adff97ed17bcefbe0c3edae.jpg",
        IMG + "c7da62588610e789d949ac6875d6dd71.webp",
        IMG + "c8f2a665529316ae727ddae0484831ea.webp",
        IMG + "c93add532002f65e80d83860a13e560c.webp",
        IMG + "cbb4a2c4c9b1d71883b32cecdf956f86.webp",
        IMG + "d1bbd95c2c658214dcc569873c556c6d.webp",
        IMG + "d2c39ef280405d7aba165006ab83dc40.webp",
        IMG + "d557ee88abffdc4720a47df783bcc090.jpg",
        IMG + "d816404f888045b43187682ff290f2aa.jpg",
        IMG + "d99c5eb38fc914895b16c4dd3edf780a.jpg",
        IMG + "daec5eab3ff306ec23fa0909f7c50984.jpg",
        IMG + "e02a2d4560504385fde2c344ddf4578f.webp",
        IMG + "e13c4d413613d49fe2aa50729f0496d1.jpg",
        IMG + "e16447016e1280fb66d221bed6a12e4d.webp",
        IMG + "e30b9c0a0dd23cbc40aacdb3e0cd47f4.webp",
        IMG + "e5b7e87599dc84b8f98af3ffdd98e5e6.webp",
        IMG + "e5d337aa2ad7f994a507778c310bfa48.webp",
        IMG + "e6aa6d8ad9a789589acb4ab9b7f345f7.jpg",
        IMG + "ee32ed354accc0fde3eec39f7e4e9ad6.webp",
        IMG + "f27a1d44ef45cf54286c1ed89c88f93d.webp",
        IMG + "f2bb04611bf0f1d67b35d57b7a34e402.jpg",
        IMG + "f3527d374c3e038d40454bd667b0ed23.webp",
        IMG + "fa1256828bf73b0aaf91b71c95fef5f0.jpg",
        IMG + "fc2170d255113c1972871682532dca10.webp",
        IMG + "fe189f3f06ea21d1e71b9e78f49a7f6f.webp",
        IMG + "ffef76478137d3b4273a125121c96be7.webp"
    };

    @PersistenceContext
    private EntityManager em;

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final CollectionRepository collectionRepository;
    private final ProductCollectionRepository productCollectionRepository;
    private final BannerRepository bannerRepository;
    private final BannerItemRepository bannerItemRepository;

    public FashionProductSeeder(
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository imageRepository,
            CollectionRepository collectionRepository,
            ProductCollectionRepository productCollectionRepository,
            BannerRepository bannerRepository,
            BannerItemRepository bannerItemRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.collectionRepository = collectionRepository;
        this.productCollectionRepository = productCollectionRepository;
        this.bannerRepository = bannerRepository;
        this.bannerItemRepository = bannerItemRepository;
    }

    @Transactional
    public void seed() {
        if (productRepository.findBySlug("nike-ao-dau-barcelona-2425-san-nha").isPresent()) {
            log.info("Sports seed already applied, skipping.");
            return;
        }

        // TRUNCATE CASCADE lets PostgreSQL cascade through all FK dependencies automatically.
        log.info("Clearing existing seed data...");
        em.createNativeQuery(
                "TRUNCATE TABLE brands, categories, collections, banners, orders, carts, wishlists CASCADE"
        ).executeUpdate();
        em.flush();
        em.clear();
        log.info("Existing data cleared.");

        // ─── BRANDS ───────────────────────────────────────────────────────────────
        Brand nike = brand("nike",         "Nike",         "Just Do It – Thương hiệu thể thao số 1 thế giới.");
        Brand adidas = brand("adidas",     "Adidas",       "Impossible is Nothing – Thương hiệu thể thao Đức hàng đầu.");
        Brand puma = brand("puma",         "Puma",         "Forever Faster – Phong cách thể thao từ Đức.");
        Brand ua = brand("under-armour",   "Under Armour", "I Will – Trang phục thể thao hiệu suất cao.");

        // ─── CATEGORIES ───────────────────────────────────────────────────────────
        Category rootAo = catGroup("ao", "Ao", null, 10);
        Category rootQuan = catGroup("quan", "Quan", null, 20);
        Category rootGiay = catGroup("giay", "Giay", null, 30);
        Category rootPhuKien = catGroup("phu-kien", "Phu kien", null, 40);

        // Bóng đá
        Category cAoDbong    = cat("ao-da-bong",        "Áo Đá Bóng", rootAo, 110);
        Category cQuanDbong  = cat("quan-da-bong",      "Quần Đá Bóng", rootQuan, 120);
        Category cGiayFG     = cat("giay-da-bong-fg",   "Giày Đá Bóng Cỏ Thật (FG)", rootGiay, 130);
        Category cGiayTF     = cat("giay-da-bong-tf",   "Giày Đá Bóng Cỏ Nhân Tạo (TF)", rootGiay, 140);
        Category cGiayFutsal = cat("giay-futsal",       "Giày Futsal (IC)", rootGiay, 150);
        Category cPhuKienDB  = cat("phu-kien-da-bong",  "Phụ Kiện Đá Bóng", rootPhuKien, 160);
        Category cGangTay    = cat("gang-tay-thu-mon",  "Găng Tay Thủ Môn", rootPhuKien, 170);
        Category cBong       = cat("bong-the-thao",     "Bóng Thể Thao", rootPhuKien, 180);
        // Chạy bộ
        Category cAoChay     = cat("ao-chay-bo",        "Áo Chạy Bộ", rootAo, 210);
        Category cQuanChay   = cat("quan-chay-bo",      "Quần Chạy Bộ", rootQuan, 220);
        Category cGiayChay   = cat("giay-chay-bo",      "Giày Chạy Bộ", rootGiay, 230);
        // Bóng rổ
        Category cAoBR       = cat("ao-bong-ro",        "Áo Bóng Rổ", rootAo, 310);
        Category cGiayBR     = cat("giay-bong-ro",      "Giày Bóng Rổ", rootGiay, 320);
        // Gym
        Category cGymNam     = cat("do-gym-nam",        "Đồ Gym Nam", rootAo, 410);
        Category cGymNu      = cat("do-gym-nu",         "Đồ Gym Nữ", rootAo, 420);
        // Cầu lông & Tennis
        Category cAoCauLong  = cat("ao-cau-long-tennis","Áo Cầu Lông & Tennis", rootAo, 510);
        Category cGiayCauLong= cat("giay-cau-long",     "Giày Cầu Lông & Tennis", rootGiay, 520);

        // ─── COLLECTIONS ──────────────────────────────────────────────────────────
        Collection cMuaGiai  = col("mua-giai-2024-25",   "Mùa Giải 2024/25",             1);
        Collection cVietNam  = col("doi-tuyen-viet-nam", "Đội Tuyển Việt Nam 2024",       2);
        Collection cSumRun   = col("summer-run-2024",    "Summer Run Collection 2024",    3);
        Collection cSaleSP   = col("sale-cuoi-mua",      "Sale Cuối Mùa – Giảm Tới 50%", 4);
        Collection cBongRo   = col("bst-bong-ro",        "Basketball Collection",         5);

        // ─── ÁO ĐÁ BÓNG ──────────────────────────────────────────────────────────
        int i = 0;
        Product pADB01 = p("nike-ao-dau-barcelona-2425-san-nha",
            "Áo Đấu CLB Barcelona 2024/25 Sân Nhà – Nike Dri-FIT ADV",
            cAoDbong, nike, "Vải Dri-FIT ADV thoát mồ hôi cực tốt, đường chỉ siêu nhẹ, logo CLB thêu sắc nét.", Gender.MEN, true, i++);
        Product pADB02 = p("nike-ao-dau-chelsea-2425-san-nha",
            "Áo Đấu CLB Chelsea 2024/25 Sân Nhà – Nike Dri-FIT Stadium",
            cAoDbong, nike, "Áo sân nhà màu xanh cobalt đặc trưng, chất liệu Stadium thoáng mát.", Gender.MEN, false, i++);
        Product pADB03 = p("nike-ao-dau-liverpool-2425-san-nha",
            "Áo Đấu CLB Liverpool 2024/25 Sân Nhà – Nike Dri-FIT",
            cAoDbong, nike, "Màu đỏ truyền thống Liverpool, logo in chất lượng cao.", Gender.MEN, false, i++);
        Product pADB04 = p("nike-ao-dau-psg-2425-san-nha",
            "Áo Đấu CLB PSG 2024/25 Sân Nhà – Nike Dri-FIT ADV",
            cAoDbong, nike, "Phiên bản đỉnh cao của áo thi đấu, vải siêu nhẹ chuyên dụng thi đấu.", Gender.MEN, true, i++);
        Product pADB05 = p("nike-ao-dau-brazil-2024-san-nha",
            "Áo Đấu ĐTQG Brazil 2024 Sân Nhà – Nike Dri-FIT ADV",
            cAoDbong, nike, "Màu vàng canary cổ điển của Seleção, vải ADV thế hệ mới.", Gender.MEN, true, i++);
        Product pADB06 = p("nike-ao-dau-phap-2024-san-nha",
            "Áo Đấu ĐTQG Pháp Euro 2024 Sân Nhà – Nike Dri-FIT",
            cAoDbong, nike, "Xanh navy Les Bleus, thiết kế tối giản sang trọng.", Gender.MEN, false, i++);
        Product pADB07 = p("nike-ao-tap-luyen-academy-den",
            "Áo Tập Luyện Nike Dri-FIT Academy 23 – Đen/Trắng",
            cAoDbong, nike, "Thiết kế cổ tròn thoải mái, cổ bo dệt chắc chắn, phù hợp tập luyện hàng ngày.", Gender.MEN, false, i++);
        Product pADB08 = p("nike-ao-futsal-tiempo-premier-trang",
            "Áo Thi Đấu Futsal Nike Tiempo Premier II – Trắng/Đen",
            cAoDbong, nike, "Vải nhẹ thông thoáng, co giãn 4 chiều tối ưu cho sân futsal.", Gender.MEN, false, i++);
        Product pADB09 = p("adidas-ao-dau-real-madrid-2425-san-nha",
            "Áo Đấu CLB Real Madrid 2024/25 Sân Nhà – Adidas Heat.RDY",
            cAoDbong, adidas, "Vải Heat.RDY quản lý thân nhiệt chuyên nghiệp, trắng tinh biểu tượng của Los Blancos.", Gender.MEN, true, i++);
        Product pADB10 = p("adidas-ao-dau-mu-2425-san-nha",
            "Áo Đấu CLB Manchester United 2024/25 Sân Nhà – Adidas",
            cAoDbong, adidas, "Đỏ Old Trafford truyền thống, công nghệ AEROREADY giữ khô thoáng.", Gender.MEN, false, i++);
        Product pADB11 = p("adidas-ao-dau-arsenal-2425-san-nha",
            "Áo Đấu CLB Arsenal 2024/25 Sân Nhà – Adidas AEROREADY",
            cAoDbong, adidas, "Đỏ Emirates với sọc trắng, chất vải dệt cao cấp siêu nhẹ.", Gender.MEN, false, i++);
        Product pADB12 = p("adidas-ao-dau-bayern-2425-san-nha",
            "Áo Đấu CLB Bayern Munich 2024/25 Sân Nhà – Adidas",
            cAoDbong, adidas, "Đỏ Mia san mia với logo sắc nét, thiết kế modern cổ điển.", Gender.MEN, true, i++);
        Product pADB13 = p("adidas-ao-tap-tiro23-navy",
            "Áo Tập Luyện Adidas Tiro 23 League – Navy/Trắng",
            cAoDbong, adidas, "Ba sọc Adidas huyền thoại, vải AEROREADY thoát ẩm nhanh.", Gender.MEN, false, i++);
        Product pADB14 = p("puma-ao-dau-man-city-2425-san-nha",
            "Áo Đấu CLB Manchester City 2024/25 Sân Nhà – Puma",
            cAoDbong, puma, "Xanh sky blue của The Citizens, Puma dryCELL thoát mồ hôi hiệu quả.", Gender.MEN, false, i++);
        Product pADB15 = p("puma-ao-dau-ac-milan-2425-san-nha",
            "Áo Đấu CLB AC Milan 2024/25 Sân Nhà – Puma",
            cAoDbong, puma, "Sọc đỏ đen huyền thoại I Rossoneri, chất vải Puma dryCELL cao cấp.", Gender.MEN, false, i++);

        // ─── QUẦN ĐÁ BÓNG ────────────────────────────────────────────────────────
        Product pQDB01 = p("adidas-quan-dau-real-madrid-2425",
            "Quần Đấu CLB Real Madrid 2024/25 – Adidas Heat.RDY",
            cQuanDbong, adidas, "Đồng bộ với áo đấu Real Madrid, chất Heat.RDY co giãn tốt.", Gender.MEN, false, i++);
        Product pQDB02 = p("nike-quan-dau-dri-fit-strike-den",
            "Quần Đá Bóng Nike Dri-FIT Strike – Đen/Trắng",
            cQuanDbong, nike, "Cạp chun bo dệt, túi bên tiện dụng, vải Dri-FIT mát lạnh.", Gender.MEN, false, i++);
        Product pQDB03 = p("adidas-quan-tap-tiro23-navy",
            "Quần Tập Luyện Adidas Tiro 23 – Navy/Trắng",
            cQuanDbong, adidas, "Ống quần thẳng thoải mái, dây rút cạp điều chỉnh được.", Gender.MEN, false, i++);
        Product pQDB04 = p("puma-quan-dau-liga-trang",
            "Quần Đấu Puma Liga Baselayer Short – Trắng",
            cQuanDbong, puma, "Siêu nhẹ, co giãn 4 chiều thoải mái trong từng pha bóng.", Gender.MEN, false, i++);
        Product pQDB05 = p("nike-quan-dau-futsal-do",
            "Quần Thi Đấu Futsal Nike Tiempo Premier II – Đỏ/Đen",
            cQuanDbong, nike, "Thiết kế gọn nhẹ cho sân futsal, vải không bai giãn.", Gender.MEN, false, i++);
        Product pQDB06 = p("ua-quan-heatgear-xam",
            "Quần Thể Thao Under Armour HeatGear Armour Comp – Xám",
            cQuanDbong, ua, "Compression nhẹ hỗ trợ cơ bắp, kháng UV50+.", Gender.MEN, false, i++);

        // ─── GIÀY ĐÁ BÓNG CỎ THẬT FG ────────────────────────────────────────────
        Product pFG01 = p("nike-giay-mercurial-vapor15-elite-fg-vang",
            "Giày Đá Bóng Nike Mercurial Vapor 15 Elite FG – Trắng/Vàng Chrome",
            cGiayFG, nike, "Dòng giày tốc độ đỉnh cao, đế carbon siêu cứng, bề mặt Flyknit ôm chân hoàn hảo.", Gender.MEN, true, i++);
        Product pFG02 = p("nike-giay-mercurial-superfly9-elite-fg-den",
            "Giày Đá Bóng Nike Mercurial Superfly 9 Elite FG – Đen/Chrome",
            cGiayFG, nike, "Cổ cao Dynamic Fit Collar ôm cổ chân, Flyknit cải tiến hỗ trợ cực tốt.", Gender.MEN, true, i++);
        Product pFG03 = p("nike-giay-phantom-gx-elite-fg-xanh",
            "Giày Đá Bóng Nike Phantom GX Elite FG – Xanh Dương/Cam",
            cGiayFG, nike, "Bề mặt Ghost Lace giúp sút bóng siêu chính xác, đế Aerotrak bám cỏ đa hướng.", Gender.MEN, false, i++);
        Product pFG04 = p("nike-giay-tiempo-legend10-elite-fg-trang",
            "Giày Đá Bóng Nike Tiempo Legend 10 Elite FG – Trắng Thuần Khiết",
            cGiayFG, nike, "Da kangaroo cao cấp ôm chân như thứ hai, cảm giác bóng tuyệt vời.", Gender.MEN, false, i++);
        Product pFG05 = p("nike-giay-mercurial-vapor15-club-fg-den-do",
            "Giày Đá Bóng Nike Mercurial Vapor 15 Club FG – Đen/Đỏ Tươi",
            cGiayFG, nike, "Phiên bản phổ thông, đế TPU bền bỉ, bề mặt lưới thoáng khí.", Gender.MEN, false, i++);
        Product pFG06 = p("adidas-giay-predator-accuracy1-low-fg-den-trang",
            "Giày Đá Bóng Adidas Predator Accuracy.1 Low FG – Đen/Trắng",
            cGiayFG, adidas, "Vùng sút bóng Predatouch 360° với 360 gai cao su, kiểm soát bóng như dính.", Gender.MEN, true, i++);
        Product pFG07 = p("adidas-giay-x-crazyfast1-fg-vang",
            "Giày Đá Bóng Adidas X Crazyfast.1 FG – Vàng Solar/Đen",
            cGiayFG, adidas, "Vải Ultralight mỏng 30% so với thế hệ trước, đế Speedframe siêu bám.", Gender.MEN, true, i++);
        Product pFG08 = p("adidas-giay-copa-pure1-fg-trang-den",
            "Giày Đá Bóng Adidas Copa Pure.1 FG – Trắng Cloud/Lõi Đen",
            cGiayFG, adidas, "Da kangaroo nguyên bản mềm mại, cảm giác chạm bóng không gì sánh được.", Gender.MEN, false, i++);
        Product pFG09 = p("adidas-giay-copa-mundial-fg-classic",
            "Giày Đá Bóng Adidas Copa Mundial FG Classic – Đen/Trắng",
            cGiayFG, adidas, "Huyền thoại 40 năm tuổi, da kangaroo đích thực, đế Torque 2G bền vô song.", Gender.MEN, false, i++);
        Product pFG10 = p("puma-giay-ultra-ultimate-fg-xanh",
            "Giày Đá Bóng Puma Ultra Ultimate FG – Xanh Dương/Trắng",
            cGiayFG, puma, "Vải MatryxEVO siêu nhẹ, đế SpeedUnit bám đất cỏ thật đa hướng.", Gender.MEN, false, i++);
        Product pFG11 = p("puma-giay-king-platinum-fg-trang",
            "Giày Đá Bóng Puma King Platinum FG – Trắng/Đen Puma",
            cGiayFG, puma, "Da kangaroo tự nhiên truyền thống, thiết kế cổ điển bất hủ 50+ năm.", Gender.MEN, false, i++);

        // ─── GIÀY ĐÁ BÓNG CỎ NHÂN TẠO TF ───────────────────────────────────────
        Product pTF01 = p("nike-giay-mercurial-vapor15-club-tf-xanh",
            "Giày Đá Bóng Nike Mercurial Vapor 15 Club TF – Xanh Dương/Đen",
            cGiayTF, nike, "Đế multi-stud TF bám cỏ nhân tạo, vải lưới nhẹ thoáng khí.", Gender.MEN, false, i++);
        Product pTF02 = p("nike-giay-phantom-gx-academy-tf-cam",
            "Giày Đá Bóng Nike Phantom GX Academy TF – Cam/Đen",
            cGiayTF, nike, "Bề mặt Ghost Lace tối ưu cảm giác bóng trên sân cỏ nhân tạo.", Gender.MEN, false, i++);
        Product pTF03 = p("adidas-giay-x-crazyfast-club-tf-den",
            "Giày Đá Bóng Adidas X Crazyfast Club TF – Đen/Trắng Solar",
            cGiayTF, adidas, "Nhẹ và nhanh, đế Ground Control TF bám chắc mặt sân nhân tạo.", Gender.MEN, false, i++);
        Product pTF04 = p("adidas-giay-copa-pure4-tf-navy",
            "Giày Đá Bóng Adidas Copa Pure.4 TF – Navy/Trắng",
            cGiayTF, adidas, "Phần thân bọc da nhân tạo mềm, cảm giác bóng tốt trên TF.", Gender.MEN, false, i++);
        Product pTF05 = p("adidas-giay-tiro23-club-tf-den",
            "Giày Đá Bóng Adidas Tiro 23 Club TF – Đen/Trắng",
            cGiayTF, adidas, "Lựa chọn entry-level bền bỉ, phù hợp tập luyện cường độ cao.", Gender.MEN, false, i++);
        Product pTF06 = p("puma-giay-future7-play-tf-vang",
            "Giày Đá Bóng Puma Future 7 Play TF – Vàng/Đen",
            cGiayTF, puma, "Bề mặt lưới tổng hợp linh hoạt, đế Future TF 12 đinh đa hướng.", Gender.MEN, false, i++);
        Product pTF07 = p("puma-giay-king-match-tf-trang-den",
            "Giày Đá Bóng Puma King Match TF – Trắng/Đen",
            cGiayTF, puma, "Cảm hứng từ Copa Mundial huyền thoại, phiên bản TF hiện đại.", Gender.MEN, false, i++);

        // ─── GIÀY FUTSAL IC ───────────────────────────────────────────────────────
        Product pFS01 = p("nike-giay-react-gato-ic-trang-den",
            "Giày Futsal Nike React Gato IC – Trắng/Đen/Tím",
            cGiayFutsal, nike, "Đệm React êm ái, đế IC phẳng ôm sàn tối ưu, cổ giày thấp linh hoạt.", Gender.MEN, true, i++);
        Product pFS02 = p("nike-giay-premier3-ic-den-trang",
            "Giày Futsal Nike Premier 3 IC – Đen/Trắng",
            cGiayFutsal, nike, "Da tổng hợp cao cấp cho cảm giác bóng nhạy, đế cao su tự nhiên.", Gender.MEN, false, i++);
        Product pFS03 = p("nike-giay-tiempo-legend10-club-ic-navy",
            "Giày Futsal Nike Tiempo Legend 10 Club IC – Navy/Trắng",
            cGiayFutsal, nike, "Thân giày da mềm, đế IC bám sàn nhà thi đấu tốt.", Gender.MEN, false, i++);
        Product pFS04 = p("adidas-giay-copa-pure3-ic-trang",
            "Giày Futsal Adidas Copa Pure.3 IC – Trắng Cloud/Lõi Đen",
            cGiayFutsal, adidas, "Da soft nhân tạo ôm chân, đế IC bám đa phương.", Gender.MEN, false, i++);
        Product pFS05 = p("puma-giay-attacanto-ic-xanh",
            "Giày Futsal Puma Attacanto IC – Xanh Dương Electric",
            cGiayFutsal, puma, "Thiết kế tối ưu cho sân futsal, bề mặt vải kéo dài cảm giác bóng.", Gender.MEN, false, i++);
        Product pFS06 = p("adidas-giay-x-crazyfast3-ic-vang",
            "Giày Futsal Adidas X Crazyfast.3 IC – Vàng Solar/Đen",
            cGiayFutsal, adidas, "Ultralight toàn phần, đế IC phẳng tối ưu cho sàn cứng nhà thi đấu.", Gender.MEN, false, i++);

        // ─── PHỤ KIỆN ĐÁ BÓNG ────────────────────────────────────────────────────
        Product pPK01 = p("nike-tat-matchfit-over-trang",
            "Tất Đá Bóng Nike Matchfit Over-the-Calf Cao Cổ – Trắng",
            cPhuKienDB, nike, "Tất cổ cao đến bắp chân, băng đàn hồi giữ cố định không tụt.", Gender.MEN, false, i++);
        Product pPK02 = p("adidas-tat-milano23-den-trang",
            "Tất Đá Bóng Adidas Milano 23 Cao Cổ – Đen/Trắng",
            cPhuKienDB, adidas, "Chất liệu kết hợp polyester và spandex, đàn hồi tốt, bền lâu.", Gender.MEN, false, i++);
        Product pPK03 = p("puma-tat-liga-long-do",
            "Tất Đá Bóng Puma LIGA Long Classic – Đỏ/Trắng",
            cPhuKienDB, puma, "Logo Puma nổi bật, cổ tất cao ôm chân chắc chắn.", Gender.MEN, false, i++);
        Product pPK04 = p("adidas-bao-ve-ong-dong-den",
            "Bảo Vệ Ống Đồng Adidas Tiro League Shin Guards – Đen",
            cPhuKienDB, adidas, "Tấm PE cứng chắn chấn thương, lưng foam EVA hấp thụ lực va đập.", Gender.MEN, false, i++);
        Product pPK05 = p("nike-bao-ve-ong-dong-charge-trang",
            "Bảo Vệ Ống Đồng Nike Charge Shin Guards – Trắng",
            cPhuKienDB, nike, "Nhẹ chỉ 85g, thiết kế khí động học không cản trở di chuyển.", Gender.MEN, false, i++);

        // ─── GĂNG TAY THỦ MÔN ────────────────────────────────────────────────────
        Product pGT01 = p("nike-gang-tay-gk-match-xanh-cam",
            "Găng Tay Thủ Môn Nike Goalkeeper Match – Xanh Dương/Cam",
            cGangTay, nike, "Lòng bàn tay latex 3mm bám bóng tốt, ngón tay linh hoạt không cứng nhắc.", Gender.MEN, true, i++);
        Product pGT02 = p("adidas-gang-tay-predator-match-do-den",
            "Găng Tay Thủ Môn Adidas Predator Match Fingersave – Đỏ/Đen",
            cGangTay, adidas, "Fingersave 5 ngón tránh gãy, lòng latex nổi gai bám bóng siêu tốt.", Gender.MEN, false, i++);
        Product pGT03 = p("puma-gang-tay-future-match-xanh-la",
            "Găng Tay Thủ Môn Puma Future Match – Xanh Lá/Đen",
            cGangTay, puma, "Latex 4mm độ bám cao, thiết kế hybrid cut ôm tay thoải mái.", Gender.MEN, false, i++);

        // ─── BÓNG THỂ THAO ───────────────────────────────────────────────────────
        Product pBD01 = p("nike-bong-da-premier-league-flight-2425",
            "Bóng Đá Nike Premier League Flight 2024/25 – Trắng/Đỏ/Xanh",
            cBong, nike, "Bóng thi đấu chính thức Premier League, công nghệ Aerowsculpt giảm lực cản.", Gender.UNISEX, true, i++);
        Product pBD02 = p("adidas-bong-da-ucl-pro-2425-vang",
            "Bóng Đá Adidas UCL Pro 2024/25 – Trắng/Vàng Kim",
            cBong, adidas, "Bóng thi đấu Champions League, vỏ polyurethane dày điều khiển ổn định.", Gender.UNISEX, true, i++);
        Product pBD03 = p("adidas-bong-da-tiro-match-trang-den",
            "Bóng Đá Adidas Tiro Match – Trắng/Đen",
            cBong, adidas, "Bóng tập luyện chuẩn size 5, chống nước tốt, bền màu lâu dài.", Gender.UNISEX, false, i++);
        Product pBD04 = p("puma-bong-da-orbita-laliga-2425",
            "Bóng Đá Puma Orbita LaLiga 1 2024/25 – Trắng/Cam",
            cBong, puma, "Bóng thi đấu chính thức La Liga, vỏ PU 4 lớp kiểm soát đường bóng ổn định.", Gender.UNISEX, false, i++);

        // ─── ÁO CHẠY BỘ ──────────────────────────────────────────────────────────
        Product pACB01 = p("nike-ao-chay-dri-fit-miler-nam-den",
            "Áo Chạy Bộ Nike Dri-FIT Miler Short-Sleeve Nam – Đen",
            cAoChay, nike, "Vải Dri-FIT thoát ẩm nhanh, form vừa vặn không gò bó khi chạy.", Gender.MEN, false, i++);
        Product pACB02 = p("nike-ao-chay-dri-fit-run-division-nam-xanh",
            "Áo Chạy Bộ Nike Dri-FIT Run Division Reflect Nam – Xanh Dương",
            cAoChay, nike, "Chi tiết phản quang 360° an toàn khi chạy đêm, vải siêu nhẹ.", Gender.MEN, false, i++);
        Product pACB03 = p("adidas-ao-chay-own-the-run-nam-trang",
            "Áo Chạy Bộ Adidas Own the Run Nam – Trắng",
            cAoChay, adidas, "AEROREADY hút ẩm nhanh, vải tái chế từ nhựa đại dương.", Gender.MEN, false, i++);
        Product pACB04 = p("ua-ao-chay-streaker-nam-xam",
            "Áo Chạy Bộ Under Armour Streaker 2.0 Nam – Xám Nhạt",
            cAoChay, ua, "Super lightweight, HeatGear fabric thoáng mát trong thời tiết nóng.", Gender.MEN, false, i++);
        Product pACB05 = p("nike-ao-chay-dri-fit-nu-hong",
            "Áo Chạy Bộ Nike Dri-FIT Miler Nữ – Hồng Pastel",
            cAoChay, nike, "Form slim fit tôn dáng, đường may phẳng không cọ sát da.", Gender.WOMEN, false, i++);
        Product pACB06 = p("adidas-ao-chay-own-the-run-nu-den",
            "Áo Chạy Bộ Adidas Own the Run Nữ – Đen",
            cAoChay, adidas, "Thiết kế không tay thoáng mát, vải AEROREADY khô thoáng cả ngày.", Gender.WOMEN, false, i++);
        Product pACB07 = p("puma-ao-chay-run-cloudspun-nam-navy",
            "Áo Chạy Bộ Puma Run Cloudspun Crewneck Nam – Navy",
            cAoChay, puma, "CloudSpun – cảm giác như mặc đám mây, siêu nhẹ và êm ái.", Gender.MEN, false, i++);

        // ─── QUẦN CHẠY BỘ ────────────────────────────────────────────────────────
        Product pQCB01 = p("nike-quan-chay-challenger-5inch-nam-den",
            "Quần Chạy Bộ Nike Dri-FIT Challenger 5\" Nam – Đen/Phản Quang",
            cQuanChay, nike, "Lót trong thoáng mát, túi kéo khóa giữ đồ an toàn khi chạy.", Gender.MEN, false, i++);
        Product pQCB02 = p("nike-quan-chay-stride-tight-nu-den",
            "Quần Chạy Bộ Nike Dri-FIT Stride Tight Nữ – Đen",
            cQuanChay, nike, "Legging dài ôm cơ thể, waistband cao che bụng, cảm giác nâng đỡ tốt.", Gender.WOMEN, false, i++);
        Product pQCB03 = p("adidas-quan-chay-own-the-run-nam-navy",
            "Quần Chạy Bộ Adidas Own the Run Nam – Navy",
            cQuanChay, adidas, "Thoáng mát và nhẹ, co giãn 4 chiều tự do di chuyển.", Gender.MEN, false, i++);
        Product pQCB04 = p("ua-quan-chay-launch-5inch-nam-do",
            "Quần Chạy Bộ Under Armour Launch 5\" Nam – Đỏ",
            cQuanChay, ua, "SuperVent mesh thông thoáng, lưng thun rộng không gò bó.", Gender.MEN, false, i++);
        Product pQCB05 = p("puma-quan-chay-5inch-nam-xanh-la",
            "Quần Chạy Bộ Puma Run Favorite Velocity 5\" Nam – Xanh Lá",
            cQuanChay, puma, "Lót mesh thoáng khí, dây rút bên trong điều chỉnh vòng eo.", Gender.MEN, false, i++);

        // ─── GIÀY CHẠY BỘ ────────────────────────────────────────────────────────
        Product pGCB01 = p("nike-giay-air-zoom-pegasus41-den-trang",
            "Giày Chạy Bộ Nike Air Zoom Pegasus 41 – Đen/Trắng",
            cGiayChay, nike, "Air Zoom đệm êm tức thì, foam React nhẹ bền, phù hợp mọi cự ly.", Gender.MEN, true, i++);
        Product pGCB02 = p("nike-giay-react-infinity-run4-xanh",
            "Giày Chạy Bộ Nike React Infinity Run 4 – Xanh Dương/Trắng",
            cGiayChay, nike, "Thiết kế giảm thiểu chấn thương, đế rộng ổn định, foam React êm.", Gender.MEN, true, i++);
        Product pGCB03 = p("nike-giay-zoomx-vaporfly-next3-hong",
            "Giày Chạy Bộ Nike ZoomX Vaporfly Next% 3 – Hồng/Trắng",
            cGiayChay, nike, "Carbon plate + ZoomX foam, tối ưu hiệu suất marathon, tiết kiệm 4% năng lượng.", Gender.UNISEX, true, i++);
        Product pGCB04 = p("nike-giay-winflo10-do-den",
            "Giày Chạy Bộ Nike Air Zoom Winflo 10 – Đỏ/Đen",
            cGiayChay, nike, "Air Zoom giữa bàn chân, đế ngoài cao su phân khu bám đường tốt.", Gender.MEN, false, i++);
        Product pGCB05 = p("adidas-giay-ultraboost23-trang-den",
            "Giày Chạy Bộ Adidas Ultraboost 23 – Trắng/Đen Core",
            cGiayChay, adidas, "Boost foam hoàn năng lượng tối đa, Primeknit ôm chân thích nghi.", Gender.UNISEX, true, i++);
        Product pGCB06 = p("adidas-giay-solarboost5-navy",
            "Giày Chạy Bộ Adidas Solarboost 5 – Xanh Navy/Trắng",
            cGiayChay, adidas, "Công nghệ Solar Propulsion Rail định hướng bước chạy, Boost êm đệm.", Gender.MEN, false, i++);
        Product pGCB07 = p("adidas-giay-adizero-boston12-xanh-la",
            "Giày Chạy Bộ Adidas Adizero Boston 12 – Xanh Lá/Đen",
            cGiayChay, adidas, "Carbon-infused nylon plate đẩy mạnh bước đà, Lightstrike Pro cực nhẹ.", Gender.MEN, true, i++);
        Product pGCB08 = p("ua-giay-hovr-sonic6-cam-den",
            "Giày Chạy Bộ Under Armour HOVR Sonic 6 – Cam/Đen",
            cGiayChay, ua, "HOVR cushioning không trọng lực, chip UA MapMyRun theo dõi form chạy.", Gender.MEN, false, i++);
        Product pGCB09 = p("puma-giay-velocity-nitro3-vang-den",
            "Giày Chạy Bộ Puma Velocity Nitro 3 – Vàng Neon/Đen",
            cGiayChay, puma, "Nitro foam nhẹ và đàn hồi cao, EvoBound ngoài vòm chân hỗ trợ bước đà.", Gender.MEN, false, i++);
        Product pGCB10 = p("adidas-giay-response-cl-den-trang",
            "Giày Chạy Bộ Adidas Response CL Retro – Đen/Trắng",
            cGiayChay, adidas, "Phong cách retro 90s, đệm Cloudfoam Plus bước êm, phù hợp đi bộ hàng ngày.", Gender.UNISEX, false, i++);

        // ─── ÁO BÓNG RỔ ──────────────────────────────────────────────────────────
        Product pABR01 = p("nike-ao-bong-ro-icon-jersey-den",
            "Áo Bóng Rổ Nike Dri-FIT Icon Jersey Nam – Đen/Trắng",
            cAoBR, nike, "Vải Dri-FIT thoát mồ hôi nhanh, cổ tròn chuẩn NBA, form vừa thoải mái.", Gender.MEN, false, i++);
        Product pABR02 = p("nike-ao-bong-ro-nba-heat-do",
            "Áo Bóng Rổ NBA Swingman Jersey Miami Heat – Đỏ",
            cAoBR, nike, "Phiên bản Swingman chất lượng thi đấu, thêu logo NBA sắc nét.", Gender.MEN, true, i++);
        Product pABR03 = p("adidas-ao-bong-ro-pro-series-xanh",
            "Áo Bóng Rổ Adidas Pro Series Reversible Nam – Xanh Dương",
            cAoBR, adidas, "Áo 2 mặt linh hoạt, AEROREADY hai chiều, phù hợp đấu nội bộ.", Gender.MEN, false, i++);
        Product pABR04 = p("puma-ao-bong-ro-pwrcool-mesh-trang",
            "Áo Bóng Rổ Puma PWRCOOL Mesh Jersey Nam – Trắng",
            cAoBR, puma, "Mesh siêu thoáng khí, chất liệu nhẹ hơn so với jersey truyền thống 20%.", Gender.MEN, false, i++);
        Product pQBR01 = p("nike-quan-bong-ro-icon-den",
            "Quần Bóng Rổ Nike Dri-FIT Icon Shorts Nam – Đen",
            cAoBR, nike, "Dài đến đầu gối, vải mềm co giãn tốt, dây rút eo điều chỉnh được.", Gender.MEN, false, i++);

        // ─── GIÀY BÓNG RỔ ────────────────────────────────────────────────────────
        Product pGBR01 = p("nike-giay-air-jordan38-pf-den-do",
            "Giày Bóng Rổ Nike Air Jordan 38 PF – Đen/Đỏ Gym",
            cGiayBR, nike, "Đệm Air Zoom Strobel phản hồi nhanh, đế Pebax siêu nhẹ, cổ thấp linh hoạt.", Gender.MEN, true, i++);
        Product pGBR02 = p("nike-giay-lebron-xxi-low-trang-vang",
            "Giày Bóng Rổ Nike LeBron XXI Low – Trắng/Vàng Metallic",
            cGiayBR, nike, "Air Max 360 độ ôm chân, thiết kế cổ thấp di chuyển tốc độ cao.", Gender.MEN, true, i++);
        Product pGBR03 = p("nike-giay-kd17-xanh-duong",
            "Giày Bóng Rổ Nike KD17 NRG – Xanh Đại Dương",
            cGiayBR, nike, "Cushlon 3.0 + Zoom Air đệm toàn diện, thiết kế thoải mái cho wing player.", Gender.MEN, false, i++);
        Product pGBR04 = p("adidas-giay-ae1-anthony-edwards-xanh-trang",
            "Giày Bóng Rổ Adidas AE 1 Anthony Edwards – Xanh/Trắng",
            cGiayBR, adidas, "Lightstrike Pro 2 đệm cực nhẹ, ôm chân ổn định cho kiểu chơi cắt bóng.", Gender.MEN, false, i++);
        Product pGBR05 = p("adidas-giay-trae-young3-do-den",
            "Giày Bóng Rổ Adidas Trae Young 3 – Đỏ/Đen",
            cGiayBR, adidas, "Bouncezone ở gót đệm cực êm, phù hợp point guard linh hoạt.", Gender.MEN, false, i++);
        Product pGBR06 = p("puma-giay-mb03-lamelo-tim-den",
            "Giày Bóng Rổ Puma MB.03 LaMelo Ball – Tím/Đen",
            cGiayBR, puma, "Nitro Elite foam siêu phản hồi, thiết kế high-fashion trên sân.", Gender.MEN, false, i++);

        // ─── ĐỒ GYM NAM ──────────────────────────────────────────────────────────
        Product pGYM01 = p("nike-ao-gym-dri-fit-tshirt-nam-den",
            "Áo Gym Nike Dri-FIT Fitness T-Shirt Nam – Đen",
            cGymNam, nike, "Form regular không gò bó, Dri-FIT giữ khô suốt buổi tập.", Gender.MEN, false, i++);
        Product pGYM02 = p("ua-ao-compression-heatgear-fitted-xam",
            "Áo Compression Under Armour HeatGear Armour Fitted Nam – Xám",
            cGymNam, ua, "Nén nhẹ hỗ trợ cơ bắp, HeatGear giữ mát khi tập cường độ cao.", Gender.MEN, false, i++);
        Product pGYM03 = p("adidas-ao-tank-hiit-heatrdy-den",
            "Áo Tank Top Adidas HIIT HEAT.RDY Training Nam – Đen",
            cGymNam, adidas, "Không tay thoáng mát tối đa, HEAT.RDY cho buổi tập nhiệt độ cao.", Gender.MEN, false, i++);
        Product pGYM04 = p("nike-quan-gym-pro-tight-5inch-den",
            "Quần Gym Nike Pro Tight 5\" Nam – Đen",
            cGymNam, nike, "Compression vừa phải hỗ trợ chuyển động, Dri-FIT khô thoáng.", Gender.MEN, false, i++);
        Product pGYM05 = p("adidas-quan-training-essentials-3s-navy",
            "Quần Training Adidas Essentials 3-Stripes – Navy/Trắng",
            cGymNam, adidas, "Ba sọc classic, vải French terry thoáng dày vừa cho tập gym.", Gender.MEN, false, i++);
        Product pGYM06 = p("ua-quan-flex-woven-9inch-xanh",
            "Quần Gym Under Armour UA Flex Woven 9\" – Xanh Dương",
            cGymNam, ua, "Woven stretch không hạn chế biên độ squat, tuck bọn dây bên trong.", Gender.MEN, false, i++);

        // ─── ĐỒ GYM NỮ ───────────────────────────────────────────────────────────
        Product pGYMF01 = p("nike-bra-swoosh-light-support-nu-den",
            "Sports Bra Nike Swoosh Light-Support Nữ – Đen",
            cGymNu, nike, "Hỗ trợ nhẹ cho yoga và pilates, dây bề rộng thoải mái.", Gender.WOMEN, false, i++);
        Product pGYMF02 = p("nike-legging-dri-fit-one-nu-den",
            "Legging Nike Dri-FIT One 7/8 Nữ – Đen",
            cGymNu, nike, "Cạp cao che bụng, túi bên tiện, vải Dri-FIT ôm dáng đẹp.", Gender.WOMEN, true, i++);
        Product pGYMF03 = p("ua-legging-heatgear-hi-rise-nu-den",
            "Legging Under Armour HeatGear Hi-Rise Nữ – Đen",
            cGymNu, ua, "Cạp rộng siêu cao, HeatGear mát lạnh dù trời nóng, 4D Stretch.", Gender.WOMEN, false, i++);
        Product pGYMF04 = p("adidas-legging-optime-7-8-nu-tim",
            "Legging Adidas Optime Training 7/8 Nữ – Tím/Đen",
            cGymNu, adidas, "Aeroknit co giãn đa chiều, giữ hình dáng không nhăn sau nhiều lần giặt.", Gender.WOMEN, false, i++);
        Product pGYMF05 = p("puma-ao-tank-evostripe-nu-hong",
            "Áo Tank Puma Evostripe Nữ – Hồng Coral",
            cGymNu, puma, "Vải mềm mại co dãn tốt, form bờ vai cắt rộng, phù hợp yoga và gym.", Gender.WOMEN, false, i++);

        // ─── ÁO CẦU LÔNG & TENNIS ────────────────────────────────────────────────
        Product pCL01 = p("nike-ao-court-dri-fit-polo-nam-trang",
            "Áo Tennis Nike Court Dri-FIT Advantage Polo Nam – Trắng",
            cAoCauLong, nike, "Vải piqué thoáng mát, cổ polo chuẩn tennis, Dri-FIT thoát ẩm.", Gender.MEN, false, i++);
        Product pCL02 = p("adidas-ao-freelift-match-nam-den",
            "Áo Tennis Adidas FreeLift Match Henley Nam – Đen",
            cAoCauLong, adidas, "FreeLift Technology không hạn chế biên độ đánh, AEROREADY mát.", Gender.MEN, false, i++);
        Product pCL03 = p("nike-ao-court-polo-nu-xanh",
            "Áo Tennis Nike Court Dri-FIT Polo Nữ – Xanh Biển",
            cAoCauLong, nike, "Form slim thanh lịch, vải thấm hút mồ hôi nhanh, cổ polo nữ tính.", Gender.WOMEN, false, i++);
        Product pCL04 = p("ua-ao-tech2-cau-long-do-den",
            "Áo Cầu Lông Under Armour Tech 2.0 – Đỏ/Đen",
            cAoCauLong, ua, "Vải Tech nhẹ thoáng, in logo UA sắc nét, phù hợp cả cầu lông lẫn gym.", Gender.MEN, false, i++);
        Product pCL05 = p("puma-ao-teamrise-jersey-vang-den",
            "Áo Cầu Lông Puma TEAMRISE Jersey – Vàng/Đen",
            cAoCauLong, puma, "Vải mesh lưới tối ưu thông khí, thiết kế thoáng nhẹ linh hoạt.", Gender.MEN, false, i++);

        // ─── GIÀY CẦU LÔNG & TENNIS ──────────────────────────────────────────────
        Product pGCL01 = p("nike-giay-court-zoom-vapor11-den-xanh",
            "Giày Tennis Nike Air Zoom Vapor 11 All Court – Đen/Xanh Dương",
            cGiayCauLong, nike, "Zoom Air ở ngón chân phản hồi nhanh, đế XDR bám tốt trên sân cứng.", Gender.MEN, true, i++);
        Product pGCL02 = p("adidas-giay-adizero-cybersonic-all-court-trang-vang",
            "Giày Tennis Adidas Adizero Cybersonic All Court – Trắng/Vàng",
            cGiayCauLong, adidas, "Lightstrike 2.0 siêu nhẹ, đế Geofit 2.0 bám đa mặt sân.", Gender.MEN, false, i++);
        Product pGCL03 = p("nike-giay-court-zoom-nxt-trang-hong-nu",
            "Giày Tennis Nike Court Zoom NXT Hard Court Nữ – Trắng/Hồng",
            cGiayCauLong, nike, "Zoom Air cushion, cổ giày thấp linh hoạt di chuyển bên trái phải.", Gender.WOMEN, false, i++);
        Product pGCL04 = p("adidas-giay-gamecourt2-all-court-navy",
            "Giày Tennis Adidas GameCourt 2.0 All Court – Navy",
            cGiayCauLong, adidas, "Cloudfoam đệm bước, đế Adiwear bền bỉ phù hợp tập luyện hàng ngày.", Gender.UNISEX, false, i++);
        Product pGCL05 = p("puma-giay-solarflash-indoor-vang-den",
            "Giày Cầu Lông Puma Solarflash II Indoor – Vàng/Đen",
            cGiayCauLong, puma, "Đế gum chống trơn trượt sàn nhà, ổn định tốt cho pha di chuyển nhanh.", Gender.MEN, false, i++);

        // ─── VARIANTS ─────────────────────────────────────────────────────────────
        // Áo đá bóng – clothing sizes S/M/L/XL/XXL
        clothSizes(pADB01, "ADB01", 1_200_000, 1_500_000, "Xanh Đỏ");
        clothSizes(pADB02, "ADB02",   850_000, 1_100_000, "Xanh Cobalt");
        clothSizes(pADB03, "ADB03",   850_000, 1_100_000, "Đỏ");
        clothSizes(pADB04, "ADB04", 1_250_000, 1_600_000, "Xanh Navy/Đỏ");
        clothSizes(pADB05, "ADB05", 1_350_000, 1_700_000, "Vàng Canary");
        clothSizes(pADB06, "ADB06",   950_000, 1_200_000, "Xanh Navy");
        clothSizes(pADB07, "ADB07",   450_000,   580_000, "Đen/Trắng");
        clothSizes(pADB08, "ADB08",   380_000,   490_000, "Trắng/Đen");
        clothSizes(pADB09, "ADB09", 1_300_000, 1_650_000, "Trắng");
        clothSizes(pADB10, "ADB10",   950_000, 1_200_000, "Đỏ");
        clothSizes(pADB11, "ADB11",   850_000, 1_100_000, "Đỏ/Trắng");
        clothSizes(pADB12, "ADB12", 1_200_000, 1_500_000, "Đỏ");
        clothSizes(pADB13, "ADB13",   420_000,   550_000, "Navy/Trắng");
        clothSizes(pADB14, "ADB14", 1_100_000, 1_400_000, "Xanh Sky Blue");
        clothSizes(pADB15, "ADB15",   950_000, 1_200_000, "Đỏ Đen");

        // Quần đá bóng – clothing sizes
        clothSizes(pQDB01, "QDB01", 550_000, 700_000, "Trắng");
        clothSizes(pQDB02, "QDB02", 380_000, 490_000, "Đen/Trắng");
        clothSizes(pQDB03, "QDB03", 320_000, 420_000, "Navy/Trắng");
        clothSizes(pQDB04, "QDB04", 280_000, 370_000, "Trắng");
        clothSizes(pQDB05, "QDB05", 320_000, 420_000, "Đỏ/Đen");
        clothSizes(pQDB06, "QDB06", 450_000, 580_000, "Xám");

        // Giày FG – shoe sizes 39-44
        shoeSizes(pFG01, "FG01", 4_500_000, 5_200_000, "Trắng/Vàng Chrome");
        shoeSizes(pFG02, "FG02", 5_200_000, 6_000_000, "Đen/Chrome");
        shoeSizes(pFG03, "FG03", 4_800_000, 5_500_000, "Xanh/Cam");
        shoeSizes(pFG04, "FG04", 4_200_000, 4_900_000, "Trắng");
        shoeSizes(pFG05, "FG05", 1_200_000, 1_550_000, "Đen/Đỏ");
        shoeSizes(pFG06, "FG06", 4_600_000, 5_300_000, "Đen/Trắng");
        shoeSizes(pFG07, "FG07", 4_800_000, 5_500_000, "Vàng Solar/Đen");
        shoeSizes(pFG08, "FG08", 4_200_000, 4_900_000, "Trắng/Đen");
        shoeSizes(pFG09, "FG09", 3_500_000, 4_200_000, "Đen/Trắng");
        shoeSizes(pFG10, "FG10", 4_200_000, 4_900_000, "Xanh Dương/Trắng");
        shoeSizes(pFG11, "FG11", 4_000_000, 4_700_000, "Trắng/Đen");

        // Giày TF – shoe sizes
        shoeSizes(pTF01, "TF01", 850_000, 1_100_000, "Xanh/Đen");
        shoeSizes(pTF02, "TF02", 900_000, 1_150_000, "Cam/Đen");
        shoeSizes(pTF03, "TF03", 780_000, 1_000_000, "Đen/Trắng");
        shoeSizes(pTF04, "TF04", 650_000,   850_000, "Navy/Trắng");
        shoeSizes(pTF05, "TF05", 580_000,   750_000, "Đen/Trắng");
        shoeSizes(pTF06, "TF06", 750_000,   980_000, "Vàng/Đen");
        shoeSizes(pTF07, "TF07", 680_000,   890_000, "Trắng/Đen");

        // Giày Futsal IC
        shoeSizes(pFS01, "FS01", 1_800_000, 2_200_000, "Trắng/Đen/Tím");
        shoeSizes(pFS02, "FS02", 1_200_000, 1_550_000, "Đen/Trắng");
        shoeSizes(pFS03, "FS03",   950_000, 1_250_000, "Navy/Trắng");
        shoeSizes(pFS04, "FS04", 1_100_000, 1_400_000, "Trắng/Đen");
        shoeSizes(pFS05, "FS05", 1_050_000, 1_350_000, "Xanh Dương");
        shoeSizes(pFS06, "FS06", 1_200_000, 1_550_000, "Vàng/Đen");

        // Phụ kiện đá bóng – fixed size (one size fits most)
        oneSize(pPK01, "PK01", 180_000, 230_000, "Trắng");
        oneSize(pPK02, "PK02", 150_000, 200_000, "Đen/Trắng");
        oneSize(pPK03, "PK03", 160_000, 210_000, "Đỏ/Trắng");
        oneSize(pPK04, "PK04", 250_000, 320_000, "Đen");
        oneSize(pPK05, "PK05", 280_000, 360_000, "Trắng");

        // Găng tay thủ môn – sizes 7/8/9/10
        gloveSizes(pGT01, "GT01", 650_000,   850_000, "Xanh/Cam");
        gloveSizes(pGT02, "GT02", 850_000, 1_100_000, "Đỏ/Đen");
        gloveSizes(pGT03, "GT03", 580_000,   750_000, "Xanh Lá/Đen");

        // Bóng thể thao – size 5
        oneSize(pBD01, "BD01", 1_800_000, 2_200_000, "Trắng/Đỏ/Xanh");
        oneSize(pBD02, "BD02", 2_200_000, 2_700_000, "Trắng/Vàng");
        oneSize(pBD03, "BD03",   350_000,   450_000, "Trắng/Đen");
        oneSize(pBD04, "BD04", 1_650_000, 2_000_000, "Trắng/Cam");

        // Áo chạy bộ – clothing sizes
        clothSizes(pACB01, "ACB01", 550_000, 720_000, "Đen");
        clothSizes(pACB02, "ACB02", 650_000, 850_000, "Xanh Dương");
        clothSizes(pACB03, "ACB03", 480_000, 620_000, "Trắng");
        clothSizes(pACB04, "ACB04", 520_000, 680_000, "Xám Nhạt");
        clothSizes(pACB05, "ACB05", 550_000, 720_000, "Hồng Pastel");
        clothSizes(pACB06, "ACB06", 480_000, 620_000, "Đen");
        clothSizes(pACB07, "ACB07", 450_000, 580_000, "Navy");

        // Quần chạy bộ – clothing sizes
        clothSizes(pQCB01, "QCB01", 480_000, 620_000, "Đen");
        clothSizes(pQCB02, "QCB02", 680_000, 880_000, "Đen");
        clothSizes(pQCB03, "QCB03", 420_000, 550_000, "Navy");
        clothSizes(pQCB04, "QCB04", 480_000, 620_000, "Đỏ");
        clothSizes(pQCB05, "QCB05", 380_000, 490_000, "Xanh Lá");

        // Giày chạy bộ – shoe sizes 38-45
        shoeSizesExtended(pGCB01, "GCB01", 3_200_000, 3_800_000, "Đen/Trắng");
        shoeSizesExtended(pGCB02, "GCB02", 3_800_000, 4_500_000, "Xanh/Trắng");
        shoeSizesExtended(pGCB03, "GCB03", 7_500_000, 8_500_000, "Hồng/Trắng");
        shoeSizesExtended(pGCB04, "GCB04", 2_200_000, 2_700_000, "Đỏ/Đen");
        shoeSizesExtended(pGCB05, "GCB05", 4_500_000, 5_200_000, "Trắng/Đen");
        shoeSizesExtended(pGCB06, "GCB06", 3_200_000, 3_800_000, "Navy/Trắng");
        shoeSizesExtended(pGCB07, "GCB07", 3_800_000, 4_500_000, "Xanh Lá/Đen");
        shoeSizesExtended(pGCB08, "GCB08", 2_500_000, 3_000_000, "Cam/Đen");
        shoeSizesExtended(pGCB09, "GCB09", 2_800_000, 3_400_000, "Vàng Neon/Đen");
        shoeSizesExtended(pGCB10, "GCB10", 1_800_000, 2_300_000, "Đen/Trắng");

        // Áo bóng rổ + quần – clothing sizes
        clothSizes(pABR01, "ABR01", 680_000,   880_000, "Đen/Trắng");
        clothSizes(pABR02, "ABR02", 1_200_000, 1_550_000, "Đỏ");
        clothSizes(pABR03, "ABR03", 750_000,   980_000, "Xanh Dương");
        clothSizes(pABR04, "ABR04", 550_000,   720_000, "Trắng");
        clothSizes(pQBR01, "QBR01", 580_000,   750_000, "Đen");

        // Giày bóng rổ – shoe sizes 39-46
        shoeSizesBasketball(pGBR01, "GBR01", 4_200_000, 5_000_000, "Đen/Đỏ");
        shoeSizesBasketball(pGBR02, "GBR02", 4_800_000, 5_700_000, "Trắng/Vàng");
        shoeSizesBasketball(pGBR03, "GBR03", 3_800_000, 4_600_000, "Xanh Dương");
        shoeSizesBasketball(pGBR04, "GBR04", 3_500_000, 4_200_000, "Xanh/Trắng");
        shoeSizesBasketball(pGBR05, "GBR05", 3_200_000, 3_900_000, "Đỏ/Đen");
        shoeSizesBasketball(pGBR06, "GBR06", 2_800_000, 3_500_000, "Tím/Đen");

        // Đồ gym nam – clothing sizes
        clothSizes(pGYM01, "GYM01", 480_000, 620_000, "Đen");
        clothSizes(pGYM02, "GYM02", 680_000, 880_000, "Xám");
        clothSizes(pGYM03, "GYM03", 520_000, 680_000, "Đen");
        clothSizes(pGYM04, "GYM04", 650_000, 850_000, "Đen");
        clothSizes(pGYM05, "GYM05", 420_000, 550_000, "Navy/Trắng");
        clothSizes(pGYM06, "GYM06", 580_000, 750_000, "Xanh Dương");

        // Đồ gym nữ – clothing sizes
        clothSizes(pGYMF01, "GFM01", 680_000,   880_000, "Đen");
        clothSizes(pGYMF02, "GFM02", 780_000, 1_000_000, "Đen");
        clothSizes(pGYMF03, "GFM03", 780_000, 1_000_000, "Đen");
        clothSizes(pGYMF04, "GFM04", 720_000,   930_000, "Tím/Đen");
        clothSizes(pGYMF05, "GFM05", 450_000,   580_000, "Hồng Coral");

        // Áo cầu lông & tennis – clothing sizes
        clothSizes(pCL01, "CL01", 780_000, 1_000_000, "Trắng");
        clothSizes(pCL02, "CL02", 680_000,   880_000, "Đen");
        clothSizes(pCL03, "CL03", 720_000,   930_000, "Xanh Biển");
        clothSizes(pCL04, "CL04", 420_000,   550_000, "Đỏ/Đen");
        clothSizes(pCL05, "CL05", 380_000,   490_000, "Vàng/Đen");

        // Giày cầu lông & tennis – shoe sizes
        shoeSizesExtended(pGCL01, "GCL01", 3_500_000, 4_200_000, "Đen/Xanh");
        shoeSizesExtended(pGCL02, "GCL02", 3_200_000, 3_900_000, "Trắng/Vàng");
        shoeSizesExtended(pGCL03, "GCL03", 2_800_000, 3_400_000, "Trắng/Hồng");
        shoeSizesExtended(pGCL04, "GCL04", 1_800_000, 2_300_000, "Navy");
        shoeSizesExtended(pGCL05, "GCL05", 1_800_000, 2_300_000, "Vàng/Đen");

        // ─── COLLECTION LINKS ─────────────────────────────────────────────────────
        // Mùa giải 2024/25
        link(pADB01, cMuaGiai, 1); link(pADB09, cMuaGiai, 2);
        link(pADB12, cMuaGiai, 3); link(pADB14, cMuaGiai, 4);
        link(pFG01,  cMuaGiai, 5); link(pFG06,  cMuaGiai, 6);
        link(pFG07,  cMuaGiai, 7); link(pFG02,  cMuaGiai, 8);
        link(pBD01,  cMuaGiai, 9); link(pBD02,  cMuaGiai, 10);

        // Đội tuyển Việt Nam 2024
        link(pADB05, cVietNam, 1); link(pADB06, cVietNam, 2);
        link(pADB07, cVietNam, 3); link(pQDB02, cVietNam, 4);
        link(pFS01,  cVietNam, 5); link(pPK01,  cVietNam, 6);

        // Summer Run Collection
        link(pGCB01, cSumRun, 1); link(pGCB02, cSumRun, 2);
        link(pGCB05, cSumRun, 3); link(pGCB07, cSumRun, 4);
        link(pACB01, cSumRun, 5); link(pACB02, cSumRun, 6);
        link(pQCB01, cSumRun, 7); link(pACB05, cSumRun, 8);

        // Sale cuối mùa
        link(pTF01, cSaleSP, 1); link(pTF03, cSaleSP, 2);
        link(pADB07, cSaleSP, 3); link(pADB13, cSaleSP, 4);
        link(pGYM01, cSaleSP, 5); link(pGYM05, cSaleSP, 6);
        link(pPK02,  cSaleSP, 7); link(pPK03,  cSaleSP, 8);

        // Basketball Collection
        link(pABR01, cBongRo, 1); link(pABR02, cBongRo, 2);
        link(pGBR01, cBongRo, 3); link(pGBR02, cBongRo, 4);
        link(pGBR04, cBongRo, 5); link(pGBR06, cBongRo, 6);
        link(pQBR01, cBongRo, 7);

        ensureHomeBanner();

        log.info("Sports seed complete: ~100 products, 17 categories, 4 brands, 5 collections, 1 banner.");
    }

    // ─── BANNER ───────────────────────────────────────────────────────────────

    private void ensureHomeBanner() {
        if (bannerRepository.existsByCode("SPORTS-HOME-HERO")) return;

        Banner banner = new Banner();
        banner.setName("Banner Trang Chủ John's Sport Shop");
        banner.setCode("SPORTS-HOME-HERO");
        banner.setPlacement(BannerPlacement.HOME_HERO);
        banner.setStatus(BannerStatus.ACTIVE);
        banner = bannerRepository.save(banner);

        String[][] slides = {
            {"/images/ivymoda/banner/6a051c7c1a148911a0f04bb13704e9e4.webp",
             "/lookbook/mua-giai-2024-25",   "Mùa Giải 2024/25 – Hàng Mới Về"},
            {"/images/ivymoda/banner/da4faa3fe3af0cef91c4696275413c54.webp",
             "/lookbook/sale-cuoi-mua",      "Sale Cuối Mùa – Giảm Tới 50%"},
        };
        for (int idx = 0; idx < slides.length; idx++) {
            BannerItem item = new BannerItem();
            item.setBanner(banner);
            item.setImageUrl(slides[idx][0]);
            item.setTargetUrl(slides[idx][1]);
            item.setTitle(slides[idx][2]);
            item.setSortOrder(idx);
            item.setActive(true);
            bannerItemRepository.save(item);
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private Brand brand(String slug, String name, String description) {
        Brand b = brandRepository.findBySlug(slug).orElseGet(Brand::new);
        b.setSlug(slug); b.setName(name);
        b.setDescription(description); b.setStatus(BrandStatus.ACTIVE);
        return brandRepository.save(b);
    }

    private Category catGroup(String slug, String name, Category parent, int sortOrder) {
        Category c = categoryRepository.findBySlug(slug).orElseGet(Category::new);
        c.setSlug(slug); c.setName(name); c.setStatus(CategoryStatus.ACTIVE);
        c.setParent(parent);
        c.setNodeType(CategoryNodeType.GROUP);
        c.setSortOrder(sortOrder);
        return categoryRepository.save(c);
    }

    private Category cat(String slug, String name, Category parent, int sortOrder) {
        Category c = categoryRepository.findBySlug(slug).orElseGet(Category::new);
        c.setSlug(slug); c.setName(name); c.setStatus(CategoryStatus.ACTIVE);
        c.setParent(parent);
        c.setNodeType(CategoryNodeType.LEAF);
        c.setSortOrder(sortOrder);
        return categoryRepository.save(c);
    }

    private Collection col(String slug, String name, int sortOrder) {
        Collection c = collectionRepository.findBySlug(slug).orElseGet(Collection::new);
        c.setSlug(slug); c.setName(name);
        c.setShortDescription("Bộ sưu tập " + name + " – John's Sport Shop.");
        c.setDescription("Khám phá " + name + " tại John's Sport Shop – trang phục & giày dép thể thao chất lượng cao.");
        c.setCollectionType(CollectionType.SEASONAL);
        c.setSeason("2024");
        c.setYear(2024);
        c.setBannerImageUrl(LOOKBOOK_IMG + "164e491a614ae80c318fc4e3376b9ac5.webp");
        c.setCoverImageUrl(LOOKBOOK_IMG + "565c2865e6497f2b6a1310017af86f39.webp");
        c.setStatus(CollectionStatus.ACTIVE);
        c.setSortOrder(sortOrder);
        c.setFeatured(sortOrder <= 3);
        return collectionRepository.save(c);
    }

    private Product p(String slug, String name, Category category, Brand brand,
                      String shortDescription, Gender gender, boolean featured, int imgIndex) {
        Product pr = productRepository.findBySlug(slug).orElseGet(Product::new);
        pr.setSlug(slug); pr.setName(name);
        pr.setCategory(category); pr.setBrand(brand);
        pr.setShortDescription(shortDescription);
        pr.setDescription("<p>" + shortDescription + "</p>"
                + "<p>Sản phẩm chính hãng 100%, bảo hành theo chính sách nhà sản xuất.</p>");
        pr.setGender(gender);

        // Detect product type from category slug
        String catSlug = category.getSlug();
        if (catSlug.startsWith("giay-") || catSlug.equals("giay-futsal")) {
            pr.setProductType(ProductType.FOOTWEAR);
        } else if (catSlug.equals("bong-the-thao")) {
            pr.setProductType(ProductType.EQUIPMENT);
        } else if (catSlug.equals("phu-kien-da-bong") || catSlug.equals("gang-tay-thu-mon")) {
            pr.setProductType(ProductType.ACCESSORY);
        } else {
            pr.setProductType(ProductType.APPAREL);
        }

        pr.setStatus(ProductStatus.ACTIVE);
        pr.setFeatured(featured);
        pr = productRepository.save(pr);

        String imgUrl = IMGS[Math.abs(imgIndex) % IMGS.length];
        String pubId = "sports-" + slug;
        ProductImage img = imageRepository.findByPublicId(pubId).orElseGet(ProductImage::new);
        img.setProduct(pr); img.setPublicId(pubId); img.setImageUrl(imgUrl);
        img.setAltText(name); img.setPrimary(true); img.setSortOrder(0);
        imageRepository.save(img);
        return pr;
    }

    /** S / M / L / XL / XXL variants – clothing. */
    private void clothSizes(Product product, String skuPrefix, int price, int compareAt, String color) {
        String[] sizes = {"S", "M", "L", "XL", "XXL"};
        int[]    qtys  = {10, 20, 25, 18, 10};
        for (int idx = 0; idx < sizes.length; idx++) {
            String sku = skuPrefix + "-" + sizes[idx];
            ProductVariant v = variantRepository.findBySku(sku).orElseGet(ProductVariant::new);
            v.setProduct(product); v.setSku(sku); v.setSize(sizes[idx]); v.setColor(color);
            v.setPrice(BigDecimal.valueOf(price)); v.setCompareAtPrice(BigDecimal.valueOf(compareAt));
            v.setStatus(VariantStatus.ACTIVE);
            if (v.getId() == null) { v.setStockQuantity(qtys[idx]); v.setReservedQuantity(0); }
            variantRepository.save(v);
        }
    }

    /** Size 39-44 – football/futsal shoes. */
    private void shoeSizes(Product product, String skuPrefix, int price, int compareAt, String color) {
        String[] sizes = {"39", "40", "41", "42", "43", "44"};
        int[]    qtys  = {8, 12, 15, 15, 12, 8};
        for (int idx = 0; idx < sizes.length; idx++) {
            String sku = skuPrefix + "-" + sizes[idx];
            ProductVariant v = variantRepository.findBySku(sku).orElseGet(ProductVariant::new);
            v.setProduct(product); v.setSku(sku); v.setSize(sizes[idx]); v.setColor(color);
            v.setPrice(BigDecimal.valueOf(price)); v.setCompareAtPrice(BigDecimal.valueOf(compareAt));
            v.setStatus(VariantStatus.ACTIVE);
            if (v.getId() == null) { v.setStockQuantity(qtys[idx]); v.setReservedQuantity(0); }
            variantRepository.save(v);
        }
    }

    /** Size 38-45 – running shoes (wider range). */
    private void shoeSizesExtended(Product product, String skuPrefix, int price, int compareAt, String color) {
        String[] sizes = {"38", "39", "40", "41", "42", "43", "44", "45"};
        int[]    qtys  = {5, 8, 12, 15, 15, 12, 8, 5};
        for (int idx = 0; idx < sizes.length; idx++) {
            String sku = skuPrefix + "-" + sizes[idx];
            ProductVariant v = variantRepository.findBySku(sku).orElseGet(ProductVariant::new);
            v.setProduct(product); v.setSku(sku); v.setSize(sizes[idx]); v.setColor(color);
            v.setPrice(BigDecimal.valueOf(price)); v.setCompareAtPrice(BigDecimal.valueOf(compareAt));
            v.setStatus(VariantStatus.ACTIVE);
            if (v.getId() == null) { v.setStockQuantity(qtys[idx]); v.setReservedQuantity(0); }
            variantRepository.save(v);
        }
    }

    /** Size 39-46 – basketball shoes. */
    private void shoeSizesBasketball(Product product, String skuPrefix, int price, int compareAt, String color) {
        String[] sizes = {"39", "40", "41", "42", "43", "44", "45", "46"};
        int[]    qtys  = {5, 8, 12, 15, 15, 12, 8, 3};
        for (int idx = 0; idx < sizes.length; idx++) {
            String sku = skuPrefix + "-" + sizes[idx];
            ProductVariant v = variantRepository.findBySku(sku).orElseGet(ProductVariant::new);
            v.setProduct(product); v.setSku(sku); v.setSize(sizes[idx]); v.setColor(color);
            v.setPrice(BigDecimal.valueOf(price)); v.setCompareAtPrice(BigDecimal.valueOf(compareAt));
            v.setStatus(VariantStatus.ACTIVE);
            if (v.getId() == null) { v.setStockQuantity(qtys[idx]); v.setReservedQuantity(0); }
            variantRepository.save(v);
        }
    }

    /** Size 7/8/9/10 – goalkeeper gloves. */
    private void gloveSizes(Product product, String skuPrefix, int price, int compareAt, String color) {
        String[] sizes = {"7", "8", "9", "10"};
        int[]    qtys  = {8, 12, 12, 8};
        for (int idx = 0; idx < sizes.length; idx++) {
            String sku = skuPrefix + "-" + sizes[idx];
            ProductVariant v = variantRepository.findBySku(sku).orElseGet(ProductVariant::new);
            v.setProduct(product); v.setSku(sku); v.setSize(sizes[idx]); v.setColor(color);
            v.setPrice(BigDecimal.valueOf(price)); v.setCompareAtPrice(BigDecimal.valueOf(compareAt));
            v.setStatus(VariantStatus.ACTIVE);
            if (v.getId() == null) { v.setStockQuantity(qtys[idx]); v.setReservedQuantity(0); }
            variantRepository.save(v);
        }
    }

    /** One-size variant – socks, balls, shin guards. */
    private void oneSize(Product product, String skuPrefix, int price, int compareAt, String color) {
        String sku = skuPrefix + "-ONESIZE";
        ProductVariant v = variantRepository.findBySku(sku).orElseGet(ProductVariant::new);
        v.setProduct(product); v.setSku(sku); v.setSize("One Size"); v.setColor(color);
        v.setPrice(BigDecimal.valueOf(price)); v.setCompareAtPrice(BigDecimal.valueOf(compareAt));
        v.setStatus(VariantStatus.ACTIVE);
        if (v.getId() == null) { v.setStockQuantity(50); v.setReservedQuantity(0); }
        variantRepository.save(v);
    }

    private void link(Product product, Collection collection, int sortOrder) {
        if (productCollectionRepository.existsByProductIdAndCollectionId(
                product.getId(), collection.getId())) return;
        ProductCollection pc = new ProductCollection();
        pc.setProduct(product);
        pc.setCollection(collection);
        pc.setSortOrder(sortOrder);
        productCollectionRepository.save(pc);
    }

}

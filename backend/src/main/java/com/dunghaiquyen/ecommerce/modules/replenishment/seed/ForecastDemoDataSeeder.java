package com.dunghaiquyen.ecommerce.modules.replenishment.seed;

import com.dunghaiquyen.ecommerce.common.time.AppTimeZone;
import com.dunghaiquyen.ecommerce.modules.replenishment.config.AppForecastDemoProperties;
import com.dunghaiquyen.ecommerce.infra.seed.SeedDataService;
import com.dunghaiquyen.ecommerce.modules.brand.entity.Brand;
import com.dunghaiquyen.ecommerce.modules.brand.repository.BrandRepository;
import com.dunghaiquyen.ecommerce.modules.category.entity.Category;
import com.dunghaiquyen.ecommerce.modules.category.repository.CategoryRepository;
import com.dunghaiquyen.ecommerce.modules.product.entity.Gender;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductType;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForecastDemoDataSeeder {

    public static final String ORDER_MARKER = "[FORECAST_DEMO]";
    private static final String PRODUCT_SLUG = "forecast-demo-sportswear";
    private static final String ORDER_SQL = """
            insert into orders (
                id, order_code, user_id, address_snapshot_json,
                subtotal_amount, shipping_fee, discount_amount, total_amount,
                payment_method, order_status, payment_status, note,
                created_at, updated_at
            ) values (?, ?, ?, cast(? as jsonb), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String ITEM_SQL = """
            insert into order_items (
                id, order_id, product_id, variant_id,
                product_name_snapshot, sku_snapshot, size_snapshot, color_snapshot,
                unit_price_snapshot, quantity, line_total
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final AppForecastDemoProperties properties;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final JdbcTemplate jdbcTemplate;

    public ForecastDemoDataSeeder(
            AppForecastDemoProperties properties,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            JdbcTemplate jdbcTemplate) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public record SeedSummary(int variants, int orders, int historyDays) {
    }

    @Transactional
    public SeedSummary seed() {
        int variantCount = variantCount();
        User customer = userRepository.findByEmail(SeedDataService.DEMO_CUSTOMER_ONE_EMAIL)
                .orElseThrow(() -> new IllegalStateException(
                        "Forecast demo seed requires core demo data; enable app.seed.enabled first"));
        Category category = categoryRepository.findBySlug("training")
                .orElseGet(() -> categoryRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Forecast demo seed requires a category")));
        Brand brand = brandRepository.findBySlug("nike")
                .orElseGet(() -> brandRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Forecast demo seed requires a brand")));

        Product product = ensureProduct(category, brand);
        List<ProductVariant> variants = ensureVariants(product, variantCount);

        // order_items cascade from orders. No user-created order is touched.
        jdbcTemplate.update("delete from orders where note = ?", ORDER_MARKER);
        insertHistory(customer, product, variants);
        return new SeedSummary(variants.size(), orderCount(), historyDays());
    }

    private Product ensureProduct(Category category, Brand brand) {
        return productRepository.findBySlug(PRODUCT_SLUG).orElseGet(() -> {
            Product product = new Product();
            product.setCategory(category);
            product.setBrand(brand);
            product.setName("Forecast Demo Sportswear");
            product.setSlug(PRODUCT_SLUG);
            product.setShortDescription("Controlled synthetic product used for replenishment experiments.");
            product.setDescription("DEMO DATA - not real customer sales data.");
            product.setGender(Gender.UNISEX);
            product.setSportType("training");
            product.setProductType(ProductType.APPAREL);
            product.setStatus(ProductStatus.ACTIVE);
            return productRepository.saveAndFlush(product);
        });
    }

    private List<ProductVariant> ensureVariants(Product product, int variantCount) {
        List<ProductVariant> variants = new ArrayList<>();
        for (int index = 0; index < variantCount; index++) {
            String sku = "FD-%03d".formatted(index + 1);
            int position = index;
            ProductVariant variant = variantRepository.findBySku(sku).orElseGet(() -> {
                ProductVariant created = new ProductVariant();
                created.setProduct(product);
                created.setSku(sku);
                created.setSize("D%02d".formatted(position + 1));
                created.setColor(demandGroup(position));
                created.setPrice(BigDecimal.valueOf(250_000L + position * 10_000L));
                created.setCompareAtPrice(BigDecimal.valueOf(300_000L + position * 10_000L));
                created.setStockQuantity(initialStock(position));
                created.setReservedQuantity(position % 4 == 0 ? Math.min(2, initialStock(position)) : 0);
                created.setStatus(VariantStatus.ACTIVE);
                return variantRepository.save(created);
            });
            variants.add(variant);
        }
        variantRepository.flush();
        variants.sort(Comparator.comparing(ProductVariant::getSku));
        return variants;
    }

    private void insertHistory(User customer, Product product, List<ProductVariant> variants) {
        Random random = new Random(randomSeed());
        LocalDate firstDate = LocalDate.now(AppTimeZone.ZONE).minusDays(historyDays() - 1L);
        List<Object[]> orders = new ArrayList<>(orderCount());
        List<Object[]> items = new ArrayList<>(orderCount());

        for (int sequence = 0; sequence < orderCount(); sequence++) {
            ProductVariant variant = chooseVariant(variants, random);
            int dayOffset = chooseDayOffset(variant, random);
            LocalDate orderDate = firstDate.plusDays(dayOffset);
            LocalTime orderTime = LocalTime.of(8 + random.nextInt(12), random.nextInt(60), random.nextInt(60));
            LocalDateTime localCreatedAt = LocalDateTime.of(orderDate, orderTime);
            Timestamp createdAt = Timestamp.from(localCreatedAt.atZone(AppTimeZone.ZONE).toInstant());
            String status = chooseStatus(random.nextDouble());
            int quantity = quantityFor(variant, orderDate, dayOffset, random);
            BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(quantity));
            UUID orderId = deterministicUuid("order-" + randomSeed() + "-" + sequence);
            UUID itemId = deterministicUuid("item-" + randomSeed() + "-" + sequence);
            String orderCode = "FD-%d-%06d".formatted(randomSeed(), sequence + 1);
            String paymentStatus = "DELIVERED".equals(status) ? "PAID" : "UNPAID";
            String addressJson = """
                    {"receiverName":"Forecast Demo Customer","phone":"0900000011","province":"Ho Chi Minh City","district":"District 1","ward":"Ben Nghe","detail":"DEMO DATA"}
                    """.trim();

            orders.add(new Object[] {
                    orderId, orderCode, customer.getId(), addressJson,
                    subtotal, BigDecimal.ZERO, BigDecimal.ZERO, subtotal,
                    "COD", status, paymentStatus, ORDER_MARKER, createdAt, createdAt
            });
            items.add(new Object[] {
                    itemId, orderId, product.getId(), variant.getId(),
                    product.getName(), variant.getSku(), variant.getSize(), variant.getColor(),
                    variant.getPrice(), quantity, subtotal
            });
        }

        jdbcTemplate.batchUpdate(ORDER_SQL, orders);
        jdbcTemplate.batchUpdate(ITEM_SQL, items);
    }

    private ProductVariant chooseVariant(List<ProductVariant> variants, Random random) {
        double draw = random.nextDouble();
        int index;
        if (draw < 0.40) index = random.nextInt(6);
        else if (draw < 0.82) index = 6 + random.nextInt(12);
        else if (draw < 0.96) index = 18 + random.nextInt(7);
        else index = 25 + random.nextInt(5);
        return variants.get(index);
    }

    private int chooseDayOffset(ProductVariant variant, Random random) {
        if (skuIndex(variant) >= 25) {
            int[] burstAnchors = {12, 42, 72, 102, 132, 162};
            int anchor = burstAnchors[random.nextInt(burstAnchors.length)];
            return Math.min(historyDays() - 1, anchor + random.nextInt(3));
        }
        return random.nextInt(historyDays());
    }

    private int quantityFor(ProductVariant variant, LocalDate date, int dayOffset, Random random) {
        int index = skuIndex(variant);
        int quantity = index < 6 ? 1 + random.nextInt(3) : 1 + random.nextInt(2);
        if (date.getDayOfWeek().getValue() >= 6 && random.nextDouble() < 0.30) quantity++;
        if ((dayOffset >= 55 && dayOffset <= 60) || (dayOffset >= 115 && dayOffset <= 120)) quantity++;
        if (index < 3 && dayOffset > historyDays() * 2 / 3 && random.nextDouble() < 0.35) quantity++;
        if (index >= 3 && index < 6 && dayOffset > historyDays() * 2 / 3) quantity = Math.max(1, quantity - 1);
        return quantity;
    }

    private String chooseStatus(double draw) {
        if (draw < 0.70) return "DELIVERED";
        if (draw < 0.80) return "SHIPPING";
        if (draw < 0.87) return "PACKING";
        if (draw < 0.92) return "CONFIRMED";
        if (draw < 0.95) return "PENDING_CONFIRMATION";
        return "CANCELLED";
    }

    private int initialStock(int index) {
        if (index < 6) return 4 + index;
        if (index < 18) return 12 + index % 8;
        if (index < 25) return 28 + index % 10;
        return 14 + index % 6;
    }

    private String demandGroup(int index) {
        if (index < 6) return "FAST";
        if (index < 18) return "NORMAL";
        if (index < 25) return "SLOW";
        return "INTERMITTENT";
    }

    private int skuIndex(ProductVariant variant) {
        return Integer.parseInt(variant.getSku().substring(3)) - 1;
    }

    private UUID deterministicUuid(String value) {
        return UUID.nameUUIDFromBytes(value.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    private long randomSeed() {
        return properties.randomSeed() == 0 ? 2026L : properties.randomSeed();
    }

    private int historyDays() {
        return properties.historyDays() == 0 ? 180 : properties.historyDays();
    }

    private int orderCount() {
        return properties.orderCount() == 0 ? 3000 : properties.orderCount();
    }

    private int variantCount() {
        int count = properties.variantCount() == 0 ? 30 : properties.variantCount();
        if (count != 30 || historyDays() < 30 || orderCount() <= 0) {
            throw new IllegalStateException("Forecast demo requires variant-count=30, history-days>=30 and order-count>0");
        }
        return count;
    }
}

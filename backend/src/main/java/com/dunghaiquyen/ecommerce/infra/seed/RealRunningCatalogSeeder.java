package com.dunghaiquyen.ecommerce.infra.seed;

import com.dunghaiquyen.ecommerce.modules.brand.entity.Brand;
import com.dunghaiquyen.ecommerce.modules.brand.entity.BrandStatus;
import com.dunghaiquyen.ecommerce.modules.brand.repository.BrandRepository;
import com.dunghaiquyen.ecommerce.modules.category.entity.Category;
import com.dunghaiquyen.ecommerce.modules.category.repository.CategoryRepository;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RealRunningCatalogSeeder {

    private static final Logger log = LoggerFactory.getLogger(RealRunningCatalogSeeder.class);
    private static final String SNAPSHOT_PATH = "seed/real-running-catalog.json";
    private static final List<Integer> DEFAULT_STOCKS = List.of(12, 18, 24, 16, 10, 8);

    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public RealRunningCatalogSeeder(
            ObjectMapper objectMapper,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository imageRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository) {
        this.objectMapper = objectMapper;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @Transactional
    public void seed() {
        if (productRepository.findBySlug("mens-tech-race-s-s-tee").isPresent()) {
            log.info("Real running catalog already applied, skipping.");
            return;
        }

        List<SeedProduct> products = loadSnapshot();
        for (SeedProduct seed : products) {
            upsert(seed);
        }
        log.info("Real running catalog seed complete: {} products.", products.size());
    }

    private void upsert(SeedProduct seed) {
        Category category = categoryRepository.findBySlug(seed.categorySlug())
                .orElseThrow(() -> new IllegalStateException("Category not found for seed: " + seed.categorySlug()));
        Brand brand = ensureBrand(seed.brandSlug(), seed.brandName(), seed.source());

        Product product = productRepository.findBySlug(seed.slug()).orElseGet(Product::new);
        product.setSlug(seed.slug());
        product.setName(seed.name());
        product.setCategory(category);
        product.setBrand(brand);
        product.setShortDescription(seed.shortDescription());
        product.setDescription(seed.description());
        product.setGender(Gender.valueOf(seed.gender()));
        product.setSportType(seed.sportType());
        product.setProductType(ProductType.valueOf(seed.productType()));
        product.setStatus(ProductStatus.valueOf(seed.status()));
        product.setFeatured(seed.featured());

        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("source", seed.source());
        attributes.put("sourceUrl", seed.sourceUrl());
        attributes.put("group", seed.group());
        attributes.put("seedType", "real-running-catalog");
        attributes.put("importedColor", seed.color());
        product.setAttributes(attributes);

        product = productRepository.save(product);

        replaceImages(product, seed);
        replaceVariants(product, seed);
    }

    private Brand ensureBrand(String slug, String name, String source) {
        Brand brand = brandRepository.findBySlug(slug).orElseGet(Brand::new);
        brand.setSlug(slug);
        brand.setName(name);
        brand.setDescription("Imported from live running catalog source: " + source + ".");
        brand.setStatus(BrandStatus.ACTIVE);
        return brandRepository.save(brand);
    }

    private void replaceImages(Product product, SeedProduct seed) {
        List<ProductImage> existing = imageRepository.findAllByProductIdOrderBySortOrderAsc(product.getId());
        if (!existing.isEmpty()) {
            imageRepository.deleteAll(existing);
        }

        for (int i = 0; i < seed.images().size(); i++) {
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setPublicId("real-running-" + seed.slug() + "-" + (i + 1));
            image.setImageUrl(seed.images().get(i));
            image.setAltText(seed.name());
            image.setSortOrder(i);
            image.setPrimary(i == 0);
            imageRepository.save(image);
        }
    }

    private void replaceVariants(Product product, SeedProduct seed) {
        List<ProductVariant> existing = variantRepository.findAllByProductIdOrderByCreatedAtAsc(product.getId());
        if (!existing.isEmpty()) {
            variantRepository.deleteAll(existing);
        }

        for (int i = 0; i < seed.sizes().size(); i++) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(resolveSku(seed, i));
            variant.setSize(seed.sizes().get(i));
            variant.setColor(seed.color());
            variant.setPrice(BigDecimal.valueOf(seed.basePrice()));
            variant.setCompareAtPrice(BigDecimal.valueOf(seed.compareAtPrice()));
            variant.setStockQuantity(DEFAULT_STOCKS.get(i % DEFAULT_STOCKS.size()));
            variant.setReservedQuantity(0);
            variant.setStatus(VariantStatus.ACTIVE);
            variantRepository.save(variant);
        }
    }

    private String resolveSku(SeedProduct seed, int index) {
        if (seed.sourceSkus() != null && index < seed.sourceSkus().size() && seed.sourceSkus().get(index) != null) {
            return seed.sourceSkus().get(index);
        }
        return ("REAL-" + seed.slug() + "-" + seed.sizes().get(index)).toUpperCase().replace("--", "-");
    }

    private List<SeedProduct> loadSnapshot() {
        ClassPathResource resource = new ClassPathResource(SNAPSHOT_PATH);
        try (InputStream input = resource.getInputStream()) {
            return objectMapper.readValue(input, new TypeReference<List<SeedProduct>>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load running catalog snapshot from " + SNAPSHOT_PATH, ex);
        }
    }

    private record SeedProduct(
            String slug,
            String name,
            String group,
            String source,
            String sourceUrl,
            String brandSlug,
            String brandName,
            String categorySlug,
            String gender,
            String sportType,
            String productType,
            String status,
            boolean featured,
            String shortDescription,
            String description,
            String color,
            int basePrice,
            int compareAtPrice,
            List<String> sizes,
            List<String> images,
            List<String> sourceSkus) {
    }
}

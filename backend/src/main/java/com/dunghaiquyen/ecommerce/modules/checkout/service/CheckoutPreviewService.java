package com.dunghaiquyen.ecommerce.modules.checkout.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.modules.address.repository.AddressRepository;
import com.dunghaiquyen.ecommerce.modules.checkout.dto.AppliedCouponSummary;
import com.dunghaiquyen.ecommerce.modules.checkout.dto.CheckoutItemPreview;
import com.dunghaiquyen.ecommerce.modules.checkout.dto.CheckoutPreviewRequest;
import com.dunghaiquyen.ecommerce.modules.checkout.dto.CheckoutPreviewResponse;
import com.dunghaiquyen.ecommerce.modules.checkout.dto.CouponValidationResponse;
import com.dunghaiquyen.ecommerce.modules.coupon.service.CouponService;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import com.dunghaiquyen.ecommerce.modules.order.service.OrderService;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deliberately a thin orchestrator, not a parallel reimplementation of
 * checkout: every rule that decides "is this line/coupon usable" is read
 * straight from OrderService.checkCartLines / CouponService.validate / and
 * OrderService.calculateShippingFee - the exact same methods
 * OrderService.createOrderFromCart itself calls. That is what makes the
 * "preview and real order must never disagree" requirement actually hold,
 * rather than just being true by construction at the moment this was
 * written and silently drifting the next time checkout's rules change.
 *
 * <p>Read-only by design: no cart row or variant row is ever locked here
 * (checkCartLines runs with lockForUpdate=false). One deliberate exception:
 * CouponService.validate still pessimistic-locks the coupon row even when
 * called from here - reusing it as-is (rather than forking a non-locking
 * variant) was judged the safer tradeoff, since a forked copy is exactly how
 * preview and real-order rules end up silently drifting apart. The risk this
 * accepts: a popular coupon being previewed very frequently could see some
 * lock contention against real checkouts. See final report.
 */
@Service
public class CheckoutPreviewService {

    private final OrderService orderService;
    private final CouponService couponService;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public CheckoutPreviewService(
            OrderService orderService,
            CouponService couponService,
            AddressRepository addressRepository,
            UserRepository userRepository) {
        this.orderService = orderService;
        this.couponService = couponService;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    /**
     * Empty cart is treated exactly like createOrderFromCart treats it - same
     * 422 status and message - rather than a 200 with a "this would fail"
     * payload. Consistency with the real checkout's own error shape was judged
     * more valuable than a richer "always 200" preview contract: a client that
     * already handles createOrderFromCart's empty-cart error handles this one
     * identically, with no extra branch.
     *
     * <p>Deliberately NOT @Transactional(readOnly = true): CouponService.validate
     * issues a SELECT ... FOR UPDATE on the coupon row (see its own javadoc),
     * and Postgres rejects FOR UPDATE outright inside a read-only transaction
     * ("cannot execute SELECT FOR NO KEY UPDATE in a read-only transaction") -
     * caught by actually exercising the coupon-preview path, not by inspection.
     * This method still never writes anything itself; the plain @Transactional
     * here exists only so the nested lock is allowed to be taken at all.
     */
    @Transactional
    public CheckoutPreviewResponse preview(UUID userId, CheckoutPreviewRequest request) {
        OrderService.CartLinesCheckResult linesResult = orderService.checkCartLines(userId);
        if (linesResult.lines().isEmpty()) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Cart is empty");
        }

        if (request.addressId() != null) {
            addressRepository.findById(request.addressId())
                    .filter(a -> a.getUser().getId().equals(userId))
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        }

        boolean allItemsValid = linesResult.allValid();
        BigDecimal subtotal = linesResult.validSubtotal();

        AppliedCouponSummary appliedCoupon = null;
        String couponError = null;
        BigDecimal discount = BigDecimal.ZERO;
        String couponCode = request.couponCode();
        if (couponCode != null && !couponCode.isBlank()) {
            if (!allItemsValid) {
                // Mirrors createOrderFromCart exactly: an order is never even
                // attempted while any line is invalid, so the coupon is never
                // reached/validated there either - its validity is simply
                // unknown here, not asserted either way.
                couponError = "Coupon was not checked because the cart has invalid items";
            } else {
                try {
                    User user = userRepository.getReferenceById(userId);
                    List<OrderItem> transientItems = toTransientOrderItems(linesResult);
                    CouponService.AppliedCoupon applied = couponService.validate(user, couponCode, subtotal, transientItems);
                    discount = applied.discountAmount();
                    appliedCoupon = new AppliedCouponSummary(applied.coupon().getCode(), discount);
                } catch (BusinessRuleException ex) {
                    couponError = ex.getMessage();
                }
            }
        }

        BigDecimal shippingFee = orderService.calculateShippingFee(subtotal);
        BigDecimal total = subtotal.add(shippingFee).subtract(discount);
        // Invalid coupon is a warning-only outcome in preview; cart validity alone decides checkoutability.
        boolean canCheckout = allItemsValid;

        List<CheckoutItemPreview> items =
                linesResult.lines().stream().map(this::toItemPreview).toList();

        return new CheckoutPreviewResponse(
                items, subtotal, discount, shippingFee, total, appliedCoupon, couponError, canCheckout);
    }

    /**
     * Standalone "would this coupon work on my current cart" check (Hướng B
     * of the public promotion/coupon phase - see this method and the class
     * javadoc for why no cart/session state is introduced). Deliberately
     * never throws for a business-invalid coupon: every failure path
     * (unknown code, expired/inactive, usage limit, min order amount, empty
     * cart, cart has invalid lines) comes back as a normal 200 with
     * valid=false and a clean message, exactly what a "checker" endpoint
     * should do - reusing the exact same OrderService.checkCartLines /
     * CouponService.validate calls preview() and createOrderFromCart both
     * use, so this can never say "valid" about something checkout would
     * then reject. Same not-readOnly reasoning as preview() (CouponService.
     * validate takes a row lock).
     */
    @Transactional
    public CouponValidationResponse validateCoupon(UUID userId, String couponCode) {
        OrderService.CartLinesCheckResult linesResult = orderService.checkCartLines(userId);
        if (linesResult.lines().isEmpty()) {
            return new CouponValidationResponse(false, couponCode, BigDecimal.ZERO, BigDecimal.ZERO, "Cart is empty");
        }

        boolean allItemsValid = linesResult.allValid();
        BigDecimal subtotal = linesResult.validSubtotal();
        if (!allItemsValid) {
            return new CouponValidationResponse(
                    false, couponCode, subtotal, BigDecimal.ZERO,
                    "Coupon was not checked because the cart has invalid items");
        }

        try {
            User user = userRepository.getReferenceById(userId);
            List<OrderItem> transientItems = toTransientOrderItems(linesResult);
            CouponService.AppliedCoupon applied = couponService.validate(user, couponCode, subtotal, transientItems);
            return new CouponValidationResponse(
                    true, applied.coupon().getCode(), subtotal, applied.discountAmount(), null);
        } catch (BusinessRuleException ex) {
            return new CouponValidationResponse(false, couponCode, subtotal, BigDecimal.ZERO, ex.getMessage());
        }
    }

    /**
     * Builds transient (never persisted) OrderItem instances purely so
     * CouponService.validate can read product/lineTotal off them exactly like
     * it does for a real order's items - no order exists yet, so there is
     * nothing to save here.
     */
    private List<OrderItem> toTransientOrderItems(OrderService.CartLinesCheckResult linesResult) {
        return linesResult.lines().stream()
                .filter(OrderService.CartLineCheck::valid)
                .map(line -> {
                    OrderItem item = new OrderItem();
                    item.setProduct(productRefOf(line));
                    item.setLineTotal(line.lineTotal());
                    return item;
                })
                .toList();
    }

    private Product productRefOf(OrderService.CartLineCheck line) {
        Product product = new Product();
        product.setId(line.productId());
        return product;
    }

    private CheckoutItemPreview toItemPreview(OrderService.CartLineCheck line) {
        return new CheckoutItemPreview(
                line.variantId(),
                line.productId(),
                line.productName(),
                line.sku(),
                line.quantity(),
                line.unitPrice(),
                line.lineTotal(),
                line.valid(),
                line.errorMessage());
    }
}

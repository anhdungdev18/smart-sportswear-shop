"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { getApiErrorMessage } from "@/lib/api-errors";
import { getAccessToken } from "@/lib/session";
import {
  createAddress,
  listAddresses,
  setDefaultAddress,
  type AddressResponse,
} from "@/modules/account/api";
import { createOrder, previewCheckout } from "@/modules/checkout/api";
import type { CheckoutPreviewResponse } from "@/modules/checkout/types";

type PaymentMethod = "COD" | "VNPAY";

const EMPTY_ADDRESS_FORM = {
  receiverName: "",
  phone: "",
  province: "",
  district: "",
  ward: "",
  addressLine: "",
};

export function CheckoutPageClient() {
  const [addresses, setAddresses] = useState<AddressResponse[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<string>("");
  const [addressForm, setAddressForm] = useState(EMPTY_ADDRESS_FORM);
  const [preview, setPreview] = useState<CheckoutPreviewResponse | null>(null);
  const [couponCode, setCouponCode] = useState("");
  const [note, setNote] = useState("");
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("COD");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [creatingAddress, setCreatingAddress] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const selectedAddress = useMemo(
    () => addresses.find((address) => address.id === selectedAddressId) ?? null,
    [addresses, selectedAddressId],
  );

  const refreshPreview = async (addressId?: string, nextCouponCode?: string) => {
    try {
      const response = await previewCheckout(addressId, nextCouponCode);
      setPreview(response);
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể tải dữ liệu thanh toán."));
    }
  };

  useEffect(() => {
    const load = async () => {
      if (!getAccessToken()) {
        setLoading(false);
        return;
      }

      try {
        const addressList = await listAddresses();
        setAddresses(addressList);
        const defaultAddress = addressList.find((item) => item.isDefault) ?? addressList[0];
        const defaultId = defaultAddress?.id ?? "";
        setSelectedAddressId(defaultId);
        await refreshPreview(defaultId, couponCode);
      } catch (err) {
        setError(getApiErrorMessage(err, "Không thể tải địa chỉ hoặc giỏ hàng."));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  const handleCreateAddress = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setCreatingAddress(true);
    setError(null);
    try {
      const address = await createAddress({ ...addressForm, isDefault: addresses.length === 0 });
      const nextAddresses = [address, ...addresses];
      setAddresses(nextAddresses);
      setSelectedAddressId(address.id);
      setAddressForm(EMPTY_ADDRESS_FORM);
      await refreshPreview(address.id, couponCode);
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể tạo địa chỉ mới."));
    } finally {
      setCreatingAddress(false);
    }
  };

  const handleSetDefault = async (id: string) => {
    try {
      await setDefaultAddress(id);
      const refreshed = await listAddresses();
      setAddresses(refreshed);
      setSelectedAddressId(id);
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể cập nhật địa chỉ mặc định."));
    }
  };

  const handleApplyCoupon = async () => {
    setError(null);
    await refreshPreview(selectedAddressId || undefined, couponCode.trim());
  };

  const handleCreateOrder = async () => {
    if (!selectedAddressId) {
      setError("Vui lòng chọn địa chỉ giao hàng.");
      return;
    }
    setSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      const order = await createOrder({
        addressId: selectedAddressId,
        paymentMethod,
        note: note || undefined,
        couponCode: couponCode.trim() || undefined,
      });
      setSuccess(`Đặt hàng thành công. Mã đơn của bạn là ${order.orderCode}.`);
      await refreshPreview(selectedAddressId, couponCode.trim());
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể tạo đơn hàng."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="flex-1 border-b border-ivy-hairline pt-[78px]">
      <div className="mx-auto max-w-[1368px] px-4 py-12 md:px-0">
        <div className="mb-10">
          <p className="mb-3 text-[13px] uppercase tracking-[0.24em] text-ivy-text-muted">Storefront</p>
          <h1 className="text-[40px] font-semibold uppercase tracking-[0.06em] text-ivy-dark">Thanh toán</h1>
        </div>

        {!getAccessToken() ? (
          <div className="border border-ivy-hairline px-6 py-10 text-[15px] text-ivy-text">
            Bạn cần đăng nhập tài khoản khách hàng để thanh toán.
          </div>
        ) : (
          <>
            {error ? <p className="mb-4 text-[14px] text-[#C62127]">{error}</p> : null}
            {success ? <p className="mb-4 text-[14px] text-[#257A4D]">{success}</p> : null}

            <div className="grid gap-10 lg:grid-cols-[minmax(0,1fr)_400px]">
              <section className="space-y-8">
                <div className="border border-ivy-hairline px-6 py-8">
                  <h2 className="mb-5 text-[26px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                    Địa chỉ nhận hàng
                  </h2>

                  {loading ? (
                    <p className="text-[15px] text-ivy-text">Đang tải địa chỉ và dữ liệu thanh toán...</p>
                  ) : addresses.length > 0 ? (
                    <div className="space-y-4">
                      {addresses.map((address) => (
                        <label
                          key={address.id}
                          className="flex cursor-pointer items-start gap-4 border border-ivy-hairline px-4 py-4"
                        >
                          <input
                            type="radio"
                            checked={selectedAddressId === address.id}
                            onChange={() => {
                              setSelectedAddressId(address.id);
                              void refreshPreview(address.id, couponCode.trim());
                            }}
                            className="mt-1 size-4 accent-ivy-dark"
                          />
                          <div className="flex-1">
                            <div className="flex items-center gap-3">
                              <p className="text-[16px] font-medium text-ivy-dark">{address.receiverName}</p>
                              {address.isDefault ? (
                                <span className="text-[12px] uppercase tracking-[0.06em] text-[#257A4D]">Mặc định</span>
                              ) : null}
                            </div>
                            <p className="mt-2 text-[14px] text-ivy-text">{address.phone}</p>
                            <p className="mt-1 text-[14px] text-ivy-text">
                              {address.addressLine}, {address.ward}, {address.district}, {address.province}
                            </p>
                            {!address.isDefault ? (
                              <button
                                type="button"
                                onClick={() => void handleSetDefault(address.id)}
                                className="mt-3 text-[12px] uppercase tracking-[0.06em] text-ivy-text underline"
                              >
                                Đặt làm mặc định
                              </button>
                            ) : null}
                          </div>
                        </label>
                      ))}
                    </div>
                  ) : (
                    <p className="mb-6 text-[15px] text-ivy-text">Bạn chưa có địa chỉ nào. Tạo mới ngay bên dưới.</p>
                  )}
                </div>

                <div className="border border-ivy-hairline px-6 py-8">
                  <h2 className="mb-5 text-[26px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                    Thêm địa chỉ mới
                  </h2>
                  <form className="grid gap-5 md:grid-cols-2" onSubmit={handleCreateAddress}>
                    <input
                      className="h-12 border border-ivy-hairline px-4 text-[15px] outline-none"
                      placeholder="Họ và tên"
                      value={addressForm.receiverName}
                      onChange={(e) => setAddressForm((current) => ({ ...current, receiverName: e.target.value }))}
                    />
                    <input
                      className="h-12 border border-ivy-hairline px-4 text-[15px] outline-none"
                      placeholder="Số điện thoại"
                      value={addressForm.phone}
                      onChange={(e) => setAddressForm((current) => ({ ...current, phone: e.target.value }))}
                    />
                    <input
                      className="h-12 border border-ivy-hairline px-4 text-[15px] outline-none"
                      placeholder="Tỉnh / Thành phố"
                      value={addressForm.province}
                      onChange={(e) => setAddressForm((current) => ({ ...current, province: e.target.value }))}
                    />
                    <input
                      className="h-12 border border-ivy-hairline px-4 text-[15px] outline-none"
                      placeholder="Quận / Huyện"
                      value={addressForm.district}
                      onChange={(e) => setAddressForm((current) => ({ ...current, district: e.target.value }))}
                    />
                    <input
                      className="h-12 border border-ivy-hairline px-4 text-[15px] outline-none"
                      placeholder="Phường / Xã"
                      value={addressForm.ward}
                      onChange={(e) => setAddressForm((current) => ({ ...current, ward: e.target.value }))}
                    />
                    <input
                      className="h-12 border border-ivy-hairline px-4 text-[15px] outline-none md:col-span-2"
                      placeholder="Địa chỉ chi tiết"
                      value={addressForm.addressLine}
                      onChange={(e) => setAddressForm((current) => ({ ...current, addressLine: e.target.value }))}
                    />
                    <div className="md:col-span-2">
                      <button
                        type="submit"
                        disabled={creatingAddress}
                        className="h-12 rounded-tl-[20px] rounded-br-[20px] border border-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-ivy-dark disabled:opacity-60"
                      >
                        {creatingAddress ? "Đang lưu..." : "Lưu địa chỉ"}
                      </button>
                    </div>
                  </form>
                </div>

                <div className="border border-ivy-hairline px-6 py-8">
                  <h2 className="mb-5 text-[26px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                    Phương thức thanh toán
                  </h2>
                  <div className="space-y-4 text-[15px] text-ivy-text">
                    <label className="flex items-center gap-3">
                      <input
                        type="radio"
                        name="payment"
                        checked={paymentMethod === "COD"}
                        onChange={() => setPaymentMethod("COD")}
                        className="size-4 accent-ivy-dark"
                      />
                      <span>Thanh toán khi nhận hàng (COD)</span>
                    </label>
                    <label className="flex items-center gap-3">
                      <input
                        type="radio"
                        name="payment"
                        checked={paymentMethod === "VNPAY"}
                        onChange={() => setPaymentMethod("VNPAY")}
                        className="size-4 accent-ivy-dark"
                      />
                      <span>Thanh toán VNPAY</span>
                    </label>
                  </div>

                  <textarea
                    value={note}
                    onChange={(e) => setNote(e.target.value)}
                    className="mt-6 min-h-[120px] w-full border border-ivy-hairline px-4 py-3 text-[15px] outline-none"
                    placeholder="Ghi chú đơn hàng"
                  />
                </div>
              </section>

              <aside className="h-fit border border-ivy-hairline px-6 py-8">
                <h2 className="mb-6 text-[28px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Đơn hàng của bạn</h2>

                <div className="mb-6 border border-ivy-hairline p-4">
                  <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                    Mã ưu đãi
                  </label>
                  <div className="flex gap-3">
                    <input
                      value={couponCode}
                      onChange={(e) => setCouponCode(e.target.value)}
                      className="h-11 flex-1 border border-ivy-hairline px-4 text-[14px] outline-none"
                      placeholder="Nhập coupon"
                    />
                    <button
                      type="button"
                      onClick={() => void handleApplyCoupon()}
                      className="h-11 rounded-tl-[16px] rounded-br-[16px] border border-ivy-dark px-5 text-[12px] font-semibold uppercase tracking-[0.05em] text-ivy-dark"
                    >
                      Áp dụng
                    </button>
                  </div>
                </div>

                <div className="space-y-5 border-b border-ivy-hairline pb-6">
                  {preview?.items?.map((item) => (
                    <div key={item.variantId} className="flex items-start justify-between gap-4">
                      <div>
                        <p className="text-[15px] font-medium text-ivy-dark">{item.productName}</p>
                        <p className="mt-1 text-[14px] text-ivy-text">SKU: {item.sku}</p>
                        <p className="mt-1 text-[14px] text-ivy-text">SL: {item.quantity}</p>
                        {!item.valid && item.errorMessage ? (
                          <p className="mt-1 text-[13px] text-[#C62127]">{item.errorMessage}</p>
                        ) : null}
                      </div>
                      <span className="text-[16px] font-semibold text-ivy-dark">
                        {item.lineTotal.toLocaleString("vi-VN")}đ
                      </span>
                    </div>
                  ))}
                </div>

                <div className="space-y-4 py-6">
                  <div className="flex items-center justify-between text-[15px] text-ivy-text">
                    <span>Tạm tính</span>
                    <span>{(preview?.subtotal ?? 0).toLocaleString("vi-VN")}đ</span>
                  </div>
                  <div className="flex items-center justify-between text-[15px] text-ivy-text">
                    <span>Giảm giá</span>
                    <span>-{(preview?.discountAmount ?? 0).toLocaleString("vi-VN")}đ</span>
                  </div>
                  <div className="flex items-center justify-between text-[15px] text-ivy-text">
                    <span>Phí vận chuyển</span>
                    <span>{(preview?.shippingFee ?? 0).toLocaleString("vi-VN")}đ</span>
                  </div>
                  {preview?.couponError ? (
                    <p className="text-[13px] text-[#C62127]">{preview.couponError}</p>
                  ) : preview?.appliedCoupon ? (
                    <p className="text-[13px] text-[#257A4D]">Đã áp dụng mã {preview.appliedCoupon.code}.</p>
                  ) : null}
                </div>

                <div className="flex items-center justify-between border-t border-ivy-hairline pt-6 text-[24px] font-semibold text-ivy-dark">
                  <span>Tổng cộng</span>
                  <span>{(preview?.totalAmount ?? 0).toLocaleString("vi-VN")}đ</span>
                </div>

                <button
                  disabled={submitting || !selectedAddress || !preview?.canCheckout}
                  onClick={() => void handleCreateOrder()}
                  className="mt-6 h-12 w-full rounded-tl-[20px] rounded-br-[20px] bg-ivy-dark text-[14px] font-semibold uppercase tracking-[0.05em] text-white disabled:opacity-60"
                >
                  {submitting ? "Đang xử lý..." : "Hoàn tất đặt hàng"}
                </button>

                <Link href="/gio-hang" className="mt-4 block text-center text-[14px] text-ivy-dark underline">
                  Quay lại giỏ hàng
                </Link>
              </aside>
            </div>
          </>
        )}
      </div>
    </main>
  );
}

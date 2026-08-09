"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { getApiErrorMessage } from "@/lib/api-errors";
import { emitSessionChange } from "@/lib/session";
import { useAuthenticated } from "@/lib/use-authenticated";
import {
  createAddress,
  deleteAddress,
  listAddresses,
  listMyOrders,
  setDefaultAddress,
  updateAddress,
  type AddressResponse,
  type OrderResponse,
} from "@/modules/account/api";
import { getMe, updateMe } from "@/modules/auth/api";
import type { AuthUser } from "@/modules/auth/types";
import { getOrderStatusLabel } from "@/modules/account/order-labels";

const EMPTY_ADDRESS = {
  receiverName: "",
  phone: "",
  province: "",
  district: "",
  ward: "",
  addressLine: "",
};

export function AccountPageClient() {
  const authenticated = useAuthenticated();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [addresses, setAddresses] = useState<AddressResponse[]>([]);
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [profileForm, setProfileForm] = useState({ fullName: "", phone: "" });
  const [addressForm, setAddressForm] = useState(EMPTY_ADDRESS);
  const [editingAddressId, setEditingAddressId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingAddress, setSavingAddress] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const latestOrders = useMemo(() => orders.slice(0, 3), [orders]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [me, addressList, orderResult] = await Promise.all([getMe(), listAddresses(), listMyOrders({ limit: 5 })]);
      setUser(me);
      setProfileForm({ fullName: me.fullName || "", phone: me.phone || "" });
      setAddresses(addressList);
      setOrders(orderResult.data);
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể tải thông tin tài khoản."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!authenticated) {
      setLoading(false);
      return;
    }
    void loadData();
  }, [authenticated]);

  const resetAddressEditor = () => {
    setEditingAddressId(null);
    setAddressForm(EMPTY_ADDRESS);
  };

  const handleSaveProfile = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSavingProfile(true);
    setError(null);
    setSuccess(null);
    try {
      const updated = await updateMe({
        fullName: profileForm.fullName || undefined,
        phone: profileForm.phone || undefined,
      });
      setUser(updated);
      setSuccess("Thông tin tài khoản đã được cập nhật.");
      emitSessionChange();
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể cập nhật thông tin."));
    } finally {
      setSavingProfile(false);
    }
  };

  const handleSaveAddress = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSavingAddress(true);
    setError(null);
    setSuccess(null);
    try {
      if (editingAddressId) {
        await updateAddress(editingAddressId, addressForm);
        setSuccess("Địa chỉ đã được cập nhật.");
      } else {
        await createAddress({
          ...addressForm,
          isDefault: addresses.length === 0,
        });
        setSuccess("Địa chỉ mới đã được thêm.");
      }
      resetAddressEditor();
      setAddresses(await listAddresses());
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể lưu địa chỉ."));
    } finally {
      setSavingAddress(false);
    }
  };

  const handleEditAddress = (address: AddressResponse) => {
    setEditingAddressId(address.id);
    setAddressForm({
      receiverName: address.receiverName,
      phone: address.phone,
      province: address.province,
      district: address.district,
      ward: address.ward,
      addressLine: address.addressLine,
    });
  };

  const handleDeleteAddress = async (id: string) => {
    setError(null);
    setSuccess(null);
    try {
      await deleteAddress(id);
      setAddresses(await listAddresses());
      if (editingAddressId === id) resetAddressEditor();
      setSuccess("Địa chỉ đã được xóa.");
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể xóa địa chỉ."));
    }
  };

  const handleDefaultAddress = async (id: string) => {
    setError(null);
    setSuccess(null);
    try {
      await setDefaultAddress(id);
      setAddresses(await listAddresses());
      setSuccess("Đã cập nhật địa chỉ mặc định.");
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể đổi địa chỉ mặc định."));
    }
  };

  if (!authenticated) {
    return (
      <main className="page-below-header flex-1 border-b border-ivy-hairline">
        <div className="mx-auto max-w-[1180px] px-4 py-16 md:px-0">
          <h1 className="text-[40px] font-semibold uppercase tracking-[0.06em] text-ivy-dark">Tài khoản</h1>
          <p className="mt-5 text-[15px] leading-7 text-ivy-text">Bạn cần đăng nhập để quản lý hồ sơ và địa chỉ giao hàng.</p>
          <Link
            href="/dang-nhap"
            className="mt-8 inline-flex h-12 items-center justify-center rounded-tl-[20px] rounded-br-[20px] bg-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-white"
          >
            Đăng nhập ngay
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className="page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1368px] px-4 py-12 md:px-0">
        <div className="mb-10">
          <p className="mb-3 text-[13px] uppercase tracking-[0.24em] text-ivy-text-muted">Khách hàng</p>
          <h1 className="text-[40px] font-semibold uppercase tracking-[0.06em] text-ivy-dark">Tài khoản của tôi</h1>
        </div>

        {error ? <p className="mb-4 text-[14px] text-[#C62127]">{error}</p> : null}
        {success ? <p className="mb-4 text-[14px] text-[#257A4D]">{success}</p> : null}

        {loading ? (
          <div className="border border-ivy-hairline px-6 py-10 text-[15px] text-ivy-text">Đang tải thông tin tài khoản...</div>
        ) : (
          <div className="grid gap-10 lg:grid-cols-[minmax(0,1fr)_420px]">
            <section className="space-y-8">
              <div className="border border-ivy-hairline px-6 py-8">
                <h2 className="mb-5 text-[26px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Hồ sơ cá nhân</h2>
                <form className="grid gap-5 md:grid-cols-2" onSubmit={handleSaveProfile}>
                  <div className="md:col-span-2">
                    <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                      Email
                    </label>
                    <input
                      value={user?.email || ""}
                      disabled
                      className="h-12 w-full border border-ivy-hairline bg-[#f8f8f8] px-4 text-[15px] text-ivy-text"
                    />
                  </div>
                  <div>
                    <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                      Họ và tên
                    </label>
                    <input
                      value={profileForm.fullName}
                      onChange={(e) => setProfileForm((current) => ({ ...current, fullName: e.target.value }))}
                      className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none"
                    />
                  </div>
                  <div>
                    <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                      Số điện thoại
                    </label>
                    <input
                      value={profileForm.phone}
                      onChange={(e) => setProfileForm((current) => ({ ...current, phone: e.target.value }))}
                      className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <button
                      type="submit"
                      disabled={savingProfile}
                      className="h-12 rounded-tl-[20px] rounded-br-[20px] bg-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-white disabled:opacity-60"
                    >
                      {savingProfile ? "Đang lưu..." : "Lưu thông tin"}
                    </button>
                  </div>
                </form>
              </div>

              <div className="border border-ivy-hairline px-6 py-8">
                <div className="mb-5 flex items-center justify-between gap-4">
                  <h2 className="text-[26px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Sổ địa chỉ</h2>
                  <button
                    type="button"
                    onClick={resetAddressEditor}
                    className="text-[12px] font-semibold uppercase tracking-[0.06em] text-ivy-text underline"
                  >
                    Thêm địa chỉ mới
                  </button>
                </div>

                <div className="space-y-4">
                  {addresses.map((address) => (
                    <article key={address.id} className="border border-ivy-hairline px-4 py-4">
                      <div className="flex items-center justify-between gap-4">
                        <div>
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
                        </div>
                        <div className="flex flex-col items-end gap-2 text-[12px] uppercase tracking-[0.06em] text-ivy-text">
                          <button type="button" className="underline" onClick={() => handleEditAddress(address)}>
                            Sửa
                          </button>
                          {!address.isDefault ? (
                            <button type="button" className="underline" onClick={() => void handleDefaultAddress(address.id)}>
                              Đặt mặc định
                            </button>
                          ) : null}
                          <button type="button" className="underline" onClick={() => void handleDeleteAddress(address.id)}>
                            Xóa
                          </button>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>

                <form className="mt-8 grid gap-5 md:grid-cols-2" onSubmit={handleSaveAddress}>
                  <div>
                    <input
                      value={addressForm.receiverName}
                      onChange={(e) => setAddressForm((current) => ({ ...current, receiverName: e.target.value }))}
                      placeholder="Họ và tên người nhận"
                      className="h-12 w-full border border-ivy-hairline px-4 text-[15px] outline-none"
                    />
                  </div>
                  <div>
                    <input
                      value={addressForm.phone}
                      onChange={(e) => setAddressForm((current) => ({ ...current, phone: e.target.value }))}
                      placeholder="Số điện thoại"
                      className="h-12 w-full border border-ivy-hairline px-4 text-[15px] outline-none"
                    />
                  </div>
                  <div>
                    <input
                      value={addressForm.province}
                      onChange={(e) => setAddressForm((current) => ({ ...current, province: e.target.value }))}
                      placeholder="Tỉnh / Thành phố"
                      className="h-12 w-full border border-ivy-hairline px-4 text-[15px] outline-none"
                    />
                  </div>
                  <div>
                    <input
                      value={addressForm.district}
                      onChange={(e) => setAddressForm((current) => ({ ...current, district: e.target.value }))}
                      placeholder="Quận / Huyện"
                      className="h-12 w-full border border-ivy-hairline px-4 text-[15px] outline-none"
                    />
                  </div>
                  <div>
                    <input
                      value={addressForm.ward}
                      onChange={(e) => setAddressForm((current) => ({ ...current, ward: e.target.value }))}
                      placeholder="Phường / Xã"
                      className="h-12 w-full border border-ivy-hairline px-4 text-[15px] outline-none"
                    />
                  </div>
                  <div>
                    <input
                      value={addressForm.addressLine}
                      onChange={(e) => setAddressForm((current) => ({ ...current, addressLine: e.target.value }))}
                      placeholder="Địa chỉ chi tiết"
                      className="h-12 w-full border border-ivy-hairline px-4 text-[15px] outline-none"
                    />
                  </div>
                  <div className="md:col-span-2 flex gap-3">
                    <button
                      type="submit"
                      disabled={savingAddress}
                      className="h-12 rounded-tl-[20px] rounded-br-[20px] border border-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-ivy-dark disabled:opacity-60"
                    >
                      {savingAddress ? "Đang lưu..." : editingAddressId ? "Cập nhật địa chỉ" : "Thêm địa chỉ"}
                    </button>
                    {editingAddressId ? (
                      <button
                        type="button"
                        onClick={resetAddressEditor}
                        className="h-12 rounded-tl-[20px] rounded-br-[20px] border border-ivy-hairline px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-ivy-text"
                      >
                        Hủy sửa
                      </button>
                    ) : null}
                  </div>
                </form>
              </div>
            </section>

            <aside className="space-y-8">
              <div className="border border-ivy-hairline px-6 py-8">
                <h2 className="mb-4 text-[24px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Tóm tắt tài khoản</h2>
                <div className="space-y-3 text-[15px] text-ivy-text">
                  <p><span className="font-medium text-ivy-dark">Khách hàng:</span> {user?.fullName}</p>
                  <p><span className="font-medium text-ivy-dark">Email:</span> {user?.email}</p>
                  <p><span className="font-medium text-ivy-dark">SĐT:</span> {user?.phone || "Chưa cập nhật"}</p>
                  <p><span className="font-medium text-ivy-dark">Vai trò:</span> {user?.role}</p>
                </div>
              </div>

              <div className="border border-ivy-hairline px-6 py-8">
                <div className="mb-5 flex items-center justify-between gap-4">
                  <h2 className="text-[24px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Đơn hàng gần đây</h2>
                  <Link href="/tra-cuu-don-hang" className="text-[12px] uppercase tracking-[0.06em] text-ivy-text underline">
                    Xem tất cả
                  </Link>
                </div>
                <div className="space-y-4">
                  {latestOrders.length > 0 ? latestOrders.map((order) => (
                    <article key={order.id} className="border border-ivy-hairline px-4 py-4">
                      <div className="flex items-start justify-between gap-4">
                        <div>
                          <p className="text-[16px] font-medium text-ivy-dark">{order.orderCode}</p>
                          <p className="mt-1 text-[14px] text-ivy-text">{new Date(order.createdAt).toLocaleString("vi-VN")}</p>
                          <p className="mt-1 text-[14px] text-ivy-text">Trạng thái: {getOrderStatusLabel(order.orderStatus)}</p>
                        </div>
                        <span className="text-[16px] font-semibold text-ivy-dark">{order.totalAmount.toLocaleString("vi-VN")}đ</span>
                      </div>
                    </article>
                  )) : (
                    <p className="text-[15px] text-ivy-text">Bạn chưa có đơn hàng nào.</p>
                  )}
                </div>
              </div>
            </aside>
          </div>
        )}
      </div>
    </main>
  );
}

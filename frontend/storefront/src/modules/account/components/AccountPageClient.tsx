"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Check, ChevronRight, KeyRound, MapPin, Package, Plus, ShieldCheck, UserRound } from "lucide-react";
import { getApiErrorMessage } from "@/lib/api-errors";
import { emitSessionChange } from "@/lib/session";
import { useAuthenticated } from "@/lib/use-authenticated";
import { createAddress, deleteAddress, listAddresses, listMyOrders, setDefaultAddress, updateAddress, type AddressResponse, type OrderResponse } from "@/modules/account/api";
import { forgotPassword, getMe, updateMe } from "@/modules/auth/api";
import type { AuthUser } from "@/modules/auth/types";
import { getOrderStatusLabel } from "@/modules/account/order-labels";
import { ProvinceWardSelect } from "@/components/shared/ProvinceWardSelect";

const EMPTY_ADDRESS = { receiverName: "", phone: "", province: "", ward: "", addressLine: "" };
const tabs = [
  { id: "profile", label: "Thông tin", icon: UserRound, description: "Hồ sơ cá nhân" },
  { id: "addresses", label: "Địa chỉ", icon: MapPin, description: "Địa chỉ nhận hàng" },
  { id: "orders", label: "Đơn hàng", icon: Package, description: "Lịch sử mua sắm" },
  { id: "password", label: "Đặt lại mật khẩu", icon: KeyRound, description: "Bảo mật tài khoản" },
] as const;
type TabId = (typeof tabs)[number]["id"];

export function AccountPageClient() {
  const authenticated = useAuthenticated();
  const searchParams = useSearchParams();
  const requestedTab = searchParams.get("tab");
  const activeTab: TabId = tabs.some((tab) => tab.id === requestedTab) ? requestedTab as TabId : "profile";
  const [user, setUser] = useState<AuthUser | null>(null);
  const [addresses, setAddresses] = useState<AddressResponse[]>([]);
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [profileForm, setProfileForm] = useState({ fullName: "", phone: "" });
  const [addressForm, setAddressForm] = useState(EMPTY_ADDRESS);
  const [editingAddressId, setEditingAddressId] = useState<string | null>(null);
  const [showAddressForm, setShowAddressForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const loadData = async () => {
    setLoading(true); setError(null);
    try {
      const [me, addressList, orderResult] = await Promise.all([getMe(), listAddresses(), listMyOrders({ limit: 20 })]);
      setUser(me); setProfileForm({ fullName: me.fullName || "", phone: me.phone || "" }); setAddresses(addressList); setOrders(orderResult.data);
    } catch (err) { setError(getApiErrorMessage(err, "Không thể tải thông tin tài khoản.")); }
    finally { setLoading(false); }
  };
  useEffect(() => { if (authenticated) void loadData(); else setLoading(false); }, [authenticated]);

  const notify = (message: string) => { setError(null); setSuccess(message); window.scrollTo({ top: 0, behavior: "smooth" }); };
  const saveProfile = async (event: React.FormEvent) => {
    event.preventDefault(); setSaving(true); setError(null);
    try { const updated = await updateMe(profileForm); setUser(updated); emitSessionChange(); notify("Thông tin cá nhân đã được cập nhật."); }
    catch (err) { setError(getApiErrorMessage(err, "Không thể cập nhật thông tin.")); } finally { setSaving(false); }
  };
  const resetAddressForm = () => { setEditingAddressId(null); setAddressForm(EMPTY_ADDRESS); setShowAddressForm(false); };
  const saveAddress = async (event: React.FormEvent) => {
    event.preventDefault(); setSaving(true); setError(null);
    try {
      if (editingAddressId) await updateAddress(editingAddressId, addressForm);
      else await createAddress({ ...addressForm, isDefault: addresses.length === 0 });
      setAddresses(await listAddresses()); resetAddressForm(); notify(editingAddressId ? "Địa chỉ đã được cập nhật." : "Đã thêm địa chỉ mới.");
    } catch (err) { setError(getApiErrorMessage(err, "Không thể lưu địa chỉ.")); } finally { setSaving(false); }
  };
  const editAddress = (address: AddressResponse) => { setEditingAddressId(address.id); setAddressForm({ receiverName: address.receiverName, phone: address.phone, province: address.province, ward: address.ward, addressLine: address.addressLine }); setShowAddressForm(true); };
  const removeAddress = async (id: string) => { try { await deleteAddress(id); setAddresses(await listAddresses()); notify("Địa chỉ đã được xóa."); } catch (err) { setError(getApiErrorMessage(err, "Không thể xóa địa chỉ.")); } };
  const makeDefault = async (id: string) => { try { await setDefaultAddress(id); setAddresses(await listAddresses()); notify("Đã đổi địa chỉ mặc định."); } catch (err) { setError(getApiErrorMessage(err, "Không thể đổi địa chỉ mặc định.")); } };
  const sendResetEmail = async () => { if (!user?.email) return; setSaving(true); setError(null); try { await forgotPassword(user.email); notify("Liên kết đặt lại mật khẩu đã được gửi tới email của bạn."); } catch (err) { setError(getApiErrorMessage(err, "Không thể gửi email đặt lại mật khẩu.")); } finally { setSaving(false); } };

  if (!authenticated) return <main className="page-below-header flex-1"><div className="mx-auto max-w-[1180px] px-4 py-20"><h1 className="text-4xl font-semibold uppercase">Tài khoản</h1><p className="mt-4 text-ivy-text">Vui lòng đăng nhập để quản lý tài khoản.</p><Link href="/dang-nhap" className="mt-7 inline-flex rounded-xl bg-ivy-dark px-7 py-3 text-sm font-semibold text-white">Đăng nhập ngay</Link></div></main>;

  return <main className="page-below-header min-h-[720px] flex-1 bg-[#faf9f7] border-b border-ivy-hairline">
    <div className="mx-auto max-w-[1280px] px-4 py-10 md:px-6 md:py-14">
      <div className="mb-8"><p className="mb-2 text-xs font-semibold uppercase tracking-[0.2em] text-[#9b6b45]">Không gian của bạn</p><h1 className="text-3xl font-semibold tracking-tight text-ivy-dark md:text-[42px]">Tài khoản của tôi</h1><p className="mt-2 text-sm text-ivy-text-muted">Quản lý thông tin, địa chỉ và đơn hàng tại một nơi.</p></div>
      {error && <div className="mb-5 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}
      {success && <div className="mb-5 flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700"><Check className="size-4" />{success}</div>}
      {loading ? <div className="rounded-2xl border border-ivy-hairline bg-white p-10 text-sm text-ivy-text">Đang tải thông tin tài khoản...</div> :
      <div className="grid items-start gap-6 lg:grid-cols-[280px_minmax(0,1fr)]">
        <aside className="overflow-hidden rounded-2xl border border-[#e8e4df] bg-white shadow-[0_8px_30px_rgba(34,31,32,0.04)]">
          <div className="border-b border-[#eeeae5] p-5"><div className="flex size-11 items-center justify-center rounded-full bg-[#f2ebe4] text-[#8a5a37]"><UserRound className="size-5" /></div><p className="mt-3 font-semibold text-ivy-dark">{user?.fullName || "Khách hàng"}</p><p className="mt-1 truncate text-xs text-ivy-text-muted">{user?.email}</p></div>
          <nav className="p-2">{tabs.map(({ id, label, icon: Icon, description }) => <Link key={id} href={`/tai-khoan?tab=${id}`} className={`flex items-center gap-3 rounded-xl px-3 py-3.5 transition ${activeTab === id ? "bg-ivy-dark text-white" : "text-ivy-dark hover:bg-[#f7f4f0]"}`}><Icon className={`size-5 ${activeTab === id ? "text-[#e4b789]" : "text-[#9b6b45]"}`} /><span className="min-w-0 flex-1"><span className="block text-sm font-semibold">{label}</span><span className={`block text-[11px] ${activeTab === id ? "text-white/60" : "text-ivy-text-muted"}`}>{description}</span></span><ChevronRight className="size-4 opacity-50" /></Link>)}</nav>
        </aside>
        <section className="rounded-2xl border border-[#e8e4df] bg-white p-5 shadow-[0_8px_30px_rgba(34,31,32,0.04)] md:p-8">
          {activeTab === "profile" && <><PanelTitle title="Thông tin cá nhân" text="Cập nhật thông tin liên hệ của bạn." /><form onSubmit={saveProfile} className="mt-7 grid gap-5 md:grid-cols-2"><Field label="Email" value={user?.email || ""} disabled /><Field label="Họ và tên" value={profileForm.fullName} onChange={(v) => setProfileForm({ ...profileForm, fullName: v })} /><Field label="Số điện thoại" value={profileForm.phone} onChange={(v) => setProfileForm({ ...profileForm, phone: v })} /><div className="md:col-span-2"><PrimaryButton disabled={saving}>{saving ? "Đang lưu..." : "Lưu thay đổi"}</PrimaryButton></div></form></>}
          {activeTab === "addresses" && <><div className="flex items-start justify-between gap-4"><PanelTitle title="Sổ địa chỉ" text={`${addresses.length} địa chỉ nhận hàng đã lưu.`} /><button onClick={() => { setAddressForm(EMPTY_ADDRESS); setEditingAddressId(null); setShowAddressForm(true); }} className="flex shrink-0 items-center gap-2 rounded-xl bg-ivy-dark px-4 py-3 text-xs font-semibold text-white"><Plus className="size-4" />Thêm địa chỉ</button></div><div className="mt-7 grid gap-4 md:grid-cols-2">{addresses.map((a) => <article key={a.id} className={`relative rounded-2xl border p-5 ${a.isDefault ? "border-[#b98055] bg-[#fffaf5]" : "border-[#ebe7e2]"}`}><div className="flex items-center gap-2"><p className="font-semibold">{a.receiverName}</p>{a.isDefault && <span className="rounded-full bg-[#ead7c7] px-2 py-1 text-[10px] font-semibold uppercase text-[#765033]">Mặc định</span>}</div><p className="mt-2 text-sm text-ivy-text">{a.phone}</p><p className="mt-2 text-sm leading-6 text-ivy-text-muted">{[a.addressLine, a.ward, a.district, a.province].filter(Boolean).join(", ")}</p><div className="mt-4 flex flex-wrap gap-4 text-xs font-semibold text-[#875b3b]"><button onClick={() => editAddress(a)}>Chỉnh sửa</button>{!a.isDefault && <button onClick={() => void makeDefault(a.id)}>Đặt mặc định</button>}<button onClick={() => void removeAddress(a.id)} className="text-red-600">Xóa</button></div></article>)}{addresses.length === 0 && <EmptyState icon={MapPin} text="Bạn chưa lưu địa chỉ nhận hàng nào." />}</div>{showAddressForm && <form onSubmit={saveAddress} className="mt-7 rounded-2xl bg-[#f8f6f3] p-5"><h3 className="mb-5 font-semibold">{editingAddressId ? "Chỉnh sửa địa chỉ" : "Thêm địa chỉ mới"}</h3><div className="grid gap-4 md:grid-cols-2"><Field label="Người nhận" value={addressForm.receiverName} onChange={(v) => setAddressForm({...addressForm, receiverName:v})} required /><Field label="Số điện thoại" value={addressForm.phone} onChange={(v) => setAddressForm({...addressForm, phone:v})} required /><ProvinceWardSelect provinceValue={addressForm.province} wardValue={addressForm.ward} onProvinceChange={(name) => setAddressForm({...addressForm, province:name})} onWardChange={(name) => setAddressForm({...addressForm, ward:name})} selectClassName="h-12 w-full rounded-xl border border-[#ddd8d2] bg-white px-4 text-sm outline-none transition focus:border-[#9b6b45] focus:ring-2 focus:ring-[#9b6b45]/10" labels={{ wrapperClassName: "", captionClassName: "mb-2 block text-xs font-semibold uppercase tracking-[0.08em] text-ivy-dark" }} /><Field label="Địa chỉ chi tiết" value={addressForm.addressLine} onChange={(v) => setAddressForm({...addressForm, addressLine:v})} required /></div><div className="mt-5 flex gap-3"><PrimaryButton disabled={saving}>{saving ? "Đang lưu..." : "Lưu địa chỉ"}</PrimaryButton><button type="button" onClick={resetAddressForm} className="rounded-xl border border-[#ddd7d0] px-5 py-3 text-sm font-semibold">Hủy</button></div></form>}</>}
          {activeTab === "orders" && <><PanelTitle title="Đơn hàng của bạn" text="Theo dõi trạng thái và xem lại các đơn đã mua." /><div className="mt-7 space-y-3">{orders.map((o) => <Link key={o.id} href={`/tai-khoan/don-hang/${o.id}`} className="grid items-center gap-3 rounded-2xl border border-[#ebe7e2] p-5 transition hover:border-[#b98055] md:grid-cols-[1fr_160px_160px_20px]"><div><p className="font-semibold">{o.orderCode}</p><p className="mt-1 text-xs text-ivy-text-muted">{new Date(o.createdAt).toLocaleDateString("vi-VN")}</p></div><span className="w-fit rounded-full bg-[#f2ebe4] px-3 py-1.5 text-xs font-semibold text-[#805638]">{getOrderStatusLabel(o.orderStatus)}</span><p className="font-semibold md:text-right">{o.totalAmount.toLocaleString("vi-VN")}đ</p><ChevronRight className="size-4 text-ivy-text-muted" /></Link>)}{orders.length === 0 && <EmptyState icon={Package} text="Bạn chưa có đơn hàng nào." />}</div></>}
          {activeTab === "password" && <><PanelTitle title="Đặt lại mật khẩu" text="Bảo vệ tài khoản bằng một mật khẩu mạnh và duy nhất." /><div className="mt-7 max-w-2xl rounded-2xl border border-[#e8e4df] bg-[#fffaf5] p-6 md:p-8"><div className="flex size-12 items-center justify-center rounded-full bg-[#ead7c7] text-[#805638]"><ShieldCheck className="size-6" /></div><h3 className="mt-5 text-lg font-semibold">Xác nhận qua email</h3><p className="mt-2 text-sm leading-6 text-ivy-text">Vì lý do bảo mật, chúng tôi sẽ gửi liên kết đặt lại mật khẩu đến <strong>{user?.email}</strong>. Liên kết chỉ có hiệu lực trong thời gian giới hạn.</p><button onClick={() => void sendResetEmail()} disabled={saving} className="mt-6 rounded-xl bg-ivy-dark px-6 py-3 text-sm font-semibold text-white disabled:opacity-60">{saving ? "Đang gửi..." : "Gửi liên kết đặt lại mật khẩu"}</button></div></>}
        </section>
      </div>}
    </div>
  </main>;
}

function PanelTitle({ title, text }: { title: string; text: string }) { return <div><h2 className="text-2xl font-semibold text-ivy-dark md:text-[28px]">{title}</h2><p className="mt-2 text-sm text-ivy-text-muted">{text}</p></div>; }
function Field({ label, value, onChange, disabled, required }: { label: string; value: string; onChange?: (value: string) => void; disabled?: boolean; required?: boolean }) { return <label className={label === "Email" ? "md:col-span-2" : ""}><span className="mb-2 block text-xs font-semibold uppercase tracking-[0.08em] text-ivy-dark">{label}</span><input value={value} onChange={(e) => onChange?.(e.target.value)} disabled={disabled} required={required} className="h-12 w-full rounded-xl border border-[#ddd8d2] bg-white px-4 text-sm outline-none transition focus:border-[#9b6b45] focus:ring-2 focus:ring-[#9b6b45]/10 disabled:bg-[#f5f4f2] disabled:text-ivy-text-muted" /></label>; }
function PrimaryButton({ children, disabled }: { children: React.ReactNode; disabled?: boolean }) { return <button type="submit" disabled={disabled} className="rounded-xl bg-ivy-dark px-6 py-3 text-sm font-semibold text-white transition hover:opacity-90 disabled:opacity-60">{children}</button>; }
function EmptyState({ icon: Icon, text }: { icon: React.ElementType; text: string }) { return <div className="md:col-span-2 flex flex-col items-center rounded-2xl border border-dashed border-[#ddd7d0] px-5 py-12 text-center"><Icon className="size-8 text-[#b89476]" /><p className="mt-3 text-sm text-ivy-text-muted">{text}</p></div>; }

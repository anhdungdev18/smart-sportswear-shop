import { AdminBannersClient } from "@/components/banners/AdminBannersClient";
import { listAdminBanners } from "@/modules/banners/api";

export default async function BannersPage() {
  const banners = await listAdminBanners().catch(() => []);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Banner</h1>
          <p>Quản lý banner public cho trang chủ, bộ sưu tập và các vị trí hiển thị khác.</p>
        </div>
      </section>

      <AdminBannersClient initialItems={banners} />
    </main>
  );
}

import { AdminPagesClient } from "@/components/pages/AdminPagesClient";
import { listAdminPages } from "@/modules/pages/api";

export default async function PagesPage() {
  const items = await listAdminPages();

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Nội dung</h1>
          <p>Quản lý các trang nội dung public như giới thiệu, chính sách và landing page tĩnh.</p>
        </div>
      </section>

      <AdminPagesClient initialItems={items} />
    </main>
  );
}

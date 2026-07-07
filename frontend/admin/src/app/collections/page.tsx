import { AdminCollectionsClient } from "@/components/catalog/AdminCollectionsClient";
import { listCollections } from "@/modules/catalog-admin/api";

export const metadata = { title: "Bộ sưu tập - Admin" };

export default async function CollectionsPage() {
  const items = await listCollections().catch(() => []);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Bộ sưu tập</h1>
          <p>Quản lý lookbook, bộ sưu tập theo mùa và chiến dịch trưng bày cho storefront.</p>
        </div>
      </section>

      <AdminCollectionsClient initialItems={items} />
    </main>
  );
}

import { VisualSearchOperationsClient } from "@/components/visual-search/VisualSearchOperationsClient";

export default function VisualSearchAdminPage() {
  return <main className="workspace"><section className="page-title"><div><h1>Visual Search</h1><p>Theo dõi coverage, model, usage, outbox và các job đồng bộ ảnh.</p></div></section><VisualSearchOperationsClient /></main>;
}

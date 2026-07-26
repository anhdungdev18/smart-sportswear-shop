import { AiInsightsWorkspace } from "@/components/inventory/AiInsightsWorkspace";
import { getDataQualitySummary, listInventoryRisks, listPendingSuggestions } from "@/modules/ai-insights/api";

export default async function AiInsightsPage() {
  const [qualityResult, risksResult, suggestionsResult] = await Promise.allSettled([
    getDataQualitySummary(),
    listInventoryRisks(),
    listPendingSuggestions(),
  ]);

  const quality = qualityResult.status === "fulfilled" ? qualityResult.value : null;
  const risks = risksResult.status === "fulfilled" ? risksResult.value : [];
  const suggestionsPage = suggestionsResult.status === "fulfilled" ? suggestionsResult.value : { content: [] };
  const loadError = qualityResult.status === "rejected" || risksResult.status === "rejected";

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>AI Inventory Insights</h1>
          <p>Decision engine cho rui ro thieu hang, du hang, chat luong forecast va mo phong chinh sach ton kho.</p>
        </div>
      </section>

      {loadError ? (
        <section className="card panel">
          <div className="error-state">
            Khong tai duoc du lieu AI. Hay kiem tra phien dang nhap, `NEXT_PUBLIC_AI_API_BASE_URL` va AI service.
          </div>
        </section>
      ) : (
        <AiInsightsWorkspace
          initialQuality={quality}
          initialRisks={risks}
          initialSuggestions={suggestionsPage.content}
        />
      )}
    </main>
  );
}

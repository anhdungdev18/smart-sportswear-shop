import { AiInsightsWorkspace } from "@/components/inventory/AiInsightsWorkspace";
import { SlowMovingInventoryPanel } from "@/components/inventory/SlowMovingInventoryPanel";
import { ForecastCoveragePanel } from "@/components/inventory/ForecastCoveragePanel";
import { getDataQualitySummary, getInventoryAgeingSummary, listDemandClassifications, listInventoryRisks, listPendingSuggestions } from "@/modules/ai-insights/api";

export default async function AiInsightsPage() {
  const [qualityResult, risksResult, suggestionsResult, ageingResult, classificationsResult] = await Promise.allSettled([
    getDataQualitySummary(),
    listInventoryRisks(),
    listPendingSuggestions(),
    getInventoryAgeingSummary(),
    listDemandClassifications(),
  ]);

  const quality = qualityResult.status === "fulfilled" ? qualityResult.value : null;
  const risks = risksResult.status === "fulfilled" ? risksResult.value : [];
  const suggestionsPage = suggestionsResult.status === "fulfilled" ? suggestionsResult.value : { content: [] };
  const loadError = qualityResult.status === "rejected" || risksResult.status === "rejected";
  const ageing = ageingResult.status === "fulfilled" ? ageingResult.value : null;
  const classifications = classificationsResult.status === "fulfilled" ? classificationsResult.value : [];

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Phân tích tồn kho bằng AI</h1>
          <p>Theo dõi nguy cơ thiếu hoặc dư hàng, chất lượng dự báo và thử nghiệm chính sách tồn kho.</p>
        </div>
      </section>

      {loadError ? (
        <section className="card panel">
          <div className="error-state">
            Không tải được dữ liệu phân tích. Vui lòng kiểm tra phiên đăng nhập và kết nối tới dịch vụ AI.
          </div>
        </section>
      ) : (
        <>
          <ForecastCoveragePanel rows={classifications} />
          <SlowMovingInventoryPanel summary={ageing} />
          <AiInsightsWorkspace initialQuality={quality} initialRisks={risks} initialSuggestions={suggestionsPage.content} />
        </>
      )}
    </main>
  );
}

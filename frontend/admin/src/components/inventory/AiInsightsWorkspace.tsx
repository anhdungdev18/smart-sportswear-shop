"use client";

import { useMemo, useState } from "react";
import {
  ArrowsClockwise,
  ChartLineUp,
  Flask,
  Info,
  MagnifyingGlass,
  Package,
  ShieldWarning,
  WarningCircle,
} from "@phosphor-icons/react";
import {
  getDataQualitySummary,
  getReplenishmentExplanation,
  listInventoryRisks,
  simulateInventoryPolicy,
} from "@/modules/ai-insights/browser-api";
import type {
  DataQualitySummaryResponse,
  ForecastConfidence,
  InventoryRiskResponse,
  InventoryRiskType,
  InventorySimulationResponse,
  ReplenishmentExplanationResponse,
  ReplenishmentSuggestionResponse,
} from "@/modules/ai-insights/types";

interface Props {
  initialQuality: DataQualitySummaryResponse | null;
  initialRisks: InventoryRiskResponse[];
  initialSuggestions: ReplenishmentSuggestionResponse[];
}

type RiskFilter = InventoryRiskType | "ALL";
type ConfidenceFilter = ForecastConfidence | "ALL";

const riskLabels: Record<InventoryRiskType, string> = {
  STOCKOUT: "Thiếu hàng",
  OVERSTOCK: "Dư hàng",
  BALANCED: "Cân bằng",
  INSUFFICIENT_DATA: "Chưa có dự báo",
};

const confidenceLabels: Record<string, string> = {
  HIGH: "Cao",
  MEDIUM: "Trung bình",
  LOW: "Thấp",
  INSUFFICIENT: "Chưa đủ dữ liệu",
};

const severityLabels: Record<string, string> = {
  CRITICAL: "Khẩn cấp",
  HIGH: "Cao",
  MEDIUM: "Trung bình",
  LOW: "Thấp",
  NONE: "Bình thường",
};

const dataSourceLabels: Record<string, string> = {
  DEMO: "Dữ liệu thử nghiệm",
  REAL: "Dữ liệu thực tế",
  IMPORTED: "Dữ liệu nhập từ tệp",
};

const demandPatternLabels: Record<string, string> = {
  SMOOTH: "Ổn định",
  ERRATIC: "Biến động",
  INTERMITTENT: "Gián đoạn",
  LUMPY: "Không đều",
  INSUFFICIENT: "Chưa đủ dữ liệu",
};

const severityRank = { CRITICAL: 5, HIGH: 4, MEDIUM: 3, LOW: 2, NONE: 1 };

function formatNumber(value: number | null | undefined, digits = 0) {
  if (value === null || value === undefined || Number.isNaN(value)) return "-";
  return new Intl.NumberFormat("vi-VN", { maximumFractionDigits: digits }).format(value);
}

function formatPercent(value: number | null | undefined) {
  if (value === null || value === undefined || Number.isNaN(value)) return "-";
  return `${(value * 100).toFixed(1)}%`;
}

function statusClass(value: string) {
  if (value === "CRITICAL" || value === "STOCKOUT" || value === "INSUFFICIENT_DATA") return "critical";
  if (value === "HIGH" || value === "LOW" || value === "OVERSTOCK") return "low";
  if (value === "BALANCED" || value === "HIGH" || value === "MEDIUM") return "active";
  return "draft";
}

function recommendationForRisk(risk: InventoryRiskResponse, suggestions: ReplenishmentSuggestionResponse[]) {
  return suggestions.find((item) => item.variantId === risk.variantId && item.status === "PENDING") ?? null;
}

function FormulaGrid({ risk }: { risk: InventoryRiskResponse }) {
  const fields = [
    ["Nhu cầu trung bình/ngày", formatNumber(risk.formula.averageDailyDemand, 2)],
    ["Thời gian nhập hàng", `${risk.formula.leadTimeDays} ngày`],
    ["Mức độ đáp ứng", formatPercent(risk.formula.serviceLevel)],
    ["Tồn kho an toàn", formatNumber(risk.formula.safetyStock)],
    ["Điểm đặt hàng lại", formatNumber(risk.formula.reorderPoint)],
    ["Mức tồn kho mục tiêu", formatNumber(risk.formula.targetStock)],
    ["Số lượng đặt tối thiểu (MOQ)", formatNumber(risk.formula.minimumOrderQuantity)],
    ["Quy cách đóng gói", formatNumber(risk.formula.packSize)],
  ];

  return (
    <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(140px,1fr))", gap: 10 }}>
      {fields.map(([label, value]) => (
        <div className="admin-subcard" key={label} style={{ gap: 4, padding: 12 }}>
          <span className="table-subtle">{label}</span>
          <strong>{value}</strong>
        </div>
      ))}
    </div>
  );
}

export function AiInsightsWorkspace({ initialQuality, initialRisks, initialSuggestions }: Props) {
  const [quality, setQuality] = useState(initialQuality);
  const [risks, setRisks] = useState(initialRisks);
  const [suggestions] = useState(initialSuggestions);
  const [selected, setSelected] = useState<InventoryRiskResponse | null>(initialRisks[0] ?? null);
  const [riskFilter, setRiskFilter] = useState<RiskFilter>("ALL");
  const [confidenceFilter, setConfidenceFilter] = useState<ConfidenceFilter>("ALL");
  const [keyword, setKeyword] = useState("");
  const [refreshing, setRefreshing] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [explanation, setExplanation] = useState<ReplenishmentExplanationResponse | null>(null);
  const [explanationLoading, setExplanationLoading] = useState(false);
  const [simulation, setSimulation] = useState<InventorySimulationResponse | null>(null);
  const [simulationLoading, setSimulationLoading] = useState(false);
  const [simForm, setSimForm] = useState({
    availableQuantity: selected?.availableQuantity ?? 0,
    incomingQuantity: selected?.incomingQuantity ?? 0,
    leadTimeDays: selected?.formula.leadTimeDays ?? 14,
    serviceLevel: selected?.formula.serviceLevel ?? 0.95,
    targetCoverDays: selected?.formula.targetCoverDays ?? 21,
    minimumOrderQuantity: selected?.formula.minimumOrderQuantity ?? 1,
    packSize: selected?.formula.packSize ?? 1,
  });

  const pendingByVariant = useMemo(() => new Map(suggestions.map((item) => [item.variantId, item])), [suggestions]);
  const stockoutCount = risks.filter((item) => item.risk === "STOCKOUT").length;
  const overstockCount = risks.filter((item) => item.risk === "OVERSTOCK").length;
  const lowConfidenceCount = risks.filter((item) => item.confidence === "LOW" || item.confidence === "INSUFFICIENT").length;
  const highErrorCount = suggestions.filter((item) => item.wape !== null && item.wape >= 0.6).length;

  const filteredRisks = useMemo(() => {
    const normalized = keyword.trim().toLowerCase();
    return risks
      .filter((item) => riskFilter === "ALL" || item.risk === riskFilter)
      .filter((item) => confidenceFilter === "ALL" || item.confidence === confidenceFilter)
      .filter((item) => {
        if (!normalized) return true;
        return item.sku.toLowerCase().includes(normalized)
          || item.productName.toLowerCase().includes(normalized)
          || item.demandPattern.toLowerCase().includes(normalized);
      })
      .sort((left, right) => severityRank[right.severity] - severityRank[left.severity]
        || right.suggestedQuantity - left.suggestedQuantity);
  }, [confidenceFilter, keyword, riskFilter, risks]);

  function selectRisk(risk: InventoryRiskResponse) {
    setSelected(risk);
    setExplanation(null);
    setSimulation(null);
    setError(null);
    setSimForm({
      availableQuantity: risk.availableQuantity,
      incomingQuantity: risk.incomingQuantity,
      leadTimeDays: risk.formula.leadTimeDays,
      serviceLevel: risk.formula.serviceLevel,
      targetCoverDays: risk.formula.targetCoverDays,
      minimumOrderQuantity: risk.formula.minimumOrderQuantity,
      packSize: risk.formula.packSize,
    });
  }

  async function refresh() {
    setRefreshing(true);
    setError(null);
    try {
      const [nextQuality, nextRisks] = await Promise.all([getDataQualitySummary(), listInventoryRisks(riskFilter)]);
      setQuality(nextQuality);
      setRisks(nextRisks);
      setSelected((current) => current ? nextRisks.find((item) => item.variantId === current.variantId) ?? nextRisks[0] ?? null : nextRisks[0] ?? null);
      setMessage("Đã cập nhật dữ liệu phân tích mới nhất.");
    } catch (cause) {
      console.error(cause);
      setError("Không thể cập nhật dữ liệu. Vui lòng kiểm tra kết nối và đăng nhập lại.");
    } finally {
      setRefreshing(false);
    }
  }

  async function loadExplanation() {
    if (!selected) return;
    const pending = recommendationForRisk(selected, suggestions);
    if (!pending) {
      setError("SKU này chưa có đề xuất nhập hàng đang chờ duyệt.");
      return;
    }
    setExplanationLoading(true);
    setError(null);
    try {
      setExplanation(await getReplenishmentExplanation(pending.id));
    } catch (cause) {
      console.error(cause);
      setError("Không tải được nội dung giải thích cho đề xuất này.");
    } finally {
      setExplanationLoading(false);
    }
  }

  async function runSimulation() {
    if (!selected) return;
    setSimulationLoading(true);
    setError(null);
    try {
      setSimulation(await simulateInventoryPolicy({
        variantId: selected.variantId,
        ...simForm,
      }));
    } catch (cause) {
      console.error(cause);
      setError("Không thể chạy mô phỏng. Vui lòng kiểm tra lại các giá trị đã nhập.");
    } finally {
      setSimulationLoading(false);
    }
  }

  return (
    <>
      <section className="kpi-grid">
        <div className="card kpi-card">
          <div className="kpi-label"><span>Chất lượng dữ liệu</span><ShieldWarning size={20} /></div>
          <div className="kpi-value">{quality ? `${quality.highQualityVariants}/${quality.totalVariants}` : "-"}</div>
          <span className="trend warn">{formatNumber(quality?.insufficientVariants ?? 0)} SKU có chuỗi dữ liệu chưa đầy đủ</span>
        </div>
        <div className="card kpi-card">
          <div className="kpi-label"><span>Nguy cơ thiếu hàng</span><WarningCircle size={20} /></div>
          <div className="kpi-value">{stockoutCount}</div>
          <span className="trend warn">Cần ưu tiên xem xét nhập hàng</span>
        </div>
        <div className="card kpi-card">
          <div className="kpi-label"><span>Nguy cơ dư hàng</span><Package size={20} /></div>
          <div className="kpi-value">{overstockCount}</div>
          <span className="trend">Cân nhắc giảm mua hoặc đẩy bán</span>
        </div>
        <div className="card kpi-card">
          <div className="kpi-label"><span>Cảnh báo dự báo</span><ChartLineUp size={20} /></div>
          <div className="kpi-value">{lowConfidenceCount + highErrorCount}</div>
          <span className="trend warn">{formatNumber(suggestions.length)} đề xuất đang chờ duyệt</span>
        </div>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Nguồn dữ liệu và độ tin cậy</h2>
            <p className="panel-copy">Phân tách dữ liệu thử nghiệm và dữ liệu thực tế để tránh hiểu sai kết quả.</p>
          </div>
          <button className="admin-btn secondary" type="button" onClick={() => void refresh()} disabled={refreshing}>
            <ArrowsClockwise size={18} weight="duotone" />
            {refreshing ? "Đang cập nhật" : "Cập nhật dữ liệu"}
          </button>
        </div>
        {message && <p className="action-message">{message}</p>}
        {error && <p className="error-state" role="alert">{error}</p>}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(210px,1fr))", gap: 12 }}>
          {(quality?.bySource ?? []).map((source) => (
            <div className="admin-subcard" key={source.dataSource}>
              <strong>{dataSourceLabels[source.dataSource] ?? source.dataSource}</strong>
              <span className="table-subtle">{formatNumber(source.totalVariants)} SKU · {formatNumber(source.highQualityVariants)} chất lượng cao · {formatNumber(source.insufficientVariants)} chưa đủ dữ liệu</span>
              <span className="table-subtle">{formatNumber(source.variantsMissingSupplier)} thiếu nhà cung cấp · {formatNumber(source.variantsWithMissingSalesDays)} thiếu lịch sử bán hàng</span>
            </div>
          ))}
          {!quality?.bySource?.length && <div className="empty-state">Chưa có thống kê theo nguồn dữ liệu.</div>}
        </div>
      </section>

      <section className="admin-grid admin-grid-2">
        <div className="card panel">
          <div className="panel-header">
            <div>
              <h2>Danh sách cần xử lý</h2>
              <p className="panel-copy">Ưu tiên theo mức độ rủi ro và số lượng đề xuất.</p>
            </div>
          </div>
          <div className="table-toolbar">
            <div className="filters">
              {(["ALL", "STOCKOUT", "OVERSTOCK", "BALANCED", "INSUFFICIENT_DATA"] as RiskFilter[]).map((value) => (
                <button
                  className={`filter-chip ${riskFilter === value ? "active" : ""}`}
                  key={value}
                  type="button"
                  onClick={() => setRiskFilter(value)}
                >
                  {value === "ALL" ? "Tất cả" : riskLabels[value]}
                </button>
              ))}
            </div>
          </div>
          <div className="filters" style={{ marginBottom: 14 }}>
            <div className="admin-search" style={{ minHeight: 42, width: "min(360px, 100%)" }}>
              <MagnifyingGlass size={18} />
              <input
                aria-label="Tìm theo SKU, sản phẩm hoặc nhu cầu"
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
                placeholder="Tìm SKU hoặc tên sản phẩm..."
                style={{ border: 0, outline: 0, width: "100%", background: "transparent" }}
              />
            </div>
            <select className="admin-input" style={{ maxWidth: 190 }} value={confidenceFilter} onChange={(event) => setConfidenceFilter(event.target.value as ConfidenceFilter)}>
              <option value="ALL">Tất cả độ tin cậy</option>
              <option value="HIGH">Cao</option>
              <option value="MEDIUM">Trung bình</option>
              <option value="LOW">Thấp</option>
              <option value="INSUFFICIENT">Chưa đủ dữ liệu</option>
            </select>
          </div>
          <div style={{ overflowX: "auto" }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Rủi ro</th>
                  <th>SKU</th>
                  <th>Tồn khả dụng</th>
                  <th>Điểm đặt lại</th>
                  <th>Đề xuất nhập</th>
                  <th>Độ tin cậy</th>
                </tr>
              </thead>
              <tbody>
                {filteredRisks.length === 0 && <tr><td colSpan={6} className="empty-state">Không có SKU phù hợp với bộ lọc.</td></tr>}
                {filteredRisks.map((risk) => (
                  <tr key={risk.variantId} className={selected?.variantId === risk.variantId ? "row-selected" : undefined} onClick={() => selectRisk(risk)} style={{ cursor: "pointer" }}>
                    <td><span className={`status ${statusClass(risk.risk)}`}>{riskLabels[risk.risk]}</span></td>
                    <td><strong>{risk.sku}</strong><div className="table-subtle">{risk.productName} - {risk.color}/{risk.size}</div></td>
                    <td>{risk.availableQuantity}</td>
                    <td>{risk.reorderPoint}</td>
                    <td><strong>{risk.suggestedQuantity}</strong></td>
                    <td><span className={`status ${statusClass(risk.confidence)}`}>{confidenceLabels[risk.confidence] ?? risk.confidence}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="admin-stack">
          {selected ? (
            <>
              <section className="card panel">
                <div className="panel-header">
                  <div>
                    <h2>{selected.sku}</h2>
                    <p className="panel-copy">{selected.productName} - {selected.color}/{selected.size}</p>
                  </div>
                  {pendingByVariant.has(selected.variantId) && <span className="status low">Chờ duyệt</span>}
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(130px,1fr))", gap: 12, marginBottom: 16 }}>
                  <div><span className="table-subtle">Rủi ro</span><div><strong>{riskLabels[selected.risk]}</strong></div></div>
                  <div><span className="table-subtle">Mức độ</span><div><strong>{severityLabels[selected.severity] ?? selected.severity}</strong></div></div>
                  <div><span className="table-subtle">Mô hình dự báo</span><div><strong>{selected.selectedModel}</strong></div></div>
                  <div><span className="table-subtle">Kiểu nhu cầu</span><div><strong>{demandPatternLabels[selected.demandPattern] ?? selected.demandPattern}</strong></div></div>
                </div>
                <FormulaGrid risk={selected} />
                <div style={{ marginTop: 16 }}>
                  <h3>Lý do đề xuất</h3>
                  <ul>{selected.reasons.map((reason) => <li key={reason}>{reason}</li>)}</ul>
                  {selected.warnings.length > 0 && (
                    <>
                      <h3>Cảnh báo</h3>
                      <ul>{selected.warnings.map((warning) => <li key={warning}>{warning}</li>)}</ul>
                    </>
                  )}
                </div>
                <button className="admin-btn secondary" type="button" onClick={() => void loadExplanation()} disabled={explanationLoading || !pendingByVariant.has(selected.variantId)}>
                  <Info size={18} weight="duotone" />
                  {explanationLoading ? "Đang tải giải thích" : "Xem giải thích chi tiết"}
                </button>
                {explanation && (
                  <div className="admin-subcard admin-subcard-tight">
                    <strong>Giải thích đề xuất #{explanation.recommendationId}</strong>
                    <pre style={{ whiteSpace: "pre-wrap", margin: 0, color: "var(--admin-muted)", fontSize: 12 }}>
                      {JSON.stringify(explanation.persistedExplanation, null, 2)}
                    </pre>
                  </div>
                )}
              </section>

              <section className="card panel">
                <div className="panel-header">
                  <div>
                    <h2>Mô phỏng chính sách tồn kho</h2>
                    <p className="panel-copy">Thử các phương án trước khi quyết định. Kết quả mô phỏng không làm thay đổi dữ liệu.</p>
                  </div>
                  <Flask size={22} weight="duotone" />
                </div>
                <div className="admin-form-grid">
                  {([
                    ["availableQuantity", "Tồn khả dụng"],
                    ["incomingQuantity", "Hàng đang về"],
                    ["leadTimeDays", "Thời gian nhập hàng (ngày)"],
                    ["serviceLevel", "Mức độ đáp ứng"],
                    ["targetCoverDays", "Số ngày tồn kho mục tiêu"],
                    ["minimumOrderQuantity", "Số lượng đặt tối thiểu (MOQ)"],
                    ["packSize", "Quy cách đóng gói"],
                  ] as const).map(([key, label]) => (
                    <label key={key}>
                      <span className="table-subtle">{label}</span>
                      <input
                        className="admin-input"
                        type="number"
                        min={key === "serviceLevel" ? 0.5 : 0}
                        step={key === "serviceLevel" ? 0.01 : 1}
                        value={simForm[key]}
                        onChange={(event) => setSimForm((current) => ({ ...current, [key]: Number(event.target.value) }))}
                      />
                    </label>
                  ))}
                </div>
                <button className="admin-btn" type="button" onClick={() => void runSimulation()} disabled={simulationLoading} style={{ marginTop: 16 }}>
                  {simulationLoading ? "Đang mô phỏng" : "Chạy mô phỏng"}
                </button>
                {simulation && (
                  <div className="admin-subcard admin-subcard-tight">
                    <strong>Kết quả mô phỏng</strong>
                    <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(150px,1fr))", gap: 10 }}>
                      <div><span className="table-subtle">Thay đổi lượng đề xuất</span><div><strong>{formatNumber(simulation.suggestedQuantityDelta)}</strong></div></div>
                      <div><span className="table-subtle">Thay đổi điểm đặt lại</span><div><strong>{formatNumber(simulation.reorderPointDelta)}</strong></div></div>
                      <div><span className="table-subtle">Thay đổi số ngày thiếu hàng</span><div><strong>{formatNumber(simulation.stockoutDaysDelta)}</strong></div></div>
                      <div><span className="table-subtle">Lượng nhập đề xuất mới</span><div><strong>{formatNumber(simulation.simulated.suggestedQuantity)}</strong></div></div>
                    </div>
                    {simulation.warnings.length > 0 && <ul>{simulation.warnings.map((warning) => <li key={warning}>{warning}</li>)}</ul>}
                  </div>
                )}
              </section>
            </>
          ) : (
            <section className="card panel"><div className="empty-state">Chọn một SKU trong danh sách để xem chi tiết.</div></section>
          )}
        </div>
      </section>
    </>
  );
}

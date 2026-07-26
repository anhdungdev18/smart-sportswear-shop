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
  STOCKOUT: "Thieu hang",
  OVERSTOCK: "Du hang",
  BALANCED: "Can bang",
  INSUFFICIENT_DATA: "Thieu du lieu",
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
    ["Avg demand/day", risk.formula.averageDailyDemand.toFixed(2)],
    ["Lead time", `${risk.formula.leadTimeDays} ngay`],
    ["Service level", formatPercent(risk.formula.serviceLevel)],
    ["Safety stock", risk.formula.safetyStock],
    ["Reorder point", risk.formula.reorderPoint],
    ["Target stock", risk.formula.targetStock],
    ["MOQ", risk.formula.minimumOrderQuantity],
    ["Pack size", risk.formula.packSize],
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
      setMessage("Da lam moi insight tu AI service.");
    } catch (cause) {
      console.error(cause);
      setError("Khong the lam moi du lieu AI. Hay kiem tra AI service va token admin.");
    } finally {
      setRefreshing(false);
    }
  }

  async function loadExplanation() {
    if (!selected) return;
    const pending = recommendationForRisk(selected, suggestions);
    if (!pending) {
      setError("SKU nay chua co recommendation dang cho duyet de truy xuat persisted explanation.");
      return;
    }
    setExplanationLoading(true);
    setError(null);
    try {
      setExplanation(await getReplenishmentExplanation(pending.id));
    } catch (cause) {
      console.error(cause);
      setError("Khong tai duoc explanation cua recommendation.");
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
      setError("Khong chay duoc what-if simulation. Gia tri nhap co the khong hop le.");
    } finally {
      setSimulationLoading(false);
    }
  }

  return (
    <>
      <section className="kpi-grid">
        <div className="card kpi-card">
          <div className="kpi-label"><span>Data quality</span><ShieldWarning size={20} /></div>
          <div className="kpi-value">{quality ? `${quality.highQualityVariants}/${quality.totalVariants}` : "-"}</div>
          <span className="trend warn">{quality?.insufficientVariants ?? 0} SKU thieu du lieu</span>
        </div>
        <div className="card kpi-card">
          <div className="kpi-label"><span>Stockout risk</span><WarningCircle size={20} /></div>
          <div className="kpi-value">{stockoutCount}</div>
          <span className="trend warn">Can xem uu tien nhap</span>
        </div>
        <div className="card kpi-card">
          <div className="kpi-label"><span>Overstock risk</span><Package size={20} /></div>
          <div className="kpi-value">{overstockCount}</div>
          <span className="trend">Can giam mua/xa hang</span>
        </div>
        <div className="card kpi-card">
          <div className="kpi-label"><span>Forecast flags</span><ChartLineUp size={20} /></div>
          <div className="kpi-value">{lowConfidenceCount + highErrorCount}</div>
          <span className="trend warn">{suggestions.length} recommendation cho duyet</span>
        </div>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Nguon du lieu va do tin cay</h2>
            <p className="panel-copy">Tach DEMO/REAL de tranh doc nham ket qua validation nhu production.</p>
          </div>
          <button className="admin-btn secondary" type="button" onClick={() => void refresh()} disabled={refreshing}>
            <ArrowsClockwise size={18} weight="duotone" />
            {refreshing ? "Dang lam moi" : "Lam moi"}
          </button>
        </div>
        {message && <p className="action-message">{message}</p>}
        {error && <p className="error-state" role="alert">{error}</p>}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(210px,1fr))", gap: 12 }}>
          {(quality?.bySource ?? []).map((source) => (
            <div className="admin-subcard" key={source.dataSource}>
              <strong>{source.dataSource}</strong>
              <span className="table-subtle">{source.totalVariants} SKU, {source.highQualityVariants} HIGH, {source.insufficientVariants} insufficient</span>
              <span className="table-subtle">{source.variantsMissingSupplier} thieu supplier, {source.variantsWithMissingSalesDays} thieu ngay sales</span>
            </div>
          ))}
          {!quality?.bySource?.length && <div className="empty-state">Chua co summary theo data source.</div>}
        </div>
      </section>

      <section className="admin-grid admin-grid-2">
        <div className="card panel">
          <div className="panel-header">
            <div>
              <h2>Risk queue</h2>
              <p className="panel-copy">Sap xep theo severity va so luong de xuat.</p>
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
                  {value === "ALL" ? "Tat ca" : riskLabels[value]}
                </button>
              ))}
            </div>
          </div>
          <div className="filters" style={{ marginBottom: 14 }}>
            <div className="admin-search" style={{ minHeight: 42, width: "min(360px, 100%)" }}>
              <MagnifyingGlass size={18} />
              <input
                aria-label="Tim SKU, san pham hoac demand pattern"
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
                placeholder="Tim SKU, san pham..."
                style={{ border: 0, outline: 0, width: "100%", background: "transparent" }}
              />
            </div>
            <select className="admin-input" style={{ maxWidth: 190 }} value={confidenceFilter} onChange={(event) => setConfidenceFilter(event.target.value as ConfidenceFilter)}>
              <option value="ALL">Tat ca confidence</option>
              <option value="HIGH">HIGH</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="LOW">LOW</option>
              <option value="INSUFFICIENT">INSUFFICIENT</option>
            </select>
          </div>
          <div style={{ overflowX: "auto" }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Risk</th>
                  <th>SKU</th>
                  <th>Ton</th>
                  <th>ROP</th>
                  <th>De xuat</th>
                  <th>Confidence</th>
                </tr>
              </thead>
              <tbody>
                {filteredRisks.length === 0 && <tr><td colSpan={6} className="empty-state">Khong co SKU phu hop bo loc.</td></tr>}
                {filteredRisks.map((risk) => (
                  <tr key={risk.variantId} className={selected?.variantId === risk.variantId ? "row-selected" : undefined} onClick={() => selectRisk(risk)} style={{ cursor: "pointer" }}>
                    <td><span className={`status ${statusClass(risk.risk)}`}>{riskLabels[risk.risk]}</span></td>
                    <td><strong>{risk.sku}</strong><div className="table-subtle">{risk.productName} - {risk.color}/{risk.size}</div></td>
                    <td>{risk.availableQuantity}</td>
                    <td>{risk.reorderPoint}</td>
                    <td><strong>{risk.suggestedQuantity}</strong></td>
                    <td><span className={`status ${statusClass(risk.confidence)}`}>{risk.confidence}</span></td>
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
                  {pendingByVariant.has(selected.variantId) && <span className="status low">Cho duyet</span>}
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(130px,1fr))", gap: 12, marginBottom: 16 }}>
                  <div><span className="table-subtle">Risk</span><div><strong>{riskLabels[selected.risk]}</strong></div></div>
                  <div><span className="table-subtle">Severity</span><div><strong>{selected.severity}</strong></div></div>
                  <div><span className="table-subtle">Model</span><div><strong>{selected.selectedModel}</strong></div></div>
                  <div><span className="table-subtle">Demand</span><div><strong>{selected.demandPattern}</strong></div></div>
                </div>
                <FormulaGrid risk={selected} />
                <div style={{ marginTop: 16 }}>
                  <h3>Ly do</h3>
                  <ul>{selected.reasons.map((reason) => <li key={reason}>{reason}</li>)}</ul>
                  {selected.warnings.length > 0 && (
                    <>
                      <h3>Canh bao</h3>
                      <ul>{selected.warnings.map((warning) => <li key={warning}>{warning}</li>)}</ul>
                    </>
                  )}
                </div>
                <button className="admin-btn secondary" type="button" onClick={() => void loadExplanation()} disabled={explanationLoading || !pendingByVariant.has(selected.variantId)}>
                  <Info size={18} weight="duotone" />
                  {explanationLoading ? "Dang tai explanation" : "Xem persisted explanation"}
                </button>
                {explanation && (
                  <div className="admin-subcard admin-subcard-tight">
                    <strong>Recommendation {explanation.recommendationId}</strong>
                    <pre style={{ whiteSpace: "pre-wrap", margin: 0, color: "var(--admin-muted)", fontSize: 12 }}>
                      {JSON.stringify(explanation.persistedExplanation, null, 2)}
                    </pre>
                  </div>
                )}
              </section>

              <section className="card panel">
                <div className="panel-header">
                  <div>
                    <h2>What-if simulator</h2>
                    <p className="panel-copy">Read-only: chi tinh lai decision, khong ghi thay doi vao database.</p>
                  </div>
                  <Flask size={22} weight="duotone" />
                </div>
                <div className="admin-form-grid">
                  {([
                    ["availableQuantity", "Ton kha dung"],
                    ["incomingQuantity", "Hang sap ve"],
                    ["leadTimeDays", "Lead time"],
                    ["serviceLevel", "Service level"],
                    ["targetCoverDays", "Target cover"],
                    ["minimumOrderQuantity", "MOQ"],
                    ["packSize", "Pack size"],
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
                  {simulationLoading ? "Dang mo phong" : "Chay mo phong"}
                </button>
                {simulation && (
                  <div className="admin-subcard admin-subcard-tight">
                    <strong>Ket qua mo phong</strong>
                    <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(150px,1fr))", gap: 10 }}>
                      <div><span className="table-subtle">Suggested delta</span><div><strong>{simulation.suggestedQuantityDelta}</strong></div></div>
                      <div><span className="table-subtle">ROP delta</span><div><strong>{simulation.reorderPointDelta}</strong></div></div>
                      <div><span className="table-subtle">Stockout delta</span><div><strong>{formatNumber(simulation.stockoutDaysDelta)}</strong></div></div>
                      <div><span className="table-subtle">New suggested</span><div><strong>{simulation.simulated.suggestedQuantity}</strong></div></div>
                    </div>
                    {simulation.warnings.length > 0 && <ul>{simulation.warnings.map((warning) => <li key={warning}>{warning}</li>)}</ul>}
                  </div>
                )}
              </section>
            </>
          ) : (
            <section className="card panel"><div className="empty-state">Chon mot SKU trong risk queue de xem chi tiet.</div></section>
          )}
        </div>
      </section>
    </>
  );
}

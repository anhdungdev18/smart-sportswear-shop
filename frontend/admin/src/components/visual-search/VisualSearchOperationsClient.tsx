"use client";

import { ArrowClockwise, CheckCircle, Database, Robot, WarningCircle } from "@phosphor-icons/react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { backfillMissingEmbeddings, getVisualSearchDashboard, reindexVisualSearch, retryFailedEmbeddings } from "@/modules/visual-search/browser-api";
import type { VisualSearchCoverage, VisualSearchJobs, VisualSearchOperations, VisualSearchUsage } from "@/modules/visual-search/types";

type Dashboard = { coverage: VisualSearchCoverage; operations: VisualSearchOperations; usage: VisualSearchUsage; jobs: VisualSearchJobs };

export function VisualSearchOperationsClient() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [retrying, setRetrying] = useState(false);
  const [notice, setNotice] = useState("");
  const [targetType, setTargetType] = useState<"imageId" | "productId">("productId");
  const [targetId, setTargetId] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try { setData(await getVisualSearchDashboard()); }
    catch { setError("Không thể tải trạng thái visual search. Hãy kiểm tra backend và visual-search-service."); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { void load(); }, [load]);
  const totals = useMemo(() => (data?.usage.rows ?? []).reduce((sum, row) => ({ requests: sum.requests + row.requests, cost: sum.cost + row.estimatedCostUsd, failures: sum.failures + row.failureCount }), { requests: 0, cost: 0, failures: 0 }), [data]);

  async function retryFailed() {
    setRetrying(true); setNotice("");
    try {
      const result = await retryFailedEmbeddings();
      setNotice(result.enqueuedCount > 0 ? `Đã đưa ${result.enqueuedCount} ảnh lỗi vào job retry.` : "Không có embedding lỗi cần retry.");
      await load();
    } catch { setNotice("Không thể tạo job retry lúc này."); }
    finally { setRetrying(false); }
  }

  async function backfillMissing() {
    setRetrying(true); setNotice("");
    try {
      const result = await backfillMissingEmbeddings();
      setNotice(result.enqueuedCount > 0 ? `Đã tạo backfill cho ${result.enqueuedCount} ảnh thiếu.` : "Không có ảnh thiếu embedding.");
      await load();
    } catch { setNotice("Không thể tạo backfill lúc này."); }
    finally { setRetrying(false); }
  }

  async function reindexTarget() {
    const id = targetId.trim();
    if (!id) { setNotice("Hãy nhập UUID ảnh hoặc sản phẩm cần reindex."); return; }
    setRetrying(true); setNotice("");
    try {
      const result = await reindexVisualSearch({ [targetType]: id });
      setNotice(`Đã tạo job reindex cho ${result.enqueuedCount} ảnh.`);
      setTargetId("");
      await load();
    } catch { setNotice("Không tìm thấy ảnh ACTIVE phù hợp hoặc UUID không hợp lệ."); }
    finally { setRetrying(false); }
  }

  if (loading && !data) return <section className="card panel">Đang tải dữ liệu vận hành visual search...</section>;
  if (error && !data) return <section className="card panel visual-error"><WarningCircle size={24} />{error}</section>;
  if (!data) return null;
  const { coverage, operations, usage, jobs } = data;

  return <>
    <section className="visual-kpi-grid" aria-label="KPI visual search">
      <article className="card kpi-card"><div className="kpi-label"><span>Coverage ảnh</span><CheckCircle size={22} /></div><div className="kpi-value">{coverage.coveragePct.toFixed(2)}%</div><span className="trend">{coverage.ready}/{coverage.totalActiveImages} READY</span></article>
      <article className="card kpi-card"><div className="kpi-label"><span>Model đang chạy</span><Robot size={22} /></div><div className="visual-model">{operations.model ?? "Chưa có"}</div><span className="trend">{operations.provider ?? "—"} · {operations.dimensions ?? "—"} chiều</span></article>
      <article className="card kpi-card"><div className="kpi-label"><span>Outbox chờ xử lý</span><Database size={22} /></div><div className="kpi-value">{operations.outboxPending + operations.outboxPublishing}</div><span className={operations.outboxFailed ? "trend warn" : "trend"}>{operations.outboxFailed} FAILED</span></article>
      <article className="card kpi-card"><div className="kpi-label"><span>Usage 30 ngày</span><ArrowClockwise size={22} /></div><div className="kpi-value">{totals.requests}</div><span className={totals.failures ? "trend warn" : "trend"}>${totals.cost.toFixed(4)} · {totals.failures} lỗi</span></article>
    </section>
    <section className="dashboard-grid">
      <article className="card panel"><div className="panel-header"><h2>Trạng thái embedding</h2><button className="admin-btn secondary" onClick={() => void load()} disabled={loading}>Làm mới</button></div><div className="visual-status-grid">{(["ready", "pending", "processing", "failed", "missing"] as const).map((key) => <div key={key}><span>{key.toUpperCase()}</span><strong>{coverage[key]}</strong></div>)}</div><div className="visual-action-row"><button className="admin-btn visual-retry" onClick={() => void retryFailed()} disabled={retrying || coverage.failed === 0}><ArrowClockwise size={17} />Retry lỗi</button><button className="admin-btn secondary" onClick={() => void backfillMissing()} disabled={retrying || coverage.missing === 0}>Backfill thiếu</button></div>{notice && <p className="visual-notice" role="status">{notice}</p>}</article>
      <article className="card panel"><div className="panel-header"><h2>Usage theo ngày</h2></div><div className="visual-usage-list">{usage.rows.length === 0 && <p>Chưa có lượt gọi provider trong 30 ngày.</p>}{usage.rows.slice(0, 8).map((row) => <div key={`${row.day}-${row.operation}`}><div><strong>{row.day}</strong><span>{row.operation.replaceAll("_", " ")}</span></div><div><strong>{row.requests} lượt</strong><span>${row.estimatedCostUsd.toFixed(4)}</span></div></div>)}</div></article>
    </section>
    <section className="dashboard-grid"><article className="card panel"><div className="panel-header"><h2>RabbitMQ queues</h2></div><div className="visual-status-grid visual-queue-grid"><div><span>MAIN</span><strong>{operations.mainQueueMessages ?? "—"}</strong></div><div><span>RETRY</span><strong>{operations.retryQueueMessages ?? "—"}</strong></div><div><span>DLQ</span><strong>{operations.dlqMessages ?? "—"}</strong></div></div><span className={operations.rabbitmqAvailable ? "trend" : "trend warn"}>{operations.rabbitmqAvailable ? "Broker khả dụng" : "Không đọc được broker"}</span></article><article className="card panel"><div className="panel-header"><h2>Reindex thủ công</h2></div><div className="visual-reindex-form"><select value={targetType} onChange={(event) => setTargetType(event.target.value as "imageId" | "productId")} aria-label="Loại đối tượng reindex"><option value="productId">Product ID</option><option value="imageId">Image ID</option></select><input value={targetId} onChange={(event) => setTargetId(event.target.value)} placeholder="UUID" aria-label="UUID cần reindex"/><button className="admin-btn" disabled={retrying} onClick={() => void reindexTarget()}>Tạo job</button></div></article></section>
    <section className="card panel"><div className="panel-header"><h2>Job indexing gần đây</h2></div><div className="table-scroll"><table className="data-table"><thead><tr><th>Loại</th><th>Trạng thái</th><th>Tiến độ</th><th>Lỗi</th><th>Khởi tạo</th></tr></thead><tbody>{jobs.jobs.length === 0 && <tr><td colSpan={5}>Chưa có job.</td></tr>}{jobs.jobs.map((job) => <tr key={job.id}><td>{job.jobType}</td><td><span className={`status ${job.status === "COMPLETED" ? "active" : job.status === "FAILED" ? "critical" : "pending"}`}>{job.status}</span></td><td>{job.completedCount}/{job.totalCount} ({job.pendingCount} chờ)</td><td>{job.failedCount}</td><td>{new Date(job.createdAt).toLocaleString("vi-VN")}</td></tr>)}</tbody></table></div></section>
  </>;
}

"use client";

import { useEffect, useState } from "react";
import { ArrowsClockwise, LockKey, Robot, ShieldCheck } from "@phosphor-icons/react";
import {
  approveApproval,
  createApproval,
  executeApproval,
  getCopilotConfig,
  listApprovals,
  listCopilotRuns,
  rejectApproval,
} from "@/modules/admin-copilot/browser-api";
import type { ApprovalAction, ApprovalResponse, CopilotConfig, CopilotRun } from "@/modules/admin-copilot/types";

function decodeRole() {
  if (typeof window === "undefined") return null;
  const token = window.localStorage.getItem("sss_access_token");
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/"))) as { role?: string; roles?: string[] };
    return payload.role ?? payload.roles?.[0] ?? null;
  } catch {
    return null;
  }
}

export function ChatbotConfigWorkspace() {
  const [config, setConfig] = useState<CopilotConfig | null>(null);
  const [runs, setRuns] = useState<CopilotRun[]>([]);
  const [approvals, setApprovals] = useState<ApprovalResponse[]>([]);
  const [role, setRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [approvalMessage, setApprovalMessage] = useState<string | null>(null);
  const [approvalForm, setApprovalForm] = useState({
    action: "ACCEPT_REPLENISHMENT" as ApprovalAction,
    resourceId: "",
    quantity: "",
    note: "",
    idempotencyKey: `approval-${Date.now()}`,
    reason: "",
  });

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [nextConfig, nextRuns, nextApprovals] = await Promise.all([getCopilotConfig(), listCopilotRuns(50), listApprovals(50)]);
      setConfig(nextConfig);
      setRuns(nextRuns);
      setApprovals(nextApprovals);
    } catch (cause) {
      console.error(cause);
      setError("Khong tai duoc cau hinh Admin Copilot. Kiem tra chatbot-admin-service va env NEXT_PUBLIC_ADMIN_COPILOT_API_BASE_URL.");
    } finally {
      setLoading(false);
    }
  }

  async function submitApprovalRequest() {
    setApprovalMessage(null);
    const payload: Record<string, unknown> = {};
    if (approvalForm.note.trim()) payload.note = approvalForm.note.trim();
    if (approvalForm.action === "ADJUST_REPLENISHMENT") payload.quantity = Number(approvalForm.quantity);
    try {
      await createApproval({
        action: approvalForm.action,
        resourceId: approvalForm.resourceId.trim(),
        payload,
        idempotencyKey: approvalForm.idempotencyKey.trim(),
        reason: approvalForm.reason.trim(),
        riskLevel: approvalForm.action === "DISMISS_REPLENISHMENT" ? "LOW" : "MEDIUM",
      });
      setApprovalMessage("Da tao approval request.");
      setApprovalForm((current) => ({ ...current, idempotencyKey: `approval-${Date.now()}` }));
      setApprovals(await listApprovals(50));
    } catch (cause) {
      console.error(cause);
      setApprovalMessage("Khong tao duoc approval. Kiem tra APPROVALS_ENABLED, recommendation id va payload.");
    }
  }

  async function decideApproval(id: string, action: "approve" | "reject" | "execute") {
    setApprovalMessage(null);
    try {
      if (action === "approve") await approveApproval(id, "Approved from chatbot config UI");
      if (action === "reject") await rejectApproval(id, "Rejected from chatbot config UI");
      if (action === "execute") await executeApproval(id);
      setApprovals(await listApprovals(50));
    } catch (cause) {
      console.error(cause);
      setApprovalMessage("Action khong thuc hien duoc. Kiem tra status, WRITE_TOOLS_ENABLED va resource revalidation.");
    }
  }

  useEffect(() => {
    setRole(decodeRole());
    void load();
  }, []);

  if (role && role !== "ADMIN") {
    return (
      <section className="card panel">
        <LockKey size={32} weight="duotone" />
        <h2>Chi ADMIN duoc truy cap cau hinh chatbot</h2>
        <p className="panel-copy">Vai tro hien tai: {role}. Copilot service van tu chan tool call voi role khong hop le.</p>
      </section>
    );
  }

  return (
    <section className="admin-grid admin-grid-2">
      <div className="admin-stack">
        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Runtime</h2>
              <p className="panel-copy">Trang thai provider, prompt va gioi han Phase 6.</p>
            </div>
            <button className="admin-btn secondary" type="button" onClick={() => void load()} disabled={loading}>
              <ArrowsClockwise size={18} weight="duotone" />
              {loading ? "Dang tai" : "Lam moi"}
            </button>
          </div>
          {error && <p className="error-state">{error}</p>}
          {config && (
            <div className="admin-form-grid">
              <div className="admin-subcard"><span className="table-subtle">Provider</span><strong>{config.modelProvider}</strong></div>
              <div className="admin-subcard"><span className="table-subtle">Model</span><strong>{config.modelName}</strong></div>
              <div className="admin-subcard"><span className="table-subtle">Prompt version</span><strong>{config.promptVersion}</strong></div>
              <div className="admin-subcard"><span className="table-subtle">Timeout</span><strong>{config.agentTimeoutSeconds}s / tool {config.toolTimeoutSeconds}s</strong></div>
              <div className="admin-subcard"><span className="table-subtle">Max tool calls</span><strong>{config.maxToolCallsPerRun}</strong></div>
              <div className="admin-subcard"><span className="table-subtle">Rate limit</span><strong>{config.rateLimitPerMinute}/min</strong></div>
            </div>
          )}
        </section>

        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Guardrails</h2>
              <p className="panel-copy">Phase 8 chi execute write action sau approval rieng, audit va idempotency.</p>
            </div>
            <ShieldCheck size={24} weight="duotone" />
          </div>
          {config && (
            <div className="filters">
              <span className={`status ${config.readOnlyMode ? "active" : "critical"}`}>READ_ONLY={String(config.readOnlyMode)}</span>
              <span className={`status ${config.writeToolsEnabled ? "critical" : "active"}`}>WRITE_TOOLS={String(config.writeToolsEnabled)}</span>
              <span className={`status ${config.approvalsEnabled ? "active" : "draft"}`}>APPROVALS={String(config.approvalsEnabled)}</span>
              <span className={`status ${config.observabilityEnabled ? "active" : "draft"}`}>TRACE={String(config.observabilityEnabled)}</span>
            </div>
          )}
        </section>

        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Approval queue</h2>
              <p className="panel-copy">Tao va duyet action replenishment; cau chat dong y khong execute write.</p>
            </div>
          </div>
          <div className="admin-form-grid">
            <select
              className="admin-input"
              value={approvalForm.action}
              onChange={(event) => setApprovalForm((current) => ({ ...current, action: event.target.value as ApprovalAction }))}
            >
              <option value="ACCEPT_REPLENISHMENT">Accept replenishment</option>
              <option value="ADJUST_REPLENISHMENT">Adjust replenishment</option>
              <option value="DISMISS_REPLENISHMENT">Dismiss replenishment</option>
            </select>
            <input className="admin-input" placeholder="Recommendation id" value={approvalForm.resourceId} onChange={(event) => setApprovalForm((current) => ({ ...current, resourceId: event.target.value }))} />
            <input className="admin-input" placeholder="Quantity for adjust" value={approvalForm.quantity} onChange={(event) => setApprovalForm((current) => ({ ...current, quantity: event.target.value }))} />
            <input className="admin-input" placeholder="Idempotency key" value={approvalForm.idempotencyKey} onChange={(event) => setApprovalForm((current) => ({ ...current, idempotencyKey: event.target.value }))} />
          </div>
          <textarea className="admin-input" rows={3} placeholder="Reason" value={approvalForm.reason} onChange={(event) => setApprovalForm((current) => ({ ...current, reason: event.target.value }))} style={{ marginTop: 12 }} />
          <textarea className="admin-input" rows={2} placeholder="Note" value={approvalForm.note} onChange={(event) => setApprovalForm((current) => ({ ...current, note: event.target.value }))} style={{ marginTop: 12 }} />
          <div className="filters" style={{ marginTop: 12 }}>
            <button className="admin-btn" type="button" onClick={() => void submitApprovalRequest()}>Tao approval</button>
            {approvalMessage && <span className="table-subtle">{approvalMessage}</span>}
          </div>
          <div className="admin-stack" style={{ marginTop: 16 }}>
            {approvals.length === 0 && <div className="empty-state">Chua co approval request.</div>}
            {approvals.map((approval) => (
              <div className="admin-subcard admin-subcard-tight" key={approval.id}>
                <strong>{approval.action}</strong>
                <span className="table-subtle">{approval.resourceId} / {approval.status} / risk {approval.riskLevel}</span>
                <span className="table-subtle">hash {approval.payloadHash.slice(0, 12)} / idem {approval.idempotencyKey}</span>
                <span className="table-subtle">audit {approval.audit.map((item) => item.event).join(" -> ")}</span>
                <div className="filters">
                  <button className="admin-btn secondary" type="button" onClick={() => void decideApproval(approval.id, "approve")} disabled={approval.status !== "PENDING"}>Approve</button>
                  <button className="admin-btn secondary" type="button" onClick={() => void decideApproval(approval.id, "reject")} disabled={approval.status !== "PENDING"}>Reject</button>
                  <button className="admin-btn secondary" type="button" onClick={() => void decideApproval(approval.id, "execute")} disabled={approval.status !== "APPROVED"}>Execute</button>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Tools va role</h2>
              <p className="panel-copy">Sales/Warehouse dang bi chan trong Phase 6 theo policy service.</p>
            </div>
            <Robot size={24} weight="duotone" />
          </div>
          <div className="admin-grid admin-grid-2">
            <div className="admin-subcard">
              <strong>Enabled tools</strong>
              <div className="filters" style={{ marginTop: 10 }}>
                {config?.enabledTools.map((tool) => <span className="status draft" key={tool}>{tool}</span>)}
              </div>
            </div>
            <div className="admin-subcard">
              <strong>Role permission</strong>
              {(config?.rolePermissions ?? []).map((permission) => (
                <p className="table-subtle" key={permission.role}>{permission.role}: {permission.access}</p>
              ))}
            </div>
          </div>
        </section>
      </div>

      <aside className="admin-stack">
        <section className="card panel">
          <h2>Evaluation</h2>
          {config ? (
            <>
              <p className="panel-copy">{config.evaluation.dataset}: {config.evaluation.cases} cases, last result {config.evaluation.lastResult}.</p>
              <p className="table-subtle">{config.cost.note}</p>
            </>
          ) : <div className="empty-state">Chua tai config.</div>}
        </section>
        <section className="card panel">
          <h2>Run history</h2>
          <div className="admin-stack">
            {runs.length === 0 && <div className="empty-state">Chua co run nao.</div>}
            {runs.map((run) => (
              <div className="admin-subcard admin-subcard-tight" key={run.runId}>
                <strong>{run.intent}</strong>
                <span className="table-subtle">{run.tool} - {run.source}</span>
                <span className="table-subtle">{run.role} / {run.actorId}</span>
              </div>
            ))}
          </div>
        </section>
      </aside>
    </section>
  );
}

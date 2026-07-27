"use client";

import { useEffect, useState } from "react";
import { ArrowsClockwise, LockKey, Robot, ShieldCheck } from "@phosphor-icons/react";
import { getCopilotConfig, listCopilotRuns } from "@/modules/admin-copilot/browser-api";
import type { CopilotConfig, CopilotRun } from "@/modules/admin-copilot/types";

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
  const [role, setRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [nextConfig, nextRuns] = await Promise.all([getCopilotConfig(), listCopilotRuns(50)]);
      setConfig(nextConfig);
      setRuns(nextRuns);
    } catch (cause) {
      console.error(cause);
      setError("Khong tai duoc cau hinh Admin Copilot. Kiem tra chatbot-admin-service va env NEXT_PUBLIC_ADMIN_COPILOT_API_BASE_URL.");
    } finally {
      setLoading(false);
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
              <p className="panel-copy">Phase 6 chi cau hinh va quan sat, khong publish write action.</p>
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

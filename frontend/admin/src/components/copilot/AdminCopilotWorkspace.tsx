"use client";

import { FormEvent, useMemo, useState } from "react";
import { ChatCircleText, CheckCircle, ClockCounterClockwise, LinkSimple, PaperPlaneTilt, WarningCircle, XCircle } from "@phosphor-icons/react";
import { listCopilotRuns, sendCopilotFeedback, sendCopilotMessage } from "@/modules/admin-copilot/browser-api";
import type { ChatResponse, CopilotRun } from "@/modules/admin-copilot/types";

type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  text: string;
  response?: ChatResponse;
  state?: "answer" | "warning" | "partial" | "error";
};

const suggestedQuestions = [
  "Ton kho hien co SKU nao co nguy co het hang?",
  "Chat luong forecast hien tai ra sao?",
  "Giai thich recommendation bo sung hang dang cho duyet.",
  "Tong quan don hang hien tai nhu the nao?",
  "San pham nao dang co hieu suat ban tot?",
  "Mo phong chinh sach ton kho co thay doi gi?",
];

function makeSessionId() {
  return `admin-ui-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function stateFor(response: ChatResponse): ChatMessage["state"] {
  if (response.partial) return "partial";
  if (response.warnings.length > 0) return "warning";
  if (response.intent === "UNKNOWN") return "partial";
  return "answer";
}

function extractLinks(response?: ChatResponse) {
  const result = response?.toolCalls[0]?.result;
  const rows = Array.isArray(result)
    ? result
    : Array.isArray((result as { content?: unknown[] } | null)?.content)
      ? (result as { content: unknown[] }).content
      : Array.isArray((result as { items?: unknown[] } | null)?.items)
        ? (result as { items: unknown[] }).items
        : [];

  return rows
    .slice(0, 3)
    .map((item) => item as { variantId?: string; productId?: string; orderId?: string; id?: string; sku?: string; productName?: string })
    .map((item) => {
      if (item.variantId) return { label: item.sku ?? item.productName ?? "Inventory detail", href: `/inventory/ai-insights?variantId=${item.variantId}` };
      if (item.productId) return { label: item.productName ?? "Product detail", href: `/products?productId=${item.productId}` };
      if (item.orderId || item.id) return { label: `Order ${item.orderId ?? item.id}`, href: `/orders?keyword=${item.orderId ?? item.id}` };
      return null;
    })
    .filter((item): item is { label: string; href: string } => item !== null);
}

export function AdminCopilotWorkspace({ initialRuns }: { initialRuns: CopilotRun[] }) {
  const [sessionId] = useState(makeSessionId);
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: "welcome",
      role: "assistant",
      text: "Admin Copilot san sang tra loi bang cac API read-only da duoc phe duyet.",
      state: "answer",
    },
  ]);
  const [runs, setRuns] = useState(initialRuns);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [feedbackNote, setFeedbackNote] = useState("");
  const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null);

  const latestResponse = useMemo(() => [...messages].reverse().find((item) => item.response)?.response, [messages]);

  async function submitMessage(value: string) {
    const trimmed = value.trim();
    if (!trimmed || loading) return;

    const userMessage: ChatMessage = { id: crypto.randomUUID(), role: "user", text: trimmed };
    setMessages((current) => [...current, userMessage]);
    setInput("");
    setLoading(true);
    setFeedbackMessage(null);

    try {
      const response = await sendCopilotMessage(sessionId, trimmed);
      setMessages((current) => [
        ...current,
        {
          id: response.runId,
          role: "assistant",
          text: response.reply,
          response,
          state: stateFor(response),
        },
      ]);
      setRuns(await listCopilotRuns(20));
    } catch (cause) {
      console.error(cause);
      setMessages((current) => [
        ...current,
        {
          id: crypto.randomUUID(),
          role: "assistant",
          text: "Khong goi duoc Admin Copilot service. Kiem tra backend chatbot-admin-service, token admin va CORS.",
          state: "error",
        },
      ]);
    } finally {
      setLoading(false);
    }
  }

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void submitMessage(input);
  }

  async function submitFeedback(rating: "CORRECT" | "INCORRECT") {
    if (!latestResponse) return;
    await sendCopilotFeedback(latestResponse.runId, rating, feedbackNote || undefined);
    setFeedbackMessage("Da luu feedback vao evaluation dataset tam thoi.");
    setFeedbackNote("");
  }

  return (
    <section className="admin-grid admin-grid-2">
      <div className="card panel">
        <div className="panel-header">
          <div>
            <h2>Admin Copilot</h2>
            <p className="panel-copy">Hoi ve ton kho, forecast, don hang va recommendation qua tool read-only.</p>
          </div>
          <span className="status active">Read-only</span>
        </div>

        <div className="filters" style={{ marginBottom: 16 }}>
          {suggestedQuestions.map((question) => (
            <button className="filter-chip" type="button" key={question} onClick={() => void submitMessage(question)} disabled={loading}>
              {question}
            </button>
          ))}
        </div>

        <div className="admin-stack" style={{ maxHeight: 560, overflowY: "auto" }}>
          {messages.map((message) => (
            <article className="admin-subcard" key={message.id} style={{ borderColor: message.state === "error" ? "#fecaca" : undefined }}>
              <div className="panel-header" style={{ alignItems: "flex-start" }}>
                <div>
                  <span className="table-subtle">{message.role === "user" ? "Ban" : "Copilot"}</span>
                  <p style={{ margin: "6px 0 0" }}>{message.text}</p>
                </div>
                {message.state === "answer" && <CheckCircle size={20} weight="duotone" />}
                {message.state === "warning" && <WarningCircle size={20} weight="duotone" />}
                {message.state === "partial" && <ClockCounterClockwise size={20} weight="duotone" />}
                {message.state === "error" && <XCircle size={20} weight="duotone" />}
              </div>

              {message.response?.warnings.map((warning) => <p className="action-message" key={warning}>{warning}</p>)}

              {extractLinks(message.response).length > 0 && (
                <div className="filters">
                  {extractLinks(message.response).map((link) => (
                    <a className="filter-chip active" href={link.href} key={link.href}>
                      <LinkSimple size={14} /> {link.label}
                    </a>
                  ))}
                </div>
              )}
            </article>
          ))}
        </div>

        <form className="table-toolbar" onSubmit={onSubmit} style={{ marginTop: 16 }}>
          <div className="admin-search" style={{ flex: 1 }}>
            <ChatCircleText size={18} />
            <input
              aria-label="Nhap cau hoi Admin Copilot"
              value={input}
              onChange={(event) => setInput(event.target.value)}
              placeholder="Hoi ve stockout, forecast, order, product..."
              style={{ border: 0, outline: 0, width: "100%", background: "transparent" }}
            />
          </div>
          <button className="admin-btn" type="submit" disabled={loading}>
            <PaperPlaneTilt size={18} weight="duotone" />
            {loading ? "Dang hoi" : "Gui"}
          </button>
        </form>
      </div>

      <aside className="admin-stack">
        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Feedback</h2>
              <p className="panel-copy">Danh dau cau tra loi de bo sung evaluation dataset.</p>
            </div>
          </div>
          <textarea
            className="admin-input"
            rows={4}
            value={feedbackNote}
            onChange={(event) => setFeedbackNote(event.target.value)}
            placeholder="Ghi chu ngan neu cau tra loi sai hoac thieu ngu canh"
          />
          <div className="filters" style={{ marginTop: 12 }}>
            <button className="admin-btn secondary" type="button" onClick={() => void submitFeedback("CORRECT")} disabled={!latestResponse}>Dung</button>
            <button className="admin-btn secondary" type="button" onClick={() => void submitFeedback("INCORRECT")} disabled={!latestResponse}>Sai</button>
          </div>
          {feedbackMessage && <p className="action-message">{feedbackMessage}</p>}
        </section>

        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Run history</h2>
              <p className="panel-copy">Trace da redact, khong co chain-of-thought.</p>
            </div>
          </div>
          <div className="admin-stack">
            {runs.length === 0 && <div className="empty-state">Chua co run nao trong memory service.</div>}
            {runs.map((run) => (
              <div className="admin-subcard admin-subcard-tight" key={run.runId}>
                <strong>{run.intent}</strong>
                <span className="table-subtle">{run.tool} - {run.source}</span>
                <span className="table-subtle">Role {run.role}, run {run.runId}</span>
                {run.warnings.length > 0 && <span className="status low">Co warning</span>}
              </div>
            ))}
          </div>
        </section>
      </aside>
    </section>
  );
}

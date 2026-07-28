"use client";

import { FormEvent, useEffect, useRef, useState, useMemo, useCallback } from "react";
import {
  ChatCircleText,
  ClockCounterClockwise,
  LinkSimple,
  PaperPlaneTilt,
  WarningCircle,
  XCircle,
  Robot,
  User,
  ThumbsUp,
  ThumbsDown,
  Sparkle,
  ArrowRight,
  ClockClockwise,
  TerminalWindow,
  Package,
  ShoppingCart,
  TShirt,
} from "@phosphor-icons/react";
import { listCopilotRuns, sendCopilotFeedback, sendCopilotMessage } from "@/modules/admin-copilot/browser-api";
import type { ChatResponse, CopilotRun } from "@/modules/admin-copilot/types";

type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  text: string;
  response?: ChatResponse;
  state?: "answer" | "warning" | "partial" | "error" | "loading";
  timestamp: string;
};

const CÂU_HỎI_GỢI_Ý = [
  "SKU nào đang có nguy cơ hết hàng?",
  "Chất lượng dự báo hiện tại ra sao?",
  "Sản phẩm nào bán chạy nhất?",
  "Tổng quan đơn hàng hôm nay?",
  "Giải thích đề xuất bổ sung hàng?",
  "Mô phỏng thay đổi chính sách tồn kho?",
];

function tạoSessionId() {
  return `admin-ui-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function xácĐịnhTrạngThái(response: ChatResponse): ChatMessage["state"] {
  if (response.partial) return "partial";
  if (response.warnings.length > 0) return "warning";
  if (response.intent === "UNKNOWN") return "partial";
  return "answer";
}

function giờHiệnTại() {
  return new Intl.DateTimeFormat("vi-VN", { hour: "2-digit", minute: "2-digit" }).format(new Date());
}

function tríchXuấtLiênKết(response?: ChatResponse) {
  if (!response) return [];
  const result = response.toolCalls[0]?.result;
  const rows = Array.isArray(result)
    ? result
    : Array.isArray((result as { content?: unknown[] } | null)?.content)
      ? (result as { content: unknown[] }).content
      : Array.isArray((result as { items?: unknown[] } | null)?.items)
        ? (result as { items: unknown[] }).items
        : [];

  return rows
    .slice(0, 5)
    .map((item) => item as { variantId?: string; productId?: string; orderId?: string; id?: string; sku?: string; productName?: string; name?: string })
    .map((item) => {
      if (item.variantId)
        return {
          label: item.sku ?? item.productName ?? "Chi tiết SKU",
          sub: item.productName ?? item.sku ?? "",
          href: `/inventory/ai-insights?variantId=${item.variantId}`,
          type: "inventory" as const,
        };
      if (item.productId)
        return {
          label: item.productName ?? item.name ?? "Chi tiết sản phẩm",
          sub: item.productId,
          href: `/products?productId=${item.productId}`,
          type: "product" as const,
        };
      if (item.orderId || item.id)
        return {
          label: `Đơn hàng #${(item.orderId ?? item.id ?? "").slice(0, 8)}`,
          sub: item.orderId ?? item.id ?? "",
          href: `/orders?keyword=${item.orderId ?? item.id}`,
          type: "order" as const,
        };
      return null;
    })
    .filter((item): item is NonNullable<typeof item> => item !== null);
}

/** Render markdown đơn giản: **bold**, xuống dòng */
function renderMarkdown(text: string) {
  return text.split("\n").map((line, lineIdx) => {
    const parts = line.split(/(\*\*[^*]+\*\*)/g);
    return (
      <span key={lineIdx}>
        {parts.map((part, i) => {
          if (part.startsWith("**") && part.endsWith("**")) {
            return <strong key={i}>{part.slice(2, -2)}</strong>;
          }
          return <span key={i}>{part}</span>;
        })}
        {lineIdx < text.split("\n").length - 1 && <br />}
      </span>
    );
  });
}

function nhãnTrạngThái(state?: ChatMessage["state"]) {
  switch (state) {
    case "answer": return "Đã trả lời";
    case "warning": return "Có cảnh báo";
    case "partial": return "Kết quả một phần";
    case "error": return "Lỗi";
    default: return "";
  }
}

function iconLiênKết(type: "inventory" | "product" | "order") {
  switch (type) {
    case "inventory": return <Package size={12} weight="duotone" />;
    case "product": return <TShirt size={12} weight="duotone" />;
    case "order": return <ShoppingCart size={12} weight="duotone" />;
  }
}

export function AdminCopilotWorkspace({ initialRuns }: { initialRuns: CopilotRun[] }) {
  const [sessionId] = useState(tạoSessionId);
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: "chào-mừng",
      role: "assistant",
      text: "Xin chào! Tôi là **Admin Copilot** 🤖\n\nTôi có thể giúp bạn tra cứu **tồn kho**, **đơn hàng**, **dự báo** và **hiệu suất sản phẩm** qua các công cụ chỉ đọc đã được phê duyệt.\n\nBạn muốn hỏi gì hôm nay?",
      state: "answer",
      timestamp: giờHiệnTại(),
    },
  ]);
  const [runs, setRuns] = useState(initialRuns);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [feedbackNote, setFeedbackNote] = useState("");
  const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"feedback" | "history">("feedback");
  const [panelOpen, setPanelOpen] = useState(true);

  const chatEndRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const latestResponse = useMemo(
    () => [...messages].reverse().find((m) => m.response)?.response,
    [messages]
  );

  // Cuộn xuống cuối mỗi khi messages thay đổi
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // Auto-resize textarea
  const autoResize = useCallback(() => {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = "auto";
    el.style.height = `${Math.min(el.scrollHeight, 140)}px`;
  }, []);

  useEffect(() => {
    autoResize();
  }, [input, autoResize]);

  async function gửiTinNhắn(nộiDung: string) {
    const trimmed = nộiDung.trim();
    if (!trimmed || loading) return;

    const now = giờHiệnTại();
    const tinUser: ChatMessage = {
      id: crypto.randomUUID(),
      role: "user",
      text: trimmed,
      timestamp: now,
    };
    const tinLoading: ChatMessage = {
      id: "loading-" + Date.now(),
      role: "assistant",
      text: "",
      state: "loading",
      timestamp: now,
    };

    setMessages((cur) => [...cur, tinUser, tinLoading]);
    setInput("");
    setLoading(true);
    setFeedbackMessage(null);

    try {
      const response = await sendCopilotMessage(sessionId, trimmed);
      setMessages((cur) => [
        ...cur.filter((m) => m.state !== "loading"),
        {
          id: response.runId,
          role: "assistant",
          text: response.reply,
          response,
          state: xácĐịnhTrạngThái(response),
          timestamp: giờHiệnTại(),
        },
      ]);
      setRuns(await listCopilotRuns(20));
    } catch (err) {
      console.error(err);
      setMessages((cur) => [
        ...cur.filter((m) => m.state !== "loading"),
        {
          id: crypto.randomUUID(),
          role: "assistant",
          text: "Không kết nối được với dịch vụ Admin Copilot.\n\nVui lòng kiểm tra backend **chatbot-admin-service** đang chạy và thử lại.",
          state: "error",
          timestamp: giờHiệnTại(),
        },
      ]);
    } finally {
      setLoading(false);
    }
  }

  function xửLýGửi(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    void gửiTinNhắn(input);
  }

  function xửLýPhímTắt(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      void gửiTinNhắn(input);
    }
  }

  async function gửiPhảnHồi(đánhGiá: "CORRECT" | "INCORRECT") {
    if (!latestResponse) return;
    await sendCopilotFeedback(latestResponse.runId, đánhGiá, feedbackNote || undefined);
    setFeedbackMessage(
      đánhGiá === "CORRECT"
        ? "✅ Cảm ơn! Phản hồi đã được lưu vào dataset."
        : "📝 Đã ghi nhận. Chúng tôi sẽ cải thiện câu trả lời này."
    );
    setFeedbackNote("");
  }

  const tênIntent: Record<string, string> = {
    INVENTORY_RISK: "Rủi ro tồn kho",
    REPLENISHMENT_EXPLANATION: "Giải thích bổ sung hàng",
    FORECAST_QUALITY: "Chất lượng dự báo",
    SALES_OVERVIEW: "Tổng quan bán hàng",
    PRODUCT_PERFORMANCE: "Hiệu suất sản phẩm",
    ORDER_OVERVIEW: "Tổng quan đơn hàng",
    WHAT_IF_SIMULATION: "Mô phỏng kịch bản",
    UNKNOWN: "Không xác định",
  };

  return (
    <div className="copilot-shell">
      {/* ═══ KHUNG CHAT CHÍNH ═══ */}
      <div className="copilot-main">

        {/* Header */}
        <div className="copilot-header">
          <div className="copilot-header-info">
            <div className="copilot-avatar-header">
              <Robot size={20} weight="duotone" />
            </div>
            <div>
              <div className="copilot-header-title">Admin Copilot</div>
              <div className="copilot-header-sub">
                <span className="copilot-online-dot" />
                Đang hoạt động · Chỉ đọc
              </div>
            </div>
          </div>
          <button
            className="copilot-panel-toggle"
            onClick={() => setPanelOpen((v) => !v)}
            title={panelOpen ? "Ẩn bảng phụ" : "Hiện bảng phụ"}
          >
            <TerminalWindow size={16} weight="duotone" />
            {panelOpen ? "Ẩn chi tiết" : "Xem chi tiết"}
          </button>
        </div>

        {/* Câu hỏi gợi ý */}
        <div className="copilot-suggestions">
          <span className="copilot-suggestions-label">
            <Sparkle size={12} weight="duotone" />
            Câu hỏi nhanh
          </span>
          <div className="copilot-chips">
            {CÂU_HỎI_GỢI_Ý.map((câu) => (
              <button
                key={câu}
                className="copilot-chip"
                type="button"
                onClick={() => void gửiTinNhắn(câu)}
                disabled={loading}
              >
                {câu}
              </button>
            ))}
          </div>
        </div>

        {/* Vùng tin nhắn */}
        <div className="copilot-messages">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`copilot-bubble-wrap ${msg.role === "user" ? "copilot-bubble-wrap--user" : "copilot-bubble-wrap--bot"}`}
            >
              {/* Avatar bot */}
              {msg.role === "assistant" && (
                <div className="copilot-avatar copilot-avatar--bot">
                  <Robot size={14} weight="duotone" />
                </div>
              )}

              <div className="copilot-bubble-group">
                {/* Bubble nội dung */}
                {msg.state === "loading" ? (
                  <div className="copilot-bubble copilot-bubble--bot">
                    <div className="copilot-typing">
                      <span /><span /><span />
                    </div>
                  </div>
                ) : (
                  <div
                    className={[
                      "copilot-bubble",
                      msg.role === "user" ? "copilot-bubble--user" : "copilot-bubble--bot",
                      msg.state === "error" ? "copilot-bubble--error" : "",
                      msg.state === "warning" ? "copilot-bubble--warning" : "",
                    ].filter(Boolean).join(" ")}
                  >
                    <div className="copilot-bubble-text">
                      {renderMarkdown(msg.text)}
                    </div>

                    {/* Cảnh báo */}
                    {msg.response?.warnings.map((w, i) => (
                      <div className="copilot-warning-row" key={i}>
                        <WarningCircle size={13} weight="duotone" />
                        <span>{w}</span>
                      </div>
                    ))}

                    {/* Liên kết nhanh tới trang chi tiết */}
                    {tríchXuấtLiênKết(msg.response).length > 0 && (
                      <div className="copilot-links">
                        <div className="copilot-links-title">
                          <LinkSimple size={11} weight="duotone" />
                          Xem chi tiết
                        </div>
                        {tríchXuấtLiênKết(msg.response).map((lk, i) => (
                          <a key={i} href={lk.href} className="copilot-link">
                            {iconLiênKết(lk.type)}
                            <div className="copilot-link-content">
                              <span className="copilot-link-label">{lk.label}</span>
                              {lk.sub && lk.sub !== lk.label && (
                                <span className="copilot-link-sub">{lk.sub.length > 28 ? lk.sub.slice(0, 28) + "…" : lk.sub}</span>
                              )}
                            </div>
                            <ArrowRight size={10} className="copilot-link-arrow" />
                          </a>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {/* Metadata */}
                {msg.state && msg.state !== "loading" && (
                  <div className={`copilot-meta ${msg.role === "user" ? "copilot-meta--user" : ""}`}>
                    <span className="copilot-meta-time">{msg.timestamp}</span>
                    {msg.state !== "answer" && (
                      <>
                        <span className="copilot-meta-dot">·</span>
                        {msg.state === "warning" && <WarningCircle size={11} weight="duotone" className="copilot-meta-icon--warn" />}
                        {msg.state === "partial" && <ClockCounterClockwise size={11} weight="duotone" />}
                        {msg.state === "error" && <XCircle size={11} weight="duotone" className="copilot-meta-icon--err" />}
                        <span>{nhãnTrạngThái(msg.state)}</span>
                      </>
                    )}
                  </div>
                )}
              </div>

              {/* Avatar người dùng */}
              {msg.role === "user" && (
                <div className="copilot-avatar copilot-avatar--user">
                  <User size={14} weight="duotone" />
                </div>
              )}
            </div>
          ))}
          <div ref={chatEndRef} />
        </div>

        {/* Ô nhập */}
        <form className="copilot-input-bar" onSubmit={xửLýGửi}>
          <div className="copilot-input-wrap">
            <ChatCircleText size={17} className="copilot-input-icon" />
            <textarea
              ref={textareaRef}
              className="copilot-input"
              rows={1}
              value={input}
              onChange={(e) => { setInput(e.target.value); autoResize(); }}
              onKeyDown={xửLýPhímTắt}
              placeholder="Hỏi về tồn kho, đơn hàng, dự báo... (Enter để gửi · Shift+Enter xuống dòng)"
              disabled={loading}
              aria-label="Nhập câu hỏi Admin Copilot"
            />
          </div>
          <button className="copilot-send-btn" type="submit" disabled={loading || !input.trim()}>
            <PaperPlaneTilt size={17} weight="duotone" />
            <span className="copilot-send-label">{loading ? "Đang xử lý..." : "Gửi"}</span>
          </button>
        </form>
      </div>

      {/* ═══ BẢNG PHỤ ═══ */}
      {panelOpen && (
        <aside className="copilot-aside">
          <div className="copilot-aside-tabs">
            <button
              className={`copilot-aside-tab ${activeTab === "feedback" ? "active" : ""}`}
              onClick={() => setActiveTab("feedback")}
            >
              <ThumbsUp size={14} weight="duotone" />
              Phản hồi
            </button>
            <button
              className={`copilot-aside-tab ${activeTab === "history" ? "active" : ""}`}
              onClick={() => setActiveTab("history")}
            >
              <ClockClockwise size={14} weight="duotone" />
              Lịch sử
            </button>
          </div>

          {/* Tab Phản hồi */}
          {activeTab === "feedback" && (
            <div className="copilot-feedback-panel">
              <div className="copilot-feedback-title">Đánh giá câu trả lời</div>
              <p className="copilot-feedback-desc">
                Phản hồi giúp cải thiện độ chính xác của Admin Copilot theo thời gian.
              </p>

              {!latestResponse && (
                <div className="copilot-feedback-empty">
                  Hãy đặt câu hỏi trước để có thể đánh giá câu trả lời.
                </div>
              )}

              {latestResponse && (
                <>
                  <div className="copilot-feedback-intent">
                    Câu trả lời gần nhất: <strong>{tênIntent[latestResponse.intent] ?? latestResponse.intent}</strong>
                  </div>
                  <textarea
                    className="copilot-feedback-input"
                    rows={3}
                    value={feedbackNote}
                    onChange={(e) => setFeedbackNote(e.target.value)}
                    placeholder="Ghi chú nếu câu trả lời sai hoặc thiếu ngữ cảnh (tuỳ chọn)..."
                  />
                  <div className="copilot-feedback-actions">
                    <button
                      className="copilot-feedback-btn copilot-feedback-btn--ok"
                      type="button"
                      onClick={() => void gửiPhảnHồi("CORRECT")}
                    >
                      <ThumbsUp size={15} weight="duotone" />
                      Đúng
                    </button>
                    <button
                      className="copilot-feedback-btn copilot-feedback-btn--bad"
                      type="button"
                      onClick={() => void gửiPhảnHồi("INCORRECT")}
                    >
                      <ThumbsDown size={15} weight="duotone" />
                      Sai
                    </button>
                  </div>
                </>
              )}

              {feedbackMessage && (
                <div className="copilot-feedback-msg">{feedbackMessage}</div>
              )}
            </div>
          )}

          {/* Tab Lịch sử */}
          {activeTab === "history" && (
            <div className="copilot-history-panel">
              <div className="copilot-feedback-title">Lịch sử chạy gần đây</div>
              <p className="copilot-feedback-desc">
                Trace đã được làm sạch, không hiển thị chuỗi suy luận nội bộ.
              </p>

              {runs.length === 0 ? (
                <div className="copilot-history-empty">
                  Chưa có lần chạy nào. Hãy bắt đầu đặt câu hỏi!
                </div>
              ) : (
                <div className="copilot-history-list">
                  {runs.map((run) => (
                    <div key={run.runId} className="copilot-history-item">
                      <div className="copilot-history-intent">
                        {tênIntent[run.intent] ?? run.intent.replace(/_/g, " ")}
                      </div>
                      <div className="copilot-history-detail">
                        <span>{run.tool}</span>
                        <span>·</span>
                        <span>{run.source}</span>
                      </div>
                      <div className="copilot-history-meta">
                        <span className="copilot-history-role">Vai trò: {run.role}</span>
                        {run.warnings.length > 0 && (
                          <span className="copilot-history-warn">
                            <WarningCircle size={10} weight="duotone" />
                            Có cảnh báo
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </aside>
      )}
    </div>
  );
}

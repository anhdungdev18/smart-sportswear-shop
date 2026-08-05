import { getAccessToken } from "@/lib/session";

const LEGACY_SESSION_KEY = "sss_chat_session_id";
const SESSION_PREFIX = "sss_chat_session_id:";
const FRESH_PREFIX = "sss_chat_session_fresh:";
const MESSAGES_PREFIX = "sss_chat_messages:";
const MAX_STORED_MESSAGES = 40;

export type StoredChatProduct = {
  name: string;
  slug: string;
  price?: number | null;
  image?: string | null;
};

export type StoredChatMessage = {
  role: "user" | "bot";
  text: string;
  products?: StoredChatProduct[];
};

function decodeJwtSubject(token: string | null): string | null {
  if (!token) return null;
  try {
    const encoded = token.split(".")[1];
    if (!encoded) return null;
    const normalized = encoded.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
    const payload = JSON.parse(atob(padded)) as { sub?: unknown };
    return typeof payload.sub === "string" && payload.sub.trim() ? payload.sub.trim() : null;
  } catch {
    return null;
  }
}

export function getChatScope(): string {
  if (typeof window === "undefined") return "guest";
  const userId = decodeJwtSubject(getAccessToken());
  return userId ? `user:${userId}` : "guest";
}

export function getOrCreateChatSession(scope: string): { id: string; isNew: boolean } {
  const sessionKey = `${SESSION_PREFIX}${scope}`;
  const freshKey = `${FRESH_PREFIX}${scope}`;
  let id = localStorage.getItem(sessionKey);

  if (!id) {
    // Keep the current conversation when upgrading from the old global key.
    id = localStorage.getItem(LEGACY_SESSION_KEY);
    if (!id) {
      id = crypto.randomUUID?.() ?? `web-${Date.now()}-${Math.random().toString(36).slice(2)}`;
      localStorage.setItem(freshKey, "1");
    }
    localStorage.setItem(sessionKey, id);
  }

  return { id, isNew: localStorage.getItem(freshKey) === "1" };
}

export function markChatSessionUsed(scope: string): void {
  localStorage.removeItem(`${FRESH_PREFIX}${scope}`);
}

export function loadChatMessages(scope: string): StoredChatMessage[] {
  try {
    const parsed: unknown = JSON.parse(localStorage.getItem(`${MESSAGES_PREFIX}${scope}`) ?? "[]");
    if (!Array.isArray(parsed)) return [];

    return parsed
      .filter(
        (item): item is StoredChatMessage =>
          Boolean(item) &&
          typeof item === "object" &&
          ((item as StoredChatMessage).role === "user" || (item as StoredChatMessage).role === "bot") &&
          typeof (item as StoredChatMessage).text === "string",
      )
      .slice(-MAX_STORED_MESSAGES);
  } catch {
    return [];
  }
}

export function saveChatMessages(scope: string, messages: StoredChatMessage[]): void {
  const completed = messages.filter((message) => message.text.trim().length > 0);
  localStorage.setItem(`${MESSAGES_PREFIX}${scope}`, JSON.stringify(completed.slice(-MAX_STORED_MESSAGES)));
}

/**
 * The AI admin workspaces are parked for now to keep them out of the normal
 * admin navigation and request path. Set the public flag to `true` to restore
 * all three screens without changing code.
 */
export const ADMIN_AI_WORKSPACES_ENABLED =
  process.env.NEXT_PUBLIC_ADMIN_AI_WORKSPACES_ENABLED === "true";

export const ADMIN_AI_WORKSPACE_ROUTES = [
  "/inventory/ai-insights",
  "/admin-copilot",
  "/chatbot-config",
] as const;

export function isAdminAiWorkspaceRoute(pathname: string) {
  return ADMIN_AI_WORKSPACE_ROUTES.some(
    (route) => pathname === route || pathname.startsWith(`${route}/`),
  );
}

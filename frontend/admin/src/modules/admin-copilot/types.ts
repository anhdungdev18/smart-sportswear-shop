export type CopilotIntent =
  | "INVENTORY_RISK"
  | "REPLENISHMENT_EXPLANATION"
  | "FORECAST_QUALITY"
  | "SALES_OVERVIEW"
  | "PRODUCT_PERFORMANCE"
  | "ORDER_OVERVIEW"
  | "WHAT_IF_SIMULATION"
  | "UNKNOWN";

export interface ToolCallRecord {
  tool: string;
  args: Record<string, unknown>;
  result: Record<string, unknown> | unknown[] | null;
  source: string;
  reason?: string | null;
}

export interface ReactTraceStep {
  step: number;
  node: string;
  tool?: string | null;
  reason?: string | null;
  observation?: string | null;
  decision?: string | null;
}

export interface ChatResponse {
  reply: string;
  intent: CopilotIntent;
  toolCalls: ToolCallRecord[];
  trace: ReactTraceStep[];
  partial: boolean;
  warnings: string[];
  groundedNumbers: string[];
  runId: string;
}

export interface CopilotRun {
  runId: string;
  sessionId: string;
  actorId: string;
  role: string;
  intent: CopilotIntent;
  tool: string;
  source: string;
  warnings: string[];
}

export interface CopilotConfig {
  environment: string;
  modelProvider: string;
  modelName: string;
  promptVersion: string;
  maxAgentSteps: number;
  maxToolCallsPerRun: number;
  agentTimeoutSeconds: number;
  toolTimeoutSeconds: number;
  maxInputChars: number;
  readOnlyMode: boolean;
  writeToolsEnabled: boolean;
  approvalsEnabled: boolean;
  observabilityEnabled: boolean;
  evaluationLoggingEnabled: boolean;
  rateLimitPerMinute: number;
  enabledTools: string[];
  approvalWriteActions?: ApprovalAction[];
  rolePermissions: Array<{ role: string; access: string }>;
  evaluation: { dataset: string; cases: number; lastResult: string; phase7ReadinessReport?: string };
  cost: { tokenCount: number; estimatedCost: number; currency: string; note: string };
  updatedAt: string;
}

export type ApprovalAction = "ACCEPT_REPLENISHMENT" | "ADJUST_REPLENISHMENT" | "DISMISS_REPLENISHMENT";
export type ApprovalStatus = "PENDING" | "APPROVED" | "EXECUTED" | "REJECTED" | "EXPIRED" | "FAILED";
export type ApprovalRiskLevel = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface ApprovalResponse {
  id: string;
  action: ApprovalAction;
  resourceType: string;
  resourceId: string;
  payload: Record<string, unknown>;
  payloadHash: string;
  idempotencyKey: string;
  reason: string;
  riskLevel: ApprovalRiskLevel;
  status: ApprovalStatus;
  requestedBy: string;
  approvedBy?: string | null;
  executedBy?: string | null;
  beforeSnapshot?: Record<string, unknown> | null;
  afterSnapshot?: Record<string, unknown> | null;
  audit: Array<{ event: string; actorId: string; role: string; at: string; extra?: Record<string, unknown> }>;
  error?: string | null;
  expiresAt: string;
  createdAt: string;
  updatedAt: string;
}

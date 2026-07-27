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
}

export interface ChatResponse {
  reply: string;
  intent: CopilotIntent;
  toolCalls: ToolCallRecord[];
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
  rolePermissions: Array<{ role: string; access: string }>;
  evaluation: { dataset: string; cases: number; lastResult: string };
  cost: { tokenCount: number; estimatedCost: number; currency: string; note: string };
  updatedAt: string;
}

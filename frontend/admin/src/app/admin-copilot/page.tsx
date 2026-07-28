import { AdminCopilotWorkspace } from "@/components/copilot/AdminCopilotWorkspace";

export default function AdminCopilotPage() {
  return (
    <main className="workspace" style={{ padding: 0 }}>
      <AdminCopilotWorkspace initialRuns={[]} />
    </main>
  );
}

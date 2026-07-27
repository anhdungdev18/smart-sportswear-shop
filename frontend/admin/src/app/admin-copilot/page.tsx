import { AdminCopilotWorkspace } from "@/components/copilot/AdminCopilotWorkspace";

export default function AdminCopilotPage() {
  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Admin Copilot</h1>
          <p>Hoi dap van hanh bang tool read-only, co trace da redact va feedback danh gia.</p>
        </div>
      </section>
      <AdminCopilotWorkspace initialRuns={[]} />
    </main>
  );
}

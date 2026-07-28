import { ChatbotConfigWorkspace } from "@/components/copilot/ChatbotConfigWorkspace";

export default function ChatbotConfigPage() {
  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Cau hinh chatbot</h1>
          <p>Quan sat runtime, tool, role policy, run history va evaluation cua Admin Copilot.</p>
        </div>
      </section>
      <ChatbotConfigWorkspace />
    </main>
  );
}

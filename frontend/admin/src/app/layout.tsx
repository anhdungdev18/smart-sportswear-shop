import type { Metadata } from "next";
import { Bricolage_Grotesque, Plus_Jakarta_Sans } from "next/font/google";
import { AdminTopbar, Sidebar } from "@/components/ui/AdminChrome";
import "./globals.css";

const display = Bricolage_Grotesque({
  subsets: ["latin", "vietnamese"],
  variable: "--font-display",
  weight: ["700", "800"]
});

const body = Plus_Jakarta_Sans({
  subsets: ["latin", "vietnamese"],
  variable: "--font-body",
  weight: ["400", "500", "700", "800"]
});

export const metadata: Metadata = {
  title: "THF Admin",
  description: "Bảng quản trị vận hành Thanh Hùng Futsal"
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="vi">
      <body className={`${display.variable} ${body.variable}`}>
        <div className="admin-shell">
          <Sidebar />
          <div className="content">
            <AdminTopbar />
            {children}
          </div>
        </div>
      </body>
    </html>
  );
}

import type { Metadata } from "next";
import { Montserrat } from "next/font/google";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { NavigationProgress } from "@/components/shared/NavigationProgress";
import { InventoryRealtimeRefresh } from "@/components/shared/InventoryRealtimeRefresh";
import { ScrollToTop } from "@/components/shared/ScrollToTop";
import "./globals.css";

const montserrat = Montserrat({
  variable: "--font-montserrat",
  subsets: ["latin", "vietnamese"],
  weight: ["400", "500", "600", "700"],
});

export const metadata: Metadata = {
  metadataBase: new URL(process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000"),
  title: "Điểm Đến Thể Thao - Đồ Thể Thao Chính Hãng | Điểm Đến Thể Thao",
  description:
    "Điểm Đến Thể Thao - Cửa hàng đồ thể thao chính hãng. Khám phá bộ sưu tập áo đấu, giày, phụ kiện thể thao mới nhất.",
  icons: {
    icon: "/seo/favicon.ico",
  },
  openGraph: {
    title: "Điểm Đến Thể Thao - Đồ Thể Thao Chính Hãng | Điểm Đến Thể Thao",
    url: "/",
    images: ["/seo/og-image.webp"],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" className={`${montserrat.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col">
        <NavigationProgress />
        <ScrollToTop />
        <InventoryRealtimeRefresh />
        <Header />
        {children}
        <Footer />
      </body>
    </html>
  );
}


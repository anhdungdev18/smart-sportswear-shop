import type { Metadata } from "next";
import { Montserrat } from "next/font/google";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { NavigationProgress } from "@/components/shared/NavigationProgress";
import "./globals.css";

const montserrat = Montserrat({
  variable: "--font-montserrat",
  subsets: ["latin", "vietnamese"],
  weight: ["400", "500", "600", "700"],
});

export const metadata: Metadata = {
  metadataBase: new URL(process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000"),
  title: "IVY moda - Thời Trang Công Sở Hiện Đại | IVY moda",
  description:
    "IVY moda - Thời Trang Công Sở Hiện Đại. Khám phá bộ sưu tập thời trang nam nữ mới nhất.",
  icons: {
    icon: "/seo/favicon.ico",
  },
  openGraph: {
    title: "IVY moda - Thời Trang Công Sở Hiện Đại | IVY moda",
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
        <Header />
        {children}
        <Footer />
      </body>
    </html>
  );
}


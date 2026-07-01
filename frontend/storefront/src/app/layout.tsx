import type { Metadata } from "next";
import { Montserrat, Playfair_Display } from "next/font/google";
import { SiteFooter } from "@/components/storefront/SiteFooter";
import { SiteHeader } from "@/components/storefront/SiteHeader";
import "./globals.css";

const displayFont = Playfair_Display({
  subsets: ["latin", "vietnamese"],
  variable: "--font-display",
  weight: ["400", "500", "600", "700", "800"],
});

const bodyFont = Montserrat({
  subsets: ["latin", "latin-ext"],
  variable: "--font-body",
  weight: ["400", "500", "600", "700"],
});

export const metadata: Metadata = {
  title: "Sporta Atelier",
  description:
    "Storefront thời trang thể thao cao cấp với bộ sưu tập quần áo, giày và phụ kiện.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body className={`${displayFont.variable} ${bodyFont.variable}`}>
        <SiteHeader />
        {children}
        <SiteFooter />
      </body>
    </html>
  );
}

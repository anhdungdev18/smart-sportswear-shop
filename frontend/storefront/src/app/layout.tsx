import type { Metadata } from "next";
import { CartProvider } from "@/modules/cart/CartContext";
import { getRequestLanguage } from "@/modules/request-language";
import "./globals.css";

export const dynamic = "force-dynamic";

export async function generateMetadata(): Promise<Metadata> {
  const language = await getRequestLanguage();
  return {
    title: language === "vi" ? "Thanh Hùng Futsal Storefront" : "Thanh Hung Futsal Storefront",
    description: language === "vi"
      ? "Giao diện mua sắm giày đá bóng và futsal chính hãng"
      : "Authentic football boots and futsal shoe shopping experience"
  };
}

export default async function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const language = await getRequestLanguage();

  return (
    <html lang={language}>
      <body>
        <CartProvider>{children}</CartProvider>
      </body>
    </html>
  );
}

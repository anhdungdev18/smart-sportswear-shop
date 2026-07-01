import Image from "next/image";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { Breadcrumb } from "@/components/Breadcrumb";
import { StoreProvinceList } from "@/components/StoreProvinceList";
import { StoreMapPanel } from "@/components/StoreMapPanel";

export default function StoreLocatorPage() {
  return (
    <>
      <Header />
      <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
        <Breadcrumb
          items={[
            { label: "Trang chủ", href: "/" },
            { label: "Danh sách cửa hàng" },
          ]}
        />
        <div className="mx-auto max-w-[1380px] px-4 pb-16">
          <div className="relative mb-10 aspect-[1380/440] overflow-hidden rounded-tl-[80px] rounded-br-[80px]">
            <Image
              src="/images/ivymoda/pages/4cd60af946dee741391dae9b3cc624a1.webp"
              alt="Hệ thống cửa hàng IVY moda"
              fill
              priority
              sizes="(max-width: 1380px) 100vw, 1380px"
              className="object-cover"
            />
          </div>

          <h1 className="mb-8 text-center text-3xl font-semibold tracking-[2px] text-ivy-dark uppercase">
            Hệ thống cửa hàng
          </h1>

          <div className="grid grid-cols-1 gap-10 lg:grid-cols-2">
            <StoreProvinceList />
            <StoreMapPanel />
          </div>
        </div>
      </main>
      <Footer />
    </>
  );
}

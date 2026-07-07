import Image from "next/image";
import { Breadcrumb } from "@/components/shared/Breadcrumb";
import { ContactInfoCards } from "@/components/contact/ContactInfoCards";
import { ContactForm } from "@/components/contact/ContactForm";

export default function ContactPage() {
  return (
    <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
        <Breadcrumb
          items={[
            { label: "Trang chủ", href: "/" },
            { label: "Liên hệ" },
          ]}
        />
        <div className="mx-auto max-w-[1380px] px-4 pb-16">
          <div className="relative mb-10 aspect-[1380/440] overflow-hidden rounded-tl-[80px] rounded-br-[80px]">
            <Image
              src="/images/ivymoda/pages/banner-lien-he.jpg"
              alt="Liên hệ IVY moda"
              fill
              priority
              sizes="(max-width: 1380px) 100vw, 1380px"
              className="object-cover"
            />
          </div>

          <div className="grid grid-cols-1 gap-8 lg:grid-cols-[1fr_1.4fr]">
            <ContactInfoCards />
            <ContactForm />
          </div>

          <div className="mt-16">
            <h2 className="mb-4 text-2xl font-bold text-ivy-dark">
              Find us on Google Maps
            </h2>
            <p className="max-w-2xl text-sm leading-6 text-ivy-text">
              IVY moda là thương hiệu thời trang Việt Nam với mong muốn đem
              lại vẻ đẹp hiện đại và sự tự tin cho khách hàng, thông qua các
              dòng sản phẩm thời trang thể hiện cá tính và xu hướng. Một trong
              những &ldquo;tôn chỉ&rdquo; về thiết kế của IVY moda chính là sự
              đa dạng, với mong muốn mang đến cho người mặc những sản phẩm phù
              hợp nhất với ngoại hình và quan trọng hơn cả là cá tính của
              chính mình.
            </p>
          </div>
        </div>
      </main>
  );
}



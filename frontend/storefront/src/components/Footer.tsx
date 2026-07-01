"use client";

import Image from "next/image";

interface FooterLink {
  label: string;
  href: string;
  external?: boolean;
}

const introLinks: FooterLink[] = [
  { label: "Về IVY moda", href: "/about/gioi-thieu" },
  {
    label: "Tuyển dụng",
    href: "https://tuyendung.ivy.com.vn",
    external: true,
  },
  {
    label: "Hệ thống cửa hàng",
    href: "/page/cuahang",
  },
];

const customerServiceLinks: FooterLink[] = [
  {
    label: "Chính sách điều khoản",
    href: "/about/chinhsach-dieukhoan",
  },
  {
    label: "Hướng dẫn mua hàng",
    href: "/about/huong-dan-mua-hang",
  },
  {
    label: "Chính sách thanh toán",
    href: "/about/chinh-sach-thanh-toan",
  },
  {
    label: "Chính sách đổi trả",
    href: "/about/chinh-sach-doi-tra",
  },
  {
    label: "Chính sách bảo hành",
    href: "/about/chinh-sach-bao-hanh",
  },
  {
    label: "Chính sách thẻ thành viên",
    href: "/about/chinh-sach-the-thanh-vien",
  },
  { label: "Q&A", href: "/about/qa" },
];

const contactLinks: FooterLink[] = [
  { label: "Hotline", href: "tel:02466623434" },
  { label: "Email", href: "mailto:saleadmin@ivy.com.vn" },
  { label: "Live Chat", href: "#" },
  {
    label: "Messenger",
    href: "http://messenger.com/t/thoitrangivymoda",
    external: true,
  },
  { label: "Liên hệ", href: "/lien-he" },
];

const socialLinks = [
  {
    label: "Facebook",
    href: "https://www.facebook.com/thoitrangivymoda/",
    src: "/images/ivymoda/common/ic_fb.svg",
    width: 12,
    height: 24,
  },
  {
    label: "Google",
    href: "/",
    src: "/images/ivymoda/common/ic_gg.svg",
    width: 22,
    height: 22,
  },
  {
    label: "Instagram",
    href: "https://www.instagram.com/ivy_moda/",
    src: "/images/ivymoda/common/ic_instagram.svg",
    width: 28,
    height: 28,
  },
];

function FooterLinkList({ links }: { links: FooterLink[] }) {
  return (
    <ul className="flex flex-col gap-3">
      {links.map((link) => (
        <li key={link.label}>
          <a
            href={link.href}
            {...(link.external
              ? { target: "_blank", rel: "nofollow" }
              : {})}
            className="text-sm text-ivy-text transition-colors hover:text-ivy-dark"
          >
            {link.label}
          </a>
        </li>
      ))}
    </ul>
  );
}

export function Footer() {
  return (
    <footer className="border-t border-ivy-hairline">
      <div className="container mx-auto max-w-[1200px] px-4 py-10">
        <div className="flex flex-col gap-10 lg:flex-row lg:justify-between lg:gap-6">
          {/* Left column */}
          <div className="flex flex-col items-start gap-4 lg:w-1/4">
            <Image
              src="/images/ivymoda/common/logo-footer.png"
              alt="IVY moda"
              width={141}
              height={39}
            />

            <div className="flex items-center gap-3">
              <a
                href="https://www.dmca.com/Protection/Status.aspx?ID=0cfdeac4-6e7f-4fca-941f-57a0a0962777"
                target="_blank"
                rel="nofollow"
              >
                <Image
                  src="/images/ivymoda/common/dmca.png"
                  alt="DMCA Protected"
                  width={121}
                  height={39}
                />
              </a>
              <a
                href="http://online.gov.vn/Home/WebDetails/36596"
                target="_blank"
                rel="nofollow"
              >
                <Image
                  src="/images/ivymoda/common/img-congthuong.png"
                  alt="Đã thông báo Bộ Công Thương"
                  width={107}
                  height={40}
                />
              </a>
            </div>

            <ul className="flex items-center gap-4">
              {socialLinks.map((social) => (
                <li key={social.label}>
                  <a
                    href={social.href}
                    target="_blank"
                    rel="nofollow"
                    aria-label={social.label}
                  >
                    <Image
                      src={social.src}
                      alt={social.label}
                      width={social.width}
                      height={social.height}
                      unoptimized
                      className="h-6 w-auto"
                    />
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Center column */}
          <div className="flex flex-col gap-10 sm:flex-row sm:gap-6 lg:w-1/2">
            <div className="flex flex-col gap-4 sm:flex-1">
              <h3 className="text-2xl font-semibold text-ivy-dark">
                Giới thiệu
              </h3>
              <FooterLinkList links={introLinks} />
            </div>
            <div className="flex flex-col gap-4 sm:flex-1">
              <h3 className="text-2xl font-semibold text-ivy-dark">
                Dịch vụ khách hàng
              </h3>
              <FooterLinkList links={customerServiceLinks} />
            </div>
            <div className="flex flex-col gap-4 sm:flex-1">
              <h3 className="text-2xl font-semibold text-ivy-dark">
                Liên hệ
              </h3>
              <FooterLinkList links={contactLinks} />
            </div>
          </div>

          {/* Right column */}
          <div className="flex flex-col gap-6 lg:w-1/4">
            <div>
              <h3 className="mb-4 text-2xl font-semibold text-ivy-dark">
                Nhận thông tin các chương trình của IVY moda
              </h3>
              <form
                onSubmit={(e) => e.preventDefault()}
                className="flex items-center gap-2"
              >
                <input
                  type="email"
                  placeholder="Nhập địa chỉ email"
                  className="h-10 min-w-0 flex-1 rounded-full border border-ivy-hairline px-4 text-sm text-ivy-text outline-none focus:border-ivy-dark"
                />
                <button
                  type="submit"
                  className="h-10 shrink-0 rounded-full border border-ivy-dark px-5 text-sm font-medium text-ivy-dark transition-colors hover:bg-ivy-dark hover:text-white"
                >
                  Đăng ký
                </button>
              </form>
            </div>

            <div>
              <h3 className="mb-4 text-2xl font-semibold text-ivy-dark">
                Download App
              </h3>
              <div className="flex flex-col gap-2">
                <a href="#" className="w-fit">
                  <Image
                    src="/images/ivymoda/common/appstore.png"
                    alt="Download on the App Store"
                    width={178}
                    height={51}
                  />
                </a>
                <a href="#" className="w-fit">
                  <Image
                    src="/images/ivymoda/common/googleplay.png"
                    alt="Get it on Google Play"
                    width={180}
                    height={52}
                  />
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="border-t border-ivy-hairline py-5">
        <p className="text-center text-sm text-ivy-text">
          ©IVYmoda All rights reserved
        </p>
      </div>
    </footer>
  );
}

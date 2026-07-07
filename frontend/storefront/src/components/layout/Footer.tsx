"use client";

import Image from "next/image";
import {
  FOOTER_CONTACT_LINKS,
  FOOTER_CUSTOMER_SERVICE_LINKS,
  FOOTER_INTRO_LINKS,
  FOOTER_SOCIAL_LINKS,
} from "@/modules/content/data/layout";
import type { FooterLinkItem } from "@/modules/content/types";

function FooterLinkList({ links }: { links: FooterLinkItem[] }) {
  return (
    <ul className="flex flex-col gap-2.5">
      {links.map((link) => (
        <li key={link.label}>
          <a
            href={link.href}
            {...(link.external ? { target: "_blank", rel: "nofollow" } : {})}
            className="text-[14px] leading-6 text-ivy-text transition-colors hover:text-ivy-dark"
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
      <div className="container mx-auto max-w-[1368px] px-4 py-12 md:px-0">
        <div className="grid gap-10 lg:grid-cols-[220px_1fr_360px] lg:gap-8">
          <div className="flex flex-col items-start gap-4">
            <Image src="/images/ivymoda/common/logo-johns.png" alt="John's Sport Shop" width={141} height={70} />

            <div className="flex items-center gap-3">
              <a href="https://www.dmca.com/Protection/Status.aspx?ID=0cfdeac4-6e7f-4fca-941f-57a0a0962777" target="_blank" rel="nofollow">
                <Image src="/images/ivymoda/common/dmca.png" alt="DMCA Protected" width={121} height={39} />
              </a>
              <a href="http://online.gov.vn/Home/WebDetails/36596" target="_blank" rel="nofollow">
                <Image src="/images/ivymoda/common/img-congthuong.png" alt="Đã thông báo Bộ Công Thương" width={107} height={40} />
              </a>
            </div>

            <ul className="flex items-center gap-4">
              {FOOTER_SOCIAL_LINKS.map((social) => (
                <li key={social.label}>
                  <a href={social.href} target="_blank" rel="nofollow" aria-label={social.label}>
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

          <div className="grid gap-8 sm:grid-cols-3">
            <div className="flex flex-col gap-4 sm:flex-1">
              <h3 className="text-[24px] leading-[1.2] font-semibold text-ivy-dark">Giới thiệu</h3>
              <FooterLinkList links={FOOTER_INTRO_LINKS} />
            </div>
            <div className="flex flex-col gap-4 sm:flex-1">
              <h3 className="text-[24px] leading-[1.2] font-semibold text-ivy-dark">Dịch vụ khách hàng</h3>
              <FooterLinkList links={FOOTER_CUSTOMER_SERVICE_LINKS} />
            </div>
            <div className="flex flex-col gap-4 sm:flex-1">
              <h3 className="text-[24px] leading-[1.2] font-semibold text-ivy-dark">Liên hệ</h3>
              <FooterLinkList links={FOOTER_CONTACT_LINKS} />
            </div>
          </div>

          <div className="flex flex-col gap-6">
            <div>
              <h3 className="mb-4 text-[30px] leading-[1.15] font-semibold text-ivy-dark">
                Nhận thông tin các chương trình của IVY moda
              </h3>
              <form onSubmit={(e) => e.preventDefault()} className="flex items-center gap-2">
                <input
                  type="email"
                  placeholder="Nhập địa chỉ email"
                  className="h-10 min-w-0 flex-1 rounded-full border border-ivy-hairline px-4 text-[14px] text-ivy-text outline-none focus:border-ivy-dark"
                />
                <button
                  type="submit"
                  className="h-10 shrink-0 rounded-full border border-ivy-dark px-5 text-[14px] font-medium text-ivy-dark transition-colors hover:bg-ivy-dark hover:text-white"
                >
                  Đăng ký
                </button>
              </form>
            </div>

            <div>
              <h3 className="mb-4 text-[30px] leading-[1.15] font-semibold text-ivy-dark">Download App</h3>
              <div className="flex flex-col gap-2">
                <a href="#" className="w-fit">
                  <Image src="/images/ivymoda/common/appstore.png" alt="Download on the App Store" width={178} height={51} />
                </a>
                <a href="#" className="w-fit">
                  <Image src="/images/ivymoda/common/googleplay.png" alt="Get it on Google Play" width={180} height={52} />
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="border-t border-ivy-hairline py-5">
        <p className="text-center text-[13px] text-ivy-text">©IVYmoda All rights reserved</p>
      </div>
    </footer>
  );
}

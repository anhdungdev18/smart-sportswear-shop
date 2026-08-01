import Image from "next/image";
import Link from "next/link";
import { SiteHeaderNav } from "@/components/layout/SiteHeaderNav";
import { HeaderActionsLive } from "@/components/layout/HeaderActionsLive";
import { SearchIcon } from "@/components/shared/icons";
import { buildHeaderNavItems } from "@/modules/category/navigation";
import { fetchCategoryTree } from "@/modules/category/queries";
import { VisualSearchDialog } from "@/modules/visual-search/VisualSearchDialog";

export async function Header() {
  const navigationItems = buildHeaderNavItems(await fetchCategoryTree());

  return (
    <header className="fixed inset-x-0 top-0 z-40 bg-white">
      <div className="h-[var(--site-header-main-height)] border-b border-ivy-hairline px-8 md:px-12 lg:px-14">
        <div className="grid h-full grid-cols-[1fr_auto_1fr] items-center gap-4">
          <Link href="/" className="flex items-center">
            <Image
              src="/images/logo-v2.png"
              alt="Điểm Đến Thể Thao"
              width={120}
              height={80}
              priority
              className="h-18 w-auto object-contain"
            />
          </Link>
          <form
            action="/tim-kiem"
            method="get"
            className="flex h-9.5 w-120 items-center gap-2 rounded-lg border border-ivy-hairline bg-white px-4 text-[12px] text-ivy-text lg:w-150"
          >
            <input
              name="q"
              type="text"
              placeholder="TÌM KIẾM SẢN PHẨM"
              className="min-w-0 flex-1 bg-transparent text-[12px] tracking-[0.01em] text-ivy-text placeholder:text-[#8b8c91] outline-none"
            />
            <button type="submit" className="flex items-center justify-center text-ivy-dark" aria-label="Tìm kiếm">
              <SearchIcon className="size-3.75" />
            </button>
            <span className="h-5 w-px bg-ivy-hairline" aria-hidden="true" />
            <VisualSearchDialog />
          </form>
          <div className="flex justify-end">
            <HeaderActionsLive />
          </div>
        </div>
      </div>
      <div className="h-[var(--site-header-nav-height)] border-b border-ivy-hairline px-8 md:px-12 lg:px-14">
        <div className="flex h-full items-center">
          <SiteHeaderNav items={navigationItems} />
        </div>
      </div>
    </header>
  );
}


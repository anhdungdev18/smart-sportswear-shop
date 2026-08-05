import Image from "next/image";
import Link from "next/link";
import { SiteHeaderNav } from "@/components/layout/SiteHeaderNav";
import { HeaderActionsLive } from "@/components/layout/HeaderActionsLive";
import { HeaderSearch } from "@/components/layout/HeaderSearch";
import { buildHeaderNavItems } from "@/modules/category/navigation";
import { fetchCategoryTree } from "@/modules/category/queries";

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
          <HeaderSearch />
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


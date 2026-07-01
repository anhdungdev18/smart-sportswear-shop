import { SiteHeaderNav } from "@/components/SiteHeaderNav";
import { HeaderActions } from "@/components/HeaderActions";

export function Header() {
  return (
    <header className="fixed inset-x-0 top-0 z-20 border-b border-ivy-hairline bg-white">
      <div className="relative mx-auto max-w-[1380px] px-4">
        <SiteHeaderNav />
        <div className="absolute right-4 top-1/2 -translate-y-1/2">
          <HeaderActions />
        </div>
      </div>
    </header>
  );
}

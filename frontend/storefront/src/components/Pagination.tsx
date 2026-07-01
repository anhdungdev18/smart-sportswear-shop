import Link from "next/link";
import { cn } from "@/lib/utils";

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  baseHref: string;
}

function pageHref(baseHref: string, page: number): string {
  return page <= 1 ? baseHref : `${baseHref}?page=${page}`;
}

const WINDOW_RADIUS = 2;

export function Pagination({
  currentPage,
  totalPages,
  baseHref,
}: PaginationProps) {
  const prevPage = Math.max(1, currentPage - 1);
  const nextPage = Math.min(totalPages, currentPage + 1);
  const isFirstPage = currentPage <= 1;
  const isLastPage = currentPage >= totalPages;

  const windowStart = Math.max(1, currentPage - WINDOW_RADIUS);
  const windowEnd = Math.min(totalPages, currentPage + WINDOW_RADIUS);
  const pages = Array.from(
    { length: windowEnd - windowStart + 1 },
    (_, i) => windowStart + i
  );

  const itemClass =
    "h-8 min-w-8 rounded-tl-lg rounded-br-lg border border-ivy-hairline bg-white text-center text-xs leading-[30px] text-ivy-text-muted";
  const linkClass = "block min-w-8";

  return (
    <ul className="flex list-none items-center justify-center pl-0">
      <li className={cn(itemClass, "mr-[17px]")}>
        <Link
          href={pageHref(baseHref, prevPage)}
          aria-label="Trang trước"
          aria-disabled={isFirstPage}
          className={cn(linkClass, isFirstPage && "pointer-events-none opacity-40")}
        >
          «
        </Link>
      </li>

      {pages.map((page) => {
        const isActive = page === currentPage;
        const isAdjacent = Math.abs(page - currentPage) <= 1;
        return (
          <li
            key={page}
            className={cn(
              itemClass,
              "mr-3",
              isActive && "border-ivy-dark bg-ivy-dark text-white",
              !isActive && !isAdjacent && "hidden sm:inline-block"
            )}
          >
            <Link href={pageHref(baseHref, page)} className={linkClass}>
              {page}
            </Link>
          </li>
        );
      })}

      <li className={cn(itemClass, "mr-3")}>
        <Link
          href={pageHref(baseHref, nextPage)}
          aria-label="Trang sau"
          aria-disabled={isLastPage}
          className={cn(linkClass, isLastPage && "pointer-events-none opacity-40")}
        >
          »
        </Link>
      </li>

      <li className={cn(itemClass, "last-page mr-3 px-3")}>
        <Link href={pageHref(baseHref, totalPages)} className={linkClass}>
          Trang cuối
        </Link>
      </li>
    </ul>
  );
}

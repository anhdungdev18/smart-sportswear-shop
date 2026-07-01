import { cn } from "@/lib/utils";

interface BreadcrumbProps {
  items: { label: string; href?: string }[];
}

export function Breadcrumb({ items }: BreadcrumbProps) {
  return (
    <nav aria-label="Breadcrumb" className="mx-auto max-w-[1380px] px-4">
      <ol className="flex list-none items-center py-3 text-[13px]">
        {items.map((item, index) => {
          const isLast = index === items.length - 1;

          return (
            <li
              key={item.label}
              className={cn(
                "flex items-center",
                index > 0 &&
                  "before:mx-2 before:text-ivy-text-muted before:content-['›']"
              )}
            >
              {!isLast && item.href ? (
                <a
                  href={item.href}
                  className={cn(
                    index === 0
                      ? "text-ivy-text-muted hover:text-ivy-dark"
                      : "text-ivy-dark"
                  )}
                >
                  {item.label}
                </a>
              ) : (
                <span
                  className={index === 0 ? "text-ivy-text-muted" : "text-ivy-dark"}
                >
                  {item.label}
                </span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}

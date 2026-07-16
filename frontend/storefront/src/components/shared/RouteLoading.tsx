"use client";

type RouteLoadingVariant = "default" | "category" | "product" | "collection" | "lookbook" | "editorial" | "article" | "search";

type RouteLoadingProps = {
  variant?: RouteLoadingVariant;
};

function PulseBlock({ className }: { className: string }) {
  return <div className={`animate-pulse rounded bg-[#f1f2f4] ${className}`} />;
}

// ─── Existing variants ────────────────────────────────────────────────────────

function CategoryLoading() {
  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1368px] px-4 pb-16 md:px-0">
        <PulseBlock className="mt-6 h-4 w-52" />
        <div className="mt-8 flex flex-col gap-8 lg:flex-row">
          <aside className="w-full lg:w-64 lg:shrink-0">
            <div className="rounded-2xl border border-ivy-hairline bg-white p-5">
              <PulseBlock className="h-6 w-28" />
              <div className="mt-6 space-y-5">
                {Array.from({ length: 4 }).map((_, i) => (
                  <div key={i}>
                    <PulseBlock className="h-4 w-24" />
                    <div className="mt-3 flex flex-wrap gap-2">
                      {Array.from({ length: i === 0 ? 5 : 6 }).map((__, j) => (
                        <PulseBlock key={j} className="h-9 w-12 rounded-md" />
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </aside>
          <div className="flex-1">
            <div className="mb-8 border-b border-ivy-hairline pb-5">
              <PulseBlock className="h-10 w-44" />
              <PulseBlock className="mt-3 h-4 w-28" />
            </div>
            <div className="grid grid-cols-2 gap-x-7 gap-y-12 sm:grid-cols-3 lg:grid-cols-4">
              {Array.from({ length: 8 }).map((_, i) => (
                <div key={i}>
                  <PulseBlock className="aspect-[0.72] w-full" />
                  <PulseBlock className="mt-4 h-4 w-3/4" />
                  <PulseBlock className="mt-3 h-6 w-1/2" />
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

function ProductLoading() {
  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1368px] px-4 pb-16 md:px-0">
        <PulseBlock className="mt-6 h-4 w-48" />
        <div className="mt-8 grid grid-cols-1 gap-10 lg:grid-cols-[620px_minmax(0,1fr)] lg:gap-12">
          <div className="grid grid-cols-[96px_minmax(0,1fr)] gap-4">
            <div className="space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <PulseBlock key={i} className="aspect-[0.8] w-full" />
              ))}
            </div>
            <PulseBlock className="aspect-[0.78] w-full" />
          </div>
          <div>
            <PulseBlock className="h-10 w-4/5" />
            <PulseBlock className="mt-4 h-5 w-32" />
            <PulseBlock className="mt-6 h-8 w-40" />
            <div className="mt-8">
              <PulseBlock className="h-4 w-20" />
              <div className="mt-3 flex gap-3">
                {Array.from({ length: 4 }).map((_, i) => (
                  <PulseBlock key={i} className="h-10 w-10 rounded-full" />
                ))}
              </div>
            </div>
            <div className="mt-8">
              <PulseBlock className="h-4 w-16" />
              <div className="mt-3 flex flex-wrap gap-2">
                {Array.from({ length: 5 }).map((_, i) => (
                  <PulseBlock key={i} className="h-10 w-12 rounded-md" />
                ))}
              </div>
            </div>
            <div className="mt-10 flex gap-4">
              <PulseBlock className="h-12 flex-1 rounded-full" />
              <PulseBlock className="h-12 flex-1 rounded-full" />
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

function DefaultLoading() {
  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1368px] px-4 pb-16 md:px-0">
        <PulseBlock className="mt-10 h-[440px] w-full rounded-[32px]" />
        <div className="mt-14">
          <div className="mx-auto w-fit">
            <PulseBlock className="mx-auto h-10 w-64" />
            <PulseBlock className="mx-auto mt-5 h-4 w-44" />
          </div>
          <div className="mt-10 grid grid-cols-2 gap-7 lg:grid-cols-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i}>
                <PulseBlock className="aspect-[0.72] w-full" />
                <PulseBlock className="mt-4 h-4 w-3/4" />
                <PulseBlock className="mt-3 h-6 w-1/2" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </main>
  );
}

// ─── New domain-specific variants ────────────────────────────────────────────

function CollectionLoading() {
  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1368px] px-4 pb-16 md:px-0">
        <PulseBlock className="mt-6 h-4 w-48" />
        <PulseBlock className="mx-auto mt-10 h-6 w-36" />
        <div className="mt-10 grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i}>
              <PulseBlock className="aspect-[4/3] w-full rounded-xl" />
              <PulseBlock className="mt-4 h-5 w-3/4" />
              <PulseBlock className="mt-2 h-4 w-1/2" />
            </div>
          ))}
        </div>
      </div>
    </main>
  );
}

function LookbookLoading() {
  return (
    <main className="site-main page-below-header flex-1">
      {/* Hero banner */}
      <PulseBlock className="h-[60vh] min-h-[400px] w-full" />
      <div className="mx-auto max-w-[1368px] px-4 pb-16 md:px-0">
        {/* Title + description */}
        <div className="mx-auto mt-12 max-w-[640px] text-center">
          <PulseBlock className="mx-auto h-4 w-24" />
          <PulseBlock className="mx-auto mt-4 h-8 w-80" />
          <PulseBlock className="mx-auto mt-4 h-4 w-full" />
          <PulseBlock className="mx-auto mt-2 h-4 w-5/6" />
        </div>
        {/* Editorial image pair */}
        <div className="mt-10 grid grid-cols-1 gap-6 md:grid-cols-2">
          <PulseBlock className="aspect-[4/3] w-full" />
          <PulseBlock className="aspect-[4/3] w-full" />
        </div>
        {/* Products */}
        <PulseBlock className="mx-auto mt-14 h-7 w-48" />
        <div className="mt-8 grid grid-cols-2 gap-6 lg:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i}>
              <PulseBlock className="aspect-[0.72] w-full" />
              <PulseBlock className="mt-4 h-4 w-3/4" />
              <PulseBlock className="mt-2 h-5 w-1/2" />
            </div>
          ))}
        </div>
      </div>
    </main>
  );
}

function EditorialLoading() {
  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1368px] px-4 pb-16 md:px-0">
        <PulseBlock className="mt-6 h-4 w-40" />
        {/* LIFESTYLE nav */}
        <div className="mt-10 flex justify-center gap-8 border-b border-ivy-hairline pb-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <PulseBlock key={i} className="h-4 w-16" />
          ))}
        </div>
        {/* Featured story cards */}
        {Array.from({ length: 2 }).map((_, i) => (
          <div
            key={i}
            className="mt-6 flex min-h-[280px] overflow-hidden rounded-tr-[80px] rounded-bl-[80px] border border-ivy-hairline"
          >
            <PulseBlock className="min-h-[280px] flex-1" />
            <div className="flex flex-1 flex-col justify-center gap-4 p-8">
              <PulseBlock className="h-3 w-16" />
              <PulseBlock className="h-7 w-full" />
              <PulseBlock className="h-7 w-4/5" />
              <PulseBlock className="h-4 w-full" />
              <PulseBlock className="h-4 w-3/4" />
              <PulseBlock className="h-3 w-20" />
            </div>
          </div>
        ))}
        {/* Magazine grid */}
        <div className="mt-10 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i}>
              <PulseBlock className="aspect-[4/3] w-full" />
              <PulseBlock className="mt-3 h-4 w-full" />
              <PulseBlock className="mt-2 h-5 w-5/6" />
              <PulseBlock className="mt-2 h-3 w-24" />
            </div>
          ))}
        </div>
      </div>
    </main>
  );
}

function ArticleLoading() {
  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1368px] px-4 pb-16 md:px-0">
        <PulseBlock className="mt-6 h-4 w-64" />
        <div className="mt-8 grid grid-cols-1 gap-10 lg:grid-cols-[220px_1fr_280px]">
          {/* Left sidebar */}
          <aside className="hidden lg:block">
            <PulseBlock className="h-5 w-28" />
            <div className="mt-4 space-y-3">
              {Array.from({ length: 5 }).map((_, i) => (
                <PulseBlock key={i} className="h-10 w-full" />
              ))}
            </div>
          </aside>
          {/* Main article */}
          <div>
            <PulseBlock className="h-9 w-full" />
            <PulseBlock className="mt-1 h-9 w-4/5" />
            <PulseBlock className="mt-3 h-4 w-24" />
            <PulseBlock className="mt-6 aspect-[16/9] w-full" />
            <div className="mt-6 space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <PulseBlock key={i} className="h-4 w-full" />
              ))}
            </div>
            <PulseBlock className="mt-6 aspect-[4/3] w-full" />
            <div className="mt-6 space-y-3">
              {Array.from({ length: 3 }).map((_, i) => (
                <PulseBlock key={i} className="h-4 w-full" />
              ))}
            </div>
          </div>
          {/* Right sidebar */}
          <aside className="hidden lg:block">
            <PulseBlock className="h-5 w-28" />
            <div className="mt-4 space-y-4">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="flex gap-3">
                  <PulseBlock className="h-20 w-20 shrink-0" />
                  <div className="flex-1 space-y-2">
                    <PulseBlock className="h-4 w-full" />
                    <PulseBlock className="h-4 w-3/4" />
                    <PulseBlock className="h-3 w-16" />
                  </div>
                </div>
              ))}
            </div>
            <PulseBlock className="mt-6 aspect-[4/5] w-full rounded-tr-3xl rounded-bl-3xl" />
          </aside>
        </div>
      </div>
    </main>
  );
}

function SearchLoading() {
  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1368px] px-4 pb-16 md:px-0">
        <PulseBlock className="mt-6 h-4 w-40" />
        <div className="mt-6 flex items-center gap-4">
          <PulseBlock className="h-10 flex-1 rounded-full" />
        </div>
        <div className="mt-6 flex items-center justify-between border-b border-ivy-hairline pb-4">
          <PulseBlock className="h-4 w-36" />
          <PulseBlock className="h-9 w-32 rounded-lg" />
        </div>
        <div className="mt-8 grid grid-cols-2 gap-x-7 gap-y-12 sm:grid-cols-3 lg:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i}>
              <PulseBlock className="aspect-[0.72] w-full" />
              <PulseBlock className="mt-4 h-4 w-3/4" />
              <PulseBlock className="mt-3 h-6 w-1/2" />
            </div>
          ))}
        </div>
      </div>
    </main>
  );
}

// ─── Dispatcher ───────────────────────────────────────────────────────────────

export function RouteLoading({ variant = "default" }: RouteLoadingProps) {
  if (variant === "category") return <CategoryLoading />;
  if (variant === "product") return <ProductLoading />;
  if (variant === "collection") return <CollectionLoading />;
  if (variant === "lookbook") return <LookbookLoading />;
  if (variant === "editorial") return <EditorialLoading />;
  if (variant === "article") return <ArticleLoading />;
  if (variant === "search") return <SearchLoading />;
  return <DefaultLoading />;
}

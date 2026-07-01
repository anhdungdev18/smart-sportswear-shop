import Image from "next/image";
import { Share2 } from "lucide-react";
import { cn } from "@/lib/utils";

interface ArticleContentBlock {
  type: "text" | "image";
  text?: string;
  src?: string;
}

interface ArticleContentProps {
  title: string;
  date: string;
  blocks: ArticleContentBlock[];
  tags: { label: string; href: string }[];
}

const SHARE_LABELS = [
  "Chia sẻ qua Facebook",
  "Chia sẻ qua Twitter",
  "Chia sẻ qua Pinterest",
  "Chia sẻ qua Zalo",
  "Sao chép liên kết",
];

export function ArticleContent({ title, date, blocks, tags }: ArticleContentProps) {
  return (
    <article className="w-full">
      <header>
        <h1 className="mb-3 text-left text-[26px] leading-[34px] font-bold uppercase text-ivy-dark">
          {title}
        </h1>
        <div className="mb-6 text-[13px] text-[#A8A9AD]">{date}</div>
      </header>

      <div className="mx-auto w-full max-w-full">
        {blocks.map((block, index) => {
          if (block.type === "image" && block.src) {
            return (
              <Image
                key={index}
                src={block.src}
                alt={title}
                width={800}
                height={600}
                sizes="(min-width: 1024px) 700px, 100vw"
                className="my-4 block h-auto w-full rounded"
              />
            );
          }

          const isCaption =
            index > 0 &&
            blocks[index - 1]?.type === "image" &&
            (block.text?.length ?? 0) < 120;

          return (
            <p
              key={index}
              className={cn(
                isCaption
                  ? "mb-4 text-center text-sm italic text-ivy-text-muted"
                  : "mb-4 text-[15px] leading-[26px] text-ivy-text"
              )}
            >
              {block.text}
            </p>
          );
        })}
      </div>

      <div className="mt-6 flex flex-col gap-4 border-t border-ivy-hairline pt-6 md:flex-row md:justify-between">
        <div className="flex flex-wrap gap-2">
          {tags.map((tag) => (
            <a
              key={tag.href}
              href={tag.href}
              className="inline-block rounded-full border border-ivy-hairline px-3.5 py-1.5 text-xs text-ivy-text transition-colors hover:border-ivy-dark hover:text-ivy-dark"
            >
              {tag.label}
            </a>
          ))}
        </div>

        <div className="flex gap-3">
          {SHARE_LABELS.map((label) => (
            <a
              key={label}
              href="#"
              aria-label={label}
              className="flex h-8 w-8 items-center justify-center rounded-full bg-[#F7F8F9] text-ivy-text transition-colors hover:bg-ivy-dark hover:text-white"
            >
              <Share2 className="h-4 w-4" />
            </a>
          ))}
        </div>
      </div>
    </article>
  );
}

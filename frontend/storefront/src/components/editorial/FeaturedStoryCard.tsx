import Image from "next/image";
import Link from "next/link";
import { cn } from "@/lib/utils";

interface FeaturedStoryCardProps {
  image: string;
  eyebrow: string;
  title: string;
  excerpt: string;
  date: string;
  href: string;
  reverse?: boolean;
}

export function FeaturedStoryCard({
  image,
  eyebrow,
  title,
  excerpt,
  date,
  href,
  reverse = false,
}: FeaturedStoryCardProps) {
  return (
    <div
      className={cn(
        "mb-6 flex min-h-[320px] flex-col overflow-hidden rounded-tr-[80px] rounded-bl-[80px] border border-ivy-hairline md:flex-row",
        reverse && "md:flex-row-reverse"
      )}
    >
      <div className="relative min-h-[240px] flex-1 md:min-h-[320px]">
        <Image
          src={image}
          alt={title}
          fill
          sizes="(max-width: 768px) 100vw, 50vw"
          className="object-cover"
        />
      </div>

      <div className="flex flex-1 flex-col justify-center p-6 md:p-10">
        <h3 className="mb-3 text-xs font-semibold uppercase tracking-[2px] text-ivy-text-muted">
          {eyebrow}
        </h3>
        <h2 className="mb-4 block text-[22px] leading-[30px] font-bold text-ivy-dark">
          <Link href={href}>{title}</Link>
        </h2>
        <p className="mb-4 text-sm leading-[22px] text-ivy-text">{excerpt}</p>
        <div className="mb-4 text-[13px] text-[#A8A9AD]">{date}</div>
        <div>
          <Link
            href={href}
            className="text-[13px] font-semibold tracking-[1px] text-ivy-dark underline"
          >
            XEM NGAY
          </Link>
        </div>
      </div>
    </div>
  );
}

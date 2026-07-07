import Image from "next/image";

interface LookbookIntroProps {
  bannerImage: string;
  eyebrow: string;
  title: string;
  description: string;
  editorialImages: [string, string];
}

export function LookbookIntro({
  bannerImage,
  eyebrow,
  title,
  description,
  editorialImages,
}: LookbookIntroProps) {
  return (
    <>
      <section className="mx-auto mb-10 max-w-[1380px] px-4 md:px-0">
        <div className="relative aspect-[1380/549] overflow-hidden rounded-tl-[80px] rounded-br-[80px]">
          <Image
            src={bannerImage}
            alt={title}
            fill
            priority
            sizes="(max-width: 768px) 100vw, 1380px"
            className="object-cover"
          />
        </div>
      </section>

      <section className="mx-auto max-w-[1380px] px-4 md:px-0">
        <div className="mx-auto mb-10 max-w-[700px] text-center">
          <h4 className="mb-2 text-sm font-semibold tracking-[2px] uppercase text-ivy-text-muted">
            {eyebrow}
          </h4>
          <h3 className="mb-4 text-2xl font-bold tracking-[2px] text-ivy-dark md:text-[32px]">
            {title}
          </h3>
          <p className="text-sm leading-6 text-ivy-text">{description}</p>
        </div>

        <div className="mb-10 flex flex-col gap-6 md:flex-row">
          {editorialImages.map((image, index) => (
            <div key={image} className="relative flex-1 aspect-[4/3]">
              <Image
                src={image}
                alt={`${title} editorial ${index + 1}`}
                fill
                sizes="(max-width: 768px) 100vw, 690px"
                className="object-cover"
              />
            </div>
          ))}
        </div>
      </section>
    </>
  );
}

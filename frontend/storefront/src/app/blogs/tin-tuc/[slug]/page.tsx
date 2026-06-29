import { notFound } from "next/navigation";
import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { blogDetails, getBlogDetail, getLocalizedBlogPost } from "@/modules/catalog/mockContent";
import { blogCopy, commonPageCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

type BlogDetailPageProps = {
  params: Promise<{ slug: string }>;
};

export function generateStaticParams() {
  return blogDetails.map((post) => ({ slug: post.slug }));
}

export async function generateMetadata({ params }: BlogDetailPageProps) {
  const language = await getRequestLanguage();
  const { slug } = await params;
  const post = getBlogDetail(slug);
  const localizedPost = post ? getLocalizedBlogPost(post, language) : null;

  return {
    title: localizedPost ? `${localizedPost.displayTitle} | Thanh Hung Futsal` : commonPageCopy[language].notFoundPost
  };
}

export default async function BlogDetailPage({ params }: BlogDetailPageProps) {
  const language = await getRequestLanguage();
  const t = blogCopy[language];
  const common = commonPageCopy[language];
  const { slug } = await params;
  const post = getBlogDetail(slug);

  if (!post) {
    notFound();
  }

  const localizedPost = getLocalizedBlogPost(post, language);

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <div className="breadcrumb-wrap">
          <div className="shell breadcrumb">
            <a href="/">{common.home}</a>
            <span>/</span>
            <a href="/blogs/tin-tuc">{t.breadcrumb}</a>
            <span>/</span>
            <span>{localizedPost.displayTitle}</span>
          </div>
        </div>

        <article className="shell article-detail">
          <img src={post.image} alt={localizedPost.displayTitle} />
          <div className="article-body">
            <span className="promo-kicker">{t.kicker}</span>
            <h1>{localizedPost.displayTitle}</h1>
            <p className="article-lead">{localizedPost.displayExcerpt}</p>
            {localizedPost.displaySections.map((section, index) => (
              <section key={section}>
                <h2>{index + 1}. {t.noteHeading}</h2>
                <p>{section}</p>
              </section>
            ))}
          </div>
        </article>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

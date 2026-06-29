import { CalendarBlank } from "@phosphor-icons/react/dist/ssr";
import { FloatingActions, StorefrontFooter, StorefrontHeader } from "@/components/ui/StorefrontChrome";
import { blogDetails, getLocalizedBlogPost } from "@/modules/catalog/mockContent";
import { blogCopy, commonPageCopy } from "@/modules/i18n";
import { getRequestLanguage } from "@/modules/request-language";

export async function generateMetadata() {
  const language = await getRequestLanguage();
  return {
    title: blogCopy[language].metadataTitle
  };
}

export default async function BlogPage() {
  const language = await getRequestLanguage();
  const t = blogCopy[language];
  const common = commonPageCopy[language];

  return (
    <>
      <StorefrontHeader initialLanguage={language} />
      <main>
        <div className="breadcrumb-wrap">
          <div className="shell breadcrumb">
            <a href="/">{common.home}</a>
            <span>/</span>
            <span>{t.breadcrumb}</span>
          </div>
        </div>

        <section className="shell collection-head">
          <h1>{t.title}</h1>
          <p>{t.intro}</p>
        </section>

        <section className="shell blog-list-page">
          {blogDetails.map((sourcePost) => {
            const post = getLocalizedBlogPost(sourcePost, language);

            return (
              <article className="blog-card article-card" key={post.slug}>
                <img src={post.image} alt={post.displayTitle} />
                <div>
                  <span>
                    <CalendarBlank size={15} />
                    28/06/2026
                  </span>
                  <h2>{post.displayTitle}</h2>
                  <p>{post.displayExcerpt}</p>
                  <a href={`/blogs/tin-tuc/${post.slug}`}>{common.readMore}</a>
                </div>
              </article>
            );
          })}
        </section>
      </main>
      <FloatingActions initialLanguage={language} />
      <StorefrontFooter initialLanguage={language} />
    </>
  );
}

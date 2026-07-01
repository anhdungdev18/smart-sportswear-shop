export type PageResponse = {
  id: string;
  title: string;
  slug: string;
  summary: string | null;
  contentHtml: string;
  status: string;
  publishedAt: string | null;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
};

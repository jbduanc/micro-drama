import type { MetadataRoute } from "next";
import { fetchDramaIdsForSitemap } from "@/lib/api/drama";

const siteUrl =
  process.env.NEXT_PUBLIC_SITE_URL?.replace(/\/$/, "") ??
  "http://localhost:3000";

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const staticRoutes: MetadataRoute.Sitemap = [
    {
      url: `${siteUrl}/dramas`,
      changeFrequency: "daily",
      priority: 1,
    },
  ];

  try {
    const ids = await fetchDramaIdsForSitemap();
    const dramaRoutes: MetadataRoute.Sitemap = ids.map((id) => ({
      url: `${siteUrl}/dramas/${id}`,
      changeFrequency: "weekly",
      priority: 0.8,
    }));
    return [...staticRoutes, ...dramaRoutes];
  } catch {
    return staticRoutes;
  }
}

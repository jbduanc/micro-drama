import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { DramaDetailClient } from "@/components/drama/DramaDetailClient";
import { fetchDramaDetail } from "@/lib/api/drama";

type Props = {
  params: Promise<{ id: string }>;
};

const siteUrl =
  process.env.NEXT_PUBLIC_SITE_URL?.replace(/\/$/, "") ??
  "http://localhost:3000";

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { id } = await params;
  const drama = await fetchDramaDetail(id);

  if (!drama) {
    return { title: "短剧不存在" };
  }

  const title = drama.title ?? "短剧详情";
  const description =
    drama.description?.slice(0, 160) ||
    `${title}，共 ${drama.totalEpisodes ?? 0} 集，在线观看微短剧。`;

  return {
    title,
    description,
    openGraph: {
      title,
      description,
      type: "video.other",
      url: `${siteUrl}/dramas/${id}`,
      images: drama.coverUrl ? [{ url: drama.coverUrl }] : undefined,
    },
    alternates: {
      canonical: `${siteUrl}/dramas/${id}`,
    },
    robots: {
      index: true,
      follow: true,
    },
  };
}

export default async function DramaDetailPage({ params }: Props) {
  const { id } = await params;
  const drama = await fetchDramaDetail(id);

  if (!drama) notFound();

  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "TVSeries",
    name: drama.title,
    description: drama.description,
    image: drama.coverUrl,
    numberOfEpisodes: drama.totalEpisodes,
    url: `${siteUrl}/dramas/${id}`,
  };

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <DramaDetailClient drama={drama} />
    </>
  );
}

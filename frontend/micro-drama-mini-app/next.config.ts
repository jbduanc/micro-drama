import type { NextConfig } from "next";

const contentTarget =
  process.env.CONTENT_API_TARGET ?? "http://127.0.0.1:6002";
const videoTarget = process.env.VIDEO_API_TARGET ?? "http://127.0.0.1:8080";
const paymentTarget =
  process.env.PAYMENT_API_TARGET ?? "http://127.0.0.1:8081";

const nextConfig: NextConfig = {
  images: {
    remotePatterns: [
      { protocol: "https", hostname: "**" },
      { protocol: "http", hostname: "**" },
    ],
  },
  async rewrites() {
    return [
      {
        source: "/content-api/:path*",
        destination: `${contentTarget}/:path*`,
      },
      {
        source: "/video-api/:path*",
        destination: `${videoTarget}/:path*`,
      },
      {
        source: "/payment-api/:path*",
        destination: `${paymentTarget}/:path*`,
      },
    ];
  },
};

export default nextConfig;

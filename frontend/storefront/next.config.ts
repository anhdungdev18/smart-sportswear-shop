import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Allows CI/build verification to avoid colliding with a running dev server's .next directory.
  distDir: process.env.NEXT_DIST_DIR ?? ".next",
  output: "standalone",
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "res.cloudinary.com",
      },
      {
        protocol: "https",
        hostname: "pubcdn.ivymoda.com",
      },
      {
        protocol: "https",
        hostname: "cdn.shopify.com",
      },
      {
        protocol: "https",
        hostname: "picsum.photos",
      },
      {
        protocol: "https",
        hostname: "placehold.co",
      },
    ],
  },
};

export default nextConfig;

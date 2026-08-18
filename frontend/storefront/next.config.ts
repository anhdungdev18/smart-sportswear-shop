import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Allow devices on the local network to load Next.js development assets and HMR.
  allowedDevOrigins: ["172.20.10.4", "172.20.10.4:3000"],
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

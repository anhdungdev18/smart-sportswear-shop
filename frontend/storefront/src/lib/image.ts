export function shouldBypassImageOptimization(src: string): boolean {
  // placehold.co serves its extensionless placeholder URLs as SVG. Next.js
  // intentionally refuses to proxy SVGs through the image optimizer.
  return src.startsWith("https://placehold.co/");
}

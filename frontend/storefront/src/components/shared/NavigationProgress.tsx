"use client";

import { useEffect, useRef, useState } from "react";
import { usePathname } from "next/navigation";

export function NavigationProgress() {
  const pathname = usePathname();
  const [visible, setVisible] = useState(false);
  const [width, setWidth] = useState(0);
  const prevPathname = useRef(pathname);
  const tickRef = useRef<ReturnType<typeof setInterval> | undefined>(undefined);
  const hideRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  // Detect navigation completion: pathname changed → snap to 100 then hide
  useEffect(() => {
    if (prevPathname.current === pathname) return;
    prevPathname.current = pathname;

    clearInterval(tickRef.current);
    hideRef.current = setTimeout(() => {
      setWidth(100);
      hideRef.current = setTimeout(() => {
        setVisible(false);
        setWidth(0);
      }, 220);
    }, 0);
  }, [pathname]);

  // Listen for anchor clicks to start the bar
  useEffect(() => {
    function handleClick(e: MouseEvent) {
      const anchor = (e.target as Element).closest("a");
      if (!anchor) return;
      const href = anchor.getAttribute("href") ?? "";
      // Skip external links, hash-only, mailto/tel, same page
      if (!href || /^(https?:|mailto:|tel:|#)/.test(href)) return;
      if (href === pathname || href === window.location.pathname) return;

      clearTimeout(hideRef.current);
      clearInterval(tickRef.current);
      setVisible(true);
      setWidth(18);

      // Trickle toward 82% while waiting for the server component
      tickRef.current = setInterval(() => {
        setWidth((prev) => {
          if (prev >= 82) {
            clearInterval(tickRef.current);
            return 82;
          }
          return prev + (82 - prev) * 0.12;
        });
      }, 280);
    }

    document.addEventListener("click", handleClick);
    return () => {
      document.removeEventListener("click", handleClick);
      clearInterval(tickRef.current);
      clearTimeout(hideRef.current);
    };
  }, [pathname]);

  if (!visible) return null;

  return (
    <div className="pointer-events-none fixed inset-x-0 top-0 z-[9999] h-[2px]">
      <div
        className="h-full bg-ivy-dark transition-[width] duration-300 ease-out"
        style={{ width: `${width}%` }}
      />
    </div>
  );
}

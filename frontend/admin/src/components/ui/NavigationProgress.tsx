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

  useEffect(() => {
    if (prevPathname.current === pathname) return;
    prevPathname.current = pathname;

    clearInterval(tickRef.current);
    setWidth(100);
    hideRef.current = setTimeout(() => {
      setVisible(false);
      setWidth(0);
    }, 220);
  }, [pathname]);

  useEffect(() => {
    function handleClick(e: MouseEvent) {
      const anchor = (e.target as Element).closest("a");
      if (!anchor) return;
      const href = anchor.getAttribute("href") ?? "";
      if (!href || /^(https?:|mailto:|tel:|#)/.test(href)) return;
      if (href === pathname || href === window.location.pathname) return;

      clearTimeout(hideRef.current);
      clearInterval(tickRef.current);
      setVisible(true);
      setWidth(18);

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
    <div
      style={{
        pointerEvents: "none",
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        height: 3,
        zIndex: 9999,
      }}
    >
      <div
        style={{
          height: "100%",
          width: `${width}%`,
          background: "var(--admin-accent)",
          boxShadow: "0 0 8px var(--admin-accent)",
          transition: "width 300ms ease-out",
        }}
      />
    </div>
  );
}

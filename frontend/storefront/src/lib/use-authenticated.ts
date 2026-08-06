"use client";

import { useSyncExternalStore } from "react";
import { getAccessToken, onSessionChange } from "@/lib/session";

export function useAuthenticated(): boolean {
  return useSyncExternalStore(
    onSessionChange,
    () => Boolean(getAccessToken()),
    () => false,
  );
}

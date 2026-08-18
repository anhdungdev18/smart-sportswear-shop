"use client";

import { useCallback, useEffect, useRef } from "react";
import Script from "next/script";
import { useRouter } from "next/navigation";
import { getApiErrorMessage } from "@/lib/api-errors";
import { setSession } from "@/lib/session";
import { googleLogin } from "@/modules/auth/api";

const CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;

type GoogleCredentialResponse = { credential?: string };

// Minimal shape of the Google Identity Services API we use.
type GoogleAccountsId = {
  initialize: (config: { client_id: string; callback: (response: GoogleCredentialResponse) => void }) => void;
  renderButton: (parent: HTMLElement, options: Record<string, unknown>) => void;
};

declare global {
  interface Window {
    google?: { accounts: { id: GoogleAccountsId } };
  }
}

export function GoogleSignInButton({
  onError,
  onSuccess,
}: {
  onError?: (message: string) => void;
  onSuccess?: (fullName: string) => void;
}) {
  const router = useRouter();
  const containerRef = useRef<HTMLDivElement>(null);
  const rendered = useRef(false);

  const handleCredential = useCallback(
    async (response: GoogleCredentialResponse) => {
      if (!response.credential) {
        onError?.("Không nhận được thông tin đăng nhập từ Google.");
        return;
      }
      try {
        const data = await googleLogin(response.credential);
        setSession(data.tokens);
        onSuccess?.(data.user.fullName);
        router.push("/");
      } catch (error) {
        onError?.(getApiErrorMessage(error, "Đăng nhập Google không thành công."));
      }
    },
    [onError, onSuccess, router],
  );

  const renderButton = useCallback(() => {
    if (rendered.current || !CLIENT_ID || !window.google || !containerRef.current) {
      return;
    }
    window.google.accounts.id.initialize({ client_id: CLIENT_ID, callback: handleCredential });
    window.google.accounts.id.renderButton(containerRef.current, {
      type: "standard",
      theme: "outline",
      size: "large",
      text: "continue_with",
      shape: "rectangular",
      logo_alignment: "center",
      locale: "vi",
      width: 320,
    });
    rendered.current = true;
  }, [handleCredential]);

  useEffect(() => {
    // If the GSI script was already loaded (e.g. client-side navigation), render now.
    if (window.google) {
      renderButton();
    }
  }, [renderButton]);

  // Not configured: render nothing so the page still works with email/password.
  if (!CLIENT_ID) {
    return null;
  }

  return (
    <>
      <Script src="https://accounts.google.com/gsi/client" strategy="afterInteractive" onLoad={renderButton} />
      <div ref={containerRef} className="flex justify-center" />
    </>
  );
}

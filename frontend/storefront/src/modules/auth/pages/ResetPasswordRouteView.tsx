import { ResetPasswordPageClient } from "@/modules/auth/components/ResetPasswordPageClient";

export async function ResetPasswordRouteView({
  searchParams,
}: {
  searchParams: Promise<{ token?: string }>;
}) {
  const { token } = await searchParams;
  return <ResetPasswordPageClient token={token ?? ""} />;
}

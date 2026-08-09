import { ResetPasswordRouteView } from "@/modules/auth/pages/ResetPasswordRouteView";

export default async function LegacyResetPasswordPage({
  searchParams,
}: {
  searchParams: Promise<{ token?: string }>;
}) {
  return <ResetPasswordRouteView searchParams={searchParams} />;
}

import { ResetPasswordRouteView } from "@/modules/auth/pages/ResetPasswordRouteView";

export default async function ResetPasswordPage({
  searchParams,
}: {
  searchParams: Promise<{ token?: string }>;
}) {
  return <ResetPasswordRouteView searchParams={searchParams} />;
}

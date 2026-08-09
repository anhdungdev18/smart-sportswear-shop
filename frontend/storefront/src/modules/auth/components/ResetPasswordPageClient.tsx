"use client";

import { useState } from "react";
import Link from "next/link";
import { getApiErrorMessage } from "@/lib/api-errors";
import { resetPassword } from "@/modules/auth/api";

export function ResetPasswordPageClient({ token }: { token: string }) {
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);

    if (!token) {
      setError("Thiếu token đặt lại mật khẩu.");
      return;
    }
    if (password !== confirmPassword) {
      setError("Mật khẩu xác nhận chưa khớp.");
      return;
    }

    setLoading(true);
    try {
      await resetPassword(token, password);
      setSuccess("Mật khẩu đã được cập nhật. Bạn có thể đăng nhập lại ngay bây giờ.");
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể đặt lại mật khẩu."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[760px] px-4 py-16 md:px-0">
        <div className="border border-ivy-hairline px-6 py-8 md:px-10">
          <h1 className="text-[32px] font-semibold uppercase tracking-[0.06em] text-ivy-dark">Đặt lại mật khẩu</h1>
          <p className="mt-4 text-[15px] leading-7 text-ivy-text">
            Nhập mật khẩu mới cho tài khoản của bạn. Mật khẩu cần có ít nhất 8 ký tự, gồm chữ và số.
          </p>

          {!token ? (
            <div className="mt-8 space-y-5">
              <p role="alert" className="text-[14px] leading-6 text-[#C62127]">
                Liên kết đặt lại mật khẩu không hợp lệ hoặc bị thiếu token. Vui lòng yêu cầu một liên kết mới.
              </p>
              <Link
                href="/dang-nhap#quen-mat-khau"
                className="inline-flex h-12 items-center rounded-tl-[20px] rounded-br-[20px] bg-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-white"
              >
                Yêu cầu liên kết mới
              </Link>
            </div>
          ) : (
          <form className="mt-8 space-y-5" onSubmit={handleSubmit}>
            <div>
              <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                Mật khẩu mới
              </label>
              <input
                type="password"
                required
                minLength={8}
                maxLength={72}
                pattern="(?=.*[A-Za-z])(?=.*\d).{8,72}"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none"
              />
            </div>
            <div>
              <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                Xác nhận mật khẩu
              </label>
              <input
                type="password"
                required
                minLength={8}
                maxLength={72}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none"
              />
            </div>
            {error ? <p className="text-[14px] text-[#C62127]">{error}</p> : null}
            {success ? <p className="text-[14px] text-[#257A4D]">{success}</p> : null}
            <div className="flex flex-wrap items-center gap-4">
              <button
                type="submit"
                disabled={loading}
                className="h-12 rounded-tl-[20px] rounded-br-[20px] bg-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-white disabled:opacity-60"
              >
                {loading ? "Đang cập nhật..." : "Lưu mật khẩu mới"}
              </button>
              <Link href="/dang-nhap" className="text-[14px] text-ivy-dark underline">
                Quay lại đăng nhập
              </Link>
            </div>
          </form>
          )}
        </div>
      </div>
    </main>
  );
}

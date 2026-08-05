"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { getApiErrorMessage } from "@/lib/api-errors";
import { setSession } from "@/lib/session";
import { forgotPassword, login, register } from "@/modules/auth/api";
import { GoogleSignInButton } from "@/modules/auth/components/GoogleSignInButton";

export function LoginPageClient() {
  const router = useRouter();
  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({
    fullName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
  });
  const [forgotEmail, setForgotEmail] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [registerLoading, setRegisterLoading] = useState(false);
  const [forgotLoading, setForgotLoading] = useState(false);
  const [loginMessage, setLoginMessage] = useState<string | null>(null);
  const [registerMessage, setRegisterMessage] = useState<string | null>(null);
  const [forgotMessage, setForgotMessage] = useState<string | null>(null);
  const [loginError, setLoginError] = useState<string | null>(null);
  const [registerError, setRegisterError] = useState<string | null>(null);
  const [forgotError, setForgotError] = useState<string | null>(null);

  const handleLoginSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoginLoading(true);
    setLoginError(null);
    setLoginMessage(null);
    try {
      const response = await login(loginForm);
      setSession(response.tokens);
      setLoginMessage(`Chào mừng ${response.user.fullName}, bạn đã đăng nhập thành công.`);
      router.push("/tai-khoan");
    } catch (error) {
      setLoginError(getApiErrorMessage(error, "Đăng nhập không thành công."));
    } finally {
      setLoginLoading(false);
    }
  };

  const handleRegisterSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setRegisterLoading(true);
    setRegisterError(null);
    setRegisterMessage(null);

    if (registerForm.password !== registerForm.confirmPassword) {
      setRegisterError("Mật khẩu xác nhận chưa khớp.");
      setRegisterLoading(false);
      return;
    }

    try {
      const response = await register({
        fullName: registerForm.fullName,
        email: registerForm.email,
        phone: registerForm.phone || undefined,
        password: registerForm.password,
      });
      setSession(response.tokens);
      setRegisterMessage("Tài khoản đã được tạo thành công.");
      router.push("/tai-khoan");
    } catch (error) {
      setRegisterError(getApiErrorMessage(error, "Đăng ký không thành công."));
    } finally {
      setRegisterLoading(false);
    }
  };

  const handleForgotSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setForgotLoading(true);
    setForgotError(null);
    setForgotMessage(null);
    try {
      await forgotPassword(forgotEmail);
      setForgotMessage("Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi đi.");
    } catch (error) {
      setForgotError(getApiErrorMessage(error, "Không thể gửi yêu cầu quên mật khẩu."));
    } finally {
      setForgotLoading(false);
    }
  };

  return (
    <main className="page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1180px] px-4 py-14 md:px-0">
        <div className="mb-10 text-center">
          <p className="mb-3 text-[13px] uppercase tracking-[0.24em] text-ivy-text-muted">Tài khoản Điểm Đến Thể Thao</p>
          <h1 className="text-[40px] font-semibold uppercase tracking-[0.06em] text-ivy-dark">Đăng nhập / Đăng ký</h1>
        </div>

        <div className="grid gap-8 lg:grid-cols-2">
          <section className="border border-ivy-hairline px-6 py-8 md:px-10">
            <h2 className="mb-2 text-[28px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Đăng nhập</h2>
            <p className="mb-8 text-[15px] leading-7 text-ivy-text">
              Đăng nhập để theo dõi đơn hàng, lưu địa chỉ và quản lý danh sách yêu thích của bạn.
            </p>

            <form className="space-y-5" onSubmit={handleLoginSubmit}>
              <div>
                <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Email</label>
                <input
                  type="email"
                  value={loginForm.email}
                  onChange={(e) => setLoginForm((current) => ({ ...current, email: e.target.value }))}
                  placeholder="Nhập email của bạn"
                  className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none focus:border-ivy-dark"
                />
              </div>
              <div>
                <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Mật khẩu</label>
                <input
                  type="password"
                  value={loginForm.password}
                  onChange={(e) => setLoginForm((current) => ({ ...current, password: e.target.value }))}
                  placeholder="Nhập mật khẩu"
                  className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none focus:border-ivy-dark"
                />
              </div>
              {loginError ? <p className="text-[14px] text-[#C62127]">{loginError}</p> : null}
              {loginMessage ? <p className="text-[14px] text-[#257A4D]">{loginMessage}</p> : null}
              <div className="flex items-center justify-between gap-4 text-[14px] text-ivy-text">
                <span>Đăng nhập bằng tài khoản khách hàng hiện có.</span>
                <Link href="/dat-lai-mat-khau" className="underline">Đặt lại mật khẩu</Link>
              </div>
              <button
                type="submit"
                disabled={loginLoading}
                className="h-12 rounded-tl-[20px] rounded-br-[20px] bg-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-white disabled:opacity-60"
              >
                {loginLoading ? "Đang đăng nhập..." : "Đăng nhập"}
              </button>
            </form>

            <div className="mt-6">
              <div className="mb-4 flex items-center gap-4">
                <span className="h-px flex-1 bg-ivy-hairline" />
                <span className="text-[13px] uppercase tracking-[0.04em] text-ivy-text">Hoặc</span>
                <span className="h-px flex-1 bg-ivy-hairline" />
              </div>
              <GoogleSignInButton
                onError={(message) => {
                  setLoginError(message);
                  setLoginMessage(null);
                }}
                onSuccess={(fullName) => {
                  setLoginError(null);
                  setLoginMessage(`Chào mừng ${fullName}, bạn đã đăng nhập bằng Google.`);
                }}
              />
            </div>
          </section>

          <section className="border border-ivy-hairline px-6 py-8 md:px-10">
            <h2 className="mb-2 text-[28px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Đăng ký mới</h2>
            <p className="mb-8 text-[15px] leading-7 text-ivy-text">
              Tạo tài khoản để nhận ưu đãi thành viên, lưu sản phẩm yêu thích và thanh toán nhanh hơn.
            </p>

            <form className="space-y-5" onSubmit={handleRegisterSubmit}>
              <div>
                <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Họ và tên</label>
                <input
                  type="text"
                  value={registerForm.fullName}
                  onChange={(e) => setRegisterForm((current) => ({ ...current, fullName: e.target.value }))}
                  placeholder="Nhập họ và tên"
                  className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none focus:border-ivy-dark"
                />
              </div>
              <div>
                <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Email</label>
                <input
                  type="email"
                  value={registerForm.email}
                  onChange={(e) => setRegisterForm((current) => ({ ...current, email: e.target.value }))}
                  placeholder="Nhập email"
                  className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none focus:border-ivy-dark"
                />
              </div>
              <div>
                <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Số điện thoại</label>
                <input
                  type="text"
                  value={registerForm.phone}
                  onChange={(e) => setRegisterForm((current) => ({ ...current, phone: e.target.value }))}
                  placeholder="Nhập số điện thoại"
                  className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none focus:border-ivy-dark"
                />
              </div>
              <div className="grid gap-5 md:grid-cols-2">
                <div>
                  <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Mật khẩu</label>
                  <input
                    type="password"
                    value={registerForm.password}
                    onChange={(e) => setRegisterForm((current) => ({ ...current, password: e.target.value }))}
                    placeholder="Tạo mật khẩu"
                    className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none focus:border-ivy-dark"
                  />
                </div>
                <div>
                  <label className="mb-2 block text-[13px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Xác nhận</label>
                  <input
                    type="password"
                    value={registerForm.confirmPassword}
                    onChange={(e) => setRegisterForm((current) => ({ ...current, confirmPassword: e.target.value }))}
                    placeholder="Nhập lại mật khẩu"
                    className="h-12 w-full border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none focus:border-ivy-dark"
                  />
                </div>
              </div>
              {registerError ? <p className="text-[14px] text-[#C62127]">{registerError}</p> : null}
              {registerMessage ? <p className="text-[14px] text-[#257A4D]">{registerMessage}</p> : null}
              <button
                type="submit"
                disabled={registerLoading}
                className="h-12 rounded-tl-[20px] rounded-br-[20px] border border-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-ivy-dark disabled:opacity-60"
              >
                {registerLoading ? "Đang tạo..." : "Tạo tài khoản"}
              </button>
            </form>
          </section>
        </div>

        <section className="mt-8 border border-ivy-hairline px-6 py-8 md:px-10">
          <h2 className="mb-2 text-[24px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">Quên mật khẩu</h2>
          <p className="mb-6 text-[15px] leading-7 text-ivy-text">Nhập email tài khoản để nhận liên kết đặt lại mật khẩu qua email.</p>
          <form className="flex flex-col gap-4 md:flex-row" onSubmit={handleForgotSubmit}>
            <input
              type="email"
              value={forgotEmail}
              onChange={(e) => setForgotEmail(e.target.value)}
              placeholder="Email khôi phục"
              className="h-12 flex-1 border border-ivy-hairline px-4 text-[15px] text-ivy-dark outline-none focus:border-ivy-dark"
            />
            <button
              type="submit"
              disabled={forgotLoading}
              className="h-12 rounded-tl-[20px] rounded-br-[20px] bg-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-white disabled:opacity-60"
            >
              {forgotLoading ? "Đang gửi..." : "Gửi email khôi phục"}
            </button>
          </form>
          {forgotError ? <p className="mt-4 text-[14px] text-[#C62127]">{forgotError}</p> : null}
          {forgotMessage ? <p className="mt-4 text-[14px] text-[#257A4D]">{forgotMessage}</p> : null}
        </section>
      </div>
    </main>
  );
}

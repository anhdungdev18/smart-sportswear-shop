"use client";

import type { ComponentType, InputHTMLAttributes, TextareaHTMLAttributes } from "react";
import { User, Phone, Mail, FileText, MessageCircle, Lock } from "lucide-react";
import { cn } from "@/lib/utils";

const CAPTCHA_TEXT = "4c0qx";
// Per-character rotation/offset so the mock captcha reads as a distorted image, not plain text.
const CAPTCHA_CHAR_STYLES = [
  { rotate: -8, translateY: 2 },
  { rotate: 6, translateY: -3 },
  { rotate: -4, translateY: 1 },
  { rotate: 9, translateY: -2 },
  { rotate: -6, translateY: 3 },
];

interface IconFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  icon: ComponentType<{ className?: string }>;
}

function IconInput({ icon: Icon, className, ...props }: IconFieldProps) {
  return (
    <div className="relative">
      <Icon className="pointer-events-none absolute left-3.5 top-4.5 size-4 text-[#A8A9AD]" />
      <input
        {...props}
        className={cn(
          "w-full rounded-lg border border-ivy-hairline py-3 pl-10 pr-4 text-sm text-ivy-dark placeholder:text-[#A8A9AD] focus:outline-none focus:ring-1 focus:ring-ivy-dark",
          className
        )}
      />
    </div>
  );
}

interface IconTextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  icon: ComponentType<{ className?: string }>;
}

function IconTextarea({ icon: Icon, className, ...props }: IconTextareaProps) {
  return (
    <div className="relative md:col-span-2">
      <Icon className="pointer-events-none absolute left-3.5 top-4.5 size-4 text-[#A8A9AD]" />
      <textarea
        {...props}
        className={cn(
          "w-full resize-none rounded-lg border border-ivy-hairline py-3 pl-10 pr-4 text-sm text-ivy-dark placeholder:text-[#A8A9AD] focus:outline-none focus:ring-1 focus:ring-ivy-dark",
          className
        )}
      />
    </div>
  );
}

export function ContactForm() {
  return (
    <div className="rounded-2xl border border-ivy-hairline p-10">
      <h2 className="mb-3 text-[28px] font-bold text-ivy-dark">Email to Điểm Đến Thể Thao</h2>
      <p className="mb-6 text-sm leading-5.5 text-ivy-text">
        We are here to help and answer any question you might have.Tell us about your issue so we
        can help you more quickly. We look forward to hearing from you.
      </p>

      <form
        onSubmit={(e) => e.preventDefault()}
        className="grid grid-cols-1 gap-4 md:grid-cols-2"
      >
        <IconInput icon={User} type="text" name="fullName" placeholder="Họ và tên" />
        <IconInput icon={Phone} type="tel" name="phone" placeholder="Điện thoại" />
        <IconInput icon={Mail} type="email" name="email" placeholder="Địa chỉ email" />
        <IconInput icon={FileText} type="text" name="subject" placeholder="Chủ đề" />
        <IconTextarea
          icon={MessageCircle}
          name="message"
          placeholder="Nội dung"
          rows={4}
        />

        <div className="flex flex-col items-center gap-4 sm:flex-row md:col-span-2">
          <div className="relative w-full flex-1">
            <Lock className="pointer-events-none absolute left-3.5 top-4.5 size-4 text-[#A8A9AD]" />
            <input
              type="text"
              name="captcha"
              placeholder="Nhập mã captcha"
              className="w-full rounded-lg border border-ivy-hairline py-3 pl-10 pr-4 text-sm text-ivy-dark placeholder:text-[#A8A9AD] focus:outline-none focus:ring-1 focus:ring-ivy-dark"
            />
          </div>

          <div
            aria-hidden="true"
            className="flex h-10 w-30 shrink-0 select-none items-center justify-center gap-px rounded-md bg-[#F1F1F2] font-mono text-base font-semibold text-[#6C6D70]"
          >
            {CAPTCHA_TEXT.split("").map((char, index) => {
              const style = CAPTCHA_CHAR_STYLES[index % CAPTCHA_CHAR_STYLES.length];
              return (
                <span
                  key={`${char}-${index}`}
                  style={{
                    transform: `rotate(${style.rotate}deg) translateY(${style.translateY}px)`,
                  }}
                  className="inline-block"
                >
                  {char}
                </span>
              );
            })}
          </div>
        </div>

        <button
          type="submit"
          className="rounded-lg bg-ivy-dark px-10 py-3.5 text-sm font-semibold uppercase tracking-[1px] text-white md:col-span-2"
        >
          GỬI
        </button>
      </form>
    </div>
  );
}

# ContactForm Specification

## Overview
- **Target file:** `src/components/ContactForm.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/contact-desktop-full.png` (right column)
- **Interaction model:** static form, UI-only (no real submission/captcha validation — mock `onSubmit={(e) => e.preventDefault()}`, per project scope: no real backend).
- **Props:** none.

## DOM Structure
```
div (root, border rounded-2xl p-10)
  h2 "Email to IVYmoda"
  p (description paragraph)
  form (grid grid-cols-2 gap-4)
    input "Họ và tên" (col-span-1, with a User icon prefix)
    input "Điện thoại" (col-span-1, with a Phone icon prefix)
    input "Địa chỉ email" (col-span-1, with a Mail icon prefix)
    input "Chủ đề" (col-span-1, with a FileText icon prefix)
    textarea "Nội dung" (col-span-2, with a MessageCircle icon prefix, rows=4)
    div (col-span-2, flex items-center gap-4)
      input "Nhập mã captcha" (flex-1, with a Lock icon prefix)
      div (mock captcha image placeholder — a fixed-size box with distorted-looking text, e.g. render the literal characters "4c0qx" in a slightly rotated/skewed monospace font per character to visually mimic a captcha, no real validation)
    button[type=submit] "GỬI" (col-span-2, solid dark, full width or auto width centered-left)
```

## Computed Styles (faithful approximation matching screenshot)
```css
.root { border: 1px solid #E7E8E9; border-radius: 20px; padding: 40px; }
h2 { font-size: 28px; font-weight: 700; color: #221F20; margin-bottom: 12px; }
p.description { font-size: 14px; line-height: 22px; color: #57585A; margin-bottom: 24px; }
input, textarea { border: 1px solid #E7E8E9; border-radius: 8px; padding: 12px 16px 12px 40px; font-size: 14px; width: 100%; }
input::placeholder, textarea::placeholder { color: #A8A9AD; }
.input-wrapper { position: relative; }
.input-wrapper svg { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #A8A9AD; width: 16px; height: 16px; }
button[type=submit] { background: #221F20; color: #FFF; border-radius: 8px; padding: 14px 40px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; }
```

## Text Content (verbatim)
- Title: "Email to IVYmoda"
- Description: "We are here to help and answer any question you might have.Tell us about your issue so we can help you more quickly. We look forward to hearing from you."
- Placeholders: "Họ và tên", "Điện thoại", "Địa chỉ email", "Chủ đề", "Nội dung", "Nhập mã captcha"
- Mock captcha display text: "4c0qx" (static, always the same since there's no real backend)
- Submit button: "GỬI"

## Icons
Import directly from `lucide-react`: `User`, `Phone`, `Mail`, `FileText`, `MessageCircle`, `Lock`.

## Responsive Behavior
- **Desktop (≥768px, `md:`):** 2-column form grid as described.
- **Mobile (<768px):** all fields stack to 1 column (`grid-cols-1 md:grid-cols-2`), captcha row stacks the input and captcha image vertically if needed.
- **Breakpoint:** `md:` (768px).

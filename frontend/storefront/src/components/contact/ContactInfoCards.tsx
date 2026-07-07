import { CONTACT_INFO_CARDS } from "@/modules/content/data/contact";

export function ContactInfoCards() {
  return (
    <div className="flex flex-col gap-4">
      {CONTACT_INFO_CARDS.map(({ icon: Icon, label, lines }) => (
        <div
          key={label}
          className="flex items-start gap-4 rounded-xl border border-ivy-hairline p-5"
        >
          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-[#F7F8F9] text-ivy-dark">
            <Icon className="h-5 w-5" />
          </div>
          <div>
            <h4 className="mb-1.5 text-base font-semibold text-ivy-dark">{label}</h4>
            <p className="text-sm leading-[22px] text-ivy-text">
              {lines.map((line, i) => (
                <span key={i}>
                  {line}
                  {i < lines.length - 1 && <br />}
                </span>
              ))}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}

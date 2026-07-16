/**
 * Neutral "no image" placeholder rendered as an inline SVG data URI. Shown when a
 * product/record genuinely has no image — it is an empty-state glyph, NOT a
 * mock/stock photo, so it does not depend on any remote host.
 */
export const NO_IMAGE =
  "data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20width='120'%20height='120'%20viewBox='0%200%20120%20120'%3E%3Crect%20width='120'%20height='120'%20fill='%23f1f5f9'/%3E%3Cg%20fill='none'%20stroke='%23cbd5e1'%20stroke-width='4'%20stroke-linecap='round'%20stroke-linejoin='round'%3E%3Crect%20x='33'%20y='40'%20width='54'%20height='40'%20rx='5'/%3E%3Ccircle%20cx='48'%20cy='54'%20r='5'/%3E%3Cpath%20d='M35%2075l17-15%2011%209%209-7%2015%2013'/%3E%3C/g%3E%3C/svg%3E";

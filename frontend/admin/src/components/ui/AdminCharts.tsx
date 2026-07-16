"use client";

import { Bar, BarChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { RevenuePoint, TopProductPoint } from "@/modules/analytics/api";

function formatVndShort(amount: number): string {
  if (!Number.isFinite(amount)) return "0";
  const abs = Math.abs(amount);
  if (abs >= 1_000_000_000) return `${(amount / 1_000_000_000).toFixed(abs >= 10_000_000_000 ? 0 : 1)} tỷ`;
  if (abs >= 1_000_000) return `${(amount / 1_000_000).toFixed(abs >= 10_000_000 ? 0 : 1)} tr`;
  if (abs >= 1_000) return `${Math.round(amount / 1_000)}k`;
  return amount.toLocaleString("vi-VN");
}

export function RevenueChart({ data }: { data: RevenuePoint[] }) {
  return (
    <ResponsiveContainer width="100%" height={290}>
      <LineChart data={data} margin={{ top: 8, right: 12, left: 6, bottom: 0 }}>
        <CartesianGrid stroke="#e6e9f2" vertical={false} />
        <XAxis dataKey="label" tickLine={false} axisLine={false} minTickGap={16} />
        <YAxis tickLine={false} axisLine={false} width={64} tick={{ fontSize: 12, fill: "#475569" }} tickFormatter={(value) => formatVndShort(Number(value))} />
        <Tooltip
          formatter={(value) => [`${Number(value).toLocaleString("vi-VN")} ₫`, "Doanh thu"]}
          labelFormatter={(label, payload) => {
            const orders = payload?.[0]?.payload?.orders;
            return typeof orders === "number" ? `${label} · ${orders.toLocaleString("vi-VN")} đơn` : String(label);
          }}
        />
        <Line type="monotone" dataKey="revenue" stroke="#2563eb" strokeWidth={3} dot={{ r: 3, fill: "#ffffff", stroke: "#2563eb" }} />
      </LineChart>
    </ResponsiveContainer>
  );
}

function truncateLabel(value: string, max = 18): string {
  return value.length > max ? `${value.slice(0, max - 1).trimEnd()}…` : value;
}

export function TopProductChart({ data }: { data: TopProductPoint[] }) {
  return (
    <ResponsiveContainer width="100%" height={238}>
      <BarChart data={data} layout="vertical" margin={{ top: 0, right: 12, left: 8, bottom: 0 }}>
        <CartesianGrid stroke="#e6e9f2" horizontal={false} />
        <XAxis type="number" hide />
        <YAxis
          dataKey="name"
          type="category"
          tickLine={false}
          axisLine={false}
          width={132}
          interval={0}
          tick={{ fontSize: 12, fill: "#475569" }}
          tickFormatter={(value: string) => truncateLabel(value)}
        />
        <Tooltip formatter={(value) => [`${value}`, "Đã bán"]} />
        <Bar dataKey="sales" fill="#2563eb" radius={[0, 8, 8, 0]} barSize={22} />
      </BarChart>
    </ResponsiveContainer>
  );
}

"use client";

import { Bar, BarChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { RevenuePoint, TopProductPoint } from "@/modules/analytics/api";

export function RevenueChart({ data }: { data: RevenuePoint[] }) {
  return (
    <ResponsiveContainer width="100%" height={290}>
      <LineChart data={data} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
        <CartesianGrid stroke="#e1e3e8" vertical={false} />
        <XAxis dataKey="month" tickLine={false} axisLine={false} />
        <YAxis tickLine={false} axisLine={false} />
        <Tooltip formatter={(value) => [`${value} triệu`, "Doanh thu"]} />
        <Line type="monotone" dataKey="revenue" stroke="#d9121f" strokeWidth={3} dot={{ r: 4, fill: "#ffffff", stroke: "#d9121f" }} />
        <Line type="monotone" dataKey="orders" stroke="#0e1c22" strokeWidth={2} dot={false} />
      </LineChart>
    </ResponsiveContainer>
  );
}

export function TopProductChart({ data }: { data: TopProductPoint[] }) {
  return (
    <ResponsiveContainer width="100%" height={238}>
      <BarChart data={data} layout="vertical" margin={{ top: 0, right: 8, left: 18, bottom: 0 }}>
        <CartesianGrid stroke="#e1e3e8" horizontal={false} />
        <XAxis type="number" hide />
        <YAxis dataKey="name" type="category" tickLine={false} axisLine={false} width={92} />
        <Tooltip formatter={(value) => [`${value}`, "Đã bán"]} />
        <Bar dataKey="sales" fill="#d9121f" radius={[0, 3, 3, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}

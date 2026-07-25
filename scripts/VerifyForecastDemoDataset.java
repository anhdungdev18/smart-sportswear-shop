import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class VerifyForecastDemoDataset {
    record Check(String name, boolean pass, String detail) {}

    public static void main(String[] args) throws Exception {
        Map<String, String> env = loadEnv(Paths.get("backend/.env"));
        String host = required(env, "DB_HOST");
        String port = env.getOrDefault("DB_PORT", "5432");
        String db = required(env, "DB_NAME");
        String params = env.getOrDefault("DB_PARAMS", "");
        String user = required(env, "DB_USERNAME");
        String password = required(env, "DB_PASSWORD");
        String marker = env.getOrDefault("FORECAST_DEMO_MARKER", "[FORECAST_DEMO_V2]");
        int expectedDays = Integer.parseInt(env.getOrDefault("FORECAST_DEMO_HISTORY_DAYS", "180"));
        int expectedOrders = Integer.parseInt(env.getOrDefault("FORECAST_DEMO_ORDER_COUNT", "12000"));
        int expectedVariants = Integer.parseInt(env.getOrDefault("FORECAST_DEMO_VARIANT_COUNT", "120"));
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db + params;

        List<Check> checks = new ArrayList<>();
        StringBuilder aggregate = new StringBuilder();
        Map<String, Object> manifest = new LinkedHashMap<>();

        try (Connection cn = DriverManager.getConnection(url, user, password)) {
            cn.setReadOnly(true);

            Map<String, Object> orderStats = one(cn, """
                    select count(*)::bigint order_count,
                           min(created_at::date)::text min_date,
                           max(created_at::date)::text max_date,
                           count(distinct created_at::date)::bigint distinct_days,
                           count(*) filter (where order_status = 'CANCELLED')::bigint cancelled_orders
                    from orders where note = ?
                    """, marker);
            long orderCount = asLong(orderStats.get("order_count"));
            long distinctDays = asLong(orderStats.get("distinct_days"));
            checks.add(new Check("180 days", distinctDays == expectedDays, "distinct_days=" + distinctDays + ", range=" + orderStats.get("min_date") + ".." + orderStats.get("max_date")));
            checks.add(new Check("12000 orders", orderCount == expectedOrders, "orders=" + orderCount));

            Map<String, Object> scenarioStats = one(cn, """
                    select count(*)::bigint scenario_count,
                           count(distinct variant_id)::bigint variant_count,
                           min(anchor_date)::text anchor_date,
                           min(history_days)::bigint min_history_days,
                           max(history_days)::bigint max_history_days,
                           min(random_seed)::text random_seed,
                           min(scenario_version) scenario_version
                    from forecast_demo_scenarios where marker = ?
                    """, marker);
            long scenarioCount = asLong(scenarioStats.get("scenario_count"));
            long variantCount = asLong(scenarioStats.get("variant_count"));
            checks.add(new Check("120 demo variants", scenarioCount == expectedVariants && variantCount == expectedVariants, "scenarios=" + scenarioCount + ", variants=" + variantCount));

            long itemCount = scalarLong(cn, "select count(*) from order_items oi join orders o on o.id = oi.order_id where o.note = ?", marker);
            checks.add(new Check("12000 order items", itemCount == expectedOrders, "items=" + itemCount));

            long lineMismatch = scalarLong(cn, """
                    select count(*) from order_items oi
                    join orders o on o.id = oi.order_id
                    where o.note = ? and oi.line_total <> oi.unit_price_snapshot * oi.quantity
                    """, marker);
            checks.add(new Check("line_total integrity", lineMismatch == 0, "mismatches=" + lineMismatch));

            Map<String, Object> validCompare = one(cn, """
                    with actual as (
                        select oi.variant_id,
                               coalesce(sum(oi.quantity) filter (where o.order_status <> 'CANCELLED'), 0)::bigint valid_units,
                               coalesce(sum(oi.quantity), 0)::bigint total_units
                        from order_items oi join orders o on o.id = oi.order_id
                        where o.note = ?
                        group by oi.variant_id
                    )
                    select count(*) filter (where coalesce(a.valid_units, 0) <> s.expected_valid_units)::bigint valid_mismatches,
                           count(*) filter (where coalesce(a.total_units, 0) <> s.expected_total_units)::bigint total_mismatches,
                           coalesce(sum(s.expected_valid_units), 0)::bigint expected_valid_units,
                           coalesce(sum(a.valid_units), 0)::bigint actual_valid_units,
                           coalesce(sum(s.expected_total_units), 0)::bigint expected_total_units,
                           coalesce(sum(a.total_units), 0)::bigint actual_total_units
                    from forecast_demo_scenarios s
                    left join actual a on a.variant_id = s.variant_id
                    where s.marker = ?
                    """, marker, marker);
            long validMismatches = asLong(validCompare.get("valid_mismatches"));
            long totalMismatches = asLong(validCompare.get("total_mismatches"));
            checks.add(new Check("cancelled excluded from valid demand", validMismatches == 0, "valid_mismatches=" + validMismatches + ", expected_valid=" + validCompare.get("expected_valid_units") + ", actual_valid=" + validCompare.get("actual_valid_units")));
            checks.add(new Check("ground-truth total demand", totalMismatches == 0, "total_mismatches=" + totalMismatches + ", expected_total=" + validCompare.get("expected_total_units") + ", actual_total=" + validCompare.get("actual_total_units")));

            Map<String, Object> noDemand = one(cn, """
                    with actual as (
                        select oi.variant_id,
                               coalesce(sum(oi.quantity) filter (where o.order_status <> 'CANCELLED'), 0)::bigint valid_units,
                               coalesce(sum(oi.quantity), 0)::bigint total_units
                        from order_items oi join orders o on o.id = oi.order_id
                        where o.note = ?
                        group by oi.variant_id
                    )
                    select count(*)::bigint no_demand_variants,
                           coalesce(sum(s.expected_valid_units), 0)::bigint expected_valid_units,
                           coalesce(sum(s.expected_total_units), 0)::bigint expected_total_units,
                           coalesce(sum(a.valid_units), 0)::bigint actual_valid_units,
                           coalesce(sum(a.total_units), 0)::bigint actual_total_units
                    from forecast_demo_scenarios s
                    left join actual a on a.variant_id = s.variant_id
                    where s.marker = ? and s.demand_profile = 'NO_DEMAND'
                    """, marker, marker);
            boolean noDemandPass = asLong(noDemand.get("expected_valid_units")) == 0 && asLong(noDemand.get("expected_total_units")) == 0 && asLong(noDemand.get("actual_valid_units")) == 0 && asLong(noDemand.get("actual_total_units")) == 0;
            checks.add(new Check("no-demand variants", noDemandPass, noDemand.toString()));

            Map<String, Object> supplierPolicy = one(cn, """
                    select count(*) filter (where s.supplier_name is null or s.lead_time_days is null or s.minimum_order_quantity is null or s.pack_size is null or s.service_level is null)::bigint scenario_missing,
                           count(*) filter (where p.id is null or p.supplier_name is null or p.lead_time_days is null or p.minimum_order_quantity is null or p.pack_size is null or p.service_level is null)::bigint policy_missing,
                           count(distinct p.supplier_name)::bigint supplier_count,
                           count(distinct p.lead_time_days)::bigint lead_time_count,
                           count(distinct p.minimum_order_quantity)::bigint moq_count,
                           count(distinct p.pack_size)::bigint pack_size_count
                    from forecast_demo_scenarios s
                    left join inventory_policies p on p.variant_id = s.variant_id
                    where s.marker = ?
                    """, marker);
            boolean policyPass = asLong(supplierPolicy.get("scenario_missing")) == 0 && asLong(supplierPolicy.get("policy_missing")) == 0;
            checks.add(new Check("supplier and policy fields", policyPass, supplierPolicy.toString()));

            List<Map<String, Object>> profileRows = list(cn, """
                    with actual as (
                        select oi.variant_id,
                               coalesce(sum(oi.quantity) filter (where o.order_status <> 'CANCELLED'), 0)::bigint valid_units,
                               coalesce(sum(oi.quantity), 0)::bigint total_units
                        from order_items oi join orders o on o.id = oi.order_id
                        where o.note = ?
                        group by oi.variant_id
                    )
                    select s.demand_profile,
                           count(*)::bigint variants,
                           coalesce(sum(s.expected_total_units), 0)::bigint expected_total_units,
                           coalesce(sum(s.expected_valid_units), 0)::bigint expected_valid_units,
                           coalesce(sum(a.total_units), 0)::bigint actual_total_units,
                           coalesce(sum(a.valid_units), 0)::bigint actual_valid_units
                    from forecast_demo_scenarios s
                    left join actual a on a.variant_id = s.variant_id
                    where s.marker = ?
                    group by s.demand_profile
                    order by s.demand_profile
                    """, marker, marker);

            aggregate.append("marker=").append(marker).append('\n');
            aggregate.append("orders=").append(orderStats).append('\n');
            aggregate.append("scenarios=").append(scenarioStats).append('\n');
            aggregate.append("items=").append(itemCount).append('\n');
            aggregate.append("valid=").append(validCompare).append('\n');
            aggregate.append("noDemand=").append(noDemand).append('\n');
            aggregate.append("policy=").append(supplierPolicy).append('\n');
            aggregate.append("profiles=").append(profileRows).append('\n');
            String hash = sha256(aggregate.toString());

            manifest.put("generatedAt", Instant.now().toString());
            manifest.put("marker", marker);
            manifest.put("scenarioVersion", scenarioStats.get("scenario_version"));
            manifest.put("randomSeed", scenarioStats.get("random_seed"));
            manifest.put("anchorDate", scenarioStats.get("anchor_date"));
            manifest.put("historyDays", expectedDays);
            manifest.put("orderCount", orderCount);
            manifest.put("orderItemCount", itemCount);
            manifest.put("variantCount", variantCount);
            manifest.put("scenarioCount", scenarioCount);
            manifest.put("dateRange", orderStats.get("min_date") + ".." + orderStats.get("max_date"));
            manifest.put("distinctDays", distinctDays);
            manifest.put("cancelledOrders", orderStats.get("cancelled_orders"));
            manifest.put("expectedValidUnits", validCompare.get("expected_valid_units"));
            manifest.put("actualValidUnits", validCompare.get("actual_valid_units"));
            manifest.put("expectedTotalUnits", validCompare.get("expected_total_units"));
            manifest.put("actualTotalUnits", validCompare.get("actual_total_units"));
            manifest.put("aggregateHash", hash);
            manifest.put("profileAggregates", profileRows);
        }

        boolean allPass = checks.stream().allMatch(Check::pass);
        for (Check c : checks) {
            System.out.println((c.pass ? "PASS" : "FAIL") + " | " + c.name + " | " + c.detail);
        }
        System.out.println("AGGREGATE_HASH=" + manifest.get("aggregateHash"));
        if (args.length > 0 && args[0].equals("--manifest")) {
            Path out = Paths.get(args.length > 1 ? args[1] : "evidence/ai-replenishment/forecast-demo-v2-manifest.json");
            Files.createDirectories(out.getParent());
            Files.writeString(out, toJson(manifest), StandardCharsets.UTF_8);
            System.out.println("MANIFEST=" + out);
        }
        if (!allPass) System.exit(2);
    }

    static Map<String, String> loadEnv(Path path) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        for (String raw : Files.readAllLines(path)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
            int idx = line.indexOf('=');
            map.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
        }
        return map;
    }
    static String required(Map<String, String> env, String key) { var v = env.get(key); if (v == null || v.isBlank()) throw new IllegalStateException("Missing " + key); return v; }
    static long asLong(Object value) { if (value == null) return 0; if (value instanceof Number n) return n.longValue(); return Long.parseLong(value.toString()); }
    static long scalarLong(Connection cn, String sql, Object... params) throws SQLException { return asLong(one(cn, sql, params).values().iterator().next()); }
    static Map<String, Object> one(Connection cn, String sql, Object... params) throws SQLException { var rows = list(cn, sql, params); return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0); }
    static List<Map<String, Object>> list(Connection cn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) row.put(md.getColumnLabel(i), rs.getObject(i));
                    rows.add(row);
                }
                return rows;
            }
        }
    }
    static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
    static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof Map<?,?> m) {
            StringBuilder sb = new StringBuilder("{\n");
            int i = 0;
            for (var e : m.entrySet()) {
                sb.append("  \"").append(escape(String.valueOf(e.getKey()))).append("\": ").append(toJson(e.getValue()));
                if (++i < m.size()) sb.append(',');
                sb.append('\n');
            }
            return sb.append('}').toString();
        }
        if (value instanceof List<?> l) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < l.size(); i++) { if (i > 0) sb.append(", "); sb.append(toJson(l.get(i))); }
            return sb.append(']').toString();
        }
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        return "\"" + escape(String.valueOf(value)) + "\"";
    }
    static String escape(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
}

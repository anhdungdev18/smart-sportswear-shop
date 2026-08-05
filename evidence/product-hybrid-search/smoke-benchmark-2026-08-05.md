# Product hybrid search smoke benchmark — 2026-08-05

Environment: live Supabase catalog, configured embedding provider, six required
queries from the implementation brief. This is a smoke benchmark, not the final
60-query relevance dataset.

| Query | Mode | Total | Returned | Latency |
|---|---:|---:|---:|---:|
| giày Nike nam màu đen sân cỏ nhân tạo dưới 2 triệu size 42 | KEYWORD | 0 | 0 | 4860 ms |
| áo chạy bộ nữ thoáng khí màu hồng dưới 1 triệu size M | HYBRID | 1 | 1 | 4530 ms |
| giày đá banh tốc độ cho tiền đạo | HYBRID | 20 | 5 | 2740 ms |
| áo MU sân nhà | HYBRID | 33 | 5 | 2370 ms |
| đồ chạy bộ mặc trời nóng | HYBRID | 26 | 5 | 2270 ms |
| giay co nhan tao nike den | KEYWORD | 0 | 0 | 2220 ms |

Result: **rollout gate failed**.

- Two required queries returned zero results.
- Observed latency exceeds the documented cache-miss target of 3 seconds for
  two queries and the cache-hit target of 1.5 seconds for all queries.
- The feature flag must remain disabled until the versioned 60-query dataset is
  completed, relevance is reviewed, and latency/cache behavior passes.

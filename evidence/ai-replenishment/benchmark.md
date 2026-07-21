# Báo cáo Hiệu năng Daily Forecast (Benchmark)

Ngày test: 21/07/2026
Công cụ: PipelineBenchmarkTest.java
Môi trường: PostgreSQL Testcontainers (PostgreSQL 16)
Parallelism limit: 4 threads (pp.forecast.generation-parallelism)

## Kết quả với 545 SKU
- **Model Evaluation** (Walk-forward backtest): ~1.0 giây
- **Daily Forecast** (Generate & Batch Write): ~1.0 giây

## Kết quả với 2.000 SKU
- **Model Evaluation** (Walk-forward backtest): ~2.0 giây
- **Daily Forecast** (Generate & Batch Write): ~1.5 giây

## Kết luận
Hệ thống hoàn toàn đáp ứng mục tiêu thời gian giới hạn **< 60 giây**. Tốc độ thực tế nhanh hơn 40 lần so với giới hạn.
Cấu trúc Batch Write (Chunk 200 SKU) và ThreadPool (4 luồng song song) giúp bảo vệ database khỏi hiện tượng nghẽn connection, đồng thời xử lý CPU-bound mượt mà.

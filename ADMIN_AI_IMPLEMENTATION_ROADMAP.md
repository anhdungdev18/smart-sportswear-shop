# LÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ trÃƒÆ’Ã‚Â¬nh triÃƒÂ¡Ã‚Â»Ã†â€™n khai AI cho Admin

## 1. QuyÃƒÂ¡Ã‚ÂºÃ‚Â¿t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh kiÃƒÂ¡Ã‚ÂºÃ‚Â¿n trÃƒÆ’Ã‚Âºc

HÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºng phÃƒÆ’Ã‚Â¹ hÃƒÂ¡Ã‚Â»Ã‚Â£p nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n tÃƒÂ¡Ã‚ÂºÃ‚Â¡i lÃƒÆ’Ã‚Â :

> XÃƒÆ’Ã‚Â¢y dÃƒÂ¡Ã‚Â»Ã‚Â±ng **Inventory & Replenishment Intelligence** lÃƒÆ’Ã‚Â m lÃƒÆ’Ã‚Âµi tÃƒÆ’Ã‚Â­nh toÃƒÆ’Ã‚Â¡n, sau Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â³ xÃƒÆ’Ã‚Â¢y
> **Admin Copilot read-only** Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ truy vÃƒÂ¡Ã‚ÂºÃ‚Â¥n vÃƒÆ’Ã‚Â  giÃƒÂ¡Ã‚ÂºÃ‚Â£i thÃƒÆ’Ã‚Â­ch kÃƒÂ¡Ã‚ÂºÃ‚Â¿t quÃƒÂ¡Ã‚ÂºÃ‚Â£ bÃƒÂ¡Ã‚ÂºÃ‚Â±ng ngÃƒÆ’Ã‚Â´n ngÃƒÂ¡Ã‚Â»Ã‚Â¯ tÃƒÂ¡Ã‚Â»Ã‚Â± nhiÃƒÆ’Ã‚Âªn.

KhÃƒÆ’Ã‚Â´ng triÃƒÂ¡Ã‚Â»Ã†â€™n khai ReAct agent Ãƒâ€žÃ¢â‚¬Ëœa nÃƒâ€žÃ†â€™ng hoÃƒÂ¡Ã‚ÂºÃ‚Â·c action tÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng ngay tÃƒÂ¡Ã‚Â»Ã‚Â« Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â§u.

Ba service cÃƒÆ’Ã‚Â³ trÃƒÆ’Ã‚Â¡ch nhiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡m riÃƒÆ’Ã‚Âªng:

```text
ai_forecasting_service
  - KiÃƒÂ¡Ã‚Â»Ã†â€™m tra chÃƒÂ¡Ã‚ÂºÃ‚Â¥t lÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£ng dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u
  - PhÃƒÆ’Ã‚Â¢n loÃƒÂ¡Ã‚ÂºÃ‚Â¡i nhu cÃƒÂ¡Ã‚ÂºÃ‚Â§u SKU
  - Forecast vÃƒÆ’Ã‚Â  backtest
  - Stockout/overstock risk
  - Replenishment recommendation
  - What-if simulation

chatbot-admin-service
  - HiÃƒÂ¡Ã‚Â»Ã†â€™u cÃƒÆ’Ã‚Â¢u hÃƒÂ¡Ã‚Â»Ã‚Âi cÃƒÂ¡Ã‚Â»Ã‚Â§a admin
  - GÃƒÂ¡Ã‚Â»Ã‚Âi cÃƒÆ’Ã‚Â¡c API read-only cÃƒÆ’Ã‚Â³ kiÃƒÂ¡Ã‚Â»Ã†â€™m soÃƒÆ’Ã‚Â¡t
  - TÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng hÃƒÂ¡Ã‚Â»Ã‚Â£p vÃƒÆ’Ã‚Â  giÃƒÂ¡Ã‚ÂºÃ‚Â£i thÃƒÆ’Ã‚Â­ch dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u
  - QuÃƒÂ¡Ã‚ÂºÃ‚Â£n lÃƒÆ’Ã‚Â½ phiÃƒÆ’Ã‚Âªn hÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢i thoÃƒÂ¡Ã‚ÂºÃ‚Â¡i vÃƒÆ’Ã‚Â  tool trace
  - TÃƒÂ¡Ã‚ÂºÃ‚Â¡o approval request ÃƒÂ¡Ã‚Â»Ã…Â¸ phase sau

backend
  - XÃƒÆ’Ã‚Â¡c thÃƒÂ¡Ã‚Â»Ã‚Â±c JWT vÃƒÆ’Ã‚Â  phÃƒÆ’Ã‚Â¢n quyÃƒÂ¡Ã‚Â»Ã‚Ân
  - NghiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡p vÃƒÂ¡Ã‚Â»Ã‚Â¥ commerce
  - ThÃƒÂ¡Ã‚Â»Ã‚Â±c hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n write action
  - Audit vÃƒÆ’Ã‚Â  idempotency
```

## 2. Baseline dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u Supabase

ThÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng kÃƒÆ’Ã‚Âª tÃƒÂ¡Ã‚ÂºÃ‚Â¡i thÃƒÂ¡Ã‚Â»Ã‚Âi Ãƒâ€žÃ¢â‚¬ËœiÃƒÂ¡Ã‚Â»Ã†â€™m khÃƒÂ¡Ã‚ÂºÃ‚Â£o sÃƒÆ’Ã‚Â¡t:

| DÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u | SÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ lÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£ng |
|---|---:|
| Ãƒâ€žÃ‚ÂÃƒâ€ Ã‚Â¡n hÃƒÆ’Ã‚Â ng | 3.008 |
| Order items | 3.013 |
| KhÃƒÆ’Ã‚Â¡ch hÃƒÆ’Ã‚Â ng cÃƒÆ’Ã‚Â³ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â¡n | 6 |
| SÃƒÂ¡Ã‚ÂºÃ‚Â£n phÃƒÂ¡Ã‚ÂºÃ‚Â©m | 226 |
| BiÃƒÂ¡Ã‚ÂºÃ‚Â¿n thÃƒÂ¡Ã‚Â»Ã†â€™/SKU | 1.159 |
| SKU cÃƒÆ’Ã‚Â³ sales snapshot | 55 |
| Sales daily snapshot | 2.221 |
| Inventory snapshot | 8.491 |
| Forecast runs | 6.015 |
| Inventory policies | 1.159 |
| Replenishment recommendations | 20 |
| Product embeddings | 103 |
| Forecast model evaluations | 0 |
| Supplier snapshot | 0 |
| Reviews/returns/shipments/audit/chat history | 0 |

CÃƒÆ’Ã‚Â¡c vÃƒÂ¡Ã‚ÂºÃ‚Â¥n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â cÃƒÂ¡Ã‚ÂºÃ‚Â§n lÃƒâ€ Ã‚Â°u ÃƒÆ’Ã‚Â½:

- ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° khoÃƒÂ¡Ã‚ÂºÃ‚Â£ng 4,7% SKU cÃƒÆ’Ã‚Â³ lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ch sÃƒÂ¡Ã‚Â»Ã‚Â­ bÃƒÆ’Ã‚Â¡n khÃƒÆ’Ã‚Â¡c 0.
- Inventory snapshot mÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi trÃƒÂ¡Ã‚ÂºÃ‚Â£i trÃƒÆ’Ã‚Âªn khoÃƒÂ¡Ã‚ÂºÃ‚Â£ng 3 ngÃƒÆ’Ã‚Â y.
- TÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ forecast hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n cÃƒÆ’Ã‚Â³ confidence `LOW`.
- WAPE trung bÃƒÆ’Ã‚Â¬nh khoÃƒÂ¡Ã‚ÂºÃ‚Â£ng 148,7%, chÃƒâ€ Ã‚Â°a Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ an toÃƒÆ’Ã‚Â n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ tÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p hÃƒÆ’Ã‚Â ng.
- ToÃƒÆ’Ã‚Â n bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ policy thiÃƒÂ¡Ã‚ÂºÃ‚Â¿u supplier vÃƒÆ’Ã‚Â  Ãƒâ€žÃ¢â‚¬Ëœang dÃƒÆ’Ã‚Â¹ng cÃƒÆ’Ã‚Â¡c giÃƒÆ’Ã‚Â¡ trÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ gÃƒÂ¡Ã‚ÂºÃ‚Â§n nhÃƒâ€ Ã‚Â° Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t.
- 3.008 Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â¡n chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° thuÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢c 6 khÃƒÆ’Ã‚Â¡ch hÃƒÆ’Ã‚Â ng, nhiÃƒÂ¡Ã‚Â»Ã‚Âu khÃƒÂ¡Ã‚ÂºÃ‚Â£ nÃƒâ€žÃ†â€™ng phÃƒÂ¡Ã‚ÂºÃ‚Â§n lÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºn lÃƒÆ’Ã‚Â  dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u demo.

## 3. NguyÃƒÆ’Ã‚Âªn tÃƒÂ¡Ã‚ÂºÃ‚Â¯c triÃƒÂ¡Ã‚Â»Ã†â€™n khai

1. TÃƒÆ’Ã‚Â­nh toÃƒÆ’Ã‚Â¡n nghiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡p vÃƒÂ¡Ã‚Â»Ã‚Â¥ phÃƒÂ¡Ã‚ÂºÃ‚Â£i deterministic vÃƒÆ’Ã‚Â  kiÃƒÂ¡Ã‚Â»Ã†â€™m thÃƒÂ¡Ã‚Â»Ã‚Â­ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c.
2. LLM khÃƒÆ’Ã‚Â´ng tÃƒÂ¡Ã‚Â»Ã‚Â± tÃƒÆ’Ã‚Â­nh forecast, tÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n kho, doanh thu hoÃƒÂ¡Ã‚ÂºÃ‚Â·c sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ lÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p.
3. LLM chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° hiÃƒÂ¡Ã‚Â»Ã†â€™u cÃƒÆ’Ã‚Â¢u hÃƒÂ¡Ã‚Â»Ã‚Âi, chÃƒÂ¡Ã‚Â»Ã‚Ân read tool vÃƒÆ’Ã‚Â  giÃƒÂ¡Ã‚ÂºÃ‚Â£i thÃƒÆ’Ã‚Â­ch structured result.
4. KhÃƒÆ’Ã‚Â´ng cho LLM chÃƒÂ¡Ã‚ÂºÃ‚Â¡y SQL tÃƒÆ’Ã‚Â¹y ÃƒÆ’Ã‚Â½.
5. KhÃƒÆ’Ã‚Â´ng tin `userId` hoÃƒÂ¡Ã‚ÂºÃ‚Â·c `userRole` trong request body; phÃƒÂ¡Ã‚ÂºÃ‚Â£i lÃƒÂ¡Ã‚ÂºÃ‚Â¥y tÃƒÂ¡Ã‚Â»Ã‚Â« JWT.
6. KhÃƒÆ’Ã‚Â´ng gÃƒÂ¡Ã‚Â»Ã‚Â­i access token, API key hoÃƒÂ¡Ã‚ÂºÃ‚Â·c PII vÃƒÆ’Ã‚Â o prompt.
7. ChÃƒâ€ Ã‚Â°a mÃƒÂ¡Ã‚Â»Ã…Â¸ write tool khi read-only evaluation chÃƒâ€ Ã‚Â°a Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t yÃƒÆ’Ã‚Âªu cÃƒÂ¡Ã‚ÂºÃ‚Â§u.
8. MÃƒÂ¡Ã‚Â»Ã‚Âi write action sau nÃƒÆ’Ã‚Â y phÃƒÂ¡Ã‚ÂºÃ‚Â£i cÃƒÆ’Ã‚Â³ approval, audit vÃƒÆ’Ã‚Â  idempotency.
9. KhÃƒÆ’Ã‚Â´ng hiÃƒÂ¡Ã‚Â»Ã†â€™n thÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ hoÃƒÂ¡Ã‚ÂºÃ‚Â·c lÃƒâ€ Ã‚Â°u raw chain-of-thought.
10. MÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i phase phÃƒÂ¡Ã‚ÂºÃ‚Â£i Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t Definition of Done trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc khi chuyÃƒÂ¡Ã‚Â»Ã†â€™n phase.

---

# Phase 0 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â ChuÃƒÂ¡Ã‚ÂºÃ‚Â©n hÃƒÆ’Ã‚Â³a dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u vÃƒÆ’Ã‚Â  baseline

## MÃƒÂ¡Ã‚Â»Ã‚Â¥c tiÃƒÆ’Ã‚Âªu

BiÃƒÂ¡Ã‚ÂºÃ‚Â¿t dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u nÃƒÆ’Ã‚Â o lÃƒÆ’Ã‚Â  demo, dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u nÃƒÆ’Ã‚Â o lÃƒÆ’Ã‚Â  thÃƒÂ¡Ã‚ÂºÃ‚Â­t vÃƒÆ’Ã‚Â  dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u nÃƒÆ’Ã‚Â o Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ Ãƒâ€žÃ¢â‚¬ËœiÃƒÂ¡Ã‚Â»Ã‚Âu kiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n forecast.

## CÃƒÆ’Ã‚Â´ng viÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡c

### 0.1 PhÃƒÆ’Ã‚Â¢n biÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u demo vÃƒÆ’Ã‚Â  dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u thÃƒÂ¡Ã‚ÂºÃ‚Â­t

- [ ] ThÃƒÆ’Ã‚Âªm nguÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u cho order/sales snapshot: `DEMO`, `REAL`, `IMPORTED`.
- [ ] KhÃƒÆ’Ã‚Â´ng dÃƒÆ’Ã‚Â¹ng dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u demo Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¡nh giÃƒÆ’Ã‚Â¡ production model.
- [ ] BÃƒÆ’Ã‚Â¡o cÃƒÆ’Ã‚Â¡o metrics tÃƒÆ’Ã‚Â¡ch riÃƒÆ’Ã‚Âªng theo nguÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n.
- [ ] Ghi rÃƒÆ’Ã‚Âµ seed version vÃƒÆ’Ã‚Â  random seed cÃƒÂ¡Ã‚Â»Ã‚Â§a demo data.

NÃƒÂ¡Ã‚ÂºÃ‚Â¿u chÃƒâ€ Ã‚Â°a muÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœn thay Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i bÃƒÂ¡Ã‚ÂºÃ‚Â£ng `orders`, cÃƒÆ’Ã‚Â³ thÃƒÂ¡Ã‚Â»Ã†â€™ thÃƒÆ’Ã‚Âªm metadata ÃƒÂ¡Ã‚Â»Ã…Â¸ cÃƒÆ’Ã‚Â¡c bÃƒÂ¡Ã‚ÂºÃ‚Â£ng AI snapshot.

### 0.2 KiÃƒÂ¡Ã‚Â»Ã†â€™m tra pipeline sales snapshot

- [ ] XÃƒÆ’Ã‚Â¡c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh vÃƒÆ’Ã‚Â¬ sao chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° 55/1.159 SKU cÃƒÆ’Ã‚Â³ sales snapshot.
- [ ] XÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n ngÃƒÆ’Ã‚Â y khÃƒÆ’Ã‚Â´ng bÃƒÆ’Ã‚Â¡n Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c hiÃƒÂ¡Ã‚Â»Ã†â€™u lÃƒÆ’Ã‚Â  demand bÃƒÂ¡Ã‚ÂºÃ‚Â±ng 0, khÃƒÆ’Ã‚Â´ng phÃƒÂ¡Ã‚ÂºÃ‚Â£i missing data.
- [ ] TÃƒÂ¡Ã‚ÂºÃ‚Â¡o chuÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i ngÃƒÆ’Ã‚Â y liÃƒÆ’Ã‚Âªn tÃƒÂ¡Ã‚Â»Ã‚Â¥c cho mÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i SKU trong training window.
- [ ] KhÃƒÆ’Ã‚Â´ng ghi trÃƒÆ’Ã‚Â¹ng `(variant_id, sales_date)`.
- [ ] KiÃƒÂ¡Ã‚Â»Ã†â€™m tra timezone khi chuyÃƒÂ¡Ã‚Â»Ã†â€™n `created_at` thÃƒÆ’Ã‚Â nh ngÃƒÆ’Ã‚Â y bÃƒÆ’Ã‚Â¡n.
- [ ] LoÃƒÂ¡Ã‚ÂºÃ‚Â¡i cÃƒÆ’Ã‚Â¡c trÃƒÂ¡Ã‚ÂºÃ‚Â¡ng thÃƒÆ’Ã‚Â¡i Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â¡n khÃƒÆ’Ã‚Â´ng hÃƒÂ¡Ã‚Â»Ã‚Â£p lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ khÃƒÂ¡Ã‚Â»Ã‚Âi demand, vÃƒÆ’Ã‚Â­ dÃƒÂ¡Ã‚Â»Ã‚Â¥ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â¡n Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ hÃƒÂ¡Ã‚Â»Ã‚Â§y.

### 0.3 TÃƒâ€žÃ†â€™ng chÃƒÂ¡Ã‚ÂºÃ‚Â¥t lÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£ng inventory history

- [ ] ChÃƒÂ¡Ã‚ÂºÃ‚Â¡y snapshot tÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n kho theo lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ch hÃƒÂ¡Ã‚ÂºÃ‚Â±ng ngÃƒÆ’Ã‚Â y.
- [ ] Theo dÃƒÆ’Ã‚Âµi sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ ngÃƒÆ’Ã‚Â y snapshot thÃƒÆ’Ã‚Â nh cÃƒÆ’Ã‚Â´ng/thÃƒÂ¡Ã‚ÂºÃ‚Â¥t bÃƒÂ¡Ã‚ÂºÃ‚Â¡i.
- [ ] Thu thÃƒÂ¡Ã‚ÂºÃ‚Â­p tÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi thiÃƒÂ¡Ã‚Â»Ã†â€™u 60 ngÃƒÆ’Ã‚Â y trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc khi dÃƒÆ’Ã‚Â¹ng inventory anomaly model.
- [ ] CÃƒÂ¡Ã‚ÂºÃ‚Â£nh bÃƒÆ’Ã‚Â¡o nÃƒÂ¡Ã‚ÂºÃ‚Â¿u thiÃƒÂ¡Ã‚ÂºÃ‚Â¿u snapshot cÃƒÂ¡Ã‚Â»Ã‚Â§a mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t SKU.

### 0.4 HoÃƒÆ’Ã‚Â n thiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n inventory policy

- [ ] BÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung supplier cho tÃƒÂ¡Ã‚Â»Ã‚Â«ng SKU hoÃƒÂ¡Ã‚ÂºÃ‚Â·c nhÃƒÆ’Ã‚Â³m SKU.
- [ ] BÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung lead time thÃƒÂ¡Ã‚Â»Ã‚Â±c tÃƒÂ¡Ã‚ÂºÃ‚Â¿.
- [ ] BÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung MOQ.
- [ ] BÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung pack size.
- [ ] BÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung target cover days.
- [ ] Ghi nguÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n vÃƒÆ’Ã‚Â  thÃƒÂ¡Ã‚Â»Ã‚Âi Ãƒâ€žÃ¢â‚¬ËœiÃƒÂ¡Ã‚Â»Ã†â€™m cÃƒÂ¡Ã‚ÂºÃ‚Â­p nhÃƒÂ¡Ã‚ÂºÃ‚Â­t policy.
- [ ] KhÃƒÆ’Ã‚Â´ng dÃƒÆ’Ã‚Â¹ng mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t policy mÃƒÂ¡Ã‚ÂºÃ‚Â·c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh cho toÃƒÆ’Ã‚Â n bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ 1.159 SKU mÃƒÆ’Ã‚Â  khÃƒÆ’Ã‚Â´ng cÃƒÂ¡Ã‚ÂºÃ‚Â£nh bÃƒÆ’Ã‚Â¡o.

### 0.5 Data-quality report

TÃƒÂ¡Ã‚ÂºÃ‚Â¡o DTO/API trÃƒÂ¡Ã‚ÂºÃ‚Â£ vÃƒÂ¡Ã‚Â»Ã‚Â:

```json
{
  "variantId": "uuid",
  "historyDays": 178,
  "nonZeroDays": 38,
  "totalUnits": 72,
  "missingDays": 0,
  "daysSinceLastSale": 3,
  "inventorySnapshotDays": 60,
  "supplierConfigured": true,
  "qualityScore": 82,
  "qualityLevel": "HIGH",
  "warnings": []
}
```

### File dÃƒÂ¡Ã‚Â»Ã‚Â± kiÃƒÂ¡Ã‚ÂºÃ‚Â¿n

```text
ai_forecasting_service/src/main/java/.../dataquality/
  dto/SkuDataQualityResponse.java
  service/SkuDataQualityService.java
  controller/AdminDataQualityController.java

ai_forecasting_service/src/test/java/.../dataquality/
  SkuDataQualityServiceTest.java
```

### API dÃƒÂ¡Ã‚Â»Ã‚Â± kiÃƒÂ¡Ã‚ÂºÃ‚Â¿n

```text
GET /api/v1/admin/ai/data-quality/summary
GET /api/v1/admin/ai/data-quality/variants
GET /api/v1/admin/ai/data-quality/variants/{variantId}
```

## Definition of Done

- [ ] XÃƒÆ’Ã‚Â¡c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c DEMO/REAL cho dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u dÃƒÆ’Ã‚Â¹ng Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¡nh giÃƒÆ’Ã‚Â¡.
- [ ] Sales series liÃƒÆ’Ã‚Âªn tÃƒÂ¡Ã‚Â»Ã‚Â¥c vÃƒÆ’Ã‚Â  ngÃƒÆ’Ã‚Â y khÃƒÆ’Ã‚Â´ng bÃƒÆ’Ã‚Â¡n Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœiÃƒÂ¡Ã‚Â»Ã‚Ân 0.
- [ ] CÃƒÆ’Ã‚Â³ data-quality score cho 100% SKU.
- [ ] API liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t kÃƒÆ’Ã‚Âª rÃƒÆ’Ã‚Âµ SKU khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u.
- [ ] CÃƒÆ’Ã‚Â³ test cho missing day, duplicate day vÃƒÆ’Ã‚Â  cancelled order.

---

# Phase 1 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â PhÃƒÆ’Ã‚Â¢n loÃƒÂ¡Ã‚ÂºÃ‚Â¡i nhu cÃƒÂ¡Ã‚ÂºÃ‚Â§u SKU

## MÃƒÂ¡Ã‚Â»Ã‚Â¥c tiÃƒÆ’Ã‚Âªu

KhÃƒÆ’Ã‚Â´ng ÃƒÆ’Ã‚Â¡p dÃƒÂ¡Ã‚Â»Ã‚Â¥ng cÃƒÆ’Ã‚Â¹ng mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t thuÃƒÂ¡Ã‚ÂºÃ‚Â­t toÃƒÆ’Ã‚Â¡n forecast cho mÃƒÂ¡Ã‚Â»Ã‚Âi SKU.

## PhÃƒÆ’Ã‚Â¢n loÃƒÂ¡Ã‚ÂºÃ‚Â¡i Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â xuÃƒÂ¡Ã‚ÂºÃ‚Â¥t

```text
NO_DEMAND
NEW_ITEM
INTERMITTENT
ERRATIC
SMOOTH
GROWING
DECLINING
INSUFFICIENT_DATA
```

## CÃƒÆ’Ã‚Â´ng viÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡c

- [ ] TÃƒÆ’Ã‚Â­nh sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ ngÃƒÆ’Ã‚Â y lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ch sÃƒÂ¡Ã‚Â»Ã‚Â­.
- [ ] TÃƒÆ’Ã‚Â­nh sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ ngÃƒÆ’Ã‚Â y cÃƒÆ’Ã‚Â³ demand khÃƒÆ’Ã‚Â¡c 0.
- [ ] TÃƒÆ’Ã‚Â­nh ADI: khoÃƒÂ¡Ã‚ÂºÃ‚Â£ng cÃƒÆ’Ã‚Â¡ch trung bÃƒÆ’Ã‚Â¬nh giÃƒÂ¡Ã‚Â»Ã‚Â¯a cÃƒÆ’Ã‚Â¡c lÃƒÂ¡Ã‚ÂºÃ‚Â§n phÃƒÆ’Ã‚Â¡t sinh nhu cÃƒÂ¡Ã‚ÂºÃ‚Â§u.
- [ ] TÃƒÆ’Ã‚Â­nh CVÃƒâ€šÃ‚Â²: Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ biÃƒÂ¡Ã‚ÂºÃ‚Â¿n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng cÃƒÂ¡Ã‚Â»Ã‚Â§a demand khÃƒÆ’Ã‚Â¡c 0.
- [ ] TÃƒÆ’Ã‚Â­nh trend slope.
- [ ] Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh nghÃƒâ€žÃ‚Â©a threshold trong config, khÃƒÆ’Ã‚Â´ng hardcode rÃƒÂ¡Ã‚ÂºÃ‚Â£i rÃƒÆ’Ã‚Â¡c.
- [ ] LÃƒâ€ Ã‚Â°u classification vÃƒÆ’Ã‚Â  phiÃƒÆ’Ã‚Âªn bÃƒÂ¡Ã‚ÂºÃ‚Â£n thuÃƒÂ¡Ã‚ÂºÃ‚Â­t toÃƒÆ’Ã‚Â¡n.
- [ ] Cho phÃƒÆ’Ã‚Â©p chÃƒÂ¡Ã‚ÂºÃ‚Â¡y lÃƒÂ¡Ã‚ÂºÃ‚Â¡i classification theo batch.
- [ ] CÃƒÆ’Ã‚Â³ giÃƒÂ¡Ã‚ÂºÃ‚Â£i thÃƒÆ’Ã‚Â­ch tÃƒÂ¡Ã‚ÂºÃ‚Â¡i sao SKU thuÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢c nhÃƒÆ’Ã‚Â³m Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â³.

VÃƒÆ’Ã‚Â­ dÃƒÂ¡Ã‚Â»Ã‚Â¥:

```json
{
  "variantId": "uuid",
  "classification": "INTERMITTENT",
  "adi": 2.7,
  "cvSquared": 0.31,
  "trendSlope": 0.02,
  "confidence": "MEDIUM",
  "reason": "Nhu cÃƒÂ¡Ã‚ÂºÃ‚Â§u phÃƒÆ’Ã‚Â¡t sinh khÃƒÆ’Ã‚Â´ng liÃƒÆ’Ã‚Âªn tÃƒÂ¡Ã‚Â»Ã‚Â¥c, trung bÃƒÆ’Ã‚Â¬nh 2,7 ngÃƒÆ’Ã‚Â y/lÃƒÂ¡Ã‚ÂºÃ‚Â§n."
}
```

### File dÃƒÂ¡Ã‚Â»Ã‚Â± kiÃƒÂ¡Ã‚ÂºÃ‚Â¿n

```text
ai_forecasting_service/src/main/java/.../demand/
  entity/DemandClassification.java
  entity/DemandPattern.java
  service/DemandClassificationService.java
  dto/DemandClassificationResponse.java

backend/src/main/resources/db/migration/
  Vxx__admin_ai_demand_classification.sql
```

## Definition of Done

- [ ] 100% SKU Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c phÃƒÆ’Ã‚Â¢n loÃƒÂ¡Ã‚ÂºÃ‚Â¡i hoÃƒÂ¡Ã‚ÂºÃ‚Â·c trÃƒÂ¡Ã‚ÂºÃ‚Â£ `INSUFFICIENT_DATA`.
- [ ] KÃƒÂ¡Ã‚ÂºÃ‚Â¿t quÃƒÂ¡Ã‚ÂºÃ‚Â£ classification cÃƒÆ’Ã‚Â³ reason rÃƒÆ’Ã‚Âµ rÃƒÆ’Ã‚Â ng.
- [ ] CÃƒÆ’Ã‚Â³ unit test cho tÃƒÂ¡Ã‚Â»Ã‚Â«ng nhÃƒÆ’Ã‚Â³m demand.
- [ ] Classification cÃƒÆ’Ã‚Â³ version Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ tÃƒÆ’Ã‚Â¡i lÃƒÂ¡Ã‚ÂºÃ‚Â­p kÃƒÂ¡Ã‚ÂºÃ‚Â¿t quÃƒÂ¡Ã‚ÂºÃ‚Â£.

---

# Phase 2 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Forecast vÃƒÆ’Ã‚Â  backtest Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¡ng tin cÃƒÂ¡Ã‚ÂºÃ‚Â­y

## MÃƒÂ¡Ã‚Â»Ã‚Â¥c tiÃƒÆ’Ã‚Âªu

ChÃƒÂ¡Ã‚Â»Ã‚Ân thuÃƒÂ¡Ã‚ÂºÃ‚Â­t toÃƒÆ’Ã‚Â¡n theo dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u tÃƒÂ¡Ã‚Â»Ã‚Â«ng SKU vÃƒÆ’Ã‚Â  khÃƒÆ’Ã‚Â´ng trÃƒÂ¡Ã‚ÂºÃ‚Â£ forecast giÃƒÂ¡Ã‚ÂºÃ‚Â£ chÃƒÆ’Ã‚Â­nh xÃƒÆ’Ã‚Â¡c khi dÃƒÂ¡Ã‚Â»Ã‚Â¯
liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§.

## ThuÃƒÂ¡Ã‚ÂºÃ‚Â­t toÃƒÆ’Ã‚Â¡n

| Demand pattern | Candidate |
|---|---|
| `NO_DEMAND` | KhÃƒÆ’Ã‚Â´ng forecast; dÃƒÆ’Ã‚Â¹ng policy/rule |
| `NEW_ITEM` | Category/brand analogue hoÃƒÂ¡Ã‚ÂºÃ‚Â·c rule |
| `INTERMITTENT` | Croston, SBA hoÃƒÂ¡Ã‚ÂºÃ‚Â·c TSB |
| `SMOOTH` | Moving Average, EWMA |
| `GROWING`, `DECLINING` | Holt trend |
| `ERRATIC` | Robust median/quantile |

## CÃƒÆ’Ã‚Â´ng viÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡c

- [ ] ThÃƒÆ’Ã‚Âªm rolling-origin backtest.
- [ ] So sÃƒÆ’Ã‚Â¡nh candidate models trÃƒÆ’Ã‚Âªn cÃƒÆ’Ã‚Â¹ng training window.
- [ ] TÃƒÆ’Ã‚Â­nh MAE, WAPE vÃƒÆ’Ã‚Â  bias.
- [ ] KhÃƒÆ’Ã‚Â´ng dÃƒÆ’Ã‚Â¹ng WAPE mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t mÃƒÆ’Ã‚Â¬nh khi actual demand quÃƒÆ’Ã‚Â¡ nhÃƒÂ¡Ã‚Â»Ã‚Â.
- [ ] LÃƒâ€ Ã‚Â°u evaluation vÃƒÆ’Ã‚Â o `forecast_model_evaluations`.
- [ ] ChÃƒÂ¡Ã‚Â»Ã‚Ân best model theo tÃƒÂ¡Ã‚Â»Ã‚Â«ng SKU.
- [ ] LÃƒâ€ Ã‚Â°u model version vÃƒÆ’Ã‚Â  training window.
- [ ] TrÃƒÂ¡Ã‚ÂºÃ‚Â£ `INSUFFICIENT_DATA` thay vÃƒÆ’Ã‚Â¬ mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t con sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¡ng tin.
- [ ] ThiÃƒÂ¡Ã‚ÂºÃ‚Â¿t lÃƒÂ¡Ã‚ÂºÃ‚Â­p confidence tÃƒÂ¡Ã‚Â»Ã‚Â« data quality vÃƒÆ’Ã‚Â  backtest, khÃƒÆ’Ã‚Â´ng tÃƒÂ¡Ã‚Â»Ã‚Â« LLM.
- [ ] Theo dÃƒÆ’Ã‚Âµi forecast drift giÃƒÂ¡Ã‚Â»Ã‚Â¯a cÃƒÆ’Ã‚Â¡c lÃƒÂ¡Ã‚ÂºÃ‚Â§n chÃƒÂ¡Ã‚ÂºÃ‚Â¡y.

### Confidence gÃƒÂ¡Ã‚Â»Ã‚Â£i ÃƒÆ’Ã‚Â½

```text
HIGH
  - history >= 120 ngÃƒÆ’Ã‚Â y
  - non-zero days >= 30
  - Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ backtest windows
  - WAPE/bias Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t threshold

MEDIUM
  - history >= 60 ngÃƒÆ’Ã‚Â y
  - non-zero days >= 12
  - cÃƒÆ’Ã‚Â³ backtest

LOW
  - cÃƒÆ’Ã‚Â¡c trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âng hÃƒÂ¡Ã‚Â»Ã‚Â£p cÃƒÆ’Ã‚Â²n lÃƒÂ¡Ã‚ÂºÃ‚Â¡i nhÃƒâ€ Ã‚Â°ng vÃƒÂ¡Ã‚ÂºÃ‚Â«n cÃƒÆ’Ã‚Â³ thÃƒÂ¡Ã‚Â»Ã†â€™ tÃƒÆ’Ã‚Â­nh tham khÃƒÂ¡Ã‚ÂºÃ‚Â£o

INSUFFICIENT
  - khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ Ãƒâ€žÃ¢â‚¬ËœiÃƒÂ¡Ã‚Â»Ã‚Âu kiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n forecast
```

## Definition of Done

- [ ] `forecast_model_evaluations` cÃƒÆ’Ã‚Â³ dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u cho SKU Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ Ãƒâ€žÃ¢â‚¬ËœiÃƒÂ¡Ã‚Â»Ã‚Âu kiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n.
- [ ] KhÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â²n mÃƒÂ¡Ã‚ÂºÃ‚Â·c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh Moving Average cho gÃƒÂ¡Ã‚ÂºÃ‚Â§n nhÃƒâ€ Ã‚Â° toÃƒÆ’Ã‚Â n bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ catalog.
- [ ] MÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i forecast cÃƒÆ’Ã‚Â³ model, metrics, data-quality level vÃƒÆ’Ã‚Â  reason.
- [ ] Batch forecast khÃƒÆ’Ã‚Â´ng thÃƒÂ¡Ã‚ÂºÃ‚Â¥t bÃƒÂ¡Ã‚ÂºÃ‚Â¡i toÃƒÆ’Ã‚Â n bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ khi mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t SKU lÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i.
- [ ] CÃƒÆ’Ã‚Â³ benchmark so vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi naive baseline.

---

# Phase 3 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Inventory Decision Engine

## MÃƒÂ¡Ã‚Â»Ã‚Â¥c tiÃƒÆ’Ã‚Âªu

ChuyÃƒÂ¡Ã‚Â»Ã†â€™n forecast thÃƒÆ’Ã‚Â nh quyÃƒÂ¡Ã‚ÂºÃ‚Â¿t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh cÃƒÆ’Ã‚Â³ thÃƒÂ¡Ã‚Â»Ã†â€™ giÃƒÂ¡Ã‚ÂºÃ‚Â£i thÃƒÆ’Ã‚Â­ch vÃƒÆ’Ã‚Â  kiÃƒÂ¡Ã‚Â»Ã†â€™m chÃƒÂ¡Ã‚Â»Ã‚Â©ng.

## ChÃƒÂ¡Ã‚Â»Ã‚Â©c nÃƒâ€žÃ†â€™ng

### 3.1 Stockout risk

```text
expected_demand_during_lead_time
reorder_point
safety_stock
estimated_stockout_date
stockout_probability/risk level
```

### 3.2 Overstock risk

```text
days_of_supply
dead_stock_days
inventory_turnover
excess_quantity
excess_value
```

Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã†â€™ tÃƒÆ’Ã‚Â­nh `excess_value`, cÃƒÂ¡Ã‚ÂºÃ‚Â§n cÃƒÆ’Ã‚Â³ cost nhÃƒÂ¡Ã‚ÂºÃ‚Â­p hÃƒÆ’Ã‚Â ng. NÃƒÂ¡Ã‚ÂºÃ‚Â¿u chÃƒâ€ Ã‚Â°a cÃƒÆ’Ã‚Â³ cost, chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° trÃƒÂ¡Ã‚ÂºÃ‚Â£ sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ lÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£ng.

### 3.3 Replenishment recommendation

MÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i recommendation phÃƒÂ¡Ã‚ÂºÃ‚Â£i trÃƒÂ¡Ã‚ÂºÃ‚Â£:

```json
{
  "variantId": "uuid",
  "risk": "STOCKOUT",
  "severity": "CRITICAL",
  "availableQuantity": 4,
  "expectedDemandDuringLeadTime": 12,
  "safetyStock": 5,
  "reorderPoint": 17,
  "suggestedQuantity": 30,
  "confidence": "MEDIUM",
  "reasons": [
    "TÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n khÃƒÂ¡Ã‚ÂºÃ‚Â£ dÃƒÂ¡Ã‚Â»Ã‚Â¥ng thÃƒÂ¡Ã‚ÂºÃ‚Â¥p hÃƒâ€ Ã‚Â¡n reorder point",
    "Nhu cÃƒÂ¡Ã‚ÂºÃ‚Â§u gÃƒÂ¡Ã‚ÂºÃ‚Â§n Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¢y tÃƒâ€žÃ†â€™ng"
  ],
  "warnings": [
    "Lead time Ãƒâ€žÃ¢â‚¬Ëœang dÃƒÆ’Ã‚Â¹ng giÃƒÆ’Ã‚Â¡ trÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ mÃƒÂ¡Ã‚ÂºÃ‚Â·c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh"
  ]
}
```

### 3.4 What-if simulation

Input:

```text
lead time
service level
target cover days
MOQ
pack size
incoming stock
forecast horizon
```

Output:

```text
reorder point
safety stock
suggested quantity
estimated stockout date
before/after comparison
warnings
```

### API dÃƒÂ¡Ã‚Â»Ã‚Â± kiÃƒÂ¡Ã‚ÂºÃ‚Â¿n

```text
GET  /api/v1/admin/ai/inventory-risks
GET  /api/v1/admin/ai/inventory-risks/{variantId}
GET  /api/v1/admin/ai/replenishment/explanations/{id}
POST /api/v1/admin/ai/inventory/simulate
```

## Definition of Done

- [ ] MÃƒÂ¡Ã‚Â»Ã‚Âi recommendation truy ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c vÃƒÂ¡Ã‚Â»Ã‚Â input vÃƒÆ’Ã‚Â  formula.
- [ ] KhÃƒÆ’Ã‚Â´ng tÃƒÂ¡Ã‚ÂºÃ‚Â¡o recommendation tÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng cho dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u `INSUFFICIENT`.
- [ ] MOQ vÃƒÆ’Ã‚Â  pack size Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c ÃƒÆ’Ã‚Â¡p dÃƒÂ¡Ã‚Â»Ã‚Â¥ng Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng.
- [ ] CÃƒÆ’Ã‚Â³ test boundary cho tÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n bÃƒÂ¡Ã‚ÂºÃ‚Â±ng 0, demand bÃƒÂ¡Ã‚ÂºÃ‚Â±ng 0 vÃƒÆ’Ã‚Â  lead time thiÃƒÂ¡Ã‚ÂºÃ‚Â¿u.
- [ ] What-if khÃƒÆ’Ã‚Â´ng ghi thay Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i vÃƒÆ’Ã‚Â o database.

---

# Phase 4 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Admin Intelligence UI

## MÃƒÂ¡Ã‚Â»Ã‚Â¥c tiÃƒÆ’Ã‚Âªu

Admin sÃƒÂ¡Ã‚Â»Ã‚Â­ dÃƒÂ¡Ã‚Â»Ã‚Â¥ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c AI decision engine mÃƒÆ’Ã‚Â  chÃƒâ€ Ã‚Â°a cÃƒÂ¡Ã‚ÂºÃ‚Â§n chatbot.

## Trang Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â xuÃƒÂ¡Ã‚ÂºÃ‚Â¥t

```text
/inventory/ai-insights
```

## ThÃƒÆ’Ã‚Â nh phÃƒÂ¡Ã‚ÂºÃ‚Â§n

- [ ] Data-quality summary.
- [ ] SKU cÃƒÆ’Ã‚Â³ nguy cÃƒâ€ Ã‚Â¡ stockout.
- [ ] SKU cÃƒÆ’Ã‚Â³ nguy cÃƒâ€ Ã‚Â¡ overstock.
- [ ] Forecast confidence thÃƒÂ¡Ã‚ÂºÃ‚Â¥p.
- [ ] Forecast error cao.
- [ ] Recommendation Ãƒâ€žÃ¢â‚¬Ëœang chÃƒÂ¡Ã‚Â»Ã‚Â duyÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t.
- [ ] BÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ lÃƒÂ¡Ã‚Â»Ã‚Âc theo SKU, product, brand, category vÃƒÆ’Ã‚Â  risk.
- [ ] Chi tiÃƒÂ¡Ã‚ÂºÃ‚Â¿t forecast/backtest.
- [ ] Explanation panel.
- [ ] What-if simulator.
- [ ] Deep link sang product/inventory detail.

## Definition of Done

- [ ] Admin hiÃƒÂ¡Ã‚Â»Ã†â€™u Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c vÃƒÆ’Ã‚Â¬ sao hÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â xuÃƒÂ¡Ã‚ÂºÃ‚Â¥t nhÃƒÂ¡Ã‚ÂºÃ‚Â­p.
- [ ] UI hiÃƒÂ¡Ã‚Â»Ã†â€™n thÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ rÃƒÆ’Ã‚Âµ confidence vÃƒÆ’Ã‚Â  warning.
- [ ] KhÃƒÆ’Ã‚Â´ng biÃƒÂ¡Ã‚ÂºÃ‚Â¿n `LOW` confidence thÃƒÆ’Ã‚Â nh Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â xuÃƒÂ¡Ã‚ÂºÃ‚Â¥t chÃƒÂ¡Ã‚ÂºÃ‚Â¯c chÃƒÂ¡Ã‚ÂºÃ‚Â¯n.
- [ ] CÃƒÆ’Ã‚Â³ loading, empty, error vÃƒÆ’Ã‚Â  stale-data state.
- [ ] Responsive vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi layout admin hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n tÃƒÂ¡Ã‚ÂºÃ‚Â¡i.

---

# Phase 5 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Scaffold `chatbot-admin-service`

## MÃƒÂ¡Ã‚Â»Ã‚Â¥c tiÃƒÆ’Ã‚Âªu

TÃƒÂ¡Ã‚ÂºÃ‚Â¡o Admin Copilot read-only trÃƒÆ’Ã‚Âªn cÃƒÆ’Ã‚Â¡c API Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ ÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh.

## CÃƒÂ¡Ã‚ÂºÃ‚Â¥u trÃƒÆ’Ã‚Âºc

```text
chatbot-admin-service/
  app/
    api/
      chat.py
      health.py
      runs.py
      routes.py
    auth/
      jwt_verifier.py
      actor_context.py
    clients/
      backend_client.py
      forecasting_client.py
      report_client.py
    graph/
      state.py
      admin_graph.py
      routing.py
      nodes/
        authenticate.py
        input_guard.py
        classify_intent.py
        select_tool.py
        policy_guard.py
        tool_executor.py
        generate_answer.py
        validate_answer.py
        save_run.py
    tools/
      registry.py
      inventory/
      replenishment/
      reports/
      orders/
    policy/
      role_policy.py
      capability_policy.py
      limits.py
    memory/
      session_store.py
      redis_store.py
    observability/
      trace_logger.py
      metrics.py
    schemas/
    services/
      llm_client.py
      redaction_service.py
    config/
      settings.py
    main.py
  prompts/
  evaluations/
  tests/
    unit/
    integration/
    contract/
    security/
    evaluation/
  Dockerfile
  requirements.txt
  requirements-test.txt
  pytest.ini
  .env.example
  README.md
```

## Intent MVP

```text
INVENTORY_RISK
REPLENISHMENT_EXPLANATION
FORECAST_QUALITY
SALES_OVERVIEW
PRODUCT_PERFORMANCE
ORDER_OVERVIEW
WHAT_IF_SIMULATION
UNKNOWN
```

## Tool MVP

```text
get_data_quality_summary
get_inventory_risks
get_inventory_risk_detail
get_replenishment_suggestions
get_replenishment_detail
get_forecast_quality
get_sales_overview
get_product_performance
get_order_overview
simulate_inventory_policy
```

TÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ tool trong phase nÃƒÆ’Ã‚Â y phÃƒÂ¡Ã‚ÂºÃ‚Â£i lÃƒÆ’Ã‚Â  read-only.

## Graph MVP

```text
authenticate
  -> input_guard
  -> classify_intent
  -> select_tool
  -> policy_guard
  -> tool_executor
  -> generate_answer
  -> validate_answer
  -> save_run
```

ChÃƒâ€ Ã‚Â°a cÃƒÂ¡Ã‚ÂºÃ‚Â§n ReAct loop. Cho phÃƒÆ’Ã‚Â©p tÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi Ãƒâ€žÃ¢â‚¬Ëœa 1 primary tool vÃƒÆ’Ã‚Â  mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ secondary read tool
Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh nghÃƒâ€žÃ‚Â©a trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc.

## BÃƒÂ¡Ã‚ÂºÃ‚Â£o mÃƒÂ¡Ã‚ÂºÃ‚Â­t

- [ ] XÃƒÆ’Ã‚Â¡c minh JWT thÃƒÂ¡Ã‚ÂºÃ‚Â­t.
- [ ] LÃƒÂ¡Ã‚ÂºÃ‚Â¥y actor ID/role tÃƒÂ¡Ã‚Â»Ã‚Â« JWT.
- [ ] KhÃƒÆ’Ã‚Â´ng tin `userId` hoÃƒÂ¡Ã‚ÂºÃ‚Â·c `userRole` trong request.
- [ ] KhÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°a token vÃƒÆ’Ã‚Â o LLM prompt.
- [ ] Tool dÃƒÆ’Ã‚Â¹ng allowlist base URL.
- [ ] Backend/forecasting tiÃƒÂ¡Ã‚ÂºÃ‚Â¿p tÃƒÂ¡Ã‚Â»Ã‚Â¥c kiÃƒÂ¡Ã‚Â»Ã†â€™m tra `@PreAuthorize`.
- [ ] Redact PII vÃƒÆ’Ã‚Â  secret trong trace.
- [ ] Rate limit theo actor.

## Definition of Done

- [ ] Copilot trÃƒÂ¡Ã‚ÂºÃ‚Â£ lÃƒÂ¡Ã‚Â»Ã‚Âi hoÃƒÆ’Ã‚Â n toÃƒÆ’Ã‚Â n tÃƒÂ¡Ã‚Â»Ã‚Â« structured API result.
- [ ] MÃƒÂ¡Ã‚Â»Ã‚Âi sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u trong cÃƒÆ’Ã‚Â¢u trÃƒÂ¡Ã‚ÂºÃ‚Â£ lÃƒÂ¡Ã‚Â»Ã‚Âi truy ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c vÃƒÂ¡Ã‚Â»Ã‚Â tool result.
- [ ] Role khÃƒÆ’Ã‚Â´ng hÃƒÂ¡Ã‚Â»Ã‚Â£p lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ khÃƒÆ’Ã‚Â´ng gÃƒÂ¡Ã‚Â»Ã‚Âi Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c tool.
- [ ] Tool timeout khÃƒÆ’Ã‚Â´ng lÃƒÆ’Ã‚Â m chÃƒÂ¡Ã‚ÂºÃ‚Â¿t service.
- [ ] CÃƒÆ’Ã‚Â³ contract test vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi backend vÃƒÆ’Ã‚Â  forecasting service.
- [ ] CÃƒÆ’Ã‚Â³ ÃƒÆ’Ã‚Â­t nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t 50 evaluation cases.

---

# Phase 6 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â TÃƒÆ’Ã‚Â­ch hÃƒÂ¡Ã‚Â»Ã‚Â£p Admin Copilot UI

## Trang

```text
/admin-copilot
/chatbot-config
```

## `/admin-copilot`

- [ ] Chat UI.
- [ ] Suggested questions.
- [ ] Tool execution timeline.
- [ ] Source/data freshness.
- [ ] Confidence vÃƒÆ’Ã‚Â  warnings.
- [ ] Deep link tÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi inventory/product/order.
- [ ] Feedback Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng/sai.
- [ ] KhÃƒÆ’Ã‚Â´ng hiÃƒÂ¡Ã‚Â»Ã†â€™n thÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ raw chain-of-thought.

## `/chatbot-config`

- [ ] Model/provider.
- [ ] Prompt version.
- [ ] Max token vÃƒÆ’Ã‚Â  timeout.
- [ ] Tool enable/disable.
- [ ] Role permission.
- [ ] Run history.
- [ ] Token/cost metrics.
- [ ] Evaluation result.
- [ ] Draft/publish/rollback.

ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° `ADMIN` Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c truy cÃƒÂ¡Ã‚ÂºÃ‚Â­p `/chatbot-config`.

## Definition of Done

- [ ] Sales/Warehouse chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° thÃƒÂ¡Ã‚ÂºÃ‚Â¥y cÃƒÆ’Ã‚Â¡c tool Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng vai trÃƒÆ’Ã‚Â².
- [ ] Admin xem Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c tool trace Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ redact.
- [ ] UI phÃƒÆ’Ã‚Â¢n biÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t answer, warning, partial answer vÃƒÆ’Ã‚Â  error.
- [ ] CÃƒÆ’Ã‚Â³ feedback Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ xÃƒÆ’Ã‚Â¢y evaluation dataset.

---

# Phase 7 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â ReAct read-only

## Ãƒâ€žÃ‚ÂiÃƒÂ¡Ã‚Â»Ã‚Âu kiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n bÃƒÂ¡Ã‚ÂºÃ‚Â¯t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â§u

ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° bÃƒÂ¡Ã‚ÂºÃ‚Â¯t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â§u khi:

- [ ] Tool selection accuracy Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t ÃƒÆ’Ã‚Â­t nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t 90% trÃƒÆ’Ã‚Âªn evaluation dataset.
- [ ] Grounded numeric answer Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t ÃƒÆ’Ã‚Â­t nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t 95%.
- [ ] KhÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ lÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i role bypass.
- [ ] API decision engine Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ ÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh.

## Graph

```text
planner
  -> policy_guard
  -> tool_executor
  -> observe
  -> decide_next
       -> planner
       -> clarify
       -> final_answer
       -> limit_reached
```

## Guardrail

- [ ] TÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi Ãƒâ€žÃ¢â‚¬Ëœa 4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“6 bÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc.
- [ ] Timeout toÃƒÆ’Ã‚Â n run.
- [ ] Timeout tÃƒÂ¡Ã‚Â»Ã‚Â«ng tool.
- [ ] GiÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi hÃƒÂ¡Ã‚ÂºÃ‚Â¡n token vÃƒÆ’Ã‚Â  chi phÃƒÆ’Ã‚Â­.
- [ ] ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng gÃƒÂ¡Ã‚Â»Ã‚Âi lÃƒÂ¡Ã‚ÂºÃ‚Â·p cÃƒÆ’Ã‚Â¹ng tool/arguments.
- [ ] KhÃƒÆ’Ã‚Â´ng cho planner tÃƒÂ¡Ã‚ÂºÃ‚Â¡o tÃƒÆ’Ã‚Âªn tool ngoÃƒÆ’Ã‚Â i registry.
- [ ] ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° read-only tools.
- [ ] TrÃƒÂ¡Ã‚ÂºÃ‚Â£ partial answer khi hÃƒÂ¡Ã‚ÂºÃ‚Â¿t giÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi hÃƒÂ¡Ã‚ÂºÃ‚Â¡n.

## Definition of Done

- [ ] Multi-tool workflow Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t ÃƒÆ’Ã‚Â­t nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t 85% task success.
- [ ] KhÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ infinite loop.
- [ ] KhÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ unauthorized tool call.
- [ ] Trace Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ debug nhÃƒâ€ Ã‚Â°ng khÃƒÆ’Ã‚Â´ng chÃƒÂ¡Ã‚Â»Ã‚Â©a chain-of-thought hoÃƒÂ¡Ã‚ÂºÃ‚Â·c secret.

---

# Phase 8 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Approval-based write actions

## Ãƒâ€žÃ‚ÂiÃƒÂ¡Ã‚Â»Ã‚Âu kiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n bÃƒÂ¡Ã‚ÂºÃ‚Â¯t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â§u

- [ ] Read-only agent chÃƒÂ¡Ã‚ÂºÃ‚Â¡y ÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh.
- [ ] Audit log Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ hoÃƒÂ¡Ã‚ÂºÃ‚Â¡t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng.
- [ ] CÃƒÆ’Ã‚Â³ approval UI.
- [ ] Backend hÃƒÂ¡Ã‚Â»Ã¢â‚¬â€ trÃƒÂ¡Ã‚Â»Ã‚Â£ idempotency.
- [ ] CÃƒÆ’Ã‚Â³ test concurrent action.

## ThÃƒÂ¡Ã‚Â»Ã‚Â© tÃƒÂ¡Ã‚Â»Ã‚Â± mÃƒÂ¡Ã‚Â»Ã…Â¸ tool

1. Accept/adjust/dismiss replenishment.
2. Update inventory policy.
3. Generate/evaluate forecast.
4. Update order status.
5. Update shipping.
6. Inventory adjustment.

KhÃƒÆ’Ã‚Â´ng Ãƒâ€ Ã‚Â°u tiÃƒÆ’Ã‚Âªn:

- Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i role ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi dÃƒÆ’Ã‚Â¹ng.
- KhÃƒÆ’Ã‚Â³a tÃƒÆ’Ã‚Â i khoÃƒÂ¡Ã‚ÂºÃ‚Â£n.
- XÃƒÆ’Ã‚Â³a catalog.
- HoÃƒÆ’Ã‚Â n tiÃƒÂ¡Ã‚Â»Ã‚Ân tÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng.

## Quy tÃƒÂ¡Ã‚ÂºÃ‚Â¯c write tool

- [ ] MÃƒÂ¡Ã‚Â»Ã‚Âi write tool cÃƒÆ’Ã‚Â³ `riskLevel`.
- [ ] MÃƒÂ¡Ã‚Â»Ã‚Âi write tool yÃƒÆ’Ã‚Âªu cÃƒÂ¡Ã‚ÂºÃ‚Â§u approval.
- [ ] Approval gÃƒÂ¡Ã‚ÂºÃ‚Â¯n vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi immutable action payload hash.
- [ ] Approval cÃƒÆ’Ã‚Â³ thÃƒÂ¡Ã‚Â»Ã‚Âi hÃƒÂ¡Ã‚ÂºÃ‚Â¡n.
- [ ] Revalidate resource trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc khi execute.
- [ ] CÃƒÆ’Ã‚Â³ idempotency key.
- [ ] LÃƒâ€ Ã‚Â°u before/after snapshot.
- [ ] Ghi audit actor, tool, resource vÃƒÆ’Ã‚Â  kÃƒÂ¡Ã‚ÂºÃ‚Â¿t quÃƒÂ¡Ã‚ÂºÃ‚Â£.
- [ ] KhÃƒÆ’Ã‚Â´ng xÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n write action chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° bÃƒÂ¡Ã‚ÂºÃ‚Â±ng cÃƒÆ’Ã‚Â¢u chat ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng ÃƒÆ’Ã‚Â½ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â.

---

# 9. Database cho Admin Copilot

CÃƒÆ’Ã‚Â¡c bÃƒÂ¡Ã‚ÂºÃ‚Â£ng dÃƒÂ¡Ã‚Â»Ã‚Â± kiÃƒÂ¡Ã‚ÂºÃ‚Â¿n:

```text
admin_agent_config_versions
admin_agent_capabilities
admin_agent_runs
admin_agent_tool_calls
admin_agent_approvals
admin_agent_feedback
```

KhÃƒÆ’Ã‚Â´ng lÃƒâ€ Ã‚Â°u:

- Access token.
- Refresh token.
- API key.
- Password.
- Raw chain-of-thought.
- PII chÃƒâ€ Ã‚Â°a Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c redact trong trace.

---

# 10. Evaluation plan

## Dataset tÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi thiÃƒÂ¡Ã‚Â»Ã†â€™u

| NhÃƒÆ’Ã‚Â³m | SÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ case tÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi thiÃƒÂ¡Ã‚Â»Ã†â€™u |
|---|---:|
| Inventory risk | 15 |
| Replenishment explanation | 15 |
| Forecast quality | 10 |
| Sales/report | 10 |
| Order overview | 10 |
| What-if | 10 |
| Permission/security | 15 |
| Prompt injection | 10 |

## Metrics

```text
intent_accuracy
tool_selection_accuracy
tool_argument_accuracy
task_success_rate
grounded_numeric_accuracy
authorization_block_rate
hallucination_rate
latency_p50
latency_p95
token_cost_per_run
```

## Release gate Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â xuÃƒÂ¡Ã‚ÂºÃ‚Â¥t

```text
intent_accuracy             >= 90%
tool_selection_accuracy     >= 90%
grounded_numeric_accuracy   >= 95%
unauthorized tool execution  = 0
critical hallucination       = 0
read-only task success      >= 85%
```

---

# 11. ThÃƒÂ¡Ã‚Â»Ã‚Â© tÃƒÂ¡Ã‚Â»Ã‚Â± cÃƒÆ’Ã‚Â´ng viÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡c thÃƒÂ¡Ã‚Â»Ã‚Â±c tÃƒÂ¡Ã‚ÂºÃ‚Â¿

ThÃƒÂ¡Ã‚Â»Ã‚Â±c hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n theo Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng thÃƒÂ¡Ã‚Â»Ã‚Â© tÃƒÂ¡Ã‚Â»Ã‚Â±:

```text
1. Data quality
2. Demand classification
3. Forecast backtest vÃƒÆ’Ã‚Â  model selection
4. Inventory decision engine
5. Admin Intelligence UI
6. chatbot-admin-service read-only
7. Admin Copilot UI
8. ReAct read-only
9. Approval-based actions
```

KhÃƒÆ’Ã‚Â´ng lÃƒÆ’Ã‚Â m bÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc 8ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“9 khi bÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“7 chÃƒâ€ Ã‚Â°a ÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh.

---

# 12. Sprint Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â xuÃƒÂ¡Ã‚ÂºÃ‚Â¥t

## Sprint 1

- [ ] Data source DEMO/REAL.
- [ ] Sales continuous series.
- [ ] Data-quality service/API.
- [ ] Unit tests.

## Sprint 2

- [ ] Demand classification.
- [ ] ADI/CVÃƒâ€šÃ‚Â².
- [ ] Classification batch.
- [ ] Classification UI/filter.

## Sprint 3

- [ ] Rolling backtest.
- [ ] Model evaluation persistence.
- [ ] Model selection per SKU.
- [ ] Confidence calibration.

## Sprint 4

- [ ] Stockout/overstock risk.
- [ ] Recommendation explanation.
- [ ] What-if simulation.
- [ ] Decision engine tests.

## Sprint 5

- [ ] `/inventory/ai-insights`.
- [ ] Risk table/detail.
- [ ] Forecast quality panel.
- [ ] What-if UI.

## Sprint 6

- [ ] Scaffold `chatbot-admin-service`.
- [ ] JWT/RBAC.
- [ ] Read-only tool registry.
- [ ] Inventory/replenishment tools.

## Sprint 7

- [ ] Report/order tools.
- [ ] Grounded answer validator.
- [ ] Run/tool trace.
- [ ] Evaluation suite.

## Sprint 8

- [ ] `/admin-copilot`.
- [ ] `/chatbot-config`.
- [ ] Feedback.
- [ ] Metrics.

Sau Sprint 8 mÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¡nh giÃƒÆ’Ã‚Â¡ cÃƒÆ’Ã‚Â³ cÃƒÂ¡Ã‚ÂºÃ‚Â§n ReAct hay khÃƒÆ’Ã‚Â´ng.

---

# 13. KÃƒÂ¡Ã‚ÂºÃ‚Â¿t quÃƒÂ¡Ã‚ÂºÃ‚Â£ cuÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi cÃƒÆ’Ã‚Â¹ng mong Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â£i

Admin cÃƒÆ’Ã‚Â³ thÃƒÂ¡Ã‚Â»Ã†â€™:

- BiÃƒÂ¡Ã‚ÂºÃ‚Â¿t dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u SKU nÃƒÆ’Ã‚Â o Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ tin cÃƒÂ¡Ã‚ÂºÃ‚Â­y.
- BiÃƒÂ¡Ã‚ÂºÃ‚Â¿t SKU nÃƒÆ’Ã‚Â o cÃƒÆ’Ã‚Â³ nguy cÃƒâ€ Ã‚Â¡ thiÃƒÂ¡Ã‚ÂºÃ‚Â¿u hoÃƒÂ¡Ã‚ÂºÃ‚Â·c dÃƒâ€ Ã‚Â° hÃƒÆ’Ã‚Â ng.
- HiÃƒÂ¡Ã‚Â»Ã†â€™u tÃƒÂ¡Ã‚ÂºÃ‚Â¡i sao hÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â xuÃƒÂ¡Ã‚ÂºÃ‚Â¥t nhÃƒÂ¡Ã‚ÂºÃ‚Â­p.
- Xem chÃƒÂ¡Ã‚ÂºÃ‚Â¥t lÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£ng forecast vÃƒÆ’Ã‚Â  model Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c chÃƒÂ¡Ã‚Â»Ã‚Ân.
- ThÃƒÂ¡Ã‚Â»Ã‚Â­ thay Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i policy mÃƒÆ’Ã‚Â  khÃƒÆ’Ã‚Â´ng ghi dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u.
- HÃƒÂ¡Ã‚Â»Ã‚Âi bÃƒÂ¡Ã‚ÂºÃ‚Â±ng tiÃƒÂ¡Ã‚ÂºÃ‚Â¿ng ViÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t vÃƒÆ’Ã‚Â  nhÃƒÂ¡Ã‚ÂºÃ‚Â­n cÃƒÆ’Ã‚Â¢u trÃƒÂ¡Ã‚ÂºÃ‚Â£ lÃƒÂ¡Ã‚Â»Ã‚Âi cÃƒÆ’Ã‚Â³ nguÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n.
- DuyÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t hÃƒÆ’Ã‚Â nh Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng nhÃƒÂ¡Ã‚ÂºÃ‚Â¡y cÃƒÂ¡Ã‚ÂºÃ‚Â£m qua approval ÃƒÂ¡Ã‚Â»Ã…Â¸ phase cuÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi.

HÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c:

- TÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p hÃƒÆ’Ã‚Â ng dÃƒÂ¡Ã‚Â»Ã‚Â±a trÃƒÆ’Ã‚Âªn forecast confidence thÃƒÂ¡Ã‚ÂºÃ‚Â¥p.
- Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã†â€™ LLM tÃƒÂ¡Ã‚Â»Ã‚Â± tÃƒÆ’Ã‚Â­nh sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u nghiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡p vÃƒÂ¡Ã‚Â»Ã‚Â¥.
- Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã†â€™ agent gÃƒÂ¡Ã‚Â»Ã‚Âi SQL hoÃƒÂ¡Ã‚ÂºÃ‚Â·c URL tÃƒÆ’Ã‚Â¹y ÃƒÆ’Ã‚Â½.
- Cho phÃƒÆ’Ã‚Â©p vÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£t quyÃƒÂ¡Ã‚Â»Ã‚Ân backend.
- Che giÃƒÂ¡Ã‚ÂºÃ‚Â¥u cÃƒÂ¡Ã‚ÂºÃ‚Â£nh bÃƒÆ’Ã‚Â¡o chÃƒÂ¡Ã‚ÂºÃ‚Â¥t lÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£ng dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u.
## Phase 0 progress notes - 2026-07-25

- [x] Added `AiDataQualityProperties` typed thresholds bound from `AI_DATA_QUALITY_*` variables.
- [x] Added read-only Admin Data Quality API under `/api/v1/admin/ai/data-quality` with summary, variant list, and variant detail endpoints.
- [x] Added `SkuDataQualityService` scoring for continuous sales history, non-zero demand days, inventory snapshot coverage, supplier configuration, and warnings.
- [x] Data-quality evaluation treats missing sales snapshot days as a blocking issue because zero-demand days must be materialized as quantity 0.
- [x] Added unit coverage for missing sales days, high-quality complete series, supplier gaps, and inventory history gaps.
- [x] Verification: `.\mvnw.cmd -Dtest=SkuDataQualityServiceTest test` passed with 3 tests on 2026-07-25.
- [ ] Data source DEMO/REAL/IMPORTED is not yet added to snapshot metadata.
- [ ] Real 180-day seed remains intentionally not run until DB demo/development and backup are confirmed.

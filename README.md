# SportAI Commerce UI

Frontend scaffold theo `prompt.md`:

- `frontend/storefront`: Next.js Storefront theme Kinetic Sport.
- `frontend/admin`: Next.js Admin Panel theme Control Deck.

## Chạy local

```bash
cd frontend/storefront
npm install
npm run dev
```

Storefront chạy ở `http://localhost:3000`.

```bash
cd frontend/admin
npm install
npm run dev
```

Admin chạy ở `http://localhost:3001`.

## Phạm vi hiện tại

- UI chạy độc lập bằng mock data.
- Có token CSS riêng cho storefront và admin.
- Storefront có Home, AI Search, Product cards, Product Detail, Size Advisor, AI Review Summary, floating chatbot.
- Admin có sidebar, topbar, KPI cards, Recharts dashboard, inventory alert table, loading và empty states.

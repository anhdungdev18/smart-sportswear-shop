export default function Loading() {
  return (
    <main className="workspace">
      {/* Page title bar */}
      <section className="page-title">
        <div style={{ display: "grid", gap: 8 }}>
          <div className="skeleton" style={{ width: 180, height: 36 }} />
          <div className="skeleton" style={{ width: 300, height: 14 }} />
        </div>
        <div style={{ display: "flex", gap: 10 }}>
          <div className="skeleton" style={{ width: 110, height: 42, borderRadius: 14 }} />
          <div className="skeleton" style={{ width: 42, height: 42, borderRadius: 14 }} />
        </div>
      </section>

      {/* KPI cards */}
      <div className="kpi-grid">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="card kpi-card">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div className="skeleton" style={{ width: 80, height: 12 }} />
              <div className="skeleton" style={{ width: 28, height: 28, borderRadius: 8 }} />
            </div>
            <div className="skeleton" style={{ width: 120, height: 32 }} />
            <div className="skeleton" style={{ width: 70, height: 22, borderRadius: 999 }} />
          </div>
        ))}
      </div>

      {/* Main content panel */}
      <div className="card panel">
        {/* Toolbar */}
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 18, gap: 10 }}>
          <div style={{ display: "flex", gap: 8 }}>
            {[100, 80, 90, 70].map((w, i) => (
              <div key={i} className="skeleton" style={{ width: w, height: 36, borderRadius: 999 }} />
            ))}
          </div>
          <div className="skeleton" style={{ width: 200, height: 36, borderRadius: 12 }} />
        </div>

        {/* Table header */}
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "2fr 1fr 1fr 1fr 1fr",
            gap: 12,
            padding: "12px 12px",
            borderBottom: "1px solid var(--admin-line)",
          }}
        >
          {[120, 70, 80, 60, 80].map((w, i) => (
            <div key={i} className="skeleton" style={{ width: w, height: 11 }} />
          ))}
        </div>

        {/* Table rows */}
        {[1, 2, 3, 4, 5, 6, 7, 8].map((row) => (
          <div
            key={row}
            style={{
              display: "grid",
              gridTemplateColumns: "2fr 1fr 1fr 1fr 1fr",
              gap: 12,
              alignItems: "center",
              padding: "14px 12px",
              borderBottom: "1px solid var(--admin-line)",
            }}
          >
            {/* Product cell with thumbnail */}
            <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
              <div className="skeleton" style={{ width: 52, height: 52, flexShrink: 0, borderRadius: 12 }} />
              <div style={{ display: "grid", gap: 6 }}>
                <div className="skeleton" style={{ width: 140, height: 13 }} />
                <div className="skeleton" style={{ width: 90, height: 11 }} />
              </div>
            </div>
            <div className="skeleton" style={{ width: 60, height: 13 }} />
            <div className="skeleton" style={{ width: 55, height: 24, borderRadius: 999 }} />
            <div className="skeleton" style={{ width: 50, height: 13 }} />
            <div className="skeleton" style={{ width: 70, height: 13 }} />
          </div>
        ))}
      </div>
    </main>
  );
}

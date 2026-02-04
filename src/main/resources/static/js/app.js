(() => {
    const API_BASE = (window.API_BASE || document.documentElement.dataset.apiBase || "").trim();
    const endpoints = {
        list: "/api/telemetry"
    };

    const getBaseUrl = () => {
        if (!API_BASE) return window.location.origin;
        if (API_BASE.startsWith("http://") || API_BASE.startsWith("https://")) {
            return API_BASE.replace(/\/$/, "");
        }
        if (API_BASE.startsWith("/")) {
            return window.location.origin + API_BASE.replace(/\/$/, "");
        }
        return window.location.origin;
    };

    const fetchJson = async (path, query = {}) => {
        const url = new URL(getBaseUrl() + path);
        Object.entries(query).forEach(([k, v]) => {
            if (v !== undefined && v !== null && v !== "") {
                url.searchParams.set(k, v);
            }
        });
        const res = await fetch(url.toString(), { headers: { "Accept": "application/json" } });
        if (!res.ok) {
            throw new Error(`API ${res.status}: ${res.statusText}`);
        }
        return res.json();
    };

    const formatDate = (str) => {
        if (!str) return "—";
        const d = new Date(str);
        if (Number.isNaN(d.getTime())) return str;
        return d.toLocaleString();
    };

    const setText = (id, value) => {
        const el = document.getElementById(id);
        if (el) el.textContent = value;
    };

    const getDisplayDate = (item) => item?.received_at ?? null;

    const updateCards = (data) => {
        setText("card-datetime", formatDate(getDisplayDate(data)));
        setText("card-uid", data?.uid ? `UID: ${data.uid}` : "UID: —");
        setText("card-temp", Number.isFinite(data?.temp) ? `${Number(data.temp).toFixed(1)}` : "—");
        setText("card-hum", Number.isFinite(data?.hum) ? `${Number(data.hum).toFixed(1)}` : "—");
        const luzText = data?.luz === 1 ? "Encendida" : data?.luz === 0 ? "Apagada" : "—";
        setText("card-luz", luzText);
        const dot = document.getElementById("card-luz-dot");
        if (dot) {
            dot.className = "h-3 w-3 rounded-full " + (data?.luz === 1 ? "bg-emerald-400" : data?.luz === 0 ? "bg-slate-600" : "bg-slate-700");
        }
    };

    let telemetryChart;
    const renderChart = (series = []) => {
        const ctx = document.getElementById("telemetry-chart");
        if (!ctx || typeof Chart === "undefined") return;
        const labels = series.map(item => formatDate(getDisplayDate(item)));
        const tempData = series.map(item => item.temp ?? null);
        const humData = series.map(item => item.hum ?? null);
        if (telemetryChart) telemetryChart.destroy();
        telemetryChart = new Chart(ctx, {
            type: "line",
            data: {
                labels,
                datasets: [
                    {
                        label: "Temp (°C)",
                        data: tempData,
                        borderColor: "#22d3ee",
                        backgroundColor: "rgba(34, 211, 238, 0.1)",
                        tension: 0.35,
                        spanGaps: true
                    },
                    {
                        label: "Hum (%)",
                        data: humData,
                        borderColor: "#a855f7",
                        backgroundColor: "rgba(168, 85, 247, 0.1)",
                        tension: 0.35,
                        spanGaps: true
                    }
                ]
            },
            options: {
                plugins: { legend: { labels: { color: "#cbd5e1" } } },
                scales: {
                    x: { ticks: { color: "#94a3b8" }, grid: { color: "rgba(148,163,184,0.1)" } },
                    y: { ticks: { color: "#94a3b8" }, grid: { color: "rgba(148,163,184,0.1)" } }
                }
            }
        });
    };

    const toTimestamp = (value) => {
        if (!value) return 0;
        const t = new Date(value).getTime();
        return Number.isNaN(t) ? 0 : t;
    };

    const normalizeRows = (rows) => (Array.isArray(rows) ? rows : []);

    const loadDashboard = async () => {
        try {
            const rows = normalizeRows(await fetchJson(endpoints.list));
            const sorted = rows.slice().sort((a, b) => toTimestamp(getDisplayDate(a)) - toTimestamp(getDisplayDate(b)));
            const latest = sorted[sorted.length - 1];
            updateCards(latest || null);
            const recent = sorted.slice(-30);
            renderChart(recent);
        } catch (err) {
            console.error("No se pudo cargar los datos", err);
            updateCards(null);
            renderChart([]);
        }
    };

    const renderTable = (rows = []) => {
        const tbody = document.getElementById("table-body");
        const counter = document.getElementById("table-count");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!rows.length) {
            tbody.innerHTML = `<tr><td colspan="5" class="px-4 py-6 text-center text-slate-400">Sin datos</td></tr>`;
        } else {
            rows.forEach(item => {
                const tr = document.createElement("tr");
                tr.className = "hover:bg-slate-800/40";
                tr.innerHTML = `
                    <td class="px-4 py-3">${formatDate(getDisplayDate(item))}</td>
                    <td class="px-4 py-3 text-slate-300">${item.uid ?? "—"}</td>
                    <td class="px-4 py-3">${item.luz === 1 ? "Encendida" : item.luz === 0 ? "Apagada" : "—"}</td>
                    <td class="px-4 py-3">${item.temp ?? "—"}</td>
                    <td class="px-4 py-3">${item.hum ?? "—"}</td>
                `;
                tbody.appendChild(tr);
            });
        }
        if (counter) counter.textContent = rows.length;
    };

    const loadTable = async () => {
        const uid = document.getElementById("filter-uid")?.value?.trim();
        const from = document.getElementById("filter-from")?.value;
        const to = document.getElementById("filter-to")?.value;
        try {
            const rows = normalizeRows(await fetchJson(endpoints.list));
            const fromDate = from ? new Date(`${from}T00:00:00`) : null;
            const toDate = to ? new Date(`${to}T23:59:59.999`) : null;
            const filtered = rows.filter(item => {
                if (uid && String(item?.uid || "").toLowerCase().includes(uid.toLowerCase()) === false) {
                    return false;
                }
                const ts = toTimestamp(getDisplayDate(item));
                if (fromDate && ts < fromDate.getTime()) return false;
                if (toDate && ts > toDate.getTime()) return false;
                return true;
            });
            const sorted = filtered.slice().sort((a, b) => toTimestamp(getDisplayDate(b)) - toTimestamp(getDisplayDate(a)));
            renderTable(sorted);
        } catch (err) {
            console.error("No se pudo cargar la tabla", err);
            renderTable([]);
        }
    };

    document.addEventListener("DOMContentLoaded", () => {
        if (document.body.id === "dashboard") {
            document.getElementById("refresh-dashboard")?.addEventListener("click", loadDashboard);
            loadDashboard();
        }
        if (document.body.id === "telemetria") {
            document.getElementById("search-btn")?.addEventListener("click", loadTable);
            document.getElementById("clear-btn")?.addEventListener("click", () => renderTable([]));
        }
    });
})();

/**
 * AI-ChainID Unified Platform API Client
 * Seamlessly interfaces with Spring Boot Backend (:8080) and Python AI Engine (:8000 via Spring Boot proxy)
 */

const API_BASE = window.location.origin;

const Auth = {
    getToken() {
        return localStorage.getItem("aichainid_token");
    },
    getUser() {
        const user = localStorage.getItem("aichainid_user");
        return user ? JSON.parse(user) : null;
    },
    setAuth(token, user) {
        localStorage.setItem("aichainid_token", token);
        localStorage.setItem("aichainid_user", JSON.stringify(user));
        window.dispatchEvent(new CustomEvent("aichainid:auth-change", { detail: { isAuthenticated: true, user } }));
    },
    clear() {
        localStorage.removeItem("aichainid_token");
        localStorage.removeItem("aichainid_user");
        window.dispatchEvent(new CustomEvent("aichainid:auth-change", { detail: { isAuthenticated: false, user: null } }));
    },
    isAuthenticated() {
        return !!this.getToken();
    },
    requireAuth(redirectUrl = "/static/login.html") {
        if (!this.isAuthenticated()) {
            window.location.href = redirectUrl;
        }
    },
    async login(email, password) {
        const res = await apiFetch("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
        if (res && res.data && res.data.accessToken) {
            this.setAuth(res.data.accessToken, res.data);
            return res.data;
        }
        throw new Error(res?.message || "Invalid credentials");
    },
    logout() {
        this.clear();
        showToast("Signed out successfully", "info");
    }
};

async function apiFetch(endpoint, options = {}) {
    const url = endpoint.startsWith("http") ? endpoint : `${API_BASE}${endpoint}`;
    const headers = options.headers || {};

    if (!(options.body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
    }

    const token = Auth.getToken();
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(url, { ...options, headers });
        const data = await response.json().catch(() => null);

        if (response.status === 401) {
            Auth.clear();
            // Don't redirect, open login modal if available
            if (typeof openLoginModal === "function") {
                openLoginModal("Session expired. Please sign in again.");
            }
            throw new Error(data?.message || "Session expired or authentication required.");
        }

        if (!response.ok) {
            throw new Error(data?.message || `Request failed with status ${response.status}`);
        }

        return data;
    } catch (err) {
        console.error(`API Error [${endpoint}]:`, err);
        throw err;
    }
}

const AiApi = {
    async checkHealth() {
        try {
            const res = await apiFetch("/api/ai/health");
            return res?.data?.aiServiceReachable === true;
        } catch (e) {
            return false;
        }
    },
    async recommendPermissions(payload) {
        const res = await apiFetch("/api/ai/recommend-permissions", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        return res?.data;
    },
    async calculateRisk(payload) {
        const res = await apiFetch("/api/ai/risk-score", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        return res?.data;
    },
    async getRiskAlerts() {
        const res = await apiFetch("/api/ai/risk-alerts");
        return res?.data;
    }
};

// Global Toast notification utility
function showToast(message, type = "info") {
    let container = document.getElementById("toast-container");
    if (!container) {
        container = document.createElement("div");
        container.id = "toast-container";
        container.className = "fixed bottom-5 right-5 z-50 flex flex-col gap-2 max-w-sm pointer-events-none";
        document.body.appendChild(container);
    }

    const toast = document.createElement("div");
    const colors = {
        success: "bg-[#0d1625] text-emerald-400 border border-emerald-500/40 shadow-emerald-950/50",
        error: "bg-[#0d1625] text-rose-400 border border-rose-500/40 shadow-rose-950/50",
        info: "bg-[#0d1625] text-indigo-300 border border-indigo-500/40 shadow-indigo-950/50",
        warning: "bg-[#0d1625] text-amber-300 border border-amber-500/40 shadow-amber-950/50"
    };

    const icons = {
        success: "check_circle",
        error: "error",
        info: "info",
        warning: "warning"
    };

    toast.className = `p-3.5 rounded-xl shadow-2xl text-xs font-semibold flex items-center gap-3 backdrop-blur-md pointer-events-auto transition-all duration-300 transform translate-y-2 opacity-0 ${colors[type] || colors.info}`;
    toast.innerHTML = `
        <span class="material-symbols-outlined text-base shrink-0">${icons[type] || 'info'}</span>
        <span class="flex-1">${message}</span>
        <button onclick="this.parentElement.remove()" class="text-gray-400 hover:text-white text-xs ml-2">✕</button>
    `;
    container.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.remove("translate-y-2", "opacity-0");
    });

    setTimeout(() => {
        toast.classList.add("opacity-0", "translate-y-2");
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

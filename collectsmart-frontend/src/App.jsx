import {
    BrowserRouter,
    Routes,
    Route,
    NavLink,
    Navigate
} from "react-router-dom";

import { useCallback, useEffect, useState } from "react";

import API from "./services/api";
import { useAuth } from "./context/AuthContext";

import Login from "./pages/Login";
import ProtectedRoute from "./components/ProtectedRoute";

import LoanManagement from "./pages/LoanManagement";
import OfficerDashboard from "./pages/OfficerDashboard";
import FieldOfficerManagement from "./pages/FieldOfficerManagement";
import OutcomeLogging from "./pages/OutcomeLogging";
import ReminderGenerator from "./pages/ReminderGenerator";
import PromiseTracker from "./pages/PromiseTracker";
import CSVImport from "./pages/CSVImport";

// ======================================================
// MANAGER DASHBOARD
// ======================================================

function Dashboard() {

    const [dashboard, setDashboard] = useState(null);
    const [aging, setAging] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadData = useCallback(async () => {

        setLoading(true);
        setError("");

        try {

            const [summary, agingData] = await Promise.all([
                API.get("/manager/dashboard/summary"),
                API.get("/manager/dashboard/aging")
            ]);

            setDashboard(summary.data);
            setAging(agingData.data || {});

        } catch (err) {

            console.error("Dashboard loading error:", err);

            setError(
                "Unable to load dashboard data. Please try again."
            );

        } finally {

            setLoading(false);

        }

    }, []);

    useEffect(() => {
        loadData();
    }, [loadData]);

    if (loading && !dashboard) {
        return (
            <div className="p-8 text-center text-slate-500">
                Loading manager dashboard...
            </div>
        );
    }

    if (error && !dashboard) {
        return (
            <div className="p-8">
                <div className="bg-red-50 text-red-700 p-5 rounded-xl">
                    {error}

                    <button
                        onClick={loadData}
                        className="block mt-3 bg-red-600 text-white px-4 py-2 rounded-lg"
                    >
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    const cards = [
        {
            title: "Total Loans",
            value: dashboard?.totalLoans ?? 0,
            icon: "📄",
            color: "bg-blue-50 text-blue-700"
        },
        {
            title: "Overdue Loans",
            value: dashboard?.overdueLoans ?? 0,
            icon: "⚠️",
            color: "bg-red-50 text-red-700"
        },
        {
            title: "Assigned Loans",
            value: dashboard?.assignedLoans ?? 0,
            icon: "👥",
            color: "bg-green-50 text-green-700"
        },
        {
            title: "Unassigned Loans",
            value: dashboard?.unassignedLoans ?? 0,
            icon: "📋",
            color: "bg-orange-50 text-orange-700"
        },
        {
            title: "Pending Promises",
            value: dashboard?.pendingPromises ?? 0,
            icon: "📅",
            color: "bg-yellow-50 text-yellow-700"
        },
        {
            title: "Broken Promises",
            value: dashboard?.brokenPromises ?? 0,
            icon: "❌",
            color: "bg-rose-50 text-rose-700"
        },
        {
            title: "Kept Promises",
            value: dashboard?.keptPromises ?? 0,
            icon: "✅",
            color: "bg-emerald-50 text-emerald-700"
        },
        {
            title: "Blocked Contact Attempts",
            value: dashboard?.blockedContactAttempts ?? 0,
            icon: "🔒",
            color: "bg-purple-50 text-purple-700"
        }
    ];

    const overdueAmount = Number(
        dashboard?.totalOverdueAmount ?? 0
    );

    const agingEntries = Object.entries(aging);

    const maxAgingCount = Math.max(
        ...agingEntries.map(([, count]) => Number(count) || 0),
        1
    );

    return (

        <div className="p-6">

            <div className="flex flex-wrap justify-between items-center gap-4 mb-6">

                <div>
                    <h1 className="text-3xl font-bold text-slate-800">
                        Manager Dashboard
                    </h1>

                    <p className="text-slate-500 mt-2">
                        Monitor collections, repayment commitments and field operations.
                    </p>
                </div>

                <button
                    onClick={loadData}
                    disabled={loading}
                    className="bg-blue-600 text-white px-5 py-3 rounded-lg hover:bg-blue-700 disabled:opacity-50"
                >
                    {loading ? "Refreshing..." : "↻ Refresh Dashboard"}
                </button>

            </div>

            {error && (
                <div className="bg-orange-50 text-orange-700 p-3 rounded-lg mb-5">
                    {error}
                </div>
            )}

            {/* Summary Cards */}

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">

                {cards.map((card) => (

                    <div
                        key={card.title}
                        className="bg-white rounded-xl shadow-sm p-5 border border-slate-100 hover:shadow-md transition"
                    >

                        <div className="flex justify-between items-center">

                            <p className="text-slate-500 text-sm font-medium">
                                {card.title}
                            </p>

                            <span className={`text-2xl p-2 rounded-lg ${card.color}`}>
                                {card.icon}
                            </span>

                        </div>

                        <h2 className="text-3xl font-bold text-slate-800 mt-4">
                            {card.value}
                        </h2>

                    </div>

                ))}

            </div>

            {/* Overdue Amount */}

            <div className="bg-white p-6 rounded-xl shadow-sm mt-6 border border-slate-100">

                <p className="text-slate-500 font-medium">
                    Total Outstanding Overdue Amount
                </p>

                <h2 className="text-4xl font-bold text-red-600 mt-3">
                    ₹{overdueAmount.toLocaleString("en-IN")}
                </h2>

            </div>

            {/* Loan Aging */}

            <div className="bg-white p-6 rounded-xl shadow-sm mt-6 border border-slate-100">

                <h2 className="text-xl font-bold text-slate-800 mb-2">
                    Loan Aging Analysis
                </h2>

                <p className="text-sm text-slate-500 mb-6">
                    Distribution of overdue loans based on days past due.
                </p>

                {agingEntries.length > 0 ? (

                    agingEntries.map(([bucket, count]) => {

                        const numericCount = Number(count) || 0;

                        const percentage = Math.min(
                            (numericCount / maxAgingCount) * 100,
                            100
                        );

                        return (

                            <div key={bucket} className="mb-6">

                                <div className="flex justify-between mb-2">

                                    <span className="font-medium text-slate-700">
                                        {bucket}
                                    </span>

                                    <span className="font-bold text-slate-800">
                                        {numericCount} loans
                                    </span>

                                </div>

                                <div className="bg-slate-100 rounded-full h-4 overflow-hidden">

                                    <div
                                        className="bg-blue-600 h-4 rounded-full transition-all duration-500"
                                        style={{
                                            width: `${percentage}%`
                                        }}
                                    />

                                </div>

                            </div>

                        );

                    })

                ) : (

                    <p className="text-slate-500">
                        No loan aging data available.
                    </p>

                )}

            </div>

            {/* Collection Performance */}

            <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mt-6">

                <div className="bg-white rounded-xl shadow-sm p-5 border-l-4 border-yellow-500">
                    <p className="text-slate-500">Pending Promises</p>

                    <h2 className="text-3xl font-bold text-yellow-600 mt-2">
                        {dashboard?.pendingPromises ?? 0}
                    </h2>
                </div>

                <div className="bg-white rounded-xl shadow-sm p-5 border-l-4 border-red-500">
                    <p className="text-slate-500">Broken Promises</p>

                    <h2 className="text-3xl font-bold text-red-600 mt-2">
                        {dashboard?.brokenPromises ?? 0}
                    </h2>
                </div>

                <div className="bg-white rounded-xl shadow-sm p-5 border-l-4 border-green-500">
                    <p className="text-slate-500">Kept Promises</p>

                    <h2 className="text-3xl font-bold text-green-600 mt-2">
                        {dashboard?.keptPromises ?? 0}
                    </h2>
                </div>

            </div>

        </div>

    );

}

// ======================================================
// APPLICATION LAYOUT
// ======================================================

function AppLayout() {

    const { user, logout } = useAuth();

    const isManager = user?.role === "MANAGER";

    // Manager navigation
    const managerNavItems = [
        { path: "/", label: "📊 Dashboard", end: true },
        { path: "/loans", label: "📄 Loan Management" },
        { path: "/officer", label: "👤 Field Officer Management" },
        { path: "/outcomes", label: "📝 Outcome Logging" },
        { path: "/promises", label: "📅 Promise Tracker" },
        { path: "/reminders", label: "✉️ Reminder Generator" },
        { path: "/csv-import", label: "📁 CSV Import" }
    ];

    // Field Officer navigation
    const officerNavItems = [
        { path: "/officer", label: "👤 My Loans", end: true },
        { path: "/outcomes", label: "📝 Outcome Logging" },
        { path: "/promises", label: "📅 Promise Tracker" },
        { path: "/reminders", label: "✉️ Reminder Generator" }
    ];

    const navItems = isManager
        ? managerNavItems
        : officerNavItems;

    return (

        <div className="min-h-screen flex bg-slate-100">

            <aside className="w-64 bg-slate-900 text-white p-6 hidden md:block">

                <h1 className="text-2xl font-bold text-blue-400">
                    CollectSmart
                </h1>

                <p className="text-xs text-slate-400 mb-10">
                    AI COLLECTION PLATFORM
                </p>

                <nav className="space-y-3">

                    {navItems.map((item) => (

                        <NavLink
                            key={item.path}
                            to={item.path}
                            end={item.end}
                            className={({ isActive }) =>
                                `block p-3 rounded-lg transition ${
                                    isActive
                                        ? "bg-blue-600"
                                        : "hover:bg-slate-800"
                                }`
                            }
                        >
                            {item.label}
                        </NavLink>

                    ))}

                </nav>

                <p className="mt-20 text-sm text-slate-400">
                    CollectSmart AI v1.0
                </p>

            </aside>

            <main className="flex-1 min-w-0">

                <header className="bg-white p-4 flex justify-between items-center shadow-sm">

                    <div>

                        <h2 className="font-semibold text-slate-800">
                            Collection Management
                        </h2>

                        <p className="text-sm text-slate-500">
                            {user?.email} ({user?.role})
                        </p>

                    </div>

                    <button
                        onClick={logout}
                        className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700"
                    >
                        Logout
                    </button>

                </header>

                <Routes>

                    {isManager ? (

                        <>
                            <Route path="/" element={<Dashboard />} />

                            <Route
                                path="/loans"
                                element={<LoanManagement />}
                            />

                            {/* Manager sees officer management page */}
                            <Route
                                path="/officer"
                                element={<FieldOfficerManagement />}
                            />

                            <Route
                                path="/outcomes"
                                element={<OutcomeLogging />}
                            />

                            <Route
                                path="/promises"
                                element={<PromiseTracker />}
                            />

                            <Route
                                path="/reminders"
                                element={<ReminderGenerator />}
                            />

                            <Route
                                path="/csv-import"
                                element={<CSVImport />}
                            />
                        </>

                    ) : (

                        <>
                            {/* Field Officer sees only personal loans */}
                            <Route
                                path="/officer"
                                element={<OfficerDashboard />}
                            />

                            <Route
                                path="/outcomes"
                                element={<OutcomeLogging />}
                            />

                            <Route
                                path="/promises"
                                element={<PromiseTracker />}
                            />

                            <Route
                                path="/reminders"
                                element={<ReminderGenerator />}
                            />
                        </>

                    )}

                    <Route
                        path="*"
                        element={
                            <Navigate
                                to={isManager ? "/" : "/officer"}
                                replace
                            />
                        }
                    />

                </Routes>

            </main>

        </div>

    );

}

// ======================================================
// ROOT APP WITH AUTHENTICATION
// ======================================================

export default function App() {

    return (

        <BrowserRouter>

            <Routes>

                {/* Public login route */}

                <Route
                    path="/login"
                    element={<Login />}
                />

                {/* All authenticated routes */}

                <Route
                    element={
                        <ProtectedRoute
                            allowedRoles={["MANAGER", "FIELD_OFFICER"]}
                        />
                    }
                >

                    <Route
                        path="/*"
                        element={<AppLayout />}
                    />

                </Route>

                {/* Unknown routes */}

                <Route
                    path="*"
                    element={<Navigate to="/login" replace />}
                />

            </Routes>

        </BrowserRouter>

    );

}

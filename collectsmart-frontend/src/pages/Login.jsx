import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
const [email, setEmail] = useState("");
const [password, setPassword] = useState("");
const [showPassword, setShowPassword] = useState(false);
const [error, setError] = useState("");

const { login, loading } = useAuth();
const navigate = useNavigate();

const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
        const user = await login(email.trim(), password);

        if (user.role === "MANAGER") {
            navigate("/", { replace: true });
        } else if (user.role === "FIELD_OFFICER") {
            navigate("/officer", { replace: true });
        } else {
            setError("Your account does not have a valid role.");
        }
    } catch (err) {
        console.error("Login error:", err);
        setError(
            err.response?.data?.message ||
            "Invalid email or password. Please try again."
        );
    }
};

return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4 sm:p-6 lg:p-10">

        <div className="w-full max-w-6xl min-h-[650px] bg-white rounded-3xl overflow-hidden shadow-2xl grid lg:grid-cols-2">

            {/* LEFT BRANDING SECTION */}
            <div className="relative hidden lg:flex flex-col justify-between overflow-hidden bg-gradient-to-br from-blue-700 via-blue-800 to-indigo-950 p-12 text-white">

                {/* Background decoration */}
                <div className="absolute -top-32 -right-32 w-96 h-96 bg-blue-400/20 rounded-full blur-3xl" />
                <div className="absolute -bottom-32 -left-20 w-80 h-80 bg-indigo-400/20 rounded-full blur-3xl" />

                <div className="relative z-10">

                    {/* Logo */}
                    <div className="flex items-center gap-4">
                        <div className="w-14 h-14 bg-white rounded-xl flex items-center justify-center text-blue-700 text-3xl font-black shadow-lg">
                            C
                        </div>

                        <div>
                            <h2 className="text-3xl font-bold tracking-tight">
                                CollectSmart
                            </h2>
                            <p className="text-blue-200 text-sm tracking-[3px]">
                                AI COLLECTION PLATFORM
                            </p>
                        </div>
                    </div>

                    {/* Main heading */}
                    <div className="mt-24">

                        <div className="inline-flex items-center gap-2 bg-white/10 border border-white/20 px-4 py-2.5 rounded-full text-base text-blue-100 mb-7">
                            <span className="w-2 h-2 bg-green-400 rounded-full animate-pulse" />
                            Intelligent Collection Management
                        </div>

                        <h1 className="text-6xl font-bold leading-tight">
                            Smarter
                            <br />
                            Collections.
                            <br />
                            <span className="text-blue-300">
                                Better Results.
                            </span>
                        </h1>

                        <p className="text-blue-100/80 text-xl leading-relaxed mt-6 max-w-md">
                            Simplify field operations, prioritize overdue
                            loans and manage collection activities with
                            intelligent insights.
                        </p>
                    </div>

                    {/* Feature cards */}
                    <div className="grid grid-cols-2 gap-4 mt-12">

                        <div className="bg-white/10 border border-white/15 rounded-2xl p-5 backdrop-blur-sm">
                            <div className="text-3xl mb-3">📊</div>
                            <h3 className="text-lg font-bold">
                                Smart Prioritization
                            </h3>
                            <p className="text-blue-200 text-base mt-2">
                                Identify high-priority loans.
                            </p>
                        </div>

                        <div className="bg-white/10 border border-white/15 rounded-2xl p-5 backdrop-blur-sm">
                            <div className="text-3xl mb-3">📍</div>
                            <h3 className="text-lg font-bold">
                                Field Tracking
                            </h3>
                            <p className="text-blue-200 text-base mt-2">
                                Monitor collection activities.
                            </p>
                        </div>

                        <div className="bg-white/10 border border-white/15 rounded-2xl p-5 backdrop-blur-sm">
                            <div className="text-3xl mb-3">🤝</div>
                            <h3 className="text-lg font-bold">
                                Promise Tracking
                            </h3>
                            <p className="text-blue-200 text-base mt-2">
                                Track payment commitments.
                            </p>
                        </div>

                        <div className="bg-white/10 border border-white/15 rounded-2xl p-5 backdrop-blur-sm">
                            <div className="text-3xl mb-3">🔒</div>
                            <h3 className="text-lg font-bold">
                                Secure Access
                            </h3>
                            <p className="text-blue-200 text-base mt-2">
                                Role-based authentication.
                            </p>
                        </div>

                    </div>
                </div>

                {/* Branding footer */}
                <div className="relative z-10 flex justify-between items-center text-blue-200/70 text-sm mt-12">
                    <span>CollectSmart AI v1.0</span>
                    <span>Smart Field Collection Management</span>
                </div>
            </div>

            {/* RIGHT LOGIN SECTION */}
            <div className="flex items-center justify-center bg-slate-50 px-6 py-12 sm:px-12 lg:px-14">

                <div className="w-full max-w-md">

                    {/* Mobile logo */}
                    <div className="lg:hidden flex justify-center mb-8">
                        <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center text-white text-4xl font-bold shadow-lg shadow-blue-200">
                            C
                        </div>
                    </div>

                    {/* Welcome section */}
                    <div className="mb-9">

                        <div className="inline-flex items-center gap-2 text-blue-600 bg-blue-50 px-4 py-2 rounded-full text-sm font-semibold mb-5">
                            <span className="w-2 h-2 bg-green-500 rounded-full" />
                            SECURE COLLECTION PORTAL
                        </div>

                        <h1 className="text-4xl sm:text-5xl font-bold text-slate-900 tracking-tight">
                            Welcome back
                        </h1>

                        <p className="text-slate-500 text-lg mt-3 leading-relaxed">
                            Sign in to continue to your CollectSmart
                            workspace.
                        </p>
                    </div>

                    {/* Login form */}
                    <form onSubmit={handleSubmit} className="space-y-6">

                        {/* Email */}
                        <div>
                            <label className="block text-base font-semibold text-slate-700 mb-3">
                                Email Address
                            </label>

                            <div className="relative">

                                <svg
                                    className="absolute left-4 top-1/2 -translate-y-1/2 w-6 h-6 text-slate-400"
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth="1.8"
                                        d="M3 8l9 6 9-6M5 5h14a2 2 0 012 2v10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2z"
                                    />
                                </svg>

                                <input
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    placeholder="name@company.com"
                                    autoComplete="username"
                                    required
                                    className="w-full bg-white border border-slate-200 rounded-xl pl-12 pr-4 py-4 text-lg text-slate-800 placeholder:text-slate-400 outline-none transition-all focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                                />
                            </div>
                        </div>

                        {/* Password */}
                        <div>
                            <label className="block text-base font-semibold text-slate-700 mb-3">
                                Password
                            </label>

                            <div className="relative">

                                <svg
                                    className="absolute left-4 top-1/2 -translate-y-1/2 w-6 h-6 text-slate-400"
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth="1.8"
                                        d="M7 10V7a5 5 0 0110 0v3m-11 0h12a2 2 0 012 2v7a2 2 0 01-2 2H6a2 2 0 01-2-2v-7a2 2 0 012-2z"
                                    />
                                </svg>

                                <input
                                    type={showPassword ? "text" : "password"}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="Enter your password"
                                    autoComplete="current-password"
                                    required
                                    className="w-full bg-white border border-slate-200 rounded-xl pl-12 pr-12 py-4 text-lg text-slate-800 placeholder:text-slate-400 outline-none transition-all focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                                />

                                {/* Password visibility toggle */}
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-blue-600 transition-colors"
                                    aria-label={showPassword ? "Hide password" : "Show password"}
                                >
                                    {showPassword ? (
                                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" d="M3 3l18 18M10.6 10.6a2 2 0 002.8 2.8M9.9 5.2A10.7 10.7 0 0112 5c5 0 8.5 4.5 9 7-.2 1.1-1.1 2.7-2.6 4M6.2 6.2C4.1 7.5 2.5 9.7 2 12c.5 2.5 4 7 10 7 1 0 2-.2 2.9-.5" />
                                        </svg>
                                    ) : (
                                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z" />
                                            <circle cx="12" cy="12" r="3" strokeWidth="1.8" />
                                        </svg>
                                    )}
                                </button>
                            </div>
                        </div>

                        {/* Error message */}
                        {error && (
                            <div
                                role="alert"
                                className="flex items-start gap-3 bg-red-50 border border-red-200 text-red-700 p-4 rounded-xl text-base"
                            >
                                <span className="font-bold">!</span>
                                <span>{error}</span>
                            </div>
                        )}

                        {/* Sign In button */}
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full flex items-center justify-center gap-2 bg-blue-600 text-white text-lg font-bold py-4 rounded-xl shadow-lg shadow-blue-200 hover:bg-blue-700 hover:shadow-xl hover:-translate-y-0.5 transition-all duration-200 disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:translate-y-0"
                        >
                            {loading ? (
                                <>
                                    <svg className="animate-spin w-6 h-6" viewBox="0 0 24 24" fill="none">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
                                    </svg>
                                    Signing in...
                                </>
                            ) : (
                                <>
                                    Sign In
                                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 12h14m-6-6l6 6-6 6" />
                                    </svg>
                                </>
                            )}
                        </button>

                    </form>

                    {/* Footer */}
                    <div className="mt-9 pt-6 border-t border-slate-200">

                        <div className="flex items-center justify-center gap-2 text-slate-500 text-sm">
                            <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 3l8 4v5c0 5-3.5 8-8 9-4.5-1-8-4-8-9V7l8-4z" />
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4" />
                            </svg>
                            Secure access for Managers and Field Officers
                        </div>

                        <p className="text-center text-sm text-slate-400 mt-4">
                            © 2026 CollectSmart AI. All rights reserved.
                        </p>

                    </div>

                </div>
            </div>

        </div>
    </div>
);

}
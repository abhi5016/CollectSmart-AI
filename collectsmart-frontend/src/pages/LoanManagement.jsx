import { useCallback, useEffect, useMemo, useState } from "react";
import API from "../services/api";

function LoanManagement() {
const [loans, setLoans] = useState([]);
const [search, setSearch] = useState("");
const [pincode, setPincode] = useState("");
const [sortByPriority, setSortByPriority] = useState(true);

const [loading, setLoading] = useState(true);
const [updatingId, setUpdatingId] = useState(null);

const [error, setError] = useState("");
const [message, setMessage] = useState("");
const [messageType, setMessageType] = useState("success");

// Fetch all loans
const fetchLoans = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
        const response = await API.get("/loans");

        setLoans(
            Array.isArray(response.data) ? response.data : []
        );
    } catch (err) {
        console.error("Failed to fetch loans:", err);

        setError(
            err.response?.data?.message ||
            "Unable to load loans. Please check the backend connection."
        );
    } finally {
        setLoading(false);
    }
}, []);

useEffect(() => {
    fetchLoans();
}, [fetchLoans]);

// Display messages
const showMessage = (text, type = "success") => {
    setMessage(text);
    setMessageType(type);
};

// Calculate priority for one loan
const calculatePriority = async (loanId) => {
    setUpdatingId(loanId);
    setMessage("");
    setError("");

    try {
        await API.post(`/loans/${loanId}/calculate-priority`);

        await fetchLoans();

        showMessage(
            `Priority recalculated successfully for Loan ID ${loanId}.`
        );
    } catch (err) {
        showMessage(
            err.response?.data?.message ||
            "Unable to calculate loan priority.",
            "error"
        );
    } finally {
        setUpdatingId(null);
    }
};

// Calculate priority for all overdue loans
const calculateAllPriorities = async () => {
    setUpdatingId("all");
    setMessage("");
    setError("");

    try {
        await API.post("/loans/calculate-all-priorities");

        await fetchLoans();

        showMessage(
            "Priority scores recalculated successfully for all overdue loans."
        );
    } catch (err) {
        showMessage(
            err.response?.data?.message ||
            "Unable to calculate all loan priorities.",
            "error"
        );
    } finally {
        setUpdatingId(null);
    }
};

// Automatically assign an unassigned loan
const assignLoan = async (loanId) => {
    setUpdatingId(loanId);
    setMessage("");
    setError("");

    try {
        const response = await API.post(
            `/loans/${loanId}/assign`
        );

        const assignedOfficerId =
            response.data?.assignedOfficerId;

        await fetchLoans();

        showMessage(
            assignedOfficerId
                ? `Loan assigned successfully to Officer ${assignedOfficerId}.`
                : "Loan assigned successfully."
        );

    } catch (err) {
        const errorMessage =
            typeof err.response?.data === "string"
                ? err.response.data
                : err.response?.data?.message;

        showMessage(
            errorMessage ||
            "Unable to assign loan. No eligible officer may be available.",
            "error"
        );
    } finally {
        setUpdatingId(null);
    }
};

// Search and filter loans
const filteredLoans = useMemo(() => {
    const searchText = search.trim().toLowerCase();
    const pincodeText = pincode.trim();

    const result = loans.filter((loan) => {
        const borrowerName =
            String(loan.borrowerName || "").toLowerCase();

        const accountNumber =
            String(loan.loanAccountNumber || "").toLowerCase();

        const loanPincode =
            String(loan.pincode || "");

        const matchesSearch =
            !searchText ||
            borrowerName.includes(searchText) ||
            accountNumber.includes(searchText);

        const matchesPincode =
            !pincodeText ||
            loanPincode.includes(pincodeText);

        return matchesSearch && matchesPincode;
    });

    if (sortByPriority) {
        result.sort(
            (a, b) =>
                Number(b.priorityScore || 0) -
                Number(a.priorityScore || 0)
        );
    }

    return result;
}, [loans, search, pincode, sortByPriority]);

// Overall portfolio statistics
const totalOverdue = loans.reduce(
    (sum, loan) =>
        sum + Number(loan.overdueAmount || 0),
    0
);

const highPriorityLoans = loans.filter(
    (loan) =>
        Number(loan.priorityScore || 0) >= 70
).length;

const unassignedLoans = loans.filter(
    (loan) => loan.assignedOfficerId == null
).length;

// Priority badge color
const getPriorityColor = (score) => {
    if (score >= 70) {
        return "bg-red-100 text-red-700";
    }

    if (score >= 40) {
        return "bg-orange-100 text-orange-700";
    }

    return "bg-green-100 text-green-700";
};

// Loading screen
if (loading && loans.length === 0) {
    return (
        <div className="p-8 text-center text-gray-600">
            Loading loans...
        </div>
    );
}

return (
    <div className="p-6">

        {/* Page Header */}
        <div className="flex flex-wrap justify-between items-center gap-4 mb-6">

            <div>
                <h1 className="text-3xl font-bold text-slate-800">
                    Loan Management
                </h1>

                <p className="text-slate-500 mt-2">
                    Review overdue loans and collection priorities.
                </p>
            </div>

            <div className="flex gap-3 flex-wrap">

                <button
                    type="button"
                    onClick={calculateAllPriorities}
                    disabled={updatingId !== null}
                    className="bg-purple-600 text-white px-5 py-3 rounded-lg hover:bg-purple-700 disabled:opacity-50"
                >
                    {updatingId === "all"
                        ? "Calculating..."
                        : "✨ Calculate All Priorities"}
                </button>

                <button
                    type="button"
                    onClick={fetchLoans}
                    disabled={loading || updatingId !== null}
                    className="bg-blue-600 text-white px-5 py-3 rounded-lg hover:bg-blue-700 disabled:opacity-50"
                >
                    ↻ Refresh
                </button>

            </div>
        </div>

        {/* Success / Error Messages */}
        {message && (
            <div
                className={`p-4 rounded-lg mb-5 ${
                    messageType === "error"
                        ? "bg-red-50 text-red-700"
                        : "bg-green-50 text-green-700"
                }`}
                role="status"
            >
                {message}
            </div>
        )}

        {error && (
            <div className="bg-red-50 text-red-700 p-4 rounded-lg mb-5">
                {error}
            </div>
        )}

        {/* Search and Filters */}
        <div className="bg-white p-5 rounded-xl shadow-sm mb-6">

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

                <input
                    type="text"
                    placeholder="Search borrower or loan account..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="border rounded-lg px-4 py-3 outline-blue-500"
                />

                <input
                    type="text"
                    placeholder="Filter by pincode"
                    value={pincode}
                    onChange={(e) => setPincode(e.target.value)}
                    maxLength={6}
                    className="border rounded-lg px-4 py-3 outline-blue-500"
                />

                <button
                    type="button"
                    onClick={() =>
                        setSortByPriority((previous) => !previous)
                    }
                    className="bg-slate-900 text-white rounded-lg px-4 py-3"
                >
                    {sortByPriority
                        ? "Priority: Highest First"
                        : "Priority Sorting Off"}
                </button>

            </div>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">

            <div className="bg-white p-5 rounded-xl shadow-sm">
                <p className="text-slate-500 text-sm">
                    Total Loans
                </p>

                <h2 className="text-3xl font-bold mt-2">
                    {loans.length}
                </h2>
            </div>

            <div className="bg-white p-5 rounded-xl shadow-sm">
                <p className="text-slate-500 text-sm">
                    Filtered Loans
                </p>

                <h2 className="text-3xl font-bold mt-2">
                    {filteredLoans.length}
                </h2>
            </div>

            <div className="bg-white p-5 rounded-xl shadow-sm">
                <p className="text-slate-500 text-sm">
                    High Priority Loans
                </p>

                <h2 className="text-3xl font-bold text-red-600 mt-2">
                    {highPriorityLoans}
                </h2>
            </div>

            <div className="bg-white p-5 rounded-xl shadow-sm">
                <p className="text-slate-500 text-sm">
                    Total Overdue Amount
                </p>

                <h2 className="text-2xl font-bold text-red-600 mt-2">
                    ₹{totalOverdue.toLocaleString("en-IN")}
                </h2>
            </div>

        </div>

        {/* Assignment Summary */}
        <div className="bg-white p-4 rounded-xl shadow-sm mb-6">
            <p className="text-slate-600">
                Unassigned Loans:{" "}
                <span className="font-bold text-orange-600">
                    {unassignedLoans}
                </span>
            </p>
        </div>

        {/* Priority Guide */}
        <div className="bg-white p-4 rounded-xl shadow-sm mb-6">

            <h3 className="font-semibold mb-3">
                Priority Score Guide
            </h3>

            <div className="flex flex-wrap gap-4 text-sm">

                <span className="bg-red-100 text-red-700 px-3 py-2 rounded-lg">
                    High: 70–100
                </span>

                <span className="bg-orange-100 text-orange-700 px-3 py-2 rounded-lg">
                    Medium: 40–69
                </span>

                <span className="bg-green-100 text-green-700 px-3 py-2 rounded-lg">
                    Low: 0–39
                </span>

            </div>
        </div>

        {/* Loan Table */}
        <div className="bg-white rounded-xl shadow-sm overflow-x-auto">

            <table className="w-full text-left min-w-[1100px]">

                <thead className="bg-slate-100 text-slate-600 text-sm">
                    <tr>
                        <th className="p-4">Loan Account</th>
                        <th className="p-4">Borrower</th>
                        <th className="p-4">Pincode</th>
                        <th className="p-4">Overdue Amount</th>
                        <th className="p-4">Days Past Due</th>
                        <th className="p-4">Priority</th>
                        <th className="p-4">Priority Reasons</th>
                        <th className="p-4">Officer ID</th>
                        <th className="p-4">Action</th>
                    </tr>
                </thead>

                <tbody>

                    {filteredLoans.map((loan) => {

                        const score = Number(
                            loan.priorityScore || 0
                        );

                        const isUpdating =
                            updatingId === loan.id;

                        const isUnassigned =
                            loan.assignedOfficerId == null;

                        return (
                            <tr
                                key={loan.id}
                                className="border-t hover:bg-slate-50"
                            >

                                <td className="p-4 font-medium">
                                    {loan.loanAccountNumber}
                                </td>

                                <td className="p-4">
                                    {loan.borrowerName}
                                </td>

                                <td className="p-4">
                                    {loan.pincode}
                                </td>

                                <td className="p-4 font-semibold text-red-600">
                                    ₹{Number(
                                        loan.overdueAmount || 0
                                    ).toLocaleString("en-IN")}
                                </td>

                                <td className="p-4">
                                    {loan.daysPastDue}
                                </td>

                                <td className="p-4">
                                    <span
                                        className={`px-3 py-1 rounded-full text-sm font-semibold ${getPriorityColor(score)}`}
                                    >
                                        {score}
                                    </span>
                                </td>

                                <td className="p-4 min-w-64">
                                    {loan.priorityReasons ? (
                                        <div className="space-y-1">
                                            {String(loan.priorityReasons)
                                                .split(",")
                                                .slice(0, 3)
                                                .map((reason, index) => (
                                                    <p
                                                        key={index}
                                                        className="text-xs text-slate-600"
                                                    >
                                                        • {reason.trim()}
                                                    </p>
                                                ))}
                                        </div>
                                    ) : (
                                        <span className="text-slate-400 text-sm">
                                            Calculate priority
                                        </span>
                                    )}
                                </td>

                                <td className="p-4">
                                    {loan.assignedOfficerId ?? (
                                        <span className="text-orange-600 font-semibold">
                                            Unassigned
                                        </span>
                                    )}
                                </td>

                                <td className="p-4">
                                    <div className="flex flex-col gap-2">

                                        <button
                                            type="button"
                                            onClick={() =>
                                                calculatePriority(loan.id)
                                            }
                                            disabled={updatingId !== null}
                                            className="bg-blue-600 text-white px-3 py-2 rounded-lg text-sm hover:bg-blue-700 disabled:opacity-50"
                                        >
                                            {isUpdating
                                                ? "Processing..."
                                                : "Recalculate"}
                                        </button>

                                        {isUnassigned && (
                                            <button
                                                type="button"
                                                onClick={() =>
                                                    assignLoan(loan.id)
                                                }
                                                disabled={updatingId !== null}
                                                className="bg-green-600 text-white px-3 py-2 rounded-lg text-sm hover:bg-green-700 disabled:opacity-50"
                                            >
                                                {isUpdating
                                                    ? "Assigning..."
                                                    : "Assign"}
                                            </button>
                                        )}

                                    </div>
                                </td>

                            </tr>
                        );
                    })}

                </tbody>

            </table>

            {filteredLoans.length === 0 && (
                <p className="text-center p-8 text-slate-500">
                    No loans found. Try changing your search or pincode filter.
                </p>
            )}

        </div>

        {/* Footer */}
        <p className="text-xs text-slate-500 mt-4">
            Priority scores are generated using transparent rule-based
            scoring. Managers should review recommendations before
            taking action. Loan assignment uses officer pincode and
            workload eligibility.
        </p>

    </div>
);

}

export default LoanManagement;
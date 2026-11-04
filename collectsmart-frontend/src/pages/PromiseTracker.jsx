import { useEffect, useState, useCallback } from "react";
import API from "../services/api";
import { useAuth } from "../context/AuthContext";

function PromiseTracker() {
const { user } = useAuth();

const [loanId, setLoanId] = useState("");
const [amount, setAmount] = useState("");
const [promisedDate, setPromisedDate] = useState("");
const [promises, setPromises] = useState([]);
const [message, setMessage] = useState("");
const [loading, setLoading] = useState(false);
const [refreshing, setRefreshing] = useState(false);

const loadPromises = useCallback(async () => {
    try {
        const response = await API.get("/promises/pending");
        setPromises(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
        setMessage(
            error.response?.data?.message ||
            "Unable to load promises."
        );
    }
}, []);

useEffect(() => {
    loadPromises();
}, [loadPromises]);

const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    if (!loanId || Number(loanId) <= 0) {
        setMessage("Please enter a valid Loan ID.");
        setLoading(false);
        return;
    }

    if (!amount || Number(amount) <= 0) {
        setMessage("Please enter a valid promised amount.");
        setLoading(false);
        return;
    }

    if (!promisedDate) {
        setMessage("Please select a promised payment date.");
        setLoading(false);
        return;
    }

    try {
        await API.post(`/promises/loan/${loanId}`, {
            promisedAmount: Number(amount),
            promisedDate
        });

        setMessage("Promise recorded successfully!");

        setLoanId("");
        setAmount("");
        setPromisedDate("");

        await loadPromises();

    } catch (error) {
        setMessage(
            error.response?.data?.message ||
            "Unable to save promise. Verify the loan assignment."
        );
    } finally {
        setLoading(false);
    }
};

const handleRefresh = async () => {
    setRefreshing(true);
    await loadPromises();
    setRefreshing(false);
};

return (
    <div className="p-6">

        <h1 className="text-3xl font-bold mb-2">
            Promise-to-Pay Tracker
        </h1>

        <p className="text-gray-500 mb-6">
            Record and monitor borrower payment commitments.
        </p>

        <form
            onSubmit={handleSubmit}
            className="bg-white rounded-xl shadow p-6 max-w-xl space-y-4 mb-8"
        >
            <h2 className="text-xl font-semibold">
                Record New Promise
            </h2>

            <div>
                <label className="block mb-1 font-semibold">
                    Logged-in Field Officer
                </label>

                <input
                    type="text"
                    value={`${user?.email || ""} (Officer ID: ${user?.fieldOfficerId ?? "Not linked"})`}
                    readOnly
                    className="w-full border rounded-lg p-3 bg-gray-100"
                />
            </div>

            <div>
                <label className="block mb-1 font-semibold">
                    Loan ID
                </label>

                <input
                    type="number"
                    min="1"
                    value={loanId}
                    onChange={(e) => setLoanId(e.target.value)}
                    placeholder="Enter numeric Loan ID"
                    className="w-full border rounded-lg p-3"
                    required
                />
            </div>

            <div>
                <label className="block mb-1 font-semibold">
                    Promised Amount (₹)
                </label>

                <input
                    type="number"
                    min="1"
                    step="0.01"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    placeholder="Enter promised amount"
                    className="w-full border rounded-lg p-3"
                    required
                />
            </div>

            <div>
                <label className="block mb-1 font-semibold">
                    Promised Payment Date
                </label>

                <input
                    type="date"
                    value={promisedDate}
                    min={new Date().toLocaleDateString("en-CA")}
                    onChange={(e) => setPromisedDate(e.target.value)}
                    className="w-full border rounded-lg p-3"
                    required
                />
            </div>

            <button
                type="submit"
                disabled={loading || !user?.fieldOfficerId}
                className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
                {loading ? "Saving..." : "Save Promise"}
            </button>

            {message && (
                <p className="font-semibold text-blue-700">
                    {message}
                </p>
            )}
        </form>

        <div className="bg-white rounded-xl shadow p-5">

            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-semibold">
                    Pending Promises
                </h2>

                <button
                    type="button"
                    onClick={handleRefresh}
                    disabled={refreshing}
                    className="bg-gray-200 px-4 py-2 rounded-lg disabled:opacity-50"
                >
                    {refreshing ? "Refreshing..." : "Refresh"}
                </button>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left">
                    <thead>
                        <tr className="border-b">
                            <th className="p-3">Promise ID</th>
                            <th className="p-3">Loan ID</th>
                            <th className="p-3">Amount</th>
                            <th className="p-3">Payment Date</th>
                            <th className="p-3">Status</th>
                        </tr>
                    </thead>

                    <tbody>
                        {promises.length > 0 ? (
                            promises.map((promise) => (
                                <tr key={promise.id} className="border-b">
                                    <td className="p-3">{promise.id}</td>

                                    <td className="p-3">
                                        {promise.loan?.id ?? promise.loanId ?? "-"}
                                    </td>

                                    <td className="p-3">
                                        ₹{Number(
                                            promise.promisedAmount || 0
                                        ).toLocaleString("en-IN")}
                                    </td>

                                    <td className="p-3">
                                        {promise.promisedDate}
                                    </td>

                                    <td className="p-3">
                                        {promise.status}
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td
                                    colSpan="5"
                                    className="p-4 text-center text-gray-500"
                                >
                                    No pending promises found.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    </div>
);

}

export default PromiseTracker;
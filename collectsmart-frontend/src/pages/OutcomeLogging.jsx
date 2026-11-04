import { useEffect, useState, useCallback } from "react";
import API from "../services/api";
import { useAuth } from "../context/AuthContext";

const STORAGE_KEY = "collectsmart_pending_outcomes";

function getPendingOutcomes() {
try {
return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
} catch {
return [];
}
}

function savePendingOutcomes(records) {
localStorage.setItem(STORAGE_KEY, JSON.stringify(records));
}

function OutcomeLogging() {
const { user } = useAuth();

const officerId = Number(user?.fieldOfficerId);

const [loanId, setLoanId] = useState("");
const [outcomeType, setOutcomeType] = useState("PAID");
const [notes, setNotes] = useState("");

const [message, setMessage] = useState("");
const [loading, setLoading] = useState(false);

const [pendingCount, setPendingCount] = useState(0);
const [syncedCount, setSyncedCount] = useState(0);
const [isOnline, setIsOnline] = useState(navigator.onLine);

const updatePendingCount = () => {
    setPendingCount(getPendingOutcomes().length);
};

// Get GPS coordinates
const getLocation = () => {
    return new Promise((resolve) => {
        if (!navigator.geolocation) {
            resolve({ latitude: null, longitude: null });
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                resolve({
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude
                });
            },
            () => {
                resolve({ latitude: null, longitude: null });
            },
            { timeout: 5000 }
        );
    });
};

// Save outcome locally when offline
const saveOffline = (record) => {
    const pending = getPendingOutcomes();

    pending.push({
        ...record,
        localId: crypto.randomUUID(),
        createdAt: new Date().toISOString()
    });

    savePendingOutcomes(pending);
    updatePendingCount();

    setMessage(
        "Internet unavailable. Outcome saved locally and waiting for sync."
    );
};

// Synchronize pending outcomes
const syncPendingOutcomes = useCallback(async () => {
    if (!navigator.onLine) return;

    const pending = getPendingOutcomes();

    if (pending.length === 0) return;

    let remaining = [...pending];
    let synced = 0;

    for (const record of pending) {
        try {
            const {
                localId,
                createdAt,
                loanId: recordLoanId,
                ...payload
            } = record;

            await API.post(
                `/outcomes/loan/${recordLoanId}`,
                payload
            );

            remaining = remaining.filter(
                (item) => item.localId !== record.localId
            );

            synced++;
            savePendingOutcomes(remaining);

        } catch (error) {
            console.error("Outcome synchronization failed:", error);

            if (!navigator.onLine) break;
        }
    }

    updatePendingCount();

    if (synced > 0) {
        setSyncedCount((previous) => previous + synced);
        setMessage(
            `${synced} offline outcome(s) synchronized successfully.`
        );
    }
}, []);

// Monitor internet connection
useEffect(() => {
    const handleOnline = () => {
        setIsOnline(true);
        setMessage("Internet restored. Synchronizing pending outcomes...");
        syncPendingOutcomes();
    };

    const handleOffline = () => {
        setIsOnline(false);
        setMessage("You are offline. Outcomes will be stored locally.");
    };

    window.addEventListener("online", handleOnline);
    window.addEventListener("offline", handleOffline);

    updatePendingCount();
    syncPendingOutcomes();

    return () => {
        window.removeEventListener("online", handleOnline);
        window.removeEventListener("offline", handleOffline);
    };
}, [syncPendingOutcomes]);

// Submit outcome
const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");

    if (!officerId || Number.isNaN(officerId)) {
        setMessage("Your account is not linked to a field officer.");
        return;
    }

    if (!loanId || Number(loanId) <= 0) {
        setMessage("Please enter a valid Loan ID.");
        return;
    }

    setLoading(true);

    try {
        const location = await getLocation();

        const record = {
            loanId: Number(loanId),
            outcomeType,
            latitude: location.latitude,
            longitude: location.longitude,
            notes
        };

        if (!navigator.onLine) {
            saveOffline(record);
        } else {
            try {
                await API.post(
                    `/outcomes/loan/${loanId}`,
                    {
                        outcomeType: record.outcomeType,
                        latitude: record.latitude,
                        longitude: record.longitude,
                        notes: record.notes
                    }
                );

                setMessage("Outcome recorded successfully!");
                setSyncedCount((previous) => previous + 1);

            } catch (error) {
                if (!error.response) {
                    saveOffline(record);
                } else {
                    throw error;
                }
            }
        }

        setLoanId("");
        setNotes("");

    } catch (error) {
        setMessage(
            error.response?.data?.message ||
            error.response?.data?.error ||
            "Unable to record outcome. Please verify the loan assignment."
        );
    } finally {
        setLoading(false);
    }
};

return (
    <div className="p-6">

        <h1 className="text-3xl font-bold mb-2">
            Outcome Logging
        </h1>

        <p className="text-gray-500 mb-6">
            Record borrower contact results, even without internet.
        </p>

        {/* Connection Status */}
        <div className={`p-4 rounded-lg mb-5 ${
            isOnline
                ? "bg-green-100 text-green-800"
                : "bg-orange-100 text-orange-800"
        }`}>
            <strong>
                {isOnline ? "● Online" : "● Offline"}
            </strong>

            <p className="text-sm mt-1">
                {isOnline
                    ? "Internet connection available."
                    : "Outcomes will be stored locally until internet returns."}
            </p>
        </div>

        {/* Sync Statistics */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">

            <div className="bg-orange-50 p-4 rounded-xl">
                <p className="text-gray-600">Pending Sync</p>
                <h2 className="text-3xl font-bold text-orange-600">
                    {pendingCount}
                </h2>
            </div>

            <div className="bg-green-50 p-4 rounded-xl">
                <p className="text-gray-600">Synced This Session</p>
                <h2 className="text-3xl font-bold text-green-600">
                    {syncedCount}
                </h2>
            </div>

            <div className="bg-blue-50 p-4 rounded-xl">
                <p className="text-gray-600">Connection</p>
                <h2 className="text-xl font-bold text-blue-600">
                    {isOnline ? "Connected" : "Disconnected"}
                </h2>
            </div>

        </div>

        {/* Outcome Form */}
        <form
            onSubmit={handleSubmit}
            className="bg-white rounded-xl shadow p-6 max-w-xl space-y-4"
        >

            {/* Logged-in Officer */}
            <div>
                <label className="block font-semibold mb-1">
                    Logged-in Field Officer
                </label>

                <input
                    type="text"
                    value={`${user?.email || ""} (Officer ID: ${user?.fieldOfficerId ?? "Not linked"})`}
                    readOnly
                    className="w-full border rounded-lg p-3 bg-gray-100"
                />
            </div>

            {/* Loan ID */}
            <div>
                <label className="block font-semibold mb-1">
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

            {/* Outcome */}
            <div>
                <label className="block font-semibold mb-1">
                    Outcome
                </label>

                <select
                    value={outcomeType}
                    onChange={(e) => setOutcomeType(e.target.value)}
                    className="w-full border rounded-lg p-3"
                >
                    <option value="PAID">Paid</option>
                    <option value="PROMISE_TO_PAY">Promise to Pay</option>
                    <option value="NOT_REACHABLE">Not Reachable</option>
                    <option value="DISPUTE">Dispute</option>
                </select>
            </div>

            {/* Notes */}
            <div>
                <label className="block font-semibold mb-1">
                    Notes
                </label>

                <textarea
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="Enter contact notes..."
                    className="w-full border rounded-lg p-3"
                    rows="4"
                />
            </div>

            <button
                type="submit"
                disabled={loading || !officerId}
                className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
                {loading ? "Saving..." : "Submit Outcome"}
            </button>

            {message && (
                <p className="font-semibold text-blue-700 mt-3">
                    {message}
                </p>
            )}

        </form>

        {/* Manual Sync */}
        <button
            type="button"
            onClick={syncPendingOutcomes}
            disabled={!isOnline || pendingCount === 0}
            className="mt-5 bg-green-600 text-white px-6 py-3 rounded-lg disabled:opacity-50"
        >
            Sync Pending Outcomes
        </button>

    </div>
);

}

export default OutcomeLogging;
import { useEffect, useState } from "react";
import API from "../services/api";

function PromiseTracker() {
const [loanId, setLoanId] = useState("");
const [officerId, setOfficerId] = useState("2");
const [amount, setAmount] = useState("");
const [promisedDate, setPromisedDate] = useState("");
const [promises, setPromises] = useState([]);
const [message, setMessage] = useState("");
const [loading, setLoading] = useState(false);

const loadPromises = async () => {
try {
const response = await API.get("/promises/pending");
setPromises(Array.isArray(response.data) ? response.data : []);
} catch (error) {
setMessage("Unable to load promises.");
}
};

useEffect(() => {
loadPromises();
}, []);

const handleSubmit = async (e) => {
e.preventDefault();
setLoading(true);
setMessage("");

try {
  await API.post(`/promises/loan/${loanId}`, {
    officerId: Number(officerId),
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
    "Unable to save promise. Check the loan and officer details."
  );
} finally {
  setLoading(false);
}

};

return (
<div className="p-6">
<h1 className="text-3xl font-bold mb-2">Promise-to-Pay Tracker</h1>
<p className="text-gray-500 mb-6">
Record and monitor borrower payment commitments.
</p>

  <form
    onSubmit={handleSubmit}
    className="bg-white rounded-xl shadow p-6 max-w-xl space-y-4 mb-8"
  >
    <h2 className="text-xl font-semibold">Record New Promise</h2>

    <div>
      <label className="block mb-1 font-semibold">Loan ID</label>
      <input
        type="number"
        value={loanId}
        onChange={(e) => setLoanId(e.target.value)}
        className="w-full border rounded-lg p-3"
        required
      />
    </div>

    <div>
      <label className="block mb-1 font-semibold">Officer</label>
      <select
        value={officerId}
        onChange={(e) => setOfficerId(e.target.value)}
        className="w-full border rounded-lg p-3"
      >
        <option value="1">Officer 1</option>
        <option value="2">Officer 2</option>
      </select>
    </div>

    <div>
      <label className="block mb-1 font-semibold">Promised Amount (₹)</label>
      <input
        type="number"
        min="1"
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
        className="w-full border rounded-lg p-3"
        required
      />
    </div>

    <div>
      <label className="block mb-1 font-semibold">Promised Payment Date</label>
      <input
        type="date"
        value={promisedDate}
        min={new Date().toISOString().split("T")[0]}
        onChange={(e) => setPromisedDate(e.target.value)}
        className="w-full border rounded-lg p-3"
        required
      />
    </div>

    <button
      type="submit"
      disabled={loading}
      className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 disabled:opacity-50"
    >
      {loading ? "Saving..." : "Save Promise"}
    </button>

    {message && <p className="text-blue-700 font-semibold">{message}</p>}
  </form>

  <div className="bg-white rounded-xl shadow p-5">
    <div className="flex justify-between items-center mb-4">
      <h2 className="text-xl font-semibold">Pending Promises</h2>
      <button
        onClick={loadPromises}
        className="bg-gray-200 px-4 py-2 rounded-lg"
      >
        Refresh
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
                <td className="p-3">{promise.loan?.id ?? promise.loanId ?? "-"}</td>
                <td className="p-3">
                  ₹{Number(promise.promisedAmount || 0).toLocaleString("en-IN")}
                </td>
                <td className="p-3">{promise.promisedDate}</td>
                <td className="p-3">{promise.status}</td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="5" className="p-4 text-center text-gray-500">
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
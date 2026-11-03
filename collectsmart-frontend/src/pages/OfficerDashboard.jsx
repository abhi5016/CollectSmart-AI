import { useEffect, useState } from "react";
import API from "../services/api";

function OfficerDashboard() {

    const [loans, setLoans] = useState([]);
    const [officerId, setOfficerId] = useState("1");
    const [loading, setLoading] = useState(false);
    const [callingLoanId, setCallingLoanId] = useState(null);
    const [message, setMessage] = useState("");
    const [messageType, setMessageType] = useState("");

    const fetchLoans = async () => {
        if (!officerId) return;

        setLoading(true);

        try {
            const response = await API.get(
                `/loans/officer/${officerId}`
            );

            setLoans(response.data);
        } catch (error) {
            console.error("Unable to fetch officer loans:", error);
            setLoans([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLoans();
    }, []);

    // Authorize contact before opening phone dialler
    const handleCall = async (loan) => {

        setMessage("");
        setMessageType("");

        if (!loan.phoneNumber) {
            setMessage("Borrower phone number is unavailable.");
            setMessageType("error");
            return;
        }

        setCallingLoanId(loan.id);

        try {

            const response = await API.post(
                "/contact-attempts/authorize",
                {
                    loanId: loan.id,
                    officerId: Number(officerId)
                }
            );

            if (response.data.allowed) {

                setMessage(
                    `Contact authorized for ${loan.borrowerName}. Opening phone dialler...`
                );

                setMessageType("success");

                // Open dialler only after backend authorization
                window.location.href = `tel:${loan.phoneNumber}`;

            } else {

                setMessage(
                    response.data.message || "Contact is not permitted."
                );

                setMessageType("error");
            }

        } catch (error) {

            const errorMessage =
                error.response?.data?.message ||
                "Unable to authorize contact. Please try again.";

            setMessage(errorMessage);
            setMessageType("error");

        } finally {
            setCallingLoanId(null);
        }
    };

    const getPriorityColor = (score) => {
        if (score >= 70) return "bg-red-100 text-red-700";
        if (score >= 40) return "bg-orange-100 text-orange-700";
        return "bg-green-100 text-green-700";
    };

    const totalOverdue = loans.reduce(
        (sum, loan) => sum + Number(loan.overdueAmount || 0),
        0
    );

    return (
        <div className="p-6">

            <div className="flex flex-wrap justify-between items-center mb-6 gap-4">

                <div>
                    <h1 className="text-2xl font-bold text-slate-800">
                        Field Officer Dashboard
                    </h1>

                    <p className="text-slate-500 mt-1">
                        Manage your assigned collection activities.
                    </p>
                </div>

                <div className="flex gap-2">

                    <select
                        value={officerId}
                        onChange={(e) => setOfficerId(e.target.value)}
                        className="border rounded-lg p-2 bg-white"
                    >
                        <option value="1">Officer 1</option>
                        <option value="2">Officer 2</option>
                    </select>

                    <button
                        onClick={fetchLoans}
                        disabled={loading}
                        className="bg-blue-600 text-white px-4 py-2 rounded-lg disabled:opacity-50"
                    >
                        {loading ? "Loading..." : "Load Loans"}
                    </button>

                </div>

            </div>

            {/* Contact authorization message */}
            {message && (
                <div
                    className={`mb-5 p-4 rounded-lg ${
                        messageType === "success"
                            ? "bg-green-100 text-green-800"
                            : "bg-red-100 text-red-800"
                    }`}
                    role="status"
                >
                    {message}
                </div>
            )}

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-5 mb-6">

                <div className="bg-white p-5 rounded-xl shadow-sm">
                    <p className="text-slate-500">Assigned Loans</p>
                    <h2 className="text-3xl font-bold mt-2">
                        {loans.length}
                    </h2>
                </div>

                <div className="bg-white p-5 rounded-xl shadow-sm">
                    <p className="text-slate-500">Total Overdue</p>
                    <h2 className="text-2xl font-bold text-red-600 mt-2">
                        ₹{totalOverdue.toLocaleString("en-IN")}
                    </h2>
                </div>

                <div className="bg-white p-5 rounded-xl shadow-sm">
                    <p className="text-slate-500">High Priority Loans</p>
                    <h2 className="text-3xl font-bold text-orange-600 mt-2">
                        {loans.filter(
                            loan => loan.priorityScore >= 70
                        ).length}
                    </h2>
                </div>

            </div>

            <div className="bg-white rounded-xl shadow-sm overflow-x-auto">

                <div className="p-5 border-b">
                    <h2 className="text-xl font-bold">
                        My Assigned Loans
                    </h2>
                </div>

                {loading ? (
                    <p className="p-6">Loading loans...</p>
                ) : (

                    <table className="w-full text-left">

                        <thead className="bg-slate-100 text-slate-600">
                            <tr>
                                <th className="p-4">Loan Account</th>
                                <th className="p-4">Borrower</th>
                                <th className="p-4">Phone</th>
                                <th className="p-4">Overdue</th>
                                <th className="p-4">Days Past Due</th>
                                <th className="p-4">Priority</th>
                                <th className="p-4">Action</th>
                            </tr>
                        </thead>

                        <tbody>

                            {loans.map(loan => (

                                <tr key={loan.id} className="border-t">

                                    <td className="p-4 font-semibold">
                                        {loan.loanAccountNumber}
                                    </td>

                                    <td className="p-4">
                                        {loan.borrowerName}
                                    </td>

                                    <td className="p-4">
                                        {loan.phoneNumber}
                                    </td>

                                    <td className="p-4 text-red-600 font-semibold">
                                        ₹{Number(
                                            loan.overdueAmount || 0
                                        ).toLocaleString("en-IN")}
                                    </td>

                                    <td className="p-4">
                                        {loan.daysPastDue}
                                    </td>

                                    <td className="p-4">
                                        <span
                                            className={`px-3 py-1 rounded-full ${getPriorityColor(
                                                loan.priorityScore
                                            )}`}
                                        >
                                            {loan.priorityScore}
                                        </span>
                                    </td>

                                    <td className="p-4">

                                        <button
                                            onClick={() => handleCall(loan)}
                                            disabled={callingLoanId === loan.id}
                                            className="bg-green-600 text-white px-3 py-2 rounded-lg disabled:opacity-50"
                                        >
                                            {callingLoanId === loan.id
                                                ? "Checking..."
                                                : "📞 Call"}
                                        </button>

                                    </td>

                                </tr>

                            ))}

                        </tbody>

                    </table>
                )}

                {!loading && loans.length === 0 && (
                    <p className="text-center p-8 text-slate-500">
                        No loans assigned to this officer.
                    </p>
                )}

            </div>

        </div>
    );
}

export default OfficerDashboard;
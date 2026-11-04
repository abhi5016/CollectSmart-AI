import { useCallback, useEffect, useState } from "react";
import API from "../services/api";

export default function FieldOfficerManagement() {
    const [officers, setOfficers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadOfficers = useCallback(async () => {
        try {
            setLoading(true);
            setError("");

            const response = await API.get("/officers");

            setOfficers(Array.isArray(response.data) ? response.data : []);
        } catch (err) {
            console.error("Officer loading error:", err);
            setError(
                err.response?.data?.message ||
                "Unable to load field officers."
            );
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadOfficers();
    }, [loadOfficers]);

    const activeCount = officers.filter(
        officer => officer.active
    ).length;

    const totalWorkload = officers.reduce(
        (total, officer) => total + (officer.maxWorkload || 0),
        0
    );

    return (
        <div className="p-6">

            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold text-slate-800">
                        Field Officer Management
                    </h1>

                    <p className="text-slate-500 mt-2">
                        Monitor your field officers and their assigned workload.
                    </p>
                </div>

                <button
                    onClick={loadOfficers}
                    disabled={loading}
                    className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
                >
                    {loading ? "Loading..." : "↻ Refresh"}
                </button>
            </div>

            {error && (
                <div className="bg-red-50 text-red-700 p-4 rounded-lg mb-5">
                    {error}
                </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mb-6">

                <div className="bg-white p-5 rounded-xl shadow-sm">
                    <p className="text-slate-500">Total Officers</p>
                    <h2 className="text-3xl font-bold mt-2">
                        {officers.length}
                    </h2>
                </div>

                <div className="bg-white p-5 rounded-xl shadow-sm">
                    <p className="text-slate-500">Active Officers</p>
                    <h2 className="text-3xl font-bold text-green-600 mt-2">
                        {activeCount}
                    </h2>
                </div>

                <div className="bg-white p-5 rounded-xl shadow-sm">
                    <p className="text-slate-500">Combined Maximum Workload</p>
                    <h2 className="text-3xl font-bold text-blue-600 mt-2">
                        {totalWorkload}
                    </h2>
                </div>

            </div>

            <div className="bg-white rounded-xl shadow-sm overflow-x-auto">

                <div className="p-5 border-b">
                    <h2 className="text-xl font-bold">
                        Registered Field Officers
                    </h2>
                </div>

                {loading ? (
                    <p className="p-6 text-slate-500">
                        Loading officers...
                    </p>
                ) : officers.length === 0 ? (
                    <p className="p-6 text-slate-500">
                        No field officers found.
                    </p>
                ) : (

                    <table className="w-full text-left">

                        <thead className="bg-slate-100">
                            <tr>
                                <th className="p-4">Officer ID</th>
                                <th className="p-4">Name</th>
                                <th className="p-4">Phone</th>
                                <th className="p-4">Pincode</th>
                                <th className="p-4">Max Workload</th>
                                <th className="p-4">Status</th>
                            </tr>
                        </thead>

                        <tbody>
                            {officers.map(officer => (
                                <tr
                                    key={officer.id}
                                    className="border-t hover:bg-slate-50"
                                >
                                    <td className="p-4">
                                        {officer.id}
                                    </td>

                                    <td className="p-4 font-medium">
                                        {officer.name}
                                    </td>

                                    <td className="p-4">
                                        {officer.phoneNumber}
                                    </td>

                                    <td className="p-4">
                                        {officer.pincode}
                                    </td>

                                    <td className="p-4">
                                        {officer.maxWorkload}
                                    </td>

                                    <td className="p-4">
                                        <span className={
                                            officer.active
                                                ? "bg-green-100 text-green-700 px-3 py-1 rounded-full text-sm"
                                                : "bg-red-100 text-red-700 px-3 py-1 rounded-full text-sm"
                                        }>
                                            {officer.active ? "Active" : "Inactive"}
                                        </span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>

                    </table>
                )}

            </div>

        </div>
    );
}
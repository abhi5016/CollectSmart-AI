import { useState } from "react";
import API from "../services/api";

function CSVImport() {
const [file, setFile] = useState(null);
const [loading, setLoading] = useState(false);
const [result, setResult] = useState(null);
const [error, setError] = useState("");

// Download sample CSV
const downloadSampleCSV = () => {
    const csvContent = [
        "loanAccountNumber,borrowerName,phoneNumber,pincode,loanAmount,overdueAmount,dueDate,daysPastDue,status",
        "LN2001,Rahul Sharma,9876543210,560001,100000,15000,2026-10-20,14,OVERDUE",
        "LN2002,Priya Singh,9876543211,560001,80000,20000,2026-10-15,19,OVERDUE",
        "LN2003,Amit Kumar,9876543212,560002,120000,25000,2026-10-10,24,OVERDUE"
    ].join("\n");

    const blob = new Blob([csvContent], {
        type: "text/csv;charset=utf-8;"
    });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = url;
    link.download = "collectsmart_sample_loans.csv";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    window.URL.revokeObjectURL(url);
};

// Handle CSV file selection
const handleFileChange = (e) => {
    const selectedFile = e.target.files?.[0];

    setResult(null);
    setError("");

    if (!selectedFile) {
        setFile(null);
        return;
    }

    if (!selectedFile.name.toLowerCase().endsWith(".csv")) {
        setFile(null);
        setError("Only CSV files are allowed.");
        e.target.value = "";
        return;
    }

    if (selectedFile.size > 5 * 1024 * 1024) {
        setFile(null);
        setError("File size must be less than 5 MB.");
        e.target.value = "";
        return;
    }

    setFile(selectedFile);
};

// Upload CSV
const handleUpload = async (e) => {
    e.preventDefault();

    if (!file) {
        setError("Please select a CSV file.");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    setLoading(true);
    setError("");
    setResult(null);

    try {
        const response = await API.post(
            "/csv/upload",
            formData
        );

        setResult(response.data);
        setFile(null);
        e.target.reset();

    } catch (err) {
        setError(
            err.response?.data?.message ||
            err.response?.data?.error ||
            "CSV upload failed. Please check the file and try again."
        );
    } finally {
        setLoading(false);
    }
};

return (
    <div className="p-6 max-w-5xl">

        {/* Page Header */}
        <div className="mb-6">
            <h1 className="text-3xl font-bold text-slate-800">
                CSV Loan Import
            </h1>

            <p className="text-slate-500 mt-2">
                Upload overdue loan records and automatically assign
                eligible loans to field officers.
            </p>
        </div>

        {/* Upload Section */}
        <div className="bg-white p-6 rounded-2xl shadow-sm">

            <div className="border-2 border-dashed border-blue-300 rounded-xl p-10 text-center">

                <div className="text-5xl mb-4">
                    📁
                </div>

                <h2 className="text-xl font-semibold text-slate-800">
                    Upload Loan CSV
                </h2>

                <p className="text-slate-500 mt-2 mb-6">
                    Select a CSV file containing overdue loan records.
                </p>

                <form onSubmit={handleUpload}>

                    <input
                        type="file"
                        accept=".csv,text/csv"
                        onChange={handleFileChange}
                        className="block w-full max-w-md mx-auto border rounded-lg p-3 mb-5"
                    />

                    {file && (
                        <div className="text-sm text-green-700 mb-4">
                            ✓ Selected: {file.name}
                            <br />
                            Size: {(file.size / 1024).toFixed(2)} KB
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading || !file}
                        className="bg-blue-600 text-white px-8 py-3 rounded-lg hover:bg-blue-700 disabled:opacity-50"
                    >
                        {loading ? "Uploading..." : "Upload CSV"}
                    </button>

                </form>

            </div>

            {/* Error Message */}
            {error && (
                <div className="mt-5 p-4 bg-red-50 text-red-700 rounded-lg">
                    ⚠️ {error}
                </div>
            )}

            {/* Import Result */}
            {result && (
                <div className="mt-6">

                    <h2 className="text-xl font-bold text-green-700 mb-4">
                        ✓ CSV Import Completed!
                    </h2>

                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">

                        <div className="bg-green-50 p-5 rounded-xl">
                            <p className="text-slate-600">Imported</p>
                            <h3 className="text-3xl font-bold text-green-700">
                                {result.imported ?? 0}
                            </h3>
                        </div>

                        <div className="bg-blue-50 p-5 rounded-xl">
                            <p className="text-slate-600">Assigned</p>
                            <h3 className="text-3xl font-bold text-blue-700">
                                {result.assigned ?? 0}
                            </h3>
                        </div>

                        <div className="bg-orange-50 p-5 rounded-xl">
                            <p className="text-slate-600">Duplicates</p>
                            <h3 className="text-3xl font-bold text-orange-700">
                                {result.duplicates ?? 0}
                            </h3>
                        </div>

                        <div className="bg-red-50 p-5 rounded-xl">
                            <p className="text-slate-600">Failed</p>
                            <h3 className="text-3xl font-bold text-red-700">
                                {result.failed ?? 0}
                            </h3>
                        </div>

                    </div>

                    {result.errors?.length > 0 && (
                        <div className="mt-4 bg-red-50 p-4 rounded-lg text-red-700">
                            <p className="font-semibold mb-2">
                                Import Errors:
                            </p>

                            {result.errors.map((item, index) => (
                                <p key={index} className="text-sm mb-1">
                                    {item}
                                </p>
                            ))}
                        </div>
                    )}

                </div>
            )}

        </div>

        {/* CSV Format Section */}
        <div className="bg-blue-50 p-5 rounded-xl mt-6">

            <h3 className="font-bold text-blue-900 mb-2">
                CSV Format Requirements
            </h3>

            <p className="text-sm text-blue-800 mb-3">
                Your CSV file should contain these columns:
            </p>

            <code className="text-sm break-all text-blue-900">
                loanAccountNumber, borrowerName, phoneNumber, pincode,
                loanAmount, overdueAmount, dueDate, daysPastDue, status
            </code>

            <div className="mt-5">
                <button
                    type="button"
                    onClick={downloadSampleCSV}
                    className="bg-green-600 text-white px-5 py-3 rounded-lg hover:bg-green-700"
                >
                    ⬇ Download Sample CSV
                </button>
            </div>

            <div className="mt-4 text-sm text-blue-800">
                <p>• Only CSV files are supported.</p>
                <p>• Maximum file size: 5 MB.</p>
                <p>• Duplicate loan account numbers are skipped by the backend.</p>
                <p>• Assignment depends on officer availability and matching pincode.</p>
            </div>

        </div>

    </div>
);

}

export default CSVImport;
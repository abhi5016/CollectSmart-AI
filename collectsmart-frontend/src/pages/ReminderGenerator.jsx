import { useState } from "react";

function ReminderGenerator() {
const [borrowerName, setBorrowerName] = useState("");
const [amount, setAmount] = useState("");
const [dueDate, setDueDate] = useState("");
const [language, setLanguage] = useState("English");
const [draft, setDraft] = useState("");
const [message, setMessage] = useState("");
const [copied, setCopied] = useState(false);

// Approved reminder templates
const approvedTemplates = {
    English: (name, amount, date) =>
        `Dear ${name}, this is a friendly reminder regarding your overdue loan payment of ₹${amount}. Kindly make the payment by ${date} or contact our branch if you need assistance. Thank you.`,

    Hindi: (name, amount, date) =>
        `प्रिय ${name}, आपके ₹${amount} के बकाया ऋण भुगतान के संबंध में यह एक विनम्र अनुस्मारक है। कृपया ${date} तक भुगतान करें या सहायता के लिए हमारी शाखा से संपर्क करें। धन्यवाद।`
};

// Restricted words
const blockedWords = [
    "threat",
    "threaten",
    "harass",
    "abuse",
    "arrest",
    "jail",
    "धमकी",
    "परेशान"
];

const containsBlockedWord = (text) => {
    const normalizedText = text.toLowerCase();

    return blockedWords.some((word) =>
        normalizedText.includes(word.toLowerCase())
    );
};

const generateDraft = (e) => {
    e.preventDefault();

    setMessage("");
    setDraft("");
    setCopied(false);

    const name = borrowerName.trim();
    const numericAmount = Number(amount);

    if (!name || !amount || !dueDate) {
        setMessage("Please fill in all fields.");
        return;
    }

    if (!Number.isFinite(numericAmount) || numericAmount <= 0) {
        setMessage("Please enter a valid overdue amount.");
        return;
    }

    const selectedDate = new Date(`${dueDate}T00:00:00`);

    if (Number.isNaN(selectedDate.getTime())) {
        setMessage("Please enter a valid payment date.");
        return;
    }

    if (containsBlockedWord(name)) {
        setMessage("Borrower name contains restricted language.");
        return;
    }

    const formattedAmount = numericAmount.toLocaleString("en-IN");

    const generated = approvedTemplates[language](
        name,
        formattedAmount,
        dueDate
    );

    if (containsBlockedWord(generated)) {
        setMessage("Reminder contains restricted language.");
        return;
    }

    setDraft(generated);
    setMessage("Reminder generated using an approved template.");
};

const copyDraft = async () => {
    if (!draft) {
        setMessage("Generate a reminder before copying.");
        return;
    }

    try {
        await navigator.clipboard.writeText(draft);

        setCopied(true);
        setMessage("Reminder copied. Review it before sending.");

    } catch {
        setMessage(
            "Unable to copy. Please select and copy the text manually."
        );
    }
};

return (
    <div className="p-6">

        <h1 className="text-3xl font-bold mb-2">
            Reminder Generator
        </h1>

        <p className="text-gray-500 mb-6">
            Generate polite, approved payment reminders in English or Hindi.
        </p>

        <div className="bg-white rounded-xl shadow p-6 max-w-2xl">

            <form onSubmit={generateDraft} className="space-y-4">

                <div>
                    <label className="block font-semibold mb-1">
                        Borrower Name
                    </label>

                    <input
                        value={borrowerName}
                        onChange={(e) => {
                            setBorrowerName(e.target.value);
                            setDraft("");
                            setCopied(false);
                        }}
                        className="w-full border rounded-lg p-3"
                        placeholder="Enter borrower name"
                        maxLength="100"
                        required
                    />
                </div>

                <div>
                    <label className="block font-semibold mb-1">
                        Overdue Amount (₹)
                    </label>

                    <input
                        type="number"
                        min="0.01"
                        step="0.01"
                        value={amount}
                        onChange={(e) => {
                            setAmount(e.target.value);
                            setDraft("");
                            setCopied(false);
                        }}
                        className="w-full border rounded-lg p-3"
                        placeholder="Enter overdue amount"
                        required
                    />
                </div>

                <div>
                    <label className="block font-semibold mb-1">
                        Payment Date
                    </label>

                    <input
                        type="date"
                        value={dueDate}
                        onChange={(e) => {
                            setDueDate(e.target.value);
                            setDraft("");
                            setCopied(false);
                        }}
                        className="w-full border rounded-lg p-3"
                        required
                    />
                </div>

                <div>
                    <label className="block font-semibold mb-1">
                        Language
                    </label>

                    <select
                        value={language}
                        onChange={(e) => {
                            setLanguage(e.target.value);
                            setDraft("");
                            setCopied(false);
                        }}
                        className="w-full border rounded-lg p-3"
                    >
                        <option value="English">English</option>
                        <option value="Hindi">Hindi</option>
                    </select>
                </div>

                <button
                    type="submit"
                    className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700"
                >
                    Generate Reminder
                </button>

            </form>

            {message && (
                <p className="mt-4 text-blue-700 font-semibold">
                    {message}
                </p>
            )}

            {draft && (
                <div className="mt-6 border rounded-lg p-4 bg-gray-50">

                    <h2 className="text-xl font-semibold mb-3">
                        Generated Reminder Draft
                    </h2>

                    <p className="leading-7 whitespace-pre-wrap">
                        {draft}
                    </p>

                    <button
                        type="button"
                        onClick={copyDraft}
                        className="mt-4 bg-green-600 text-white px-5 py-2 rounded-lg hover:bg-green-700"
                    >
                        {copied ? "Copied!" : "Copy Reminder"}
                    </button>

                    <div className="mt-4 bg-yellow-50 border border-yellow-200 rounded-lg p-3">

                        <p className="text-sm text-yellow-800 font-semibold">
                            Important: Draft Only
                        </p>

                        <p className="text-sm text-gray-600 mt-1">
                            This reminder is generated from an approved
                            template. The field officer must review and
                            approve it before sending. No automatic SMS
                            or WhatsApp messages are sent.
                        </p>

                    </div>

                </div>
            )}

        </div>
    </div>
);

}

export default ReminderGenerator;
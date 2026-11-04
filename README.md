# CollectSmart AI

### AI-Assisted MSME Loan Collection and Field Officer Management Platform

CollectSmart AI is a web-based loan collection management platform designed to help MSME lenders manage overdue loans, assign field officers, prioritise collection activities, track promises to pay, and monitor collection performance.

The project was developed as part of the **Isocode Labs Software Engineering Intern take-home assignment**.

## Technology Stack

### Backend

* Java 21
* Spring Boot
* Spring Data JPA / Hibernate
* MySQL 8
* REST APIs
* Spring Security
* JWT Authentication
* Maven

### Frontend

* React
* Vite
* Tailwind CSS
* Axios
* React Router

## Key Features

* **CSV Loan Import:** Import overdue loan records using CSV files.
* **Automatic Assignment:** Assign loans to field officers based on pincode and workload.
* **AI-Assisted Prioritisation:** Generate transparent, rule-based priority scores with explanations.
* **Officer Dashboard:** View assigned loans and prioritise daily collection activities.
* **Outcome Logging:** Record payment, promise to pay, not reachable, and dispute outcomes.
* **Offline Outcome Queue:** Store pending outcomes locally using browser localStorage.
* **Promise-to-Pay Tracking:** Record and monitor borrower payment commitments.
* **Contact Window Enforcement:** Restrict contact attempts to the permitted 8 AM–7 PM window (IST).
* **Bilingual Reminder Drafts:** Generate English and Hindi reminder drafts using approved templates.
* **Manager Dashboard:** Monitor overdue loans, outstanding amounts, promises, and collection activity.
* **Authentication and Authorization:** JWT-based authentication with manager and field officer roles.

## AI Prioritisation

The current MVP uses a transparent, deterministic, rule-based scoring engine rather than a trained machine learning model.

Priority scores are calculated using:

* Days past due (DPD).
* Overdue outstanding amount.
* Overdue amount relative to the loan amount.

Each loan receives a priority score with explanations highlighting the main contributing factors.

The score is advisory and helps field officers understand which loans require attention.

## Reminder Generation

The application supports English and Hindi reminder drafts using approved templates.

* Uses predefined reminder templates.
* Applies prohibited-word filtering.
* Generates drafts rather than automatically sending messages.
* Requires human review and action.

SMS and WhatsApp delivery integrations are not implemented in the current MVP.

## Security

* JWT-based authentication.
* Role-based access for branch managers and field officers.
* Backend authorization and field-officer ownership checks.
* Database credentials and JWT secrets are configured through environment variables.

Use synthetic sample data for demonstrations. Do not commit passwords, database credentials, or other secrets.

## Project Structure

```text
CollectSmart-AI/
│
├── collectsmart-backend/
│   ├── src/main/java/com/collectsmart/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   │
│   └── src/main/resources/
│
├── collectsmart-frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── context/
│   │   ├── pages/
│   │   ├── services/
│   │   └── assets/
│   │
│   └── package.json
│
├── README.md
└── deployment.md
```

## Local Setup

### Prerequisites

Install:

* Java JDK 21
* MySQL 8
* Node.js and npm
* Git

### 1. Clone the Repository

```bash
git clone https://github.com/abhi5016/CollectSmart-AI.git
cd CollectSmart-AI
```

### 2. Database Setup

Create the MySQL database:

```sql
CREATE DATABASE collectsmart_db;
```

The backend uses:

```text
jdbc:mysql://localhost:3306/collectsmart_db
```

### 3. Backend Setup

Navigate to the backend directory:

```powershell
cd collectsmart-backend
```

Configure environment variables in PowerShell:

```powershell
$env:DB_USERNAME="your_mysql_username"
$env:DB_PASSWORD="your_mysql_password"
$env:APP_JWT_SECRET="your_long_random_secret"
$env:APP_JWT_EXPIRATION_MINUTES="60"
```

Start the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend URL:

```text
http://localhost:8081
```

### 4. Frontend Setup

Open another terminal and navigate to the frontend directory:

```bash
cd collectsmart-frontend
```

Install dependencies:

```bash
npm install
```

Start the frontend:

```bash
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

Additional environment variables for initial manager and officer account creation are documented in `deployment.md`.

## Configuration

| Environment Variable         | Description                    |
| ---------------------------- | ------------------------------ |
| `DB_USERNAME`                | MySQL username                 |
| `DB_PASSWORD`                | MySQL password                 |
| `APP_JWT_SECRET`             | JWT signing secret             |
| `APP_JWT_EXPIRATION_MINUTES` | JWT token expiry               |
| `BOOTSTRAP_MANAGER_EMAIL`    | Initial manager email          |
| `BOOTSTRAP_MANAGER_PASSWORD` | Initial manager password       |
| `BOOTSTRAP_OFFICER_EMAIL`    | Initial field officer email    |
| `BOOTSTRAP_OFFICER_PASSWORD` | Initial field officer password |
| `BOOTSTRAP_OFFICER_ID`       | Initial field officer ID       |

Never commit actual credentials or secrets.

## Important Implementation Notes

* Priority scoring is deterministic and rule-based, not a trained ML model.
* Reminder generation uses approved templates and prohibited-word filtering.
* Reminder messages are drafts; automatic SMS/WhatsApp delivery is not implemented.
* Offline outcome storage currently uses browser localStorage. Robust offline synchronisation and conflict resolution require further development.
* The contact window is enforced using India Standard Time (IST).
* Automated weekly priority-score back-testing is not implemented.
* The application is an evaluation MVP and requires additional testing and production hardening before real-world deployment.

## Repository

GitHub: https://github.com/abhi5016/CollectSmart-AI

## Project Status

MVP developed for the **Isocode Labs Software Engineering Intern take-home assignment**.

The project demonstrates loan import, field-officer workflows, priority scoring, outcome logging, promise tracking, reminder drafting, and manager-facing collection monitoring.

**Deployment status:** Local development setup. No public production deployment URL is currently available.

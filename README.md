# CollectSmart AI

### AI-Assisted MSME Loan Collection and Field Officer Management Platform

CollectSmart AI is a web-based platform designed to help MSME lenders manage overdue loan collections, assign field officers, prioritize collection activities, track promises to pay, and monitor collection performance.

## Technology Stack

**Backend**

* Java 21
* Spring Boot
* Spring Data JPA / Hibernate
* MySQL
* REST APIs
* Maven

**Frontend**

* React
* Vite
* Tailwind CSS
* Axios
* React Router

## Key Features

* **CSV Loan Import:** Import overdue loan records using CSV files.
* **Automatic Assignment:** Assign loans to field officers based on pincode and workload.
* **AI-Assisted Prioritization:** Rule-based priority scoring with explanations for each loan.
* **Officer Dashboard:** View assigned loans and prioritize collection activities.
* **Outcome Logging:** Record payment, promise to pay, not reachable, and dispute outcomes.
* **Offline Outcome Queue:** Store outcomes locally and synchronize when connectivity returns.
* **Promise-to-Pay Tracking:** Record and monitor borrower payment commitments.
* **Contact Window Enforcement:** Restrict contact attempts to the permitted 8 AM–7 PM window (IST).
* **Bilingual Reminder Drafts:** Generate English and Hindi reminder drafts using approved templates.
* **Manager Dashboard:** Monitor overdue loans, outstanding amounts, promises, and loan aging.

## AI Prioritization

The current MVP uses a transparent, rule-based scoring engine rather than a trained machine learning model.

Priority scores are calculated using:

* Days past due.
* Outstanding overdue amount.
* Overdue amount relative to the original loan amount.

Each score includes explanations to help officers understand why a loan is prioritized.

## Project Structure

```text
CollectSmart-AI/
│
├── collectsmart-backend/
│   ├── src/main/java/com/collectsmart/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   └── src/main/resources/
│
├── collectsmart-frontend/
│   ├── src/
│   │   ├── pages/
│   │   ├── services/
│   │   └── assets/
│   └── package.json
│
└── README.md
```

## Local Setup

### Backend

1. Install Java 21 and MySQL 8.
2. Create a database named `collectsmart_db`.
3. Navigate to `collectsmart-backend`.
4. Configure database environment variables.
5. Run:

```bash
.\mvnw.cmd spring-boot:run
```

Backend runs at:

`http://localhost:8081`

### Frontend

1. Navigate to `collectsmart-frontend`.
2. Install dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm run dev
```

Frontend runs at:

`http://localhost:5173`

## Configuration

Configure these backend environment variables:

| Variable    | Description               |
| ----------- | ------------------------- |
| DB_URL      | MySQL JDBC connection URL |
| DB_USERNAME | MySQL username            |
| DB_PASSWORD | MySQL password            |

Do not commit actual database credentials or other secrets.

## Important Implementation Notes

* Priority scoring is deterministic and rule-based.
* Reminder generation uses approved templates and safety filtering; messages are drafts and require human action.
* Offline outcome storage currently uses browser local storage.
* This is an evaluation MVP. Production deployment requires authentication, role-based authorization, stronger offline synchronization, and additional security controls.

## Repository

GitHub: https://github.com/abhi5016/CollectSmart-AI

## Project Status

MVP implementation completed for the Isocode Labs Software Engineering Intern take-home assignment.

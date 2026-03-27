# 🎫 AI Support Ticket Classifier

A full-stack AI-powered customer support ticket management system that automatically classifies, prioritizes, and routes incoming support tickets using the **Groq API (LLaMA 3.3-70B)**. Built with Spring Boot, secured with JWT-based RBAC, and containerized with Docker Compose.

---

## 🚀 Features

- **AI Classification** — Tickets are automatically classified by category, priority, assigned team, and flagged for manual review if the AI's confidence is low
- **Auto-Routing** — Each ticket is routed to the correct support team (Billing / Technical / Account / Refund) based on AI output
- **3-Role RBAC** — Customers, Agents, and Admins each have distinct permissions enforced via JWT + Spring Security
- **Email Notifications** — Customers receive confirmation emails on ticket creation and resolution emails when their ticket is closed
- **Dockerized** — Full stack (app + MySQL) runs with a single `docker-compose up` command

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Java 17 |
| AI / LLM | Groq API (LLaMA 3.3-70B) |
| Database | MySQL 8 |
| Auth | JWT (jjwt 0.11.5) + BCrypt |
| Email | Spring Mail (Gmail SMTP) |
| Infra | Docker, Docker Compose |
| Build | Maven |

---

## 📐 Architecture

```
┌─────────────┐       ┌──────────────────────┐       ┌─────────────┐
│   Client    │──────▶│   Spring Boot API     │──────▶│  MySQL DB   │
│  (REST API) │       │                      │       │             │
└─────────────┘       │  ┌────────────────┐  │       └─────────────┘
                      │  │  GeminiService │  │
                      │  │  (Groq/LLaMA)  │  │       ┌─────────────┐
                      │  └────────────────┘  │──────▶│  Groq API   │
                      │                      │       │ LLaMA 3.3   │
                      │  ┌────────────────┐  │       └─────────────┘
                      │  │  EmailService  │  │
                      │  │ (Spring Mail)  │  │       ┌─────────────┐
                      │  └────────────────┘  │──────▶│  Gmail SMTP │
                      └──────────────────────┘       └─────────────┘
```

---

## 👥 Roles & Permissions

| Role | Permissions |
|---|---|
| `CUSTOMER` | Register, login, create tickets, view own tickets |
| `AGENT` | View all tickets, resolve tickets, get assigned tickets |
| `ADMIN` | All of the above + assign agents to tickets, view flagged tickets, filter by status |

---

## 🤖 AI Classification

When a ticket is submitted, the system sends the title and description to the Groq API with a structured prompt. The model returns a JSON response:

```json
{
  "category": "BILLING",
  "priority": "HIGH",
  "assignedTeam": "BILLING",
  "suggestedResponse": "We will review your billing issue within 24 hours.",
  "confidenceScore": 87,
  "flaggedForReview": false
}
```

- **Category:** `BILLING` | `TECHNICAL` | `ACCOUNT` | `REFUND`
- **Priority:** `LOW` | `MEDIUM` | `HIGH` | `URGENT`
- **Flagged for Review:** Set by the AI when confidence is low, or automatically set to `true` if JSON parsing fails

---

## 📁 Project Structure

```
src/main/java/com/ticket/classifier/
├── config/
│   ├── JwtConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AdminController.java
│   ├── AuthController.java
│   └── TicketController.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── TicketRequest.java
│   └── response/
│       ├── LoginResponse.java
│       └── TicketResponse.java
├── entity/
│   ├── Ticket.java
│   └── User.java
├── enums/
│   ├── Category.java
│   ├── Priority.java
│   ├── Role.java
│   ├── Team.java
│   └── TicketStatus.java
├── repository/
│   ├── TicketRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtFilter.java
│   └── JwtUtil.java
├── service/
│   ├── AdminService.java
│   ├── AuthService.java
│   ├── EmailService.java
│   ├── GeminiService.java   ← calls Groq API
│   └── TicketService.java
└── TicketClassifierApplication.java
```

---

## ⚙️ Setup & Running

### Prerequisites

- Java 17+
- Maven
- Docker & Docker Compose
- A [Groq API key](https://console.groq.com)
- A Gmail account with an App Password

---

### Option 1: Docker Compose (Recommended)

1. **Clone the repo**
   ```bash
   git clone https://github.com/your-username/ai-ticket-classifier.git
   cd ai-ticket-classifier
   ```

2. **Set environment variables** in `application.yml` (or use `.env`):
   ```yaml
   groq:
     api:
       key: YOUR_GROQ_API_KEY
   spring:
     mail:
       username: your-email@gmail.com
       password: your-gmail-app-password
   ```

3. **Build the JAR**
   ```bash
   mvn clean package -DskipTests
   ```

4. **Start the stack**
   ```bash
   docker-compose up --build
   ```

The API will be available at `http://localhost:8080`.

---

### Option 2: Run Locally (without Docker)

1. Make sure MySQL is running locally with a database named `ticket_classifier`
2. Update `application.yml` with your local DB credentials
3. Run:
   ```bash
   mvn spring-boot:run
   ```

---

## 🔌 API Endpoints

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Login and receive JWT |

### Tickets
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/tickets` | Authenticated | Create a new ticket (triggers AI classification) |
| GET | `/api/tickets/my` | Authenticated | Get all tickets created by current user |
| GET | `/api/tickets/{id}` | Authenticated | Get a specific ticket |
| PUT | `/api/tickets/{id}/resolve` | Agent/Admin | Mark a ticket as resolved |

### Admin
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/admin/tickets` | Admin | Get all tickets |
| GET | `/api/admin/tickets/flagged` | Admin | Get AI-flagged tickets |
| GET | `/api/admin/tickets/status/{status}` | Admin | Filter tickets by status |
| GET | `/api/admin/agents` | Admin | List all agents |
| PUT | `/api/admin/tickets/{ticketId}/assign/{agentId}` | Admin | Assign ticket to agent |
| PUT | `/api/admin/tickets/{ticketId}/resolve` | Admin | Resolve a ticket |

---

## 🔐 Security Notes

> ⚠️ The `application.yml` in this repo contains placeholder credentials. Before deploying:
> - Move all secrets (API keys, DB passwords, JWT secret, email password) to **environment variables** or a secrets manager
> - Never commit real credentials to version control

---

## 📧 Email Notifications

The system sends two types of emails via Spring Mail (Gmail SMTP):

- **Ticket Created** — Sent to the customer immediately after submission, includes ticket ID, category, priority, assigned team, and AI-suggested response
- **Ticket Resolved** — Sent to the customer when an agent or admin marks the ticket as resolved

---

## 🗺️ Ticket Lifecycle

```
OPEN  ──▶  IN_PROGRESS  ──▶  RESOLVED
 │                               ▲
 └── (flaggedForReview = true) ──┘
         Manual review needed
```

---

## 📄 License

MIT License. Feel free to use, modify, and distribute.

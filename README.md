![SmartHire Backend Banner](file:///C:/Users/Admin/.gemini/antigravity/brain/bf2e4e15-9e5b-4f89-becd-874451779a93/smarthire_backend_banner_1776016979809.png)

# SmartHire Backend API ⚙️

The robust, secure, and scalable backend engine for the SmartHire ATS Platform. Built on Java Spring Boot, this sophisticated service handles all job operations, candidate profiling, CV parsing logic, security, and document verification workflows.

## 🛠 Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Framework** | Java 17, Spring Boot 3.x |
| **Security** | Spring Security, JWT (JSON Web Tokens), OAuth2 |
| **Database** | PostgreSQL |
| **ORM** | Hibernate / Spring Data JPA |
| **DevOps** | Docker (Multi-stage Temurin JRE), Docker Compose |

## 📐 Architecture & Project Structure

```text
smarthire-app-backend/
├── src/main/java/com/smarthire/backend/
│   ├── core/              # Global configs, Security, Exceptions, Filters
│   ├── features/          # Domain slices: (auth, application, cv, employer, jobs)
│   │   └── .../           # Controller, Service, Repository, DTO inside each feature
│   └── shared/            # Shared utilities, Enums, File handlers
├── src/main/resources/    # Application properties (dev, prod)
├── pom.xml                # Maven Dependencies
├── Dockerfile             # Multi-stage production container
└── docker-compose.yml     # Orchestration encompassing DB and App
```

## ✨ Core Features

- **🔐 Enterprise Authentication**: Stateful-less JWT Authentication combined with OAuth2 callback mechanisms, strictly rate-limited for security.
- **📄 Advanced CV Processing**: API endpoints designed to parse and structure CV uploads.
- **🗂 Secure Document Storage**: Verified applicant document management for the onboarding module.
- **⚙️ Dynamic Job Feeds**: High-performance querying of job postings and applicants using Spring Data JPA.
- **🤖 Safe DDL Execution**: Startup validators to prevent accidental data overwrites (`StartupValidator.java`).

## 📦 Getting Started

### Prerequisites
- Java JDK 17
- Maven 3.8+
- PostgreSQL Server 14+
- Docker

### 1. Local Setup
```bash
git clone https://github.com/khoazandev/smart-hire-backend.git
cd smart-hire-backend

# Package the application
./mvnw clean install -DskipTests

# Run the app with development profile
java -jar target/smarthire-backend-0.0.1-SNAPSHOT.jar
# Server runs on http://localhost:8080
```

### 2. Docker Deployment
We provide a unified `docker-compose.yml` to spin up both Postgres and the Spring app instantly:
```bash
docker-compose up -d --build
```

## 🔑 Environment Variables

| Variable | Description |
| :--- | :--- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME`| Database user |
| `SPRING_DATASOURCE_PASSWORD`| Database password |
| `APP_JWT_SECRET` | 256-bit Hex secret for JWT signing |
| `APP_OAUTH2_GITHUB_CLIENT_ID`| GitHub OAuth App Client ID |
| `APP_OAUTH2_GITHUB_CLIENT_SECRET`| GitHub OAuth App Secret |
| `APP_FRONTEND_URL` | Used for CORS and OAuth redirects |

## 🗄 Database Schema (Core Overview)

```text
┌────────────────┐     ┌────────────────┐     ┌────────────────┐
│      User      │     │  Job Posting   │     │  Application   │
├────────────────┤     ├────────────────┤     ├────────────────┤
│ ID             │◄───┐│ ID             │◄───┐│ ID             │
│ Email          │    ││ EmployerID (FK)│    ││ JobID (FK)     │
│ Role           │    ││ Title / Desc   │    ││ UserID (FK)    │
│ PasswordHash   │    ││ Status         │────┘│ Stage          │
└────────────────┘    │└────────────────┘     └────────────────┘
                      │
┌────────────────┐    │┌────────────────┐  
│ Candidate Prof │    ││ Onboarding Doc │  
├────────────────┤    │├────────────────┤  
│ UserID (FK)    │───▶││ AppID (FK)     │  
│ FirstName      │    ││ DocumentType   │  
│ Experience     │    ││ AI Feedback    │  
└────────────────┘    │└────────────────┘  
                      │                    
┌────────────────┐    │                    
│    CV Record   │    │                    
├────────────────┤    │                    
│ UserID (FK)    │◄───┘                    
│ RawFileUrl     │                         
│ AI_Parse_Data  │                         
└────────────────┘                         
```

<br />
<p align="center"><i>Powered by Spring Boot & Java 17</i></p>
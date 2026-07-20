# 🚀 Uptime Monitor MVP

A lightweight, full-stack uptime monitoring application built with Java 21 (Spring Boot), Vanilla JS, Nginx, and PostgreSQL. Designed as a strict MVP to demonstrate rapid execution velocity, clean architecture, and containerized orchestration.

## 📋 Table of Contents

- [🛠️ Tech Stack](#️-tech-stack)
- [⚡ Quick Start](#-quick-start)
- [✅ System Verification](#-system-verification)
- [🧪 Testing](#-testing)
- [☁️ Cloud Deployment](#️-cloud-deployment)

---

## 🛠️ Tech Stack

- **Backend:** Java 21, Spring Boot, Spring Data JPA, Native HttpClient
- **Frontend:** Vanilla HTML/CSS/JS, Nginx (Alpine)
- **Database:** PostgreSQL 15
- **Infrastructure:** Docker, Docker Compose
- **Testing:** JUnit 5, MockMvc, Mockito

---

## ⚡ Quick Start

### Prerequisites

- Docker
- Docker Compose

### Running the Application

From the root directory, run:

```bash
docker compose up --build
```

> ⚠️ **Note:** Initial build may take 1-2 minutes while Maven downloads dependencies and compiles the Spring Boot JAR inside the container. Wait until you see the Spring Boot startup logs complete.

Once running, access the dashboard at: **http://localhost:3000**

---

## ✅ System Verification

The application features a background scheduled task that pings registered URLs every 60 seconds, and a frontend that auto-refreshes every 5 seconds.

### Test 1: Verify "UP" State (Healthy URL)

1. Open http://localhost:3000 in your browser
2. Enter a known healthy URL (e.g., https://example.com)
3. Click **Monitor URL**
4. **Expected Result:** The table updates within a few seconds showing:
   - Status: **UP** (green)
   - Valid response time (e.g., 145 ms)

> **Under the hood:** The Java HttpClient successfully resolves DNS, completes the TCP handshake, and receives an HTTP 200 OK response.

### Test 2: Verify "DOWN" State (Unreachable URL)

1. Enter an intentionally broken URL (e.g., https://this-domain-does-not-exist-12345.com)
2. Click **Monitor URL**
3. **Expected Result:** The table updates showing:
   - Status: **DOWN** (red)
   - The DNS resolution fails and the backend gracefully catches `UnknownHostException`

---

## 🧪 Testing

The backend includes isolated slice tests using `@WebMvcTest` and `MockMvc` to verify REST API contracts (e.g., ensuring 201 Created and 400 Bad Request status codes) without requiring a live database.

### Run Unit Tests

From the `/backend` directory:

```bash
# Mac/Linux
./mvnw clean test

# Windows
.\mvnw.cmd clean test
```

---

## ☁️ Cloud Deployment

### Recommended Architecture

For a production MVP deployment, bypass complex EC2/Kubernetes setups and leverage:

- **AWS ECS Fargate** — Serverless container orchestration
- **Application Load Balancer (ALB)** — SSL termination and traffic routing
- **Amazon RDS (PostgreSQL)** — Managed, backed-up database

**Benefits:**

- Fargate removes the need to manage underlying servers and scales seamlessly
- ALB handles SSL termination and routes traffic; Nginx acts as a reverse proxy for `/api/*` traffic, eliminating CORS issues
- RDS provides a managed, backed-up PostgreSQL instance

### Example Terraform Configuration

```hcl
resource "aws_ecs_task_definition" "backend_task" {
  family                   = "uptime-backend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([{
    name      = "backend"
    image     = "${var.aws_account_id}.dkr.ecr.${var.region}.amazonaws.com/uptime-backend:latest"
    essential = true
    portMappings = [{
      containerPort = 8080
      hostPort      = 8080
    }]
    environment = [
      {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:postgresql://${aws_db_instance.uptime_db.endpoint}/uptimedb"
      },
      {
        name  = "SPRING_DATASOURCE_USERNAME"
        value = var.db_username
      },
      {
        name  = "SPRING_DATASOURCE_PASSWORD"
        value = var.db_password
      }
    ]
  }])
}
```

---

## 📄 License

This project is open source and available under the MIT License.
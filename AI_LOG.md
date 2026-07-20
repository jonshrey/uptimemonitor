# 🤖 AI Collaboration Log

This log documents how AI tools were used to build the Uptime Monitor MVP, including the core prompts that scaffolded the project and the manual corrections required to bring the generated code up to production standards.

📎 [First prompt to DeepSeek](https://chat.deepseek.com/share/oxz0mxf2ap64ocwh20)

---

## 🛠️ The AI Tech Stack

- **IDE & Copilot:** Cursor / VS Code, leveraging inline completions, terminal integration, and architectural reasoning
- **Underlying LLMs:** DeepSeek and Qwen for Java/Spring Boot boilerplate, Docker orchestration, and debugging; Claude for frontend logic, REST API design patterns, and documentation

---

## 💬 The Prompts That Shipped It

To rapidly scaffold the application while strictly adhering to the "beautifully simple" MVP constraint, I used three core prompts.

**1. Backend Scaffolding**
> *"Generate a Spring Boot backend using Java 21. Include a `Url` JPA entity and a `HealthCheck` entity. Create a REST controller to accept a POST request to add a URL, and a scheduled service that runs every 60 seconds to ping all registered URLs using Java's native `HttpClient`. Store the HTTP status code, response time in ms, and timestamp."*

**2. Frontend Generation**
> *"Write a purely static HTML/CSS/JS frontend. No React, no build steps. It should have an input form to add a URL, and a table that fetches the Spring Boot API every 5 seconds to display the URL, Status (UP/DOWN), Response Time, and Last Checked timestamp."*

**3. Dockerization**
> *"Provide a multi-stage Dockerfile for a Maven Spring Boot app using Eclipse Temurin, and a simple Nginx Dockerfile for the static frontend. Write a `docker-compose.yml` that spins up Postgres, the backend, and the frontend, passing the correct DB environment variables to Spring Boot."*

---

## 🔄 Course Corrections & Debugging

The AI tools were excellent at generating boilerplate, but they defaulted to outdated patterns or missed modern framework shifts in several places. Below are the key issues I had to catch and fix manually.

### 1. Field Injection vs. Constructor Injection
**The mistake:** The initial service and controller generation used `@Autowired` field injection for dependencies (`UrlRepository`, `PingService`).

**The fix:** Field injection is an anti-pattern that makes unit testing harder and hides `NullPointerException`s until runtime. I refactored the code to use **constructor injection** with `final` fields, giving immutability, fail-fast startup behavior, and cleaner `@WebMvcTest` slicing.

### 2. RESTful Standards & `ResponseEntity`
**The mistake:** The AI initially returned raw JPA entities directly from the `@PostMapping`, which defaults to an HTTP `200 OK` status regardless of what actually happened.

**The fix:** I refactored the controller to wrap responses in `ResponseEntity`, returning a proper **`201 Created`** with a `Location` header on success, and **`400 Bad Request`** for invalid input, in line with REST API conventions.

### 3. Java 21 vs. Java 17 Docker Mismatch
**The mistake:** Spring Initializr defaulted to Java 21, but the AI's initial Dockerfile used `eclipse-temurin:17-jre`. This caused a fatal Maven compiler error (`release version 21 not supported`) during the Docker build.

**The fix:** I read the build logs, identified the JDK version mismatch, and updated the multi-stage Dockerfile to use `maven:3.9-eclipse-temurin-21` and `eclipse-temurin:21-jre` to match the `pom.xml` configuration.

### 4. The Jackson 3 Namespace Migration (Spring Boot 4.x)
**The mistake:** When writing `@WebMvcTest` unit tests, the AI hallucinated the legacy Jackson 2 import (`com.fasterxml.jackson.databind.ObjectMapper`). Since the project runs on Spring Boot 4.x, it uses Jackson 3, which migrated its core package to `tools.jackson`.

**The fix:** I caught the `package does not exist` compilation error, researched the Jackson 3 migration, and updated the test imports to `tools.jackson.databind.ObjectMapper`, letting the test suite compile and pass.

### 5. Nginx & Windows CRLF Line Endings
**The mistake:** The generated `nginx.conf` failed to start inside the Alpine Linux container, silently crashing the frontend container.

**The fix:** I checked `docker compose logs frontend` and found that VS Code on Windows had saved the file with `CRLF` line endings, which broke the Linux Nginx parser. Switching the file to `LF` line endings in the IDE resolved the crash, and the static frontend started serving correctly on port 3000.

---

## 🧭 Takeaway

AI tools accelerated scaffolding significantly, but every layer — dependency injection, REST semantics, JDK/Docker alignment, framework migrations, and even line-ending conventions — still needed a human pass to catch subtle, non-obvious issues that only surface at build or runtime. Final finsihing touches was done with the help of claude specially formatting and writing README.
# Payment Routing Engine 💳

A sophisticated Spring Boot application that intelligently routes payment transactions across multiple payment gateways, optimizing for cost, speed, and availability while respecting gateway limits and quotas.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Smart Routing Algorithm](#smart-routing-algorithm)
- [Database Schema](#database-schema)
- [API Documentation](#api-documentation)
- [Setup Instructions](#setup-instructions)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Usage Examples](#usage-examples)
- [Contributing](#contributing)

---

## 🎯 Overview

This payment routing engine helps financial service providers (like Fawry) automatically select the optimal payment gateway for each transaction. The system analyzes multiple factors including transaction amount, processing urgency, commission costs, gateway availability, and daily quotas to recommend the best gateway for each payment.

### Why This Matters

In real-world payment processing:
- **Different gateways have different costs** - Some charge fixed fees, others percentage-based
- **Processing times vary** - Mobile wallets are instant, bank transfers take hours/days
- **Availability windows differ** - Some gateways operate 24/7, others have business hours
- **Daily limits exist** - Each biller has spending caps per gateway that reset daily

This engine optimizes gateway selection to **minimize costs** while meeting **urgency requirements** and respecting all **operational constraints**.

---

## ✨ Features

- ✅ **Smart Gateway Recommendation** - Multi-factor algorithm for optimal routing
- ✅ **Real-time Availability Checking** - Day/time-based gateway scheduling
- ✅ **Dynamic Commission Calculation** - Fixed + percentage-based fees
- ✅ **Quota Management** - Daily limits per biller per gateway (auto-reset at midnight)
- ✅ **Transaction History** - Complete audit trail with summaries and analytics
- ✅ **JWT Authentication** - Secure API access with role-based authorization
- ✅ **RESTful API** - Clean, well-documented endpoints
- ✅ **Dynamic Configuration** - Add/update gateways without code changes

---

## 🏗️ Architecture

### System Components

```
┌─────────────────────┐
│   Angular Frontend  │  (User Interface)
│   (Port 4200)       │
└──────────┬──────────┘
           │ HTTP/REST + JWT
           │
┌──────────▼─────────────────────────────────────┐
│         Spring Boot Backend (Port 8080)        │
│  ┌──────────────────────────────────────┐     │
│  │     Controllers Layer                │     │
│  │  - AuthController                    │     │
│  │  - PaymentController                 │     │
│  │  - GatewayController                 │     │
│  │  - TransactionController             │     │
│  │  - BillerController                  │     │
│  └──────────────┬───────────────────────┘     │
│                 │                              │
│  ┌──────────────▼───────────────────────┐     │
│  │        Service Layer                 │     │
│  │  - RoutingAlgorithmService ⭐        │     │
│  │  - GatewayService                    │     │
│  │  - TransactionService                │     │
│  │  - QuotaService                      │     │
│  │  - BillerService                     │     │
│  │  - AuthService                       │     │
│  └──────────────┬───────────────────────┘     │
│                 │                              │
│  ┌──────────────▼───────────────────────┐     │
│  │    Repository Layer (JPA)            │     │
│  │  - Spring Data JPA Repositories      │     │
│  └──────────────┬───────────────────────┘     │
└─────────────────┼────────────────────────────┘
                  │
         ┌────────▼──────────┐
         │  PostgreSQL/      │
         │  Oracle Database  │
         └───────────────────┘
```

### Design Principles

1. **Separation of Concerns** - Clear layering (Controller → Service → Repository)
2. **Single Responsibility** - Each service handles one domain
3. **Dependency Injection** - Loose coupling via Spring's DI container
4. **RESTful Design** - Stateless, resource-oriented APIs
5. **Transaction Management** - ACID compliance for financial operations

---

## 🧠 Smart Routing Algorithm

The routing algorithm is the **heart of the system**. It uses a **multi-stage filtering and scoring approach** to select the optimal gateway.

### Algorithm Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│  INPUT: billerCode, amount, urgency                     │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────▼────────────┐
         │  Stage 1: HARD FILTERS │
         │  (Must Pass All)       │
         └───────────┬────────────┘
                     │
    ┌────────────────┼────────────────┐
    │                │                │
    ▼                ▼                ▼
┌────────┐    ┌──────────┐    ┌──────────┐
│ Amount │    │Available │    │  Quota   │
│  Fits  │    │   Now?   │    │Remaining?│
│ Limits │    │(Day/Time)│    │          │
└───┬────┘    └────┬─────┘    └────┬─────┘
    │              │               │
    └──────┬───────┴───────┬───────┘
           │               │
           ▼               ▼
      ✓ PASS          ✗ REJECT
           │
           │
    ┌──────▼──────────────────┐
    │ Stage 2: URGENCY FILTER │
    └──────┬──────────────────┘
           │
    ┌──────┴───────┐
    │              │
    ▼              ▼
┌─────────┐   ┌──────────┐
│ INSTANT │   │ CAN_WAIT │
│         │   │          │
│ Filter  │   │ Keep All │
│ to only │   │ Gateways │
│ instant │   │          │
│gateways │   │          │
└────┬────┘   └─────┬────┘
     │              │
     └──────┬───────┘
            │
    ┌───────▼────────────────┐
    │ Stage 3: SCORING       │
    │ & RANKING              │
    └───────┬────────────────┘
            │
    ┌───────▼───────┐
    │ 1. Commission │ (Primary - Lowest First)
    │    (Ascending)│
    └───────┬───────┘
            │
    ┌───────▼───────┐
    │ 2. Remaining  │ (Secondary - Highest First)
    │    Quota      │ (Tiebreaker)
    │  (Descending) │
    └───────┬───────┘
            │
    ┌───────▼────────────────┐
    │ OUTPUT:                │
    │ - Best Gateway         │
    │ - Alternatives (Top 2) │
    │ - Recommendation Reason│
    └────────────────────────┘
```

---

## 📊 Detailed Algorithm Stages

### Stage 1: Hard Filters (MUST Pass)

These are **binary checks** - gateway either passes or is eliminated.

#### 1.1 Amount Fit Check (`fitAmount()`)

```java
boolean fitAmount(Gateway gateway, BigDecimal amount)
```

**Logic:**
- ✅ `amount > gateway.minTransaction` → PASS
- ✅ `amount < gateway.maxTransaction` → PASS
- ❌ Otherwise → REJECT

**Example:**

| Gateway | Min | Max | Amount = 1000 | Amount = 15000 | Amount = 50 |
|---------|-----|-----|---------------|----------------|-------------|
| Gateway 1 | 10 | 5000 | ✅ PASS | ❌ REJECT (too high) | ✅ PASS |
| Gateway 2 | 100 | 0 (unlimited) | ✅ PASS | ✅ PASS | ❌ REJECT (too low) |
| Gateway 3 | 50 | 10000 | ✅ PASS | ❌ REJECT (too high) | ✅ PASS |

#### 1.2 Availability Check (`isAvailableNow()`)

```java
boolean isAvailableNow(Gateway gateway)
```

**Logic:**
- Get current day (MON, TUE, WED, etc.) and time
- Check gateway_availability table for matching schedule
- If `is_24_7 = TRUE` → ✅ PASS
- Else check if `currentTime BETWEEN startTime AND endTime` → ✅ PASS
- Otherwise → ❌ REJECT

**Example:**

| Gateway | Schedule | Current: Tuesday 2:30 PM | Current: Friday 10:00 PM |
|---------|----------|--------------------------|--------------------------|
| Gateway 1 | 24/7 | ✅ PASS | ✅ PASS |
| Gateway 2 | Sun-Thu 9AM-5PM | ✅ PASS (within window) | ❌ REJECT (Friday closed) |
| Gateway 3 | 24/7 | ✅ PASS | ✅ PASS |

#### 1.3 Quota Remaining Check (`hasQuotaRemaining()`)

```java
boolean hasQuotaRemaining(Long billerId, Gateway gateway, BigDecimal amount)
```

**Logic:**
- Calculate: `remainingQuota = gateway.dailyLimit - todayUsedAmount`
- Check if `amount <= remainingQuota` → ✅ PASS
- Otherwise → ❌ REJECT

**Example:**

Biller wants to pay 3000 EGP:

| Gateway | Daily Limit | Used Today | Remaining | Amount = 3000 | Result |
|---------|-------------|------------|-----------|---------------|--------|
| Gateway 1 | 50,000 | 45,000 | 5,000 | 3,000 | ✅ PASS |
| Gateway 2 | 200,000 | 150,000 | 50,000 | 3,000 | ✅ PASS |
| Gateway 3 | 100,000 | 98,000 | 2,000 | 3,000 | ❌ REJECT (insufficient) |

---

### Stage 2: Urgency Filtering

This stage **narrows down** the gateway pool based on urgency requirements.

#### Case 1: `urgency = INSTANT`

```java
if (urgency == INSTANT) {
    List<Gateway> instantGateways = gateways.stream()
        .filter(g -> g.processingTime == 0)  // 0 seconds = instant
        .collect(Collectors.toList());
    
    if (!instantGateways.isEmpty()) {
        gateways = instantGateways;  // Use only instant gateways
    } else {
        // Fallback: Keep all gateways if no instant available
        log.warn("No instant gateways available");
    }
}
```

**Key Behavior:**
- **Prioritizes instant gateways** (processingTime = 0)
- Falls back to all available gateways if no instant options exist
- This ensures urgent payments get fastest processing when possible

#### Case 2: `urgency = CAN_WAIT`

```java
else {
    // Keep all gateways - no filtering
    // All processing times are acceptable (instant, 2 hours, 24 hours)
}
```

**Key Behavior:**
- **No filtering applied** - all gateways remain eligible
- Instant gateways have **NO priority** over slower ones
- Pure cost optimization takes over

**Example:**

Available gateways after hard filters:

| Gateway | Processing Time | Urgency = INSTANT | Urgency = CAN_WAIT |
|---------|----------------|-------------------|-------------------|
| Gateway 1 (Vodafone) | 0 seconds (Instant) | ✅ Eligible | ✅ Eligible |
| Gateway 2 (Bank) | 24 hours | ❌ Filtered out* | ✅ Eligible |
| Gateway 3 (Card) | 2 hours | ❌ Filtered out* | ✅ Eligible |

*Unless no instant gateways exist, then kept as fallback

---

### Stage 3: Scoring & Ranking

Gateways are **sorted** by two criteria:

```java
List<ScoredGateway> scoredGateways = gateways.stream()
    .map(g -> scoreGateway(g, billerId, amount))
    .sorted(
        Comparator.comparing(ScoredGateway::getCommission)           // 1️⃣ Primary
            .thenComparing(ScoredGateway::getRemainingQuota,        // 2️⃣ Secondary
                           Comparator.reverseOrder())
    )
    .collect(Collectors.toList());
```

#### Primary Sort: Commission (Ascending)

**Formula:**
```
commission = gateway.commissionFixed + (amount × gateway.commissionPercentage)
```

**Lower commission = Better rank**

#### Secondary Sort: Remaining Quota (Descending - Tiebreaker)

**When two gateways have identical commission:**
- Higher remaining quota = Better rank
- Rationale: Preserves capacity for future transactions

**Complete Example:**

**Scenario:** Biller needs to pay **1000 EGP**, urgency = **CAN_WAIT**

| Gateway | Fixed Fee | % Fee | Commission | Daily Limit | Used | Remaining | Rank |
|---------|-----------|-------|------------|-------------|------|-----------|------|
| Gateway 2 | 5.00 | 0.8% | **13.00** | 200,000 | 50,000 | **150,000** | 🥇 **1st** |
| Gateway 1 | 2.00 | 1.5% | **17.00** | 50,000 | 30,000 | 20,000 | 🥈 2nd |
| Gateway 3 | 0.00 | 2.5% | **25.00** | 100,000 | 10,000 | 90,000 | 🥉 3rd |

**Winner:** Gateway 2
- **Why?** Lowest commission (13.00 EGP)
- **Savings:** 4 EGP vs Gateway 1, 12 EGP vs Gateway 3

---

### Stage 4: Response Building

```java
GatewayRecommendationResponse {
    recommendedGateway: {
        id, code, name,
        estimatedCommission,
        urgency,
        remainingQuota
    },
    alternatives: [
        // Top 2 next-best options
    ],
    recommendationReason: "Lowest commission (13.00 EGP) among available gateways"
}
```

---

## 🔄 Algorithm Pseudocode

```
FUNCTION recommendGateway(billerCode, amount, urgency):
    
    // Stage 1: Hard Filters
    gateways = getAllActiveGateways()
    gateways = filter(gateways, WHERE amount fits limits)
    gateways = filter(gateways, WHERE available now)
    gateways = filter(gateways, WHERE quota remaining >= amount)
    
    IF gateways is EMPTY:
        THROW NoAvailableGatewayException
    
    // Stage 2: Urgency Filter
    IF urgency == INSTANT:
        instantGateways = filter(gateways, WHERE processingTime == 0)
        IF instantGateways is NOT EMPTY:
            gateways = instantGateways
        ELSE:
            // Keep all gateways as fallback
            log warning "No instant gateways available"
    // ELSE: urgency == CAN_WAIT → no filtering
    
    // Stage 3: Scoring & Ranking
    FOR EACH gateway IN gateways:
        commission = calculateCommission(gateway, amount)
        remainingQuota = getRemainingQuota(biller, gateway)
        scoredGateways.add(gateway, commission, remainingQuota)
    
    SORT scoredGateways BY:
        1. commission (ascending)
        2. remainingQuota (descending) // tiebreaker
    
    // Stage 4: Build Response
    bestGateway = scoredGateways[0]
    alternatives = scoredGateways[1..2]  // Top 2 alternatives
    
    // Create transaction record
    createTransaction(billerCode, bestGateway.code, amount)
    
    RETURN {
        recommendedGateway: bestGateway,
        alternatives: alternatives,
        recommendationReason: "Lowest commission (...)"
    }
```

---

## 💾 Database Schema

### Entity Relationship Diagram

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   BILLERS   │         │ PAYMENT_GATEWAYS │         │GATEWAY_         │
│             │         │                  │         │AVAILABILITY     │
│ id (PK)     │◄───┐    │ id         (PK)  │◄────────│                 │
│ code        │    │    │ code             │         │ gateway_id (FK) │
│ name        │    │    │ commission_fixed │         │ day_of_week     │
│ email       │    │    │ commission_%     │         │ start_time      │
│ status      │    │    │ min_transaction  │         │ end_time        │
└─────────────┘    │    │ max_transaction  │         │ is_24_7         │
                   │    │ daily_limit      │         └─────────────────┘
                   │    │ processing_time  │
                   │    │ status           │
                   │    └──────────────────┘
                   │              │
                   │              │
                   │              │
┌──────────────┐  │              │  ┌─────────────────┐
│ TRANSACTIONS │  │              │  │ DAILY_QUOTAS    │
│              │  │              │  │                 │
│ id (PK)      │  │              │  │ biller_id (FK)  │
│ code         │  │              └──┤ gateway_id (FK) │
│ biller_id ───┼──┘                 │ quota_date      │
│ gateway_id ──┼────────────────────│ total_amount    │
│ amount       │                    │ trans_count     │
│ commission   │                    │ daily_limit     │
│ urgency      │                    └─────────────────┘
│ status       │
│ created_at   │
└──────────────┘
```

### Key Tables

#### 1. `billers` - Merchants/Clients
```sql
CREATE TABLE billers (
    id  INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE'
);
```

#### 2. `payment_gateways` - Gateway Configurations
```sql
CREATE TABLE payment_gateways (
    id  INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    commission_fixed DECIMAL(10,2) NOT NULL,
    commission_percentage DECIMAL(5,4) NOT NULL,
    min_transaction DECIMAL(15,2) NOT NULL,
    max_transaction DECIMAL(15,2),  -- NULL = unlimited
    daily_limit DECIMAL(15,2) NOT NULL,
    processing_time VARCHAR(50) NOT NULL,  -- stored as BigDecimal (seconds)
    status VARCHAR(20) DEFAULT 'ACTIVE'
);
```

#### 3. `gateway_availability` - Operating Hours
```sql
CREATE TABLE gateway_availability (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    gateway_id INT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,  -- MON, TUE, ..., ALL
    start_time TIME,
    end_time TIME,
    is_24_7 BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (gateway_id) REFERENCES payment_gateways(id_gateway)
);
```

#### 4. `daily_gateway_quotas` - Usage Tracking
```sql
CREATE TABLE daily_gateway_quotas (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    biller_id INT NOT NULL,
    gateway_id INT NOT NULL,
    quota_date DATE NOT NULL,
    total_amount DECIMAL(15,2) DEFAULT 0.00,
    transaction_count INT DEFAULT 0,
    daily_limit DECIMAL(15,2) NOT NULL,
    UNIQUE(biller_id, gateway_id, quota_date)
);
```

**Important:** Quotas automatically reset at midnight (new date = new record).

#### 5. `transactions` - Transaction Log
```sql
CREATE TABLE transactions (
    id  BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) UNIQUE NOT NULL,
    biller_id INT NOT NULL,
    gateway_id INT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    commission DECIMAL(15,2) NOT NULL,
    urgency VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME
);
```

---

## 🚀 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication
All endpoints (except `/auth/login`) require JWT token:
```
Authorization: Bearer <your-jwt-token>
```

---

### 1. Authentication

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "admin",
    "email": "admin@fawry.com",
    "role": "ADMIN"
  }
}
```

---

### 2. Gateway Recommendation (⭐ Core Feature)

#### Get Recommendation
```http
POST /payments/recommend
Authorization: Bearer <token>
Content-Type: application/json

{
  "billerCode": "BI_ABCCORPORA",
  "amount": 1000.00,
  "urgency": "INSTANT"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "recommendedGateway": {
      "id": 1,
      "code": "GW_VODAFONEC",
      "name": "Vodafone Cash",
      "estimatedCommission": 17.00,
      "urgency": "INSTANT",
      "remainingQuota": 20000.00
    },
    "alternatives": [
      {
        "id": 3,
        "code": "GW_CREDITCAR",
        "name": "Credit Card Gateway",
        "estimatedCommission": 25.00,
        "urgency": "CAN_WAIT"
      }
    ],
    "recommendationReason": "Lowest commission (17.00 EGP) among available gateways"
  }
}
```

**Algorithm Applied:**
1. ✅ Amount 1000 fits all gateways
2. ✅ All gateways available (if 24/7)
3. ✅ All have sufficient quota
4. 🔥 Urgency=INSTANT → Filter to instant gateways only
5. 📊 Sort by commission → Vodafone wins (2 + 1.5% = 17.00)

---

### 3. Gateway Management

#### Create Gateway
```http
POST /gateways
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Orange Money",
  "commissionFixed": 1.50,
  "commissionPercentage": 0.0120,
  "minTransaction": 20.00,
  "maxTransaction": 3000.00,
  "dailyLimit": 40000.00,
  "processingTime": 0,
  "isActive": true,
  "availability": [
    {
      "dayOfWeek": "ALL",
      "startTime": null,
      "endTime": null,
      "is24_7": true
    }
  ]
}
```

#### Get All Gateways
```http
GET /gateways
Authorization: Bearer <token>
```

#### Update Gateway
```http
PUT /gateways/{gatewayCode}
Authorization: Bearer <token>
Content-Type: application/json

{
  "commissionFixed": 2.50,
  "dailyLimit": 60000.00
}
```

---

### 4. Transaction History

#### Get Transactions by Biller
```http
GET /billers/{billerCode}/transactions?date=2025-01-26
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "code": "TXN-a3f2c891-...",
      "billerCode": "BI_ABCCORPORA",
      "gatewayCode": "GW_VODAFONEC",
      "gatewayName": "Vodafone Cash",
      "amount": 1000.00,
      "commission": 17.00,
      "urgency": "INSTANT",
      "status": "COMPLETED",
      "createdAt": "2025-01-26T14:30:00",
      "completedAt": "2025-01-26T14:30:02"
    }
  ]
}
```

#### Get Transaction Summary
```http
GET /billers/{billerCode}/transactions/summary?startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "billerCode": "BI_ABCCORPORA",
    "period": {
      "startDate": "2025-01-01",
      "endDate": "2025-01-31"
    },
    "totalTransactions": 45,
    "totalAmount": 125000.00,
    "totalCommission": 2450.00,
    "byGateway": [
      {
        "gatewayCode": "GW_VODAFONEC",
        "gatewayName": "Vodafone Cash",
        "transactionCount": 30,
        "totalAmount": 85000.00,
        "totalCommission": 1675.00,
        "averageCommission": 55.83
      }
    ],
    "byStatus": {
      "COMPLETED": 43,
      "FAILED": 2
    }
  }
}
```

---

## 🛠️ Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 13+ or Oracle 19c+
- Node.js 16+ (for Angular frontend)

### Backend Setup

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/payment-routing-engine.git
cd payment-routing-engine
```

2. **Configure database**

Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_routing
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

3. **Create database**
```sql
CREATE DATABASE payment_routing;
```

4. **Run the application**
```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

5. **Initialize sample data**

The application automatically creates:
- Sample billers
- 3 pre-configured gateways
- Admin user (username: `admin`, password: `password`)

---

### Using Docker (Alternative)

```bash
docker-compose up -d
```

This starts:
- PostgreSQL database
- Spring Boot application

---

## 💻 Technology Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database access
- **PostgreSQL/Oracle** - Relational database
- **JWT (jjwt)** - Token-based authentication
- **Lombok** - Boilerplate code reduction
- **SLF4J/Logback** - Logging

### Frontend (Separate Repo)
- **Angular 16+** - UI framework
- **TypeScript** - Programming language
- **RxJS** - Reactive programming
- **Angular Material** - UI components

### Tools
- **Maven** - Build automation
- **Docker** - Containerization
- **Postman** - API testing
- **Git** - Version control

---

## 📁 Project Structure

```
payment-routing-engine/
├── src/
│   ├── main/
│   │   ├── java/com/fawry/paymentroutingengine/
│   │   │   ├── config/               # Security, CORS, JWT configs
│   │   │   ├── constant/             # Enums (Urgency, Status, etc.)
│   │   │   ├── controller/           # REST endpoints
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── GatewayController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   └── BillerController.java
│   │   │   ├── dto/                  # Request/Response DTOs
│   │   │   ├── entity/               # JPA entities
│   │   │   ├── exception/            # Custom exceptions & handler
│   │   │   ├── repository/           # JPA repositories
│   │   │   ├── service/              # Business logic
│   │   │   │   ├── RoutingAlgorithmService.java ⭐
│   │   │   │   ├── GatewayService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── QuotaService.java
│   │   │   │   ├── BillerService.java
│   │   │   │   └── AuthService.java
│   │   │   └── util/                 # JWT utilities
│   │   └── resources/
│   │       ├── application.yml       # Configuration
│   │       └── data.sql             # Sample data
│   └── test/                         # Unit & integration tests
├── docker-compose.yml               # Docker setup
├── pom.xml                          # Maven dependencies
└── README.md                        # This file
```

---

## 📝 Usage Examples

### Example 1: Urgent Payment - Instant Gateway Selection

**Scenario:** ABC Corporation needs to pay 1000 EGP **immediately**.

**Request:**
```bash
curl -X POST http://localhost:8080/api/payments/recommend \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "billerCode": "BI_ABCCORPORA",
    "amount": 1000.00,
    "urgency": "INSTANT"
  }'
```

**Algorithm Processing:**

**Stage 1 - Hard Filters:**
| Gateway | Min/Max Check | Available? | Quota? | Result |
|---------|--------------|------------|---------|---------|
| Gateway 1 (Vodafone) | ✅ 10-5000 | ✅ 24/7 | ✅ 20k remaining | ✅ PASS |
| Gateway 2 (Bank) | ✅ 100-unlimited | ✅ 9AM-5PM (Now: 2PM) | ✅ 150k remaining | ✅ PASS |
| Gateway 3 (Card) | ✅ 50-10000 | ✅ 24/7 | ✅ 90k remaining | ✅ PASS |

**Stage 2 - Urgency Filter (INSTANT):**
| Gateway | Processing Time | Filtered? |
|---------|----------------|-----------|
| Gateway 1 | 0 sec (Instant) | ✅ KEEP |
| Gateway 2 | 86400 sec (24h) | ❌ REMOVE |
| Gateway 3 | 7200 sec (2h) | ❌ REMOVE |

**Stage 3 - Scoring:**
| Gateway | Commission Calculation | Total | Rank |
|---------|----------------------|-------|------|
| Gateway 1 | 2.00 + (1000 × 0.015) = 2.00 + 15.00 | **17.00** | 🥇 |

**Result:** Gateway 1 (Vodafone Cash) selected with 17.00 EGP commission.

---

### Example 2: Non-Urgent Payment - Cost Optimization

**Scenario:** ABC Corporation needs to pay 1000 EGP, **no rush**.

**Request:**
```json
{
  "billerCode": "BI_ABCCORPORA",
  "amount": 1000.00,
  "urgency": "CAN_WAIT"
}
```

**Algorithm Processing:**

**Stage 1:** All 3 gateways pass (same as Example 1)

**Stage 2 - Urgency Filter (CAN_WAIT):**
- ✅ Keep ALL gateways (no filtering)
- Instant gateways have NO priority

**Stage 3 - Scoring:**
| Gateway | Processing | Commission | Remaining Quota | Rank |
|---------|-----------|------------|-----------------|------|
| Gateway 2 (Bank) | 24h | 5.00 + (1000 × 0.008) = **13.00** | 150,000 | 🥇 **1st** |
| Gateway 1 (Vodafone) | Instant | 2.00 + (1000 × 0.015) = **17.00** | 20,000 | 🥈 2nd |
| Gateway 3 (Card) | 2h | 0.00 + (1000 × 0.025) = **25.00** | 90,000 | 🥉 3rd |

**Result:** Gateway 2 (Bank Transfer) selected with 13.00 EGP commission.
- **Savings:** 4 EGP vs instant gateway
- **Trade-off:** Takes 24 hours instead of instant

---

### Example 3: Quota Exhaustion - Fallback Selection

**Scenario:** Biller has used most of their daily quota on preferred gateway.

**Current Quota Status (4:30 PM):**
| Gateway | Daily Limit | Used Today | Remaining |
|---------|------------|------------|-----------|
| Gateway 1 | 50,000 | 48,000 | **2,000** |
| Gateway 2 | 200,000 | 150,000 | **50,000** |
| Gateway 3 | 100,000 | 10,000 | **90,000** |

**Request:** Pay 3000 EGP (INSTANT)

**Algorithm Processing:**

**Stage 1 - Quota Check:**
| Gateway | Amount Needed | Remaining | Result |
|---------|--------------|-----------|---------|
| Gateway 1 | 3,000 | 2,000 | ❌ **REJECT** (insufficient) |
| Gateway 2 | 3,000 | 50,000 | ✅ PASS |
| Gateway 3 | 3,000 | 90,000 | ✅ PASS |

**Stage 2 - Urgency (INSTANT):**
| Gateway | Processing | Result |
|---------|-----------|---------|
| Gateway 2 | 24h | ❌ REMOVE |
| Gateway 3 | 2h | ❌ REMOVE |

**Result:** ❌ `NoAvailableGatewayException`
- Gateway 1 (only instant): No quota
- Gateway 2 & 3: Not instant
- **Recommendation:** Wait until midnight for quota reset, or accept slower gateway

---

### Example 4: Tiebreaker - Remaining Quota Decides

**Scenario:** Two gateways have identical commission.

**Setup:**
- Gateway A: Commission = 20.00, Remaining Quota = 5,000
- Gateway B: Commission = 20.00, Remaining Quota = 80,000

**Algorithm Decision:**
```
Sort by:
1. Commission: 20.00 = 20.00 (TIE)
2. Remaining Quota: 80,000 > 5,000 (WINNER)
```

**Result:** Gateway B selected
- **Reason:** Preserves Gateway A's limited capacity for future use

---

### Example 5: Outside Operating Hours

**Scenario:** Payment attempt at 10 PM on Thursday.

**Gateway Schedules:**
| Gateway | Schedule |
|---------|----------|
| Gateway 1 | 24/7 |
| Gateway 2 | Sun-Thu 9AM-5PM |
| Gateway 3 | 24/7 |

**Current Time:** Thursday 10:00 PM

**Stage 1 - Availability Check:**
| Gateway | Check | Result |
|---------|-------|---------|
| Gateway 1 | is_24_7 = TRUE | ✅ PASS |
| Gateway 2 | 22:00 NOT BETWEEN 09:00-17:00 | ❌ **REJECT** |
| Gateway 3 | is_24_7 = TRUE | ✅ PASS |

**Result:** Only Gateways 1 and 3 available.

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RoutingAlgorithmServiceTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Coverage

The project includes comprehensive tests for:

#### Unit Tests
- ✅ `RoutingAlgorithmServiceTest` - Algorithm logic
- ✅ `GatewayServiceTest` - Gateway CRUD operations
- ✅ `TransactionServiceTest` - Transaction creation
- ✅ `QuotaServiceTest` - Quota calculations
- ✅ `CommissionCalculationTest` - Fee calculations

#### Integration Tests
- ✅ `PaymentControllerIntegrationTest` - End-to-end API tests
- ✅ `DatabaseIntegrationTest` - JPA repository tests
- ✅ `SecurityIntegrationTest` - JWT authentication tests

### Manual API Testing with Postman

1. **Import the collection:**
   - File: `postman_collection.json` (included in repo)
   - Import into Postman

2. **Set environment variables:**
   ```
   base_url: http://localhost:8080/api
   jwt_token: (obtained from login)
   ```

3. **Test flow:**
   ```
   1. Login → Get JWT token
   2. Create Gateway → Verify creation
   3. Request Recommendation → Check algorithm result
   4. View Transactions → Verify logging
   ```

---

## 🚀 Deployment

### Production Deployment Checklist

- [ ] Update `application-prod.yml` with production database credentials
- [ ] Configure HTTPS/SSL certificates
- [ ] Set strong JWT secret key (min 256 bits)
- [ ] Enable database connection pooling
- [ ] Configure logging levels (INFO/WARN for production)
- [ ] Set up monitoring (Prometheus, Grafana)
- [ ] Configure CORS for production frontend URL
- [ ] Enable rate limiting
- [ ] Set up database backups
- [ ] Configure health check endpoints

### Docker Deployment

**Build image:**
```bash
docker build -t payment-routing-engine:1.0 .
```

**Run container:**
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/payment_routing \
  -e SPRING_DATASOURCE_USERNAME=prod_user \
  -e SPRING_DATASOURCE_PASSWORD=secure_password \
  --name payment-engine \
  payment-routing-engine:1.0
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-routing-engine
spec:
  replicas: 3
  selector:
    matchLabels:
      app: payment-engine
  template:
    metadata:
      labels:
        app: payment-engine
    spec:
      containers:
      - name: app
        image: payment-routing-engine:1.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
```

---

## 🔧 Configuration

### Application Properties

**Development (`application.yml`):**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_routing
    username: dev_user
    password: dev_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

jwt:
  secret: your-256-bit-secret-key-here
  expiration: 86400000  # 24 hours in milliseconds

logging:
  level:
    com.fawry.paymentroutingengine: DEBUG
    org.hibernate.SQL: DEBUG
```

**Production (`application-prod.yml`):**
```yaml
server:
  port: 8080
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEY_PASSWORD}

spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: 3600000  # 1 hour

logging:
  level:
    com.fawry.paymentroutingengine: INFO
```

---

## 📊 Performance Considerations

### Database Optimization

**Indexes:**
```sql
-- Critical for recommendation algorithm
CREATE INDEX idx_gateway_status ON payment_gateways(status);
CREATE INDEX idx_gateway_active ON payment_gateways(is_active);
CREATE INDEX idx_quota_lookup ON daily_gateway_quotas(biller_id, gateway_id, quota_date);
CREATE INDEX idx_availability_lookup ON gateway_availability(gateway_id, day_of_week);
```

**Query Optimization:**
- Quota queries use composite index (biller_id, gateway_id, quota_date)
- Availability checks leverage day_of_week index
- Gateway filtering uses status index

### Caching Strategy

**Recommended caching:**
```java
@Cacheable("gateways")
public List<Gateway> getActiveGateways() {
    return gatewayRepository.findByIsActiveTrue();
}

@CacheEvict(value = "gateways", allEntries = true)
public void updateGateway(String code, GatewayUpdateRequest request) {
    // Update logic
}
```

### Expected Performance

- **Recommendation endpoint:** < 100ms (with 10 gateways)
- **Transaction creation:** < 50ms
- **Gateway list:** < 20ms (cached)
- **Transaction history:** < 200ms (with pagination)

---

## 🐛 Troubleshooting

### Common Issues

#### 1. "NoAvailableGatewayException"

**Cause:** No gateway passes all hard filters.

**Debug steps:**
```bash
# Check gateway configurations
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/gateways

# Verify quota status
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/billers/{billerCode}/quota
```

**Solutions:**
- Verify amount fits gateway min/max limits
- Check if gateways are available at current time
- Confirm biller has remaining quota
- Ensure at least one gateway is ACTIVE

---

#### 2. "JWT Token Expired"

**Cause:** Token TTL exceeded (default: 24 hours).

**Solution:**
```bash
# Re-login to get fresh token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

---

#### 3. Database Connection Issues

**Error:** `Connection refused to localhost:5432`

**Check PostgreSQL status:**
```bash
# Linux/Mac
sudo systemctl status postgresql

# Docker
docker ps | grep postgres
```

**Fix connection string:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_routing
    # Or for Docker: jdbc:postgresql://host.docker.internal:5432/payment_routing
```

---

#### 4. Transaction Creation Fails

**Error:** `InvalidTransactionException: Amount exceeds maximum transaction limit`

**Cause:** Amount violates gateway constraints.

**Check gateway limits:**
```sql
SELECT code_gateway, min_transaction, max_transaction 
FROM payment_gateways 
WHERE status = 'ACTIVE';
```

---

#### 5. Quota Not Resetting

**Cause:** Records not cleaned up for new day.

**Manual reset (development only):**
```sql
DELETE FROM daily_gateway_quotas 
WHERE quota_date < CURRENT_DATE;
```

**Automated solution:** Add scheduled job:
```java
@Scheduled(cron = "0 0 0 * * *")  // Midnight daily
public void cleanupOldQuotas() {
    quotaRepository.deleteByQuotaDateBefore(LocalDate.now());
}
```

---

## 🔒 Security Best Practices

### Implemented Security Measures

1. **JWT Authentication**
   - Stateless token-based auth
   - Tokens expire after 24 hours
   - Role-based access control (ADMIN, USER, BILLER)

2. **Password Security**
   - BCrypt hashing with salt
   - Minimum 8 characters required
   - No plain text storage

3. **SQL Injection Prevention**
   - JPA parameterized queries
   - No raw SQL concatenation

4. **CORS Configuration**
   - Restricted to allowed origins
   - Credentials support enabled
   - Preflight request handling

5. **Input Validation**
   - Jakarta Bean Validation on all DTOs
   - Custom validators for business rules
   - SQL injection pattern filtering

### Security Checklist for Production

- [ ] Change default admin password
- [ ] Use environment variables for secrets
- [ ] Enable HTTPS only
- [ ] Configure rate limiting
- [ ] Set up API gateway (Kong, AWS API Gateway)
- [ ] Enable audit logging
- [ ] Implement IP whitelisting
- [ ] Use secrets management (HashiCorp Vault, AWS Secrets Manager)
- [ ] Regular dependency updates (OWASP checks)
- [ ] Enable SQL query logging in production

---

## 📈 Monitoring & Logging

### Health Check Endpoint

```bash
curl http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

### Logging Levels

**Key log events:**
- `INFO` - Gateway recommendations, transaction creation
- `DEBUG` - Algorithm filtering steps, SQL queries
- `WARN` - Quota near limit, gateway unavailable
- `ERROR` - Transaction failures, connection issues

**Example logs:**
```
2025-01-26 14:30:15.123 INFO  RoutingAlgorithmService - Starting gateway recommendation for biller: BI_ABCCORPORA, amount: 1000.00, urgency: INSTANT
2025-01-26 14:30:15.145 DEBUG RoutingAlgorithmService - Found 3 active gateways
2025-01-26 14:30:15.167 DEBUG RoutingAlgorithmService - After hard filters: 3 gateways remaining
2025-01-26 14:30:15.189 DEBUG RoutingAlgorithmService - Urgency=INSTANT: Filtered to 1 instant gateways
2025-01-26 14:30:15.201 INFO  RoutingAlgorithmService - Sorted 1 gateways. Best: GW_VODAFONEC with commission: 17.00
2025-01-26 14:30:15.

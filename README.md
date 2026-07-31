# Travel360

Travel360 is a full-stack, role-based travel booking and management platform designed to provide a centralized solution for managing travel services, bookings, customers, travel agents, partners, payments, invoices, complaints, compliance activities, and administrative operations.

The application follows a **monolithic architecture**, with an Angular frontend communicating with a Spring Boot REST API backed by a MySQL relational database.

Travel360 supports multiple modes of travel and travel-related services including **Flights, Hotels, Buses, Cabs, and Tour Packages**, while providing dedicated workflows and dashboards for different categories of users.

---

## Project Overview

Travel360 is designed as an end-to-end travel management system rather than only a basic booking application.

The platform brings together:

- Customer travel discovery and booking
- Multi-modal travel inventory
- Travel-agent assisted booking
- Partner and service management
- Booking request workflows
- Passenger management
- Payments and invoicing
- Complaint handling
- Administrative operations
- Finance management
- Compliance monitoring
- Audit logging
- Analytics and reporting
- Role-based authentication and authorization

The objective is to provide a single application capable of handling the complete lifecycle of a travel transaction, from discovering available travel services to booking, payment, invoice generation, administration, auditing and post-booking operations.

---

# Architecture

Travel360 currently follows a **monolithic full-stack architecture**.

```text
┌───────────────────────────────────────┐
│              Angular UI               │
│                                       │
│ Customer │ Admin │ Agent │ Partner    │
│ Finance  │ Compliance                 │
└───────────────────┬───────────────────┘
                    │
                    │ HTTP / REST API
                    │ JWT Authentication
                    ▼
┌───────────────────────────────────────┐
│          Spring Boot Backend          │
│                                       │
│ Controllers                           │
│      ↓                                │
│ Services / Business Logic             │
│      ↓                                │
│ Repositories                          │
│      ↓                                │
│ JPA / Hibernate                       │
└───────────────────┬───────────────────┘
                    │
                    ▼
             ┌─────────────┐
             │    MySQL    │
             │  Database   │
             └─────────────┘
```

The backend is maintained as a single Spring Boot application containing the application's business modules and exposing REST APIs consumed by the Angular frontend.

---

# Core Features

## Authentication & Security

Travel360 provides authentication and authorization mechanisms for protecting application resources.

Key capabilities include:

- User registration
- User login
- JWT-based authentication
- Role-based authorization
- Protected Angular routes
- Authentication guards
- HTTP authentication interceptor
- Password change functionality
- Forgot-password workflow
- Password reset functionality
- Secure backend endpoints
- Spring Security integration

JWT tokens are used to authenticate requests between the Angular application and Spring Boot backend.

---

# Role-Based Application

Travel360 provides different functionality depending on the authenticated user's role.

The application contains dedicated functionality for:

### Customer

Customers can interact with the travel booking platform and manage their travel activities.

Customer functionality includes:

- Browse available travel inventory
- View individual travel/service details
- Search travel options
- View flights
- View hotels
- View buses
- View tour packages
- Create and manage bookings
- View booking details
- Submit booking requests
- View invoices
- Make payments
- Maintain passenger information
- Maintain profile information
- Raise and track complaints

---

### Administrator

The administration module provides centralized control over the Travel360 platform.

Administrative functionality includes:

- Administrative dashboard
- Platform overview
- User management
- User auditing
- Travel-agent registration
- Partner registration
- Inventory management
- Add inventory
- Edit inventory
- Inventory auditing
- Booking management
- Booking-request management
- Booking dashboard
- Finance monitoring
- Invoice monitoring
- Payment monitoring
- Complaint monitoring
- Compliance monitoring
- Audit-log viewing
- Data auditing
- Administrative profile management

The administrator therefore has visibility across the major operational areas of the platform.

---

### Travel Agent

Travel360 contains a dedicated workflow for travel agents.

Travel-agent functionality includes:

- Travel-agent dashboard
- Agent profile
- Assisted booking
- Booking-form management
- Booking-request handling
- Request-list management

This enables travel agents to assist customers and participate in booking-related workflows.

---

### Partner

Travel partners can interact with the platform through dedicated partner functionality.

Partner capabilities include:

- Partner dashboard
- Partner overview
- Service management
- Managing travel-related services
- Partner profile/data management

Partners can therefore participate in the supply side of the Travel360 platform.

---

### Finance

The finance module provides dedicated functionality for financial operations.

Features include:

- Finance dashboard
- Payment monitoring
- Invoice management
- Finance analytics
- Billing-related operations

This separates financial workflows from general administrative operations.

---

### Compliance

Travel360 also includes dedicated compliance and auditing functionality.

Features include:

- Compliance dashboard
- Compliance analytics
- Audit-log monitoring
- Complaint monitoring
- Compliance reporting

These capabilities provide better visibility into application activities and operational records.

---

# Multi-Modal Travel Inventory

Travel360 supports multiple categories of travel inventory within the same platform.

Supported inventory types include:

### Flights

Flight inventory and booking workflows support flight-related travel services.

### Hotels

Hotel inventory allows accommodation options to be maintained and booked through the platform.

### Buses

Bus-related functionality supports bus inventory, bus stops and booking operations.

### Cabs

Cab inventory enables cab-related travel services to be managed.

### Tour Packages

Tour package inventory provides support for packaged travel and tourism services.

This allows Travel360 to operate as a **multi-modal travel platform** instead of being restricted to one category of travel.

---

# Inventory Management

The inventory module manages the travel services available for booking.

The backend contains dedicated inventory models and processing for:

- Flight Inventory
- Hotel Inventory
- Bus Inventory
- Cab Inventory
- Tour Package Inventory
- Seat Tier Capacity
- Bus Stop Details

Inventory-related functionality includes:

- Creating inventory
- Updating inventory
- Browsing inventory
- Checking availability
- Capacity validation
- Inventory auditing
- Inventory-type validation
- Preventing invalid inventory operations
- Managing inventory used by bookings

---

# Booking Management

Booking management is one of the core modules of Travel360.

The system contains dedicated booking workflows for:

- Flights
- Hotels
- Buses
- Cabs
- Tour Packages

Booking functionality includes:

- Creating bookings
- Viewing bookings
- Booking details
- Booking requests
- Booking-request approval/decision workflows
- Booking response processing
- Capacity checking
- Inventory availability validation
- Partial cancellation support
- Reservation management
- Booking timeline validation

Dedicated booking utilities are used to handle the business rules associated with different travel types.

---

# Booking Request Workflow

Travel360 separates booking requests from finalized bookings where required.

The system contains functionality for:

- Creating booking requests
- Viewing booking requests
- Processing booking requests
- Booking-request decisions
- Tracking booking-request status
- Travel-agent request handling
- Administrative request management

This enables structured booking workflows rather than treating every user action as an immediately confirmed booking.

---

# Dynamic Pricing

Travel360 contains a dedicated **Dynamic Pricing Engine** in the backend.

The pricing layer is designed to support dynamic travel-price calculation based on business rules rather than relying entirely on fixed prices.

This enables pricing logic to remain centralized in the backend and reusable across booking operations.

---

# Passenger Management

Travel360 contains a passenger-directory system for maintaining passenger information used during travel bookings.

Passenger-related functionality includes:

- Passenger profiles
- Passenger directory
- Passenger snapshots
- Passenger information associated with bookings
- Identity-proof handling
- Identity-proof validation

This allows customers to maintain reusable passenger information for travel transactions.

---

# Payment Management

The platform contains a dedicated payment workflow.

Payment functionality includes:

- Payment requests
- Payment processing
- Payment responses
- Multiple payment-related data models
- Payment status management
- Payment history/monitoring
- Finance-side payment visibility

Payment operations are integrated with booking and finance functionality.

---

# Invoice Management

Travel360 contains an invoice-management module for travel transactions.

Invoice functionality includes:

- Invoice creation/management
- Invoice retrieval
- Customer invoice viewing
- Finance invoice management
- Invoice cancellation response handling
- Booking-related invoice records

---

# Complaint Management

Customers can raise complaints through the platform.

The complaint module supports:

- Complaint creation
- Complaint tracking
- Complaint status
- Complaint resolution
- Administrative complaint management
- Compliance-side complaint monitoring

This provides a structured mechanism for post-booking issue management.

---

# Notifications

Travel360 contains a notification module for delivering and maintaining application notifications.

The backend includes notification entities, repositories, services and APIs to support notification-related workflows.

---

# Analytics

Travel360 includes analytics capabilities for monitoring application and business information.

The system contains:

- Analytics services
- Analytics dashboard data
- KPI reports
- Finance analytics
- Compliance analytics
- Administrative dashboards

These modules provide summarized information for operational monitoring and decision-making.

---

# Audit Logging

Auditability is an important part of Travel360.

The backend contains dedicated functionality for:

- Audit-log creation
- Audit-log querying
- Asynchronous audit processing
- Application activity tracking
- Compliance monitoring
- Administrative audit views

The project also contains aspect-based logging functionality, allowing cross-cutting application operations to be logged without duplicating logging logic throughout the business layer.

---

# Validation & Exception Handling

Travel360 implements centralized validation and error handling.

The backend contains custom exceptions for scenarios such as:

- Resource not found
- Invalid credentials
- Booking capacity exhausted
- Inventory unavailable
- Inventory already in use
- Inventory-type mismatch
- Identity conflicts
- Invalid timelines
- Data-isolation violations

A global exception handler is used to provide structured application error responses.

---

# Backend Architecture

The Spring Boot backend follows a layered architecture.

```text
Controller Layer
      │
      ▼
Service Layer
      │
      ▼
Service Implementation
      │
      ▼
Repository Layer
      │
      ▼
JPA / Hibernate
      │
      ▼
MySQL Database
```

Additional backend packages are used for:

```text
config/
controller/
dto/
entity/
enumeration/
exception/
mapper/
repository/
security/
service/
serviceimpl/
util/
```

### Controllers

Controllers expose REST endpoints to the Angular application.

The project includes controllers for areas such as:

- Users
- Inventory
- Bookings
- Booking requests
- Partners
- Passengers
- Payments
- Invoices
- Complaints
- Notifications
- Itineraries
- Analytics
- Audit logs

### Service Layer

The service layer contains the application's business logic and separates API handling from business operations.

### Repository Layer

Spring Data JPA repositories provide persistence operations for application entities.

### DTO Layer

DTOs are used for transferring request and response data without directly exposing persistence entities.

### Mapper Layer

Mapper classes transform data between entities, DTOs and booking/inventory representations.

### Utility Layer

Reusable business and validation functionality is maintained in utility classes, including:

- Dynamic pricing
- Flight booking
- Hotel booking
- Transit booking
- Payment processing
- Inventory management
- Inventory availability
- ID-proof validation
- Pagination
- JWT operations
- Security utilities
- Transactional utilities

---

# Frontend Architecture

The frontend is developed using Angular and organized around application features and user roles.

```text
src/app/
│
├── components/
│
├── core/
│   ├── guards/
│   ├── interceptors/
│   ├── layout/
│   └── services/
│
├── features/
│   ├── Customer/
│   ├── admin/
│   ├── compliance/
│   ├── finance/
│   ├── partner/
│   └── travel-agent/
│
└── shared/
    └── models/
```

This structure keeps reusable infrastructure separate from role-specific application functionality.

---

# Technology Stack

## Frontend

| Technology | Purpose |
|---|---|
| Angular | Frontend application framework |
| TypeScript | Frontend programming language |
| HTML | Application templates |
| SCSS / CSS | Styling |
| Angular Router | Navigation and route management |
| Route Guards | Protect authenticated routes |
| HTTP Interceptors | Attach authentication information to API requests |
| Angular Services | API communication and shared business functionality |
| npm | Frontend dependency management |

## Backend

| Technology | Purpose |
|---|---|
| Java 17 | Backend programming language |
| Spring Boot 3.5.14 | Backend application framework |
| Spring Web | REST API development |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database persistence |
| Hibernate | ORM |
| Jakarta Validation | Request/data validation |
| JWT / JJWT | Token-based authentication |
| Jackson | JSON/XML data handling |
| Lombok | Boilerplate-code reduction |
| Maven | Build and dependency management |
| SpringDoc OpenAPI | REST API documentation |
| Embedded Tomcat | Application web server |

## Database

| Technology | Purpose |
|---|---|
| MySQL | Relational database |
| MySQL Connector/J | Java-to-MySQL connectivity |
| JPA / Hibernate | Object-relational mapping |

## Development Tools

| Tool | Purpose |
|---|---|
| Spring Tools for Eclipse (STS) | Spring Boot backend development |
| MySQL Workbench | Database management |
| Git | Version control |
| GitHub | Source-code repository |
| npm / Angular CLI | Angular development and dependency management |

---

# API Documentation

The backend integrates **SpringDoc OpenAPI**, enabling API documentation for the REST endpoints.

When the backend is running, Swagger/OpenAPI can be used to inspect and test available REST APIs, subject to the application's configured Swagger path and security rules.

---

# Security Architecture

The backend uses Spring Security together with JWT-based authentication.

A typical authenticated request follows this flow:

```text
User Login
    │
    ▼
Credentials Validation
    │
    ▼
JWT Generated
    │
    ▼
Angular Client
    │
    ▼
Authorization Header
    │
    ▼
JWT Filter
    │
    ▼
Token Validation
    │
    ▼
Spring Security Context
    │
    ▼
Protected REST API
```

The frontend complements backend security through authentication services, route guards and an HTTP interceptor.

---

# Database Model

Travel360 uses MySQL to persist application data.

The backend contains entities representing areas including:

- Users
- Partners
- Passenger Profiles
- Inventory
- Flight Inventory
- Hotel Inventory
- Bus Inventory
- Cab Inventory
- Tour Package Inventory
- Seat Tier Capacity
- Bookings
- Booking Requests
- Reservations
- Payments
- Invoices
- Itineraries
- Notifications
- Complaints
- Audit Logs
- Compliance Reports
- KPI Reports

Spring Data JPA repositories provide persistence access for these entities.

---

# Project Structure

```text
Travel360/
│
├── Backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cts/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── enumeration/
│   │   │   │   ├── exception/
│   │   │   │   ├── mapper/
│   │   │   │   ├── repository/
│   │   │   │   ├── security/
│   │   │   │   ├── service/
│   │   │   │   ├── serviceimpl/
│   │   │   │   └── util/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── logback-spring.xml
│   │   │
│   │   └── test/
│   │
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── Frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   ├── core/
│   │   │   ├── features/
│   │   │   └── shared/
│   │   ├── index.html
│   │   ├── main.ts
│   │   └── styles.scss
│   │
│   ├── angular.json
│   ├── package.json
│   └── package-lock.json
│
├── .gitignore
└── README.md
```

---

# Running the Project

## Prerequisites

Before running Travel360, install:

- Java 17
- Maven or use the included Maven Wrapper
- MySQL
- Node.js
- npm
- Angular dependencies

---

## Database Configuration

Create/configure the MySQL database required by the application.

Sensitive credentials should **not** be committed to source control.

The backend can use environment variables for database credentials:

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Configure these variables locally before starting the backend.

---

## Run the Backend

Navigate to:

```bash
cd Backend
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Or, if Maven is installed globally:

```bash
mvn spring-boot:run
```

The backend is configured to run on:

```text
http://localhost:9095
```

---

## Run the Frontend

Open another terminal:

```bash
cd Frontend
```

Install dependencies:

```bash
npm install
```

Start the Angular development server:

```bash
npm start
```

or:

```bash
ng serve
```

Open the local URL displayed by Angular in the terminal.

---

# Testing

The project contains backend and frontend test files.

The backend includes tests covering areas such as:

- Controllers
- Booking services
- Booking-request services
- Complaint services
- Analytics
- Exception handling
- Dynamic pricing
- Inventory management
- Payment processing
- Flight booking
- Hotel booking
- Transit booking
- ID-proof validation
- Security utilities

Backend tests can be executed with:

```bash
mvnw.cmd test
```

Angular tests can be executed using the test command configured in the frontend project.

---

# Configuration & Secret Management

Sensitive information should never be committed directly to the repository.

Examples include:

- Database passwords
- JWT secrets
- API keys
- Private keys
- Environment-specific credentials

Use environment variables or ignored local configuration files for confidential values.

Example:

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Generated folders such as `target`, `node_modules`, `dist`, `.angular`, IDE metadata and local environment files are excluded from version control through `.gitignore`.

---

# Key Highlights

Travel360 demonstrates the implementation of a complete enterprise-style full-stack application with:

- Multi-modal travel booking
- Role-based user experiences
- JWT authentication
- Spring Security
- Angular route protection
- Layered backend architecture
- RESTful API communication
- Relational data persistence
- Dynamic pricing
- Inventory and capacity management
- Passenger-directory management
- Payment and invoice workflows
- Travel-agent workflows
- Partner management
- Administrative dashboards
- Finance operations
- Complaint management
- Compliance monitoring
- Audit logging
- Analytics and KPI reporting
- Global exception handling
- DTO and mapper patterns
- Automated backend and frontend tests

---

# Repository Scope

This repository contains the **monolithic version of Travel360**:

```text
Angular Frontend
       +
Spring Boot Monolithic Backend
       +
MySQL Database
```

Microservices are **not part of this repository architecture/documentation**.

---

# Author

**Shashidhar R**

GitHub: `In-Shashidhar-R`

---

## Travel360

**A full-stack multi-modal travel booking and management platform built with Angular, Spring Boot and MySQL.**

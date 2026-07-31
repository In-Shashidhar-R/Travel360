package com.cts.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI travel360OpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Travel360 - Integrated Travel Booking & Management Platform")
                        .description("REST API documentation for the Travel360 backend platform. "
                                + "Sections are ordered to follow the natural usage flow: identity, partners, "
                                + "passengers, inventory (split per travel mode), bookings, invoices, payments, then support tools.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CTS Development Team")
                                .email("developer@cts.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                
                .tags(List.of(
                        new Tag().name("01. User Identity & Access")
                                .description("Registration, login, agent provisioning, password recovery, user lookup."),
                        new Tag().name("02. Partner Management")
                                .description("Onboarding and directory of travel merchant partners."),
                        new Tag().name("03. Passenger Directory")
                                .description("A customer's reusable passenger profiles."),
                        new Tag().name("04a. Inventory - Flight")
                                .description("Create, update and delete flight inventory."),
                        new Tag().name("04b. Inventory - Hotel")
                                .description("Create, update and delete hotel inventory."),
                        new Tag().name("04c. Inventory - Bus")
                                .description("Create, update and delete bus inventory."),
                        new Tag().name("04d. Inventory - Cab")
                                .description("Create, update and delete cab inventory."),
                        new Tag().name("04e. Inventory - Tour Package")
                                .description("Create, update and delete tour package inventory."),
                        new Tag().name("04f. Inventory - Browse & Lifecycle")
                                .description("Search, filter, fetch, activate and deactivate inventory."),
                        new Tag().name("05. Booking, Reservation & Itinerary")
                                .description("Create bookings, fetch them, and process partial or full cancellations."),
                        new Tag().name("05b. Booking Requests (Customer ↔ Travel Agent)")
                                .description("Customer requests an agent to tailor and place a package booking on their behalf."),
                        new Tag().name("05c. Itinerary Management")
                                .description("Unified upcoming/past trip view aggregated across all booking types."),
                        new Tag().name("06. Invoice & Billing")
                                .description("Invoice statements for customers and administrators."),
                        new Tag().name("07. Payments")
                                .description("Payment clearance and ledger, including refunds."),
                        new Tag().name("08. Notification Center")
                                .description("Customer notifications and read receipts."),
                        new Tag().name("08a. Analytics & Reporting")
                                .description("Operational dashboards and KPIs for admin / finance / corporate roles."),
                        new Tag().name("09. Audit Trail (Administrative)")
                                .description("System-wide and per-user audit history, action filters, and compliance reports."),
                        new Tag().name("10. Complaints Management")
                                .description("Customer complaint workflow: raise, view, and resolve grievances (compliance officer / admin).")
                ));
    }
}

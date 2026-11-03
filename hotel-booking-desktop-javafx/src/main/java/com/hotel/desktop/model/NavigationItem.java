package com.hotel.desktop.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines all sidebar navigation items with strict role-based access control.
 * Navigation visibility and route dispatching are gated strictly by the authenticated
 * role from SessionManager, preventing unauthorized navigation attempts.
 */
public enum NavigationItem {

    // === GUEST / USER ITEMS ===
    HOME(
            "Home",
            "🏠",
            "Guest Welcome & Portal",
            "Welcome to the Hotel Booking Management System. Access curated stays and active reservations.",
            Set.of(UserRole.USER)
    ),
    BROWSE_HOTELS(
            "Browse Hotels",
            "🔍",
            "Hotel Catalog & Search",
            "Search and filter hotels across premier destinations with price, rating, and amenity preferences.",
            Set.of(UserRole.USER)
    ),
    MY_BOOKINGS(
            "My Bookings",
            "📅",
            "Personal Reservations",
            "Review your upcoming, completed, and cancelled hotel stays with real-time reservation details.",
            Set.of(UserRole.USER)
    ),

    // === HOTEL OWNER ITEMS ===
    OWNER_DASHBOARD(
            "Dashboard",
            "📊",
            "Hotel Owner Overview",
            "Operational summary of your registered properties, active guest bookings, and occupancy trends.",
            Set.of(UserRole.HOTEL_OWNER)
    ),
    MY_HOTELS(
            "My Hotels",
            "🏨",
            "Property Management",
            "Manage your hotel listings, room configurations, night rates, facilities, and photo galleries.",
            Set.of(UserRole.HOTEL_OWNER)
    ),
    ADD_HOTEL(
            "Add Hotel",
            "➕",
            "Property Onboarding",
            "Register a new property listing with location, pricing tiers, amenities, and room availability.",
            Set.of(UserRole.HOTEL_OWNER)
    ),
    OWNER_BOOKINGS(
            "Bookings",
            "📑",
            "Guest Reservations Hub",
            "Inspect and manage guest booking records across all properties under your ownership.",
            Set.of(UserRole.HOTEL_OWNER)
    ),

    // === ADMIN ITEMS ===
    ADMIN_DASHBOARD(
            "Admin Dashboard",
            "🛡️",
            "Platform Administration",
            "System-wide administrative health, total platform reservations, revenue oversight, and security alerts.",
            Set.of(UserRole.ADMIN)
    ),
    MANAGE_HOTELS(
            "Manage Hotels",
            "🏢",
            "Global Hotel Oversight",
            "Audit all registered hotel properties, approve listings, and configure platform-wide visibility.",
            Set.of(UserRole.ADMIN)
    ),
    MANAGE_USERS(
            "Manage Users",
            "👥",
            "User Management & RBAC",
            "Oversee user accounts, assign roles, manage active account statuses, and inspect audit records.",
            Set.of(UserRole.ADMIN)
    ),
    MANAGE_BOOKINGS(
            "Manage Bookings",
            "📖",
            "Platform Booking Registry",
            "Global booking registry covering all guest reservations, payment settlements, and cancellations.",
            Set.of(UserRole.ADMIN)
    ),
    MANAGE_REVIEWS(
            "Manage Reviews",
            "⭐",
            "Review Moderation",
            "Moderate guest feedback, verify rating authenticity, and manage community review standards.",
            Set.of(UserRole.ADMIN)
    ),
    OWNER_REQUESTS(
            "Owner Requests",
            "📝",
            "Owner Approval Workflow",
            "Review and approve pending partner applications from guests requesting hotel-owner status.",
            Set.of(UserRole.ADMIN)
    ),
    API_DOCS(
            "API Documentation",
            "📄",
            "OpenAPI / Swagger Docs",
            "Access interactive Spring Boot REST API specifications and OpenAPI schema definitions.",
            Set.of(UserRole.ADMIN)
    ),
    API_STATUS(
            "API Status",
            "⚡",
            "System Health & Telemetry",
            "Live server liveness probes, database connection health, JVM memory metrics, and uptime.",
            Set.of(UserRole.ADMIN)
    ),

    // === SHARED / MULTI-ROLE ITEMS ===
    BUSINESS_INSIGHTS(
            "Business Insights",
            "📈",
            "Financial Analytics & Reports",
            "Role-tailored financial analytics: owners inspect their properties while admins view global revenue.",
            Set.of(UserRole.HOTEL_OWNER, UserRole.ADMIN)
    ),
    MY_PROFILE(
            "My Profile",
            "👤",
            "Account Settings",
            "Manage personal information, contact phone, default billing address, and stay preferences.",
            Set.of(UserRole.USER, UserRole.HOTEL_OWNER, UserRole.ADMIN)
    );

    private final String title;
    private final String icon;
    private final String subtitle;
    private final String description;
    private final Set<UserRole> allowedRoles;

    NavigationItem(String title, String icon, String subtitle, String description, Set<UserRole> allowedRoles) {
        this.title = title;
        this.icon = icon;
        this.subtitle = subtitle;
        this.description = description;
        this.allowedRoles = Collections.unmodifiableSet(allowedRoles);
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public Set<UserRole> getAllowedRoles() {
        return allowedRoles;
    }

    public boolean isAccessibleBy(UserRole role) {
        if (role == null) {
            return false;
        }
        return allowedRoles.contains(role);
    }

    /**
     * Returns an ordered list of navigation items strictly accessible to the provided role.
     */
    public static List<NavigationItem> getItemsForRole(UserRole role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(values())
                .filter(item -> item.isAccessibleBy(role))
                .collect(Collectors.toList());
    }

    /**
     * Determines default initial landing page for a role.
     */
    public static NavigationItem getDefaultItemForRole(UserRole role) {
        if (role == null) {
            return HOME;
        }
        return switch (role) {
            case ADMIN -> ADMIN_DASHBOARD;
            case HOTEL_OWNER -> OWNER_DASHBOARD;
            case USER -> HOME;
        };
    }
}


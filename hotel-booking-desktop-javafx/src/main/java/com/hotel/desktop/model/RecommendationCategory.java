package com.hotel.desktop.model;

/**
 * Categorization of hotel recommendations in the JavaFX client.
 */
public enum RecommendationCategory {
    PERSONALIZED("Personalized For You", "✨", "rec-badge-personalized"),
    POPULAR("Trending & Popular", "🔥", "rec-badge-popular"),
    TOP_RATED("Top Rated Stays", "🏆", "rec-badge-top-rated"),
    SIMILAR("Similar Hotel", "🏨", "rec-badge-similar");

    private final String displayName;
    private final String icon;
    private final String badgeStyleClass;

    RecommendationCategory(String displayName, String icon, String badgeStyleClass) {
        this.displayName = displayName;
        this.icon = icon;
        this.badgeStyleClass = badgeStyleClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getBadgeStyleClass() {
        return badgeStyleClass;
    }
}


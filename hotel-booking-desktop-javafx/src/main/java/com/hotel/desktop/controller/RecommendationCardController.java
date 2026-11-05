package com.hotel.desktop.controller;

import com.hotel.desktop.model.RecommendationCategory;
import com.hotel.desktop.model.RecommendationDto;
import com.hotel.desktop.model.RecommendationExplanation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controller for rendering a single recommendation hotel card.
 * Displays property details, image, rating, grounded explanation, and expandable features.
 */
public class RecommendationCardController {

    @FXML private StackPane imageContainer;
    @FXML private ImageView hotelImageView;
    @FXML private Label placeholderImageLabel;
    @FXML private Label categoryBadge;
    @FXML private Label scoreBadge;

    @FXML private Label hotelNameLabel;
    @FXML private Label locationLabel;
    @FXML private Label starRatingLabel;
    @FXML private Label ratingReviewsLabel;
    @FXML private FlowPane tagsContainer;
    @FXML private Label priceLabel;

    @FXML private VBox primaryReasonBox;
    @FXML private Label primaryReasonLabel;
    @FXML private Button toggleDetailsButton;
    @FXML private VBox detailsBox;
    @FXML private VBox supportingReasonsBox;
    @FXML private FlowPane featuresContainer;

    @FXML private Button viewDetailsButton;

    private RecommendationDto recommendation;
    private Consumer<RecommendationDto> viewDetailsHandler;
    private boolean detailsExpanded = false;

    public void setRecommendation(RecommendationDto recommendation, Consumer<RecommendationDto> viewDetailsHandler) {
        this.recommendation = recommendation;
        this.viewDetailsHandler = viewDetailsHandler;

        populateCard();
    }

    private void populateCard() {
        if (recommendation == null) return;

        // 1. Hotel info
        hotelNameLabel.setText(recommendation.getName());
        locationLabel.setText("📍 " + recommendation.getLocationDisplay());
        starRatingLabel.setText(recommendation.getStarRatingDisplay());
        ratingReviewsLabel.setText(recommendation.getRatingDisplay() + " • " + recommendation.getReviewCountDisplay());
        priceLabel.setText(recommendation.getFormattedPrice());

        // 2. Category badge & score badge
        applyCategoryBadge(recommendation.getCategory());
        applyScoreBadge(recommendation);

        // 3. Hotel Type & Key Facilities
        tagsContainer.getChildren().clear();
        for (String type : recommendation.getType()) {
            if (type != null && !type.isBlank()) {
                Label tag = new Label(type);
                tag.getStyleClass().add("hotel-type-badge");
                tagsContainer.getChildren().add(tag);
            }
        }
        int facilityCount = 0;
        for (String fac : recommendation.getFacilities()) {
            if (facilityCount >= 2) break;
            if (fac != null && !fac.isBlank()) {
                Label tag = new Label(fac);
                tag.getStyleClass().add("hotel-facility-badge");
                tagsContainer.getChildren().add(tag);
                facilityCount++;
            }
        }

        // 4. Grounded Explanation
        RecommendationExplanation explanation = recommendation.getExplanation();
        String primary = explanation != null && !explanation.getPrimaryReason().isBlank()
                ? explanation.getPrimaryReason()
                : "Top recommended stay matching your travel profile.";
        primaryReasonLabel.setText(primary);

        boolean hasExtras = explanation != null && (explanation.hasSupportingReasons() || explanation.hasRelevantFeatures());
        if (hasExtras) {
            toggleDetailsButton.setVisible(true);
            toggleDetailsButton.setManaged(true);
            toggleDetailsButton.setText("Why this stay? ▾");
            detailsExpanded = false;
            detailsBox.setVisible(false);
            detailsBox.setManaged(false);

            // Populate supporting reasons
            supportingReasonsBox.getChildren().clear();
            List<String> supporting = explanation.getSupportingReasons();
            for (String s : supporting) {
                if (s != null && !s.isBlank()) {
                    Label l = new Label("• " + s);
                    l.setWrapText(true);
                    l.getStyleClass().add("rec-sub-reason-text");
                    supportingReasonsBox.getChildren().add(l);
                }
            }

            // Populate relevant features
            featuresContainer.getChildren().clear();
            List<String> features = explanation.getRelevantFeatures();
            for (String f : features) {
                if (f != null && !f.isBlank()) {
                    Label pill = new Label(f);
                    pill.getStyleClass().add("rec-feature-pill");
                    featuresContainer.getChildren().add(pill);
                }
            }

            toggleDetailsButton.setOnAction(e -> toggleDetails());
        } else {
            toggleDetailsButton.setVisible(false);
            toggleDetailsButton.setManaged(false);
            detailsBox.setVisible(false);
            detailsBox.setManaged(false);
        }

        // 5. Image loading (async)
        loadImageAsync(recommendation.getFirstImageUrl());

        // 6. Action button
        viewDetailsButton.setOnAction(event -> {
            if (viewDetailsHandler != null) {
                viewDetailsHandler.accept(recommendation);
            }
        });
    }

    private void toggleDetails() {
        detailsExpanded = !detailsExpanded;
        detailsBox.setVisible(detailsExpanded);
        detailsBox.setManaged(detailsExpanded);
        toggleDetailsButton.setText(detailsExpanded ? "Hide details ▴" : "Why this stay? ▾");
    }

    private void applyCategoryBadge(RecommendationCategory category) {
        categoryBadge.getStyleClass().removeAll(
                "rec-badge-personalized", "rec-badge-popular", "rec-badge-top-rated", "rec-badge-similar"
        );
        if (category == null) {
            category = RecommendationCategory.PERSONALIZED;
        }

        categoryBadge.setText(category.getIcon() + " " + category.name().replace('_', ' '));
        categoryBadge.getStyleClass().add(category.getBadgeStyleClass());
    }

    private void applyScoreBadge(RecommendationDto dto) {
        if (dto.getCategory() == RecommendationCategory.PERSONALIZED && dto.getScore() > 0) {
            scoreBadge.setText(dto.getScorePercentageDisplay());
            scoreBadge.setVisible(true);
            scoreBadge.setManaged(true);
        } else if (dto.getAverageRating() > 0) {
            scoreBadge.setText(String.format(java.util.Locale.US, "★ %.1f", dto.getAverageRating()));
            scoreBadge.setVisible(true);
            scoreBadge.setManaged(true);
        } else {
            scoreBadge.setVisible(false);
            scoreBadge.setManaged(false);
        }
    }

    private void loadImageAsync(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            showImagePlaceholder();
            return;
        }

        try {
            Image image = new Image(imageUrl, 280, 150, true, true, true);
            image.errorProperty().addListener((obs, oldVal, hasError) -> {
                if (Boolean.TRUE.equals(hasError)) {
                    Platform.runLater(this::showImagePlaceholder);
                }
            });

            image.progressProperty().addListener((obs, oldVal, progress) -> {
                if (progress.doubleValue() >= 1.0 && !image.isError()) {
                    Platform.runLater(() -> {
                        hotelImageView.setImage(image);
                        hotelImageView.setVisible(true);
                        placeholderImageLabel.setVisible(false);
                    });
                }
            });

            if (!image.isError() && image.getProgress() >= 1.0) {
                hotelImageView.setImage(image);
                hotelImageView.setVisible(true);
                placeholderImageLabel.setVisible(false);
            }
        } catch (Exception e) {
            showImagePlaceholder();
        }
    }

    private void showImagePlaceholder() {
        hotelImageView.setImage(null);
        hotelImageView.setVisible(false);
        placeholderImageLabel.setVisible(true);
    }
}


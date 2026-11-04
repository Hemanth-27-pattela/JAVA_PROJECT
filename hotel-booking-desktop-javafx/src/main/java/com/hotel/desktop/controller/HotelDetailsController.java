package com.hotel.desktop.controller;

import com.hotel.desktop.model.Hotel;
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

/**
 * Controller for the Hotel Details view.
 * Displays comprehensive property info, photos, amenities, policies, and contact.
 */
public class HotelDetailsController {

    @FXML private Button backButton;
    @FXML private Label hotelNameLabel;
    @FXML private Label starRatingLabel;
    @FXML private Label locationLabel;
    @FXML private Label featuredBadge;
    @FXML private Label priceLabel;

    @FXML private StackPane heroImageContainer;
    @FXML private ImageView heroImageView;
    @FXML private Label placeholderHeroLabel;
    @FXML private HBox imageThumbnailsRow;

    @FXML private Label adultCapacityLabel;
    @FXML private Label childCapacityLabel;
    @FXML private Label checkInPolicyLabel;
    @FXML private Label checkOutPolicyLabel;
    @FXML private Label cancellationPolicyLabel;

    @FXML private Label descriptionTextLabel;
    @FXML private FlowPane typesContainer;
    @FXML private FlowPane facilitiesContainer;

    @FXML private Label contactPhoneLabel;
    @FXML private Label contactEmailLabel;
    @FXML private Label contactWebsiteLabel;

    private Hotel hotel;
    private Runnable backAction;

    public void setHotel(Hotel hotel, Runnable backAction) {
        this.hotel = hotel;
        this.backAction = backAction;

        populateView();
    }

    @FXML
    public void initialize() {
        if (backButton != null) {
            backButton.setOnAction(e -> {
                if (backAction != null) {
                    backAction.run();
                }
            });
        }
    }

    private void populateView() {
        if (hotel == null) return;

        hotelNameLabel.setText(hotel.getName());
        starRatingLabel.setText(hotel.getStarRatingDisplay());
        locationLabel.setText("📍 " + hotel.getLocationDisplay());
        priceLabel.setText(hotel.getFormattedPrice() + " / night");

        if (Boolean.TRUE.equals(hotel.getIsFeatured())) {
            featuredBadge.setVisible(true);
            featuredBadge.setManaged(true);
        } else {
            featuredBadge.setVisible(false);
            featuredBadge.setManaged(false);
        }

        // Capacities
        adultCapacityLabel.setText("Adults: " + hotel.getAdultCount());
        childCapacityLabel.setText("Children: " + hotel.getChildCount());

        // Policies
        Hotel.HotelPolicies policies = hotel.getPolicies();
        checkInPolicyLabel.setText(policies.getCheckInTime());
        checkOutPolicyLabel.setText(policies.getCheckOutTime());
        cancellationPolicyLabel.setText(policies.getCancellationPolicy());

        // Description
        descriptionTextLabel.setText(hotel.getDescription() != null && !hotel.getDescription().isBlank()
                ? hotel.getDescription()
                : "No detailed description provided for this property.");

        // Types
        typesContainer.getChildren().clear();
        for (String type : hotel.getType()) {
            if (type != null && !type.isBlank()) {
                Label badge = new Label(type);
                badge.getStyleClass().add("hotel-type-badge");
                typesContainer.getChildren().add(badge);
            }
        }

        // Facilities
        facilitiesContainer.getChildren().clear();
        for (String fac : hotel.getFacilities()) {
            if (fac != null && !fac.isBlank()) {
                Label badge = new Label(fac);
                badge.getStyleClass().add("hotel-facility-badge");
                facilitiesContainer.getChildren().add(badge);
            }
        }

        // Contact info
        Hotel.HotelContact contact = hotel.getContact();
        contactPhoneLabel.setText(contact.getPhone().isEmpty() ? "Not specified" : contact.getPhone());
        contactEmailLabel.setText(contact.getEmail().isEmpty() ? "Not specified" : contact.getEmail());
        contactWebsiteLabel.setText(contact.getWebsite().isEmpty() ? "Not specified" : contact.getWebsite());

        // Load Main Hero Image
        loadHeroImage(hotel.getFirstImageUrl());

        // Load Thumbnails if available
        imageThumbnailsRow.getChildren().clear();
        if (hotel.getImageUrls() != null && hotel.getImageUrls().size() > 1) {
            for (String url : hotel.getImageUrls()) {
                if (url != null && !url.isBlank()) {
                    ImageView thumb = createThumbnailView(url);
                    imageThumbnailsRow.getChildren().add(thumb);
                }
            }
        }
    }

    private void loadHeroImage(String url) {
        if (url == null || url.isBlank()) {
            heroImageView.setVisible(false);
            placeholderHeroLabel.setVisible(true);
            return;
        }

        try {
            Image image = new Image(url, 560, 320, true, true, true);
            image.errorProperty().addListener((obs, oldVal, hasError) -> {
                if (Boolean.TRUE.equals(hasError)) {
                    Platform.runLater(() -> {
                        heroImageView.setVisible(false);
                        placeholderHeroLabel.setVisible(true);
                    });
                }
            });

            image.progressProperty().addListener((obs, oldVal, progress) -> {
                if (progress.doubleValue() >= 1.0 && !image.isError()) {
                    Platform.runLater(() -> {
                        heroImageView.setImage(image);
                        heroImageView.setVisible(true);
                        placeholderHeroLabel.setVisible(false);
                    });
                }
            });

            if (!image.isError() && image.getProgress() >= 1.0) {
                heroImageView.setImage(image);
                heroImageView.setVisible(true);
                placeholderHeroLabel.setVisible(false);
            }
        } catch (Exception e) {
            heroImageView.setVisible(false);
            placeholderHeroLabel.setVisible(true);
        }
    }

    private ImageView createThumbnailView(String url) {
        ImageView thumb = new ImageView();
        thumb.setFitWidth(100.0);
        thumb.setFitHeight(65.0);
        thumb.setPreserveRatio(true);
        thumb.getStyleClass().add("hotel-thumbnail");

        try {
            Image img = new Image(url, 100, 65, true, true, true);
            thumb.setImage(img);
            thumb.setOnMouseClicked(e -> loadHeroImage(url));
        } catch (Exception ignored) {}

        return thumb;
    }
}

package com.hotel.desktop.controller;

import com.hotel.desktop.model.Hotel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Controller for rendering an individual hotel summary card.
 */
public class HotelCardController {

    @FXML private StackPane imageContainer;
    @FXML private ImageView hotelImageView;
    @FXML private Label placeholderImageLabel;
    @FXML private Label featuredBadge;
    @FXML private Label starRatingLabel;
    @FXML private Label hotelNameLabel;
    @FXML private Label locationLabel;
    @FXML private Label descriptionLabel;
    @FXML private FlowPane tagsContainer;
    @FXML private Label priceLabel;
    @FXML private Button viewDetailsButton;

    private Hotel hotel;
    private Consumer<Hotel> viewDetailsHandler;

    public void setHotel(Hotel hotel, Consumer<Hotel> viewDetailsHandler) {
        this.hotel = hotel;
        this.viewDetailsHandler = viewDetailsHandler;

        populateCard();
    }

    private void populateCard() {
        if (hotel == null) return;

        hotelNameLabel.setText(hotel.getName());
        locationLabel.setText("📍 " + hotel.getLocationDisplay());
        starRatingLabel.setText(hotel.getStarRatingDisplay());
        priceLabel.setText(hotel.getFormattedPrice() + " / night");

        // Featured Badge
        if (Boolean.TRUE.equals(hotel.getIsFeatured())) {
            featuredBadge.setVisible(true);
            featuredBadge.setManaged(true);
        } else {
            featuredBadge.setVisible(false);
            featuredBadge.setManaged(false);
        }

        // Short Description (capped to avoid overflowing layout)
        String desc = hotel.getDescription();
        if (desc != null && desc.length() > 140) {
            desc = desc.substring(0, 137).trim() + "...";
        }
        descriptionLabel.setText(desc != null && !desc.isBlank() ? desc : "No property description available.");

        // Tags: Types + Top Facilities
        tagsContainer.getChildren().clear();
        for (String type : hotel.getType()) {
            if (type != null && !type.isBlank()) {
                Label tag = new Label(type);
                tag.getStyleClass().add("hotel-type-badge");
                tagsContainer.getChildren().add(tag);
            }
        }
        int facilityCount = 0;
        for (String fac : hotel.getFacilities()) {
            if (facilityCount >= 3) break;
            if (fac != null && !fac.isBlank()) {
                Label tag = new Label(fac);
                tag.getStyleClass().add("hotel-facility-badge");
                tagsContainer.getChildren().add(tag);
                facilityCount++;
            }
        }

        // Image Loading (Async without blocking UI thread)
        loadImageAsync(hotel.getFirstImageUrl());

        // Action Handler
        viewDetailsButton.setOnAction(event -> {
            if (viewDetailsHandler != null) {
                viewDetailsHandler.accept(hotel);
            }
        });
    }

    private void loadImageAsync(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            showImagePlaceholder();
            return;
        }

        try {
            // JavaFX Image constructor with backgroundLoading=true executes asynchronously
            Image image = new Image(imageUrl, 240, 160, true, true, true);
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


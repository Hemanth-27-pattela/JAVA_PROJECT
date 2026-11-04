package com.hotel.desktop.controller;

import com.hotel.desktop.model.CreateBookingRequest;
import com.hotel.desktop.model.Hotel;
import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.model.PaymentIntentResponse;
import com.hotel.desktop.model.User;
import com.hotel.desktop.service.ApiException;
import com.hotel.desktop.service.BookingService;
import com.hotel.desktop.service.SessionManager;
import com.hotel.desktop.util.ViewNavigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller for the Hotel Details view.
 * Displays comprehensive property info, photos, amenities, policies, and
 * hosts the active guest reservation form with real-time pricing and simulated checkout.
 */
public class HotelDetailsController {

    @FXML private Button backButton;
    @FXML private ScrollPane detailsScrollPane;
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

    // === Phase 9.6 Booking Form Injections ===
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private Spinner<Integer> adultSpinner;
    @FXML private Spinner<Integer> childSpinner;
    @FXML private TextField guestNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField specialRequestsField;
    @FXML private Label breakdownRateLabel;
    @FXML private Label breakdownTotalLabel;
    @FXML private Label bookingStatusLabel;
    @FXML private Button bookNowButton;
    @FXML private ProgressIndicator bookingSpinner;

    private final BookingService bookingService;
    private final SessionManager sessionManager;

    private Hotel hotel;
    private Runnable backAction;

    public HotelDetailsController() {
        this(new BookingService(), SessionManager.getInstance());
    }

    public HotelDetailsController(BookingService bookingService) {
        this(bookingService, SessionManager.getInstance());
    }

    public HotelDetailsController(BookingService bookingService, SessionManager sessionManager) {
        this.bookingService = bookingService;
        this.sessionManager = sessionManager;
    }

    public void scrollToBooking() {
        if (detailsScrollPane != null) {
            detailsScrollPane.setVvalue(0.40);
        }
    }

    public void setHotel(Hotel hotel, Runnable backAction) {
        this.hotel = hotel;
        this.backAction = backAction;

        populateView();
        initializeBookingForm();
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

    private void initializeBookingForm() {
        if (hotel == null || checkInPicker == null) return;

        LocalDate today = LocalDate.now();
        LocalDate defaultCheckIn = today.plusDays(1);
        LocalDate defaultCheckOut = today.plusDays(4); // 3 nights default

        // Setup Check-In Picker constraints: cannot pick dates before today
        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isBefore(today)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8;");
                }
            }
        });

        // Setup Check-Out Picker constraints: cannot pick dates <= checkIn
        checkOutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                LocalDate currentIn = checkInPicker.getValue();
                if (item != null && currentIn != null && !item.isAfter(currentIn)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8;");
                }
            }
        });

        checkInPicker.setValue(defaultCheckIn);
        checkOutPicker.setValue(defaultCheckOut);

        // Date change listeners
        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                LocalDate currentOut = checkOutPicker.getValue();
                if (currentOut == null || !currentOut.isAfter(newVal)) {
                    checkOutPicker.setValue(newVal.plusDays(1));
                }
            }
            updatePriceCalculation();
        });

        checkOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> updatePriceCalculation());

        // Setup Spinners bounded by hotel capacity
        int maxAdults = hotel.getAdultCount() != null && hotel.getAdultCount() > 0 ? hotel.getAdultCount() : 10;
        int maxChildren = hotel.getChildCount() != null && hotel.getChildCount() >= 0 ? hotel.getChildCount() : 10;

        adultSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Math.max(1, maxAdults), 1));
        childSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Math.max(0, maxChildren), 0));

        adultSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePriceCalculation());
        childSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePriceCalculation());

        // Pre-fill user contact fields from session
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            guestNameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
        }

        // Book Now button handler
        bookNowButton.setOnAction(e -> handleBookNow());

        // Initial price calculation
        updatePriceCalculation();
    }

    private void updatePriceCalculation() {
        if (hotel == null || breakdownRateLabel == null || breakdownTotalLabel == null) return;

        LocalDate in = checkInPicker.getValue();
        LocalDate out = checkOutPicker.getValue();

        if (in == null || out == null || !out.isAfter(in)) {
            breakdownRateLabel.setText("Invalid date range");
            breakdownTotalLabel.setText("£0.00");
            bookNowButton.setDisable(true);
            setBookingStatusMessage("⚠️ Please select a check-out date after your check-in date.", false);
            return;
        }

        long nights = ChronoUnit.DAYS.between(in, out);
        if (nights < 1) nights = 1;

        // Perform client-side capacity check
        int adults = adultSpinner.getValue() != null ? adultSpinner.getValue() : 1;
        int children = childSpinner.getValue() != null ? childSpinner.getValue() : 0;

        if (hotel.getAdultCount() != null && adults > hotel.getAdultCount()) {
            bookNowButton.setDisable(true);
            setBookingStatusMessage("⚠️ Adult count (" + adults + ") exceeds hotel capacity (" + hotel.getAdultCount() + ").", false);
            return;
        }
        if (hotel.getChildCount() != null && children > hotel.getChildCount()) {
            bookNowButton.setDisable(true);
            setBookingStatusMessage("⚠️ Child count (" + children + ") exceeds hotel capacity (" + hotel.getChildCount() + ").", false);
            return;
        }

        BigDecimal rate = hotel.getPricePerNight() != null ? hotel.getPricePerNight() : BigDecimal.ZERO;
        BigDecimal totalCost = rate.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);

        String nightText = nights + (nights == 1 ? " night" : " nights");
        breakdownRateLabel.setText(nightText + " × " + hotel.getFormattedPrice());
        breakdownTotalLabel.setText("£" + String.format(Locale.UK, "%.2f", totalCost));

        bookNowButton.setDisable(false);
        setBookingStatusMessage("✓ Pre-flight validation passed (Subject to room availability at checkout)", true);
    }

    private void handleBookNow() {
        if (hotel == null) return;

        LocalDate in = checkInPicker.getValue();
        LocalDate out = checkOutPicker.getValue();

        if (in == null || out == null || !out.isAfter(in)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Dates", "Please select valid check-in and check-out dates.");
            return;
        }

        int adults = adultSpinner.getValue() != null ? adultSpinner.getValue() : 1;
        int children = childSpinner.getValue() != null ? childSpinner.getValue() : 0;

        String guestName = guestNameField.getText() != null ? guestNameField.getText().trim() : "";
        if (guestName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter guest full name.");
            guestNameField.requestFocus();
            return;
        }

        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        if (email.isEmpty() || !email.contains("@")) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid guest email address.");
            emailField.requestFocus();
            return;
        }

        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
        String specialRequests = specialRequestsField.getText() != null ? specialRequestsField.getText().trim() : "";

        // Parse first and last names
        String firstName = guestName;
        String lastName = "";
        int spaceIdx = guestName.indexOf(' ');
        if (spaceIdx > 0) {
            firstName = guestName.substring(0, spaceIdx).trim();
            lastName = guestName.substring(spaceIdx + 1).trim();
        }
        if (lastName.isEmpty()) {
            lastName = "Guest";
        }

        long nights = ChronoUnit.DAYS.between(in, out);
        if (nights < 1) nights = 1;
        BigDecimal rate = hotel.getPricePerNight() != null ? hotel.getPricePerNight() : BigDecimal.ZERO;
        BigDecimal calculatedCost = rate.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);

        // Convert dates to Instant at standard midnight UTC to align with backend Duration.toDays()
        Instant checkInInstant = in.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant checkOutInstant = out.atStartOfDay(ZoneOffset.UTC).toInstant();

        // Step 1: Show Confirmation Dialog with complete pricing and simulated payment disclosure
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Hotel Reservation");
        confirmDialog.setHeaderText("Reservation Review — " + hotel.getName());

        StringBuilder summary = new StringBuilder();
        summary.append("📍 Location: ").append(hotel.getLocationDisplay()).append("\n");
        summary.append("📅 Dates: ").append(in).append(" to ").append(out).append(" (").append(nights).append(nights == 1 ? " night" : " nights").append(")\n");
        summary.append("👥 Guests: ").append(adults).append(adults == 1 ? " Adult" : " Adults");
        if (children > 0) summary.append(", ").append(children).append(children == 1 ? " Child" : " Children");
        summary.append("\n");
        summary.append("👤 Primary Guest: ").append(firstName).append(" ").append(lastName).append(" (").append(email).append(")\n");
        summary.append("💰 Nightly Rate: ").append(hotel.getFormattedPrice()).append("\n");
        summary.append("💳 Total Cost: £").append(String.format(Locale.UK, "%.2f", calculatedCost)).append(" GBP\n\n");
        summary.append("ℹ️ Payment Mode: Simulated/Demo Mode (Instant server-side verification, no real financial card charge).");

        confirmDialog.setContentText(summary.toString());
        ButtonType bookBtnType = new ButtonType("Authorize & Reserve");
        ButtonType cancelBtnType = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        confirmDialog.getButtonTypes().setAll(bookBtnType, cancelBtnType);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isEmpty() || result.get() != bookBtnType) {
            return;
        }

        // Proceed with two-step booking: Payment Intent -> Final Booking Creation
        setFormBusy(true);
        setBookingStatusMessage("⏳ Contacting reservation server and initializing payment intent...", true);

        final String fFirstName = firstName;
        final String fLastName = lastName;
        final String fEmail = email;
        final String fPhone = phone;
        final String fSpecialReq = specialRequests;
        final int fNights = (int) nights;

        Task<Void> bookingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Step A: Request payment intent from backend
                PaymentIntentResponse intent = bookingService.createPaymentIntent(hotel.getId(), fNights);
                if (intent == null || intent.getPaymentIntentId() == null || intent.getPaymentIntentId().isBlank()) {
                    throw new ApiException(400, "Server failed to initialize payment intent.");
                }

                // Step B: Submit booking request with verified paymentIntentId
                CreateBookingRequest req = new CreateBookingRequest();
                req.setFirstName(fFirstName);
                req.setLastName(fLastName);
                req.setEmail(fEmail);
                req.setPhone(fPhone);
                req.setAdultCount(adults);
                req.setChildCount(children);
                req.setCheckIn(checkInInstant);
                req.setCheckOut(checkOutInstant);
                req.setTotalCost(calculatedCost);
                req.setPaymentIntentId(intent.getPaymentIntentId());
                req.setSpecialRequests(fSpecialReq.isEmpty() ? null : fSpecialReq);

                bookingService.createBooking(hotel.getId(), req);
                return null;
            }
        };

        bookingTask.setOnSucceeded(workerStateEvent -> {
            setFormBusy(false);
            setBookingStatusMessage("✓ Reservation confirmed successfully!", true);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Booking Confirmed! 🎉");
            successAlert.setHeaderText("Reservation Confirmed for " + hotel.getName());
            successAlert.setContentText("Your stay for " + fNights + " nights (Total: £" +
                    String.format(Locale.UK, "%.2f", calculatedCost) + ") has been confirmed.\n\n" +
                    "A simulated payment receipt has been issued. You can view, track, or cancel this booking anytime from My Bookings.");

            ButtonType viewBookingsBtn = new ButtonType("View in My Bookings");
            ButtonType stayBtn = new ButtonType("Stay on Details", ButtonType.CANCEL.getButtonData());
            successAlert.getButtonTypes().setAll(viewBookingsBtn, stayBtn);

            Optional<ButtonType> choice = successAlert.showAndWait();
            if (choice.isPresent() && choice.get() == viewBookingsBtn) {
                if (ViewNavigator.getMainLayoutController() != null) {
                    ViewNavigator.getMainLayoutController().navigateTo(NavigationItem.MY_BOOKINGS);
                }
            }
        });

        bookingTask.setOnFailed(workerStateEvent -> {
            setFormBusy(false);
            Throwable ex = bookingTask.getException();
            String message = ex != null ? ex.getMessage() : "Failed to create booking.";
            setBookingStatusMessage("❌ Booking failed: " + message, false);
            showAlert(Alert.AlertType.ERROR, "Booking Failed", message);
        });

        Thread thread = new Thread(bookingTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void setFormBusy(boolean busy) {
        if (bookNowButton != null) bookNowButton.setDisable(busy);
        if (bookingSpinner != null) {
            bookingSpinner.setVisible(busy);
            bookingSpinner.setManaged(busy);
        }
        if (checkInPicker != null) checkInPicker.setDisable(busy);
        if (checkOutPicker != null) checkOutPicker.setDisable(busy);
        if (adultSpinner != null) adultSpinner.setDisable(busy);
        if (childSpinner != null) childSpinner.setDisable(busy);
    }

    private void setBookingStatusMessage(String message, boolean isSuccessOrInfo) {
        if (bookingStatusLabel == null) return;
        if (message == null || message.isBlank()) {
            bookingStatusLabel.setVisible(false);
            bookingStatusLabel.setManaged(false);
            return;
        }
        bookingStatusLabel.setText(message);
        bookingStatusLabel.setStyle(isSuccessOrInfo
                ? "-fx-text-fill: #15803d; -fx-font-size: 11px; -fx-font-weight: 600;"
                : "-fx-text-fill: #b91c1c; -fx-font-size: 11px; -fx-font-weight: 600;");
        bookingStatusLabel.setVisible(true);
        bookingStatusLabel.setManaged(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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

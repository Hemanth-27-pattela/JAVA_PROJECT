package com.hotel.desktop.controller;

import com.hotel.desktop.model.Booking;
import com.hotel.desktop.model.BookingStatus;
import com.hotel.desktop.model.CancelBookingResponse;
import com.hotel.desktop.model.HotelWithBookings;
import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.service.BookingService;
import com.hotel.desktop.util.ViewNavigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for the Guest "My Bookings" page.
 * Displays user reservations grouped with status badges, stay dates, pricing,
 * detailed breakdown modals, and self-service cancellation with simulated refund.
 */
public class MyBookingsController {

    private enum FilterCategory {
        ALL, UPCOMING, COMPLETED, CANCELLED
    }

    @FXML private Button refreshButton;
    @FXML private Button filterAllButton;
    @FXML private Button filterUpcomingButton;
    @FXML private Button filterCompletedButton;
    @FXML private Button filterCancelledButton;

    @FXML private ScrollPane scrollPane;
    @FXML private VBox bookingsListBox;
    @FXML private VBox loadingBox;
    @FXML private VBox emptyBox;
    @FXML private Label emptyTitleLabel;
    @FXML private Label emptySubtitleLabel;
    @FXML private Button browseHotelsButton;
    @FXML private VBox errorBox;
    @FXML private Label errorMessageLabel;
    @FXML private Button retryButton;

    private final BookingService bookingService;
    private final List<Booking> allBookings = new ArrayList<>();
    private FilterCategory currentFilter = FilterCategory.ALL;

    public MyBookingsController() {
        this(new BookingService());
    }

    public MyBookingsController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @FXML
    public void initialize() {
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> loadBookings());
        }
        if (retryButton != null) {
            retryButton.setOnAction(e -> loadBookings());
        }
        if (browseHotelsButton != null) {
            browseHotelsButton.setOnAction(e -> {
                if (ViewNavigator.getMainLayoutController() != null) {
                    ViewNavigator.getMainLayoutController().navigateTo(NavigationItem.BROWSE_HOTELS);
                }
            });
        }

        // Setup Filter Tab Buttons
        if (filterAllButton != null) {
            filterAllButton.setOnAction(e -> setFilter(FilterCategory.ALL));
        }
        if (filterUpcomingButton != null) {
            filterUpcomingButton.setOnAction(e -> setFilter(FilterCategory.UPCOMING));
        }
        if (filterCompletedButton != null) {
            filterCompletedButton.setOnAction(e -> setFilter(FilterCategory.COMPLETED));
        }
        if (filterCancelledButton != null) {
            filterCancelledButton.setOnAction(e -> setFilter(FilterCategory.CANCELLED));
        }

        loadBookings();
    }

    private void setFilter(FilterCategory category) {
        this.currentFilter = category;
        updateFilterTabStyles();
        renderFilteredBookings();
    }

    private void updateFilterTabStyles() {
        setTabActive(filterAllButton, currentFilter == FilterCategory.ALL);
        setTabActive(filterUpcomingButton, currentFilter == FilterCategory.UPCOMING);
        setTabActive(filterCompletedButton, currentFilter == FilterCategory.COMPLETED);
        setTabActive(filterCancelledButton, currentFilter == FilterCategory.CANCELLED);
    }

    private void setTabActive(Button btn, boolean active) {
        if (btn == null) return;
        btn.getStyleClass().remove("filter-tab-button-active");
        if (active) {
            btn.getStyleClass().add("filter-tab-button-active");
        }
    }

    public void loadBookings() {
        showLoadingState();

        Task<List<HotelWithBookings>> task = new Task<>() {
            @Override
            protected List<HotelWithBookings> call() throws Exception {
                return bookingService.getMyBookings();
            }
        };

        task.setOnSucceeded(event -> {
            List<HotelWithBookings> hotelsWithBookings = task.getValue();
            allBookings.clear();

            if (hotelsWithBookings != null) {
                for (HotelWithBookings h : hotelsWithBookings) {
                    if (h.getBookings() != null) {
                        for (Booking b : h.getBookings()) {
                            if (b.getHotelName() == null || b.getHotelName().isBlank()) {
                                b.setHotelName(h.getName());
                            }
                            if (b.getHotelLocation() == null || b.getHotelLocation().isBlank()) {
                                b.setHotelLocation(h.getLocationDisplay());
                            }
                            allBookings.add(b);
                        }
                    }
                }
            }

            // Sort bookings newest created first
            allBookings.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });

            updateTabCounts();
            renderFilteredBookings();
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            String msg = ex != null ? ex.getMessage() : "Unknown error connecting to booking service.";
            showErrorState(msg);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateTabCounts() {
        long allCount = allBookings.size();
        long upcomingCount = allBookings.stream().filter(this::isUpcoming).count();
        long completedCount = allBookings.stream().filter(Booking::isPastOrCompleted).count();
        long cancelledCount = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REFUNDED).count();

        if (filterAllButton != null) filterAllButton.setText("All Bookings (" + allCount + ")");
        if (filterUpcomingButton != null) filterUpcomingButton.setText("Upcoming (" + upcomingCount + ")");
        if (filterCompletedButton != null) filterCompletedButton.setText("Completed (" + completedCount + ")");
        if (filterCancelledButton != null) filterCancelledButton.setText("Cancelled (" + cancelledCount + ")");
    }

    private boolean isUpcoming(Booking b) {
        if (b == null) return false;
        boolean activeStatus = (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING);
        return activeStatus && !b.isPastOrCompleted();
    }

    private void renderFilteredBookings() {
        List<Booking> filtered;
        switch (currentFilter) {
            case UPCOMING -> filtered = allBookings.stream().filter(this::isUpcoming).collect(Collectors.toList());
            case COMPLETED -> filtered = allBookings.stream().filter(Booking::isPastOrCompleted).collect(Collectors.toList());
            case CANCELLED -> filtered = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REFUNDED).collect(Collectors.toList());
            case ALL -> filtered = allBookings;
            default -> filtered = allBookings;
        }

        bookingsListBox.getChildren().clear();

        if (filtered.isEmpty()) {
            showEmptyState();
            return;
        }

        showContentState();
        for (Booking booking : filtered) {
            VBox card = createBookingCard(booking);
            bookingsListBox.getChildren().add(card);
        }
    }

    private VBox createBookingCard(Booking booking) {
        VBox card = new VBox(12.0);
        card.getStyleClass().add("my-bookings-card");

        // Top Row: Hotel Name, Location, Status Badges
        HBox topRow = new HBox(12.0);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2.0);
        Label hotelNameLabel = new Label(booking.getHotelName() != null ? booking.getHotelName() : "Hotel Reservation");
        hotelNameLabel.getStyleClass().add("booking-hotel-name");

        Label locationLabel = new Label("📍 " + (booking.getHotelLocation() != null ? booking.getHotelLocation() : "Destination"));
        locationLabel.getStyleClass().add("booking-hotel-location");
        titleCol.getChildren().addAll(hotelNameLabel, locationLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status Badge
        Label statusBadge = new Label(booking.getStatus().getDisplayName().toUpperCase());
        statusBadge.getStyleClass().addAll("badge-status", booking.getStatus().getStyleClass());

        // Payment Badge
        Label paymentBadge = new Label(booking.getPaymentStatus().getDisplayName().toUpperCase());
        paymentBadge.getStyleClass().addAll("badge-status", booking.getPaymentStatus().getStyleClass());

        topRow.getChildren().addAll(titleCol, spacer, statusBadge, paymentBadge);

        Separator sep1 = new Separator();

        // Details Row: Stay Dates, Guests, Primary Guest, Price
        HBox detailsRow = new HBox(28.0);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        // Dates
        VBox datesCol = new VBox(3.0);
        Label datesHeader = new Label("Dates of Stay");
        datesHeader.getStyleClass().add("booking-meta-label");
        Label datesVal = new Label(booking.getFormattedCheckIn() + " → " + booking.getFormattedCheckOut() + " (" + booking.getNightsCount() + " n)");
        datesVal.getStyleClass().add("booking-meta-value");
        datesCol.getChildren().addAll(datesHeader, datesVal);

        // Guests
        VBox guestsCol = new VBox(3.0);
        Label guestsHeader = new Label("Guests");
        guestsHeader.getStyleClass().add("booking-meta-label");
        Label guestsVal = new Label(booking.getGuestsSummary());
        guestsVal.getStyleClass().add("booking-meta-value");
        guestsCol.getChildren().addAll(guestsHeader, guestsVal);

        // Primary Guest
        VBox guestCol = new VBox(3.0);
        Label guestHeader = new Label("Lead Guest");
        guestHeader.getStyleClass().add("booking-meta-label");
        Label guestVal = new Label(booking.getFullName());
        guestVal.getStyleClass().add("booking-meta-value");
        guestCol.getChildren().addAll(guestHeader, guestVal);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Cost
        VBox costCol = new VBox(3.0);
        costCol.setAlignment(Pos.CENTER_RIGHT);
        Label costHeader = new Label("Total Paid (GBP)");
        costHeader.getStyleClass().add("booking-meta-label");
        Label costVal = new Label(booking.getFormattedTotalCost());
        costVal.getStyleClass().add("booking-price-badge");
        costCol.getChildren().addAll(costHeader, costVal);

        detailsRow.getChildren().addAll(datesCol, guestsCol, guestCol, spacer2, costCol);

        // Action Buttons Row
        HBox actionsRow = new HBox(12.0);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.getStyleClass().add("btn-outline");
        viewDetailsBtn.setOnAction(e -> showBookingDetailsModal(booking));

        actionsRow.getChildren().addAll(actionSpacer, viewDetailsBtn);

        // Add Cancel Button only if booking is cancellable
        if (booking.isCancellable()) {
            Button cancelBtn = new Button("Cancel Reservation");
            cancelBtn.getStyleClass().add("btn-cancel-booking");
            cancelBtn.setOnAction(e -> handleCancelBooking(booking));
            actionsRow.getChildren().add(cancelBtn);
        }

        card.getChildren().addAll(topRow, sep1, detailsRow, actionsRow);
        return card;
    }

    private void showBookingDetailsModal(Booking b) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details — Ref #" + (b.getId() != null ? b.getId() : "N/A"));
        alert.setHeaderText("Reservation Summary: " + b.getHotelName());

        StringBuilder sb = new StringBuilder();
        sb.append("📋 Reservation Reference: ").append(b.getId()).append("\n");
        sb.append("🏨 Property: ").append(b.getHotelName()).append(" (").append(b.getHotelLocation()).append(")\n");
        sb.append("👤 Guest Name: ").append(b.getFullName()).append("\n");
        sb.append("📧 Contact: ").append(b.getEmail()).append(" | ").append(b.getPhone() != null ? b.getPhone() : "N/A").append("\n");
        sb.append("📅 Dates: ").append(b.getFormattedCheckIn()).append(" → ").append(b.getFormattedCheckOut()).append(" (").append(b.getNightsCount()).append(" nights)\n");
        sb.append("👥 Occupancy: ").append(b.getGuestsSummary()).append("\n");
        sb.append("💰 Total Amount: ").append(b.getFormattedTotalCost()).append(" GBP\n");
        sb.append("📊 Booking Status: ").append(b.getStatus().getDisplayName().toUpperCase()).append("\n");
        sb.append("💳 Payment Status: ").append(b.getPaymentStatus().getDisplayName().toUpperCase()).append(" (Simulated Mode)\n");

        if (b.getSpecialRequests() != null && !b.getSpecialRequests().isBlank()) {
            sb.append("📝 Special Requests: ").append(b.getSpecialRequests()).append("\n");
        }
        if (b.getCreatedAt() != null) {
            sb.append("🕒 Created At: ").append(b.getFormattedCreatedAt()).append("\n");
        }
        if (b.getStatus() == BookingStatus.CANCELLED) {
            sb.append("❌ Cancellation Reason: ").append(b.getCancellationReason() != null ? b.getCancellationReason() : "None provided").append("\n");
            sb.append("💸 Refund Processed: £").append(b.getRefundAmount() != null ? b.getRefundAmount() : "0.00").append("\n");
        }

        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void handleCancelBooking(Booking b) {
        // Confirmation dialog with reason input
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Cancel Hotel Reservation");
        dialog.setHeaderText("Cancel Booking at " + b.getHotelName() + "\nDates: " + b.getFormattedCheckIn() + " - " + b.getFormattedCheckOut());

        ButtonType confirmButtonType = new ButtonType("Confirm Cancellation & Refund", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        VBox contentBox = new VBox(10.0);
        contentBox.setPadding(new Insets(10, 0, 10, 0));

        Label warningLabel = new Label("⚠️ Are you sure you want to cancel this booking?\n" +
                "A full simulated refund of " + b.getFormattedTotalCost() + " will be credited back immediately.");
        warningLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: 600; -fx-font-size: 12px;");

        Label reasonLabel = new Label("Reason for cancellation (optional):");
        reasonLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600;");

        TextField reasonField = new TextField();
        reasonField.setPromptText("e.g. Change of itinerary, flight delayed");

        contentBox.getChildren().addAll(warningLabel, reasonLabel, reasonField);
        dialog.getDialogPane().setContent(contentBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return reasonField.getText() != null ? reasonField.getText().trim() : "";
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String cancellationReason = result.get();
            showLoadingState();

            Task<CancelBookingResponse> cancelTask = new Task<>() {
                @Override
                protected CancelBookingResponse call() throws Exception {
                    return bookingService.cancelBooking(b.getId(), cancellationReason);
                }
            };

            cancelTask.setOnSucceeded(e -> {
                CancelBookingResponse resp = cancelTask.getValue();
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Reservation Cancelled");
                successAlert.setHeaderText("Your reservation has been cancelled.");
                successAlert.setContentText("A simulated refund of " +
                        (resp != null ? resp.getFormattedRefundAmount() : b.getFormattedTotalCost()) +
                        " has been processed back to your simulated payment method.");
                successAlert.showAndWait();

                loadBookings(); // Reload fresh state from backend
            });

            cancelTask.setOnFailed(e -> {
                Throwable ex = cancelTask.getException();
                String err = ex != null ? ex.getMessage() : "Failed to cancel reservation.";
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Cancellation Failed");
                errorAlert.setHeaderText("Unable to cancel booking");
                errorAlert.setContentText(err);
                errorAlert.showAndWait();

                loadBookings();
            });

            Thread thread = new Thread(cancelTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void showLoadingState() {
        if (loadingBox != null) {
            loadingBox.setVisible(true);
            loadingBox.setManaged(true);
        }
        if (scrollPane != null) {
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
        }
        if (emptyBox != null) {
            emptyBox.setVisible(false);
            emptyBox.setManaged(false);
        }
        if (errorBox != null) {
            errorBox.setVisible(false);
            errorBox.setManaged(false);
        }
    }

    private void showContentState() {
        if (loadingBox != null) {
            loadingBox.setVisible(false);
            loadingBox.setManaged(false);
        }
        if (scrollPane != null) {
            scrollPane.setVisible(true);
            scrollPane.setManaged(true);
        }
        if (emptyBox != null) {
            emptyBox.setVisible(false);
            emptyBox.setManaged(false);
        }
        if (errorBox != null) {
            errorBox.setVisible(false);
            errorBox.setManaged(false);
        }
    }

    private void showEmptyState() {
        if (loadingBox != null) {
            loadingBox.setVisible(false);
            loadingBox.setManaged(false);
        }
        if (scrollPane != null) {
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
        }
        if (emptyBox != null) {
            emptyBox.setVisible(true);
            emptyBox.setManaged(true);
        }
        if (errorBox != null) {
            errorBox.setVisible(false);
            errorBox.setManaged(false);
        }
    }

    private void showErrorState(String message) {
        if (loadingBox != null) {
            loadingBox.setVisible(false);
            loadingBox.setManaged(false);
        }
        if (scrollPane != null) {
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
        }
        if (emptyBox != null) {
            emptyBox.setVisible(false);
            emptyBox.setManaged(false);
        }
        if (errorBox != null) {
            errorBox.setVisible(true);
            errorBox.setManaged(true);
            if (errorMessageLabel != null) {
                errorMessageLabel.setText(message != null ? message : "Error communicating with the hotel backend.");
            }
        }
    }
}


package com.hotel.desktop.controller;

import com.hotel.desktop.model.Hotel;
import com.hotel.desktop.model.HotelSearchParams;
import com.hotel.desktop.model.HotelSearchResponse;
import com.hotel.desktop.model.Pagination;
import com.hotel.desktop.service.ApiException;
import com.hotel.desktop.service.HotelService;
import com.hotel.desktop.util.ViewNavigator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Guest Hotel Browsing and Search screen.
 * Handles search parameters, multi-faceted filtering, asynchronous data loading,
 * pagination, and hotel details transition.
 */
public class BrowseHotelsController {

    // === Search Bar Controls ===
    @FXML private TextField destinationField;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private Spinner<Integer> adultCountSpinner;
    @FXML private Spinner<Integer> childCountSpinner;
    @FXML private Button searchButton;
    @FXML private Button resetSearchButton;

    // === Filter Sidebar Controls ===
    @FXML private ComboBox<String> maxPriceComboBox;
    @FXML private ComboBox<String> sortComboBox;

    @FXML private CheckBox star5CheckBox;
    @FXML private CheckBox star4CheckBox;
    @FXML private CheckBox star3CheckBox;
    @FXML private CheckBox star2CheckBox;
    @FXML private CheckBox star1CheckBox;

    @FXML private CheckBox typeLuxuryCheckBox;
    @FXML private CheckBox typeBoutiqueCheckBox;
    @FXML private CheckBox typeBudgetCheckBox;
    @FXML private CheckBox typeBusinessCheckBox;
    @FXML private CheckBox typeFamilyCheckBox;
    @FXML private CheckBox typeRomanticCheckBox;
    @FXML private CheckBox typeResortCheckBox;

    @FXML private CheckBox facWifiCheckBox;
    @FXML private CheckBox facParkingCheckBox;
    @FXML private CheckBox facPoolCheckBox;
    @FXML private CheckBox facSpaCheckBox;
    @FXML private CheckBox facFitnessCheckBox;
    @FXML private CheckBox facShuttleCheckBox;
    @FXML private CheckBox facFamilyRoomsCheckBox;
    @FXML private CheckBox facNonSmokingCheckBox;

    @FXML private Button applyFiltersButton;
    @FXML private Button clearFiltersButton;

    // === Results & States ===
    @FXML private Label resultsCountLabel;
    @FXML private Label activeFilterBadge;
    @FXML private VBox hotelListContainer;

    @FXML private VBox loadingContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label loadingMessageLabel;

    @FXML private VBox emptyContainer;
    @FXML private Button emptyResetButton;

    @FXML private VBox errorContainer;
    @FXML private Label errorMessageLabel;
    @FXML private Button retryButton;

    // === Pagination Controls ===
    @FXML private HBox paginationBar;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton;

    private final HotelService hotelService;
    private int currentPage = 1;
    private int totalPages = 1;
    private Task<HotelSearchResponse> currentSearchTask;

    public BrowseHotelsController() {
        this(new HotelService());
    }

    public BrowseHotelsController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @FXML
    public void initialize() {
        // Spinners
        if (adultCountSpinner != null) {
            adultCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        }
        if (childCountSpinner != null) {
            childCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        }

        // Price ComboBox
        if (maxPriceComboBox != null) {
            maxPriceComboBox.setItems(FXCollections.observableArrayList(
                    "Any Price",
                    "Under £100",
                    "Under £150",
                    "Under £200",
                    "Under £250",
                    "Under £300",
                    "Under £500"
            ));
            maxPriceComboBox.setValue("Any Price");
        }

        // Sort ComboBox
        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList(
                    "Recently Updated",
                    "Highest Star Rating",
                    "Price: Low to High",
                    "Price: High to Low"
            ));
            sortComboBox.setValue("Recently Updated");
        }

        // Set default dates (tomorrow -> 3 days later)
        if (checkInDatePicker != null) {
            checkInDatePicker.setValue(LocalDate.now().plusDays(1));
        }
        if (checkOutDatePicker != null) {
            checkOutDatePicker.setValue(LocalDate.now().plusDays(4));
        }

        // Button Listeners
        if (searchButton != null) searchButton.setOnAction(e -> executeSearch(1));
        if (resetSearchButton != null) resetSearchButton.setOnAction(e -> handleResetAll());
        if (applyFiltersButton != null) applyFiltersButton.setOnAction(e -> executeSearch(1));
        if (clearFiltersButton != null) clearFiltersButton.setOnAction(e -> handleResetAll());
        if (emptyResetButton != null) emptyResetButton.setOnAction(e -> handleResetAll());
        if (retryButton != null) retryButton.setOnAction(e -> executeSearch(currentPage));

        if (prevPageButton != null) {
            prevPageButton.setOnAction(e -> {
                if (currentPage > 1) {
                    executeSearch(currentPage - 1);
                }
            });
        }
        if (nextPageButton != null) {
            nextPageButton.setOnAction(e -> {
                if (currentPage < totalPages) {
                    executeSearch(currentPage + 1);
                }
            });
        }

        if (sortComboBox != null) {
            sortComboBox.setOnAction(e -> executeSearch(1));
        }

        // Initial Load
        executeSearch(1);
    }

    public void searchDestination(String destination) {
        if (destinationField != null) {
            destinationField.setText(destination);
        }
        executeSearch(1);
    }

    public void filterBy(Integer starRating, String hotelType) {
        if (starRating != null) {
            if (starRating == 5 && star5CheckBox != null) star5CheckBox.setSelected(true);
            if (starRating == 4 && star4CheckBox != null) star4CheckBox.setSelected(true);
            if (starRating == 3 && star3CheckBox != null) star3CheckBox.setSelected(true);
            if (starRating == 2 && star2CheckBox != null) star2CheckBox.setSelected(true);
            if (starRating == 1 && star1CheckBox != null) star1CheckBox.setSelected(true);
        }
        if (hotelType != null) {
            if ("Luxury".equals(hotelType) && typeLuxuryCheckBox != null) typeLuxuryCheckBox.setSelected(true);
            if ("Boutique".equals(hotelType) && typeBoutiqueCheckBox != null) typeBoutiqueCheckBox.setSelected(true);
            if ("Budget".equals(hotelType) && typeBudgetCheckBox != null) typeBudgetCheckBox.setSelected(true);
        }
        executeSearch(1);
    }

    public void executeSearch(int page) {
        if (currentSearchTask != null && currentSearchTask.isRunning()) {
            currentSearchTask.cancel();
        }

        this.currentPage = page;
        HotelSearchParams params = buildSearchParams(page);

        showLoadingState("Searching premier hotels...");

        currentSearchTask = new Task<>() {
            @Override
            protected HotelSearchResponse call() throws Exception {
                return hotelService.searchHotels(params);
            }
        };

        currentSearchTask.setOnSucceeded(event -> {
            HotelSearchResponse response = currentSearchTask.getValue();
            Platform.runLater(() -> renderResults(response));
        });

        currentSearchTask.setOnFailed(event -> {
            if (currentSearchTask.isCancelled()) return;
            Throwable ex = currentSearchTask.getException();
            String msg = ex instanceof ApiException ? ex.getMessage() : "Failed to retrieve hotels: " + ex.getMessage();
            Platform.runLater(() -> showErrorState(msg));
        });

        Thread thread = new Thread(currentSearchTask, "HotelSearchThread");
        thread.setDaemon(true);
        thread.start();
    }

    private HotelSearchParams buildSearchParams(int page) {
        HotelSearchParams params = new HotelSearchParams();
        params.setPage(page);

        // Destination
        if (destinationField != null && !destinationField.getText().trim().isEmpty()) {
            params.setDestination(destinationField.getText().trim());
        }

        // Dates
        if (checkInDatePicker != null) params.setCheckIn(checkInDatePicker.getValue());
        if (checkOutDatePicker != null) params.setCheckOut(checkOutDatePicker.getValue());

        // Guests
        if (adultCountSpinner != null) params.setAdultCount(adultCountSpinner.getValue());
        if (childCountSpinner != null) params.setChildCount(childCountSpinner.getValue());

        // Price Cap
        if (maxPriceComboBox != null && maxPriceComboBox.getValue() != null) {
            String selectedPrice = maxPriceComboBox.getValue();
            if (selectedPrice.startsWith("Under £")) {
                try {
                    String numStr = selectedPrice.replace("Under £", "").trim();
                    params.setMaxPrice(new BigDecimal(numStr));
                } catch (Exception ignored) {}
            }
        }

        // Sort Option
        if (sortComboBox != null && sortComboBox.getValue() != null) {
            String sort = sortComboBox.getValue();
            switch (sort) {
                case "Highest Star Rating" -> params.setSortOption("starRating");
                case "Price: Low to High" -> params.setSortOption("pricePerNightAsc");
                case "Price: High to Low" -> params.setSortOption("pricePerNightDesc");
                default -> params.setSortOption("");
            }
        }

        // Stars
        List<Integer> stars = new ArrayList<>();
        if (star5CheckBox != null && star5CheckBox.isSelected()) stars.add(5);
        if (star4CheckBox != null && star4CheckBox.isSelected()) stars.add(4);
        if (star3CheckBox != null && star3CheckBox.isSelected()) stars.add(3);
        if (star2CheckBox != null && star2CheckBox.isSelected()) stars.add(2);
        if (star1CheckBox != null && star1CheckBox.isSelected()) stars.add(1);
        params.setStars(stars);

        // Hotel Types
        List<String> types = new ArrayList<>();
        if (typeLuxuryCheckBox != null && typeLuxuryCheckBox.isSelected()) types.add("Luxury");
        if (typeBoutiqueCheckBox != null && typeBoutiqueCheckBox.isSelected()) types.add("Boutique");
        if (typeBudgetCheckBox != null && typeBudgetCheckBox.isSelected()) types.add("Budget");
        if (typeBusinessCheckBox != null && typeBusinessCheckBox.isSelected()) types.add("Business");
        if (typeFamilyCheckBox != null && typeFamilyCheckBox.isSelected()) types.add("Family");
        if (typeRomanticCheckBox != null && typeRomanticCheckBox.isSelected()) types.add("Romantic");
        if (typeResortCheckBox != null && typeResortCheckBox.isSelected()) types.add("Beach Resort");
        params.setTypes(types);

        // Facilities
        List<String> facilities = new ArrayList<>();
        if (facWifiCheckBox != null && facWifiCheckBox.isSelected()) facilities.add("Free WiFi");
        if (facParkingCheckBox != null && facParkingCheckBox.isSelected()) facilities.add("Parking");
        if (facPoolCheckBox != null && facPoolCheckBox.isSelected()) facilities.add("Outdoor Pool");
        if (facSpaCheckBox != null && facSpaCheckBox.isSelected()) facilities.add("Spa");
        if (facFitnessCheckBox != null && facFitnessCheckBox.isSelected()) facilities.add("Fitness Center");
        if (facShuttleCheckBox != null && facShuttleCheckBox.isSelected()) facilities.add("Airport Shuttle");
        if (facFamilyRoomsCheckBox != null && facFamilyRoomsCheckBox.isSelected()) facilities.add("Family Rooms");
        if (facNonSmokingCheckBox != null && facNonSmokingCheckBox.isSelected()) facilities.add("Non-Smoking Rooms");
        params.setFacilities(facilities);

        return params;
    }

    private void renderResults(HotelSearchResponse response) {
        hideAllStateOverlays();

        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            showEmptyState();
            return;
        }

        List<Hotel> hotels = response.getData();
        Pagination pagination = response.getPagination();
        this.totalPages = pagination != null && pagination.getPages() > 0 ? pagination.getPages() : 1;
        long totalCount = pagination != null ? pagination.getTotal() : hotels.size();

        resultsCountLabel.setText("Found " + totalCount + (totalCount == 1 ? " hotel" : " hotels"));

        // Render Cards
        hotelListContainer.getChildren().clear();
        for (Hotel hotel : hotels) {
            try {
                URL fxmlUrl = getClass().getResource("/fxml/hotel-card.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent cardNode = loader.load();

                HotelCardController cardController = loader.getController();
                cardController.setHotel(hotel, this::openHotelDetails);

                hotelListContainer.getChildren().add(cardNode);
            } catch (IOException e) {
                System.err.println("Failed to load hotel card FXML: " + e.getMessage());
            }
        }

        // Update Pagination Controls
        updatePaginationUI();
    }

    private void updatePaginationUI() {
        if (totalPages <= 1) {
            paginationBar.setVisible(false);
            paginationBar.setManaged(false);
        } else {
            paginationBar.setVisible(true);
            paginationBar.setManaged(true);
            pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
            prevPageButton.setDisable(currentPage <= 1);
            nextPageButton.setDisable(currentPage >= totalPages);
        }
    }

    private void openHotelDetails(Hotel hotel) {
        if (hotel == null) return;

        try {
            URL fxmlUrl = getClass().getResource("/fxml/hotel-details.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent detailsNode = loader.load();

            HotelDetailsController controller = loader.getController();
            controller.setHotel(hotel, () -> executeSearch(currentPage));

            MainLayoutController mainShell = ViewNavigator.getMainLayoutController();
            if (mainShell != null) {
                mainShell.setContentNode(detailsNode, hotel.getName());
            }
        } catch (IOException e) {
            System.err.println("Failed to load hotel details view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleResetAll() {
        if (destinationField != null) destinationField.clear();
        if (maxPriceComboBox != null) maxPriceComboBox.setValue("Any Price");
        if (sortComboBox != null) sortComboBox.setValue("Recently Updated");

        CheckBox[] allCheckBoxes = {
                star5CheckBox, star4CheckBox, star3CheckBox, star2CheckBox, star1CheckBox,
                typeLuxuryCheckBox, typeBoutiqueCheckBox, typeBudgetCheckBox, typeBusinessCheckBox,
                typeFamilyCheckBox, typeRomanticCheckBox, typeResortCheckBox,
                facWifiCheckBox, facParkingCheckBox, facPoolCheckBox, facSpaCheckBox,
                facFitnessCheckBox, facShuttleCheckBox, facFamilyRoomsCheckBox, facNonSmokingCheckBox
        };
        for (CheckBox cb : allCheckBoxes) {
            if (cb != null) cb.setSelected(false);
        }

        executeSearch(1);
    }

    private void showLoadingState(String message) {
        hideAllStateOverlays();
        if (loadingContainer != null) {
            loadingContainer.setVisible(true);
            loadingContainer.setManaged(true);
        }
        if (loadingMessageLabel != null) {
            loadingMessageLabel.setText(message);
        }
    }

    private void showEmptyState() {
        hideAllStateOverlays();
        if (emptyContainer != null) {
            emptyContainer.setVisible(true);
            emptyContainer.setManaged(true);
        }
        if (resultsCountLabel != null) {
            resultsCountLabel.setText("Found 0 hotels");
        }
        if (paginationBar != null) {
            paginationBar.setVisible(false);
            paginationBar.setManaged(false);
        }
    }

    private void showErrorState(String message) {
        hideAllStateOverlays();
        if (errorContainer != null) {
            errorContainer.setVisible(true);
            errorContainer.setManaged(true);
        }
        if (errorMessageLabel != null) {
            errorMessageLabel.setText(message);
        }
        if (resultsCountLabel != null) {
            resultsCountLabel.setText("Unable to load results");
        }
        if (paginationBar != null) {
            paginationBar.setVisible(false);
            paginationBar.setManaged(false);
        }
    }

    private void hideAllStateOverlays() {
        if (loadingContainer != null) {
            loadingContainer.setVisible(false);
            loadingContainer.setManaged(false);
        }
        if (emptyContainer != null) {
            emptyContainer.setVisible(false);
            emptyContainer.setManaged(false);
        }
        if (errorContainer != null) {
            errorContainer.setVisible(false);
            errorContainer.setManaged(false);
        }
        if (hotelListContainer != null) {
            hotelListContainer.getChildren().clear();
        }
    }
}

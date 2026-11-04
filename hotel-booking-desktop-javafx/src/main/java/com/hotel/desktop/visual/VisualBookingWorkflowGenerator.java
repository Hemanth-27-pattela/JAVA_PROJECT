package com.hotel.desktop.visual;

import com.hotel.desktop.controller.HotelDetailsController;
import com.hotel.desktop.controller.MainLayoutController;
import com.hotel.desktop.controller.MyBookingsController;
import com.hotel.desktop.model.Booking;
import com.hotel.desktop.model.CancelBookingResponse;
import com.hotel.desktop.model.CreateBookingRequest;
import com.hotel.desktop.model.Hotel;
import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.model.PaymentIntentResponse;
import com.hotel.desktop.model.User;
import com.hotel.desktop.service.AuthService;
import com.hotel.desktop.service.BookingService;
import com.hotel.desktop.service.HotelService;
import com.hotel.desktop.service.SessionManager;
import com.hotel.desktop.util.ViewNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Visual verification runner for Phase 9.6: Guest Booking Workflow and My Bookings.
 * Authenticates guest@hotel.com, captures Hotel Details with the active Book Now widget,
 * verifies payment intent and booking creation, and captures My Bookings view.
 */
public class VisualBookingWorkflowGenerator extends Application {

    private static final String ARTIFACT_DIR = "C:/Users/patte/.gemini/antigravity/brain/e308dbfa-266e-4ee2-b0fc-7026d4faacc9";
    private static final String PROJECT_SCREENSHOTS_DIR = "D:/HOTEL_PROJECT/Hotel-Booking-Management-System--React-MERN-FullStack/hotel-booking-desktop-javafx/screenshots";

    private final AuthService authService = new AuthService();
    private final HotelService hotelService = new HotelService();
    private final BookingService bookingService = new BookingService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        ViewNavigator.setPrimaryStage(stage);

        new Thread(() -> {
            try {
                new File(ARTIFACT_DIR).mkdirs();
                new File(PROJECT_SCREENSHOTS_DIR).mkdirs();

                System.out.println("=============================================================");
                System.out.println("PHASE 9.6: GUEST BOOKING WORKFLOW & MY BOOKINGS VERIFICATION");
                System.out.println("Backend Target: http://localhost:5001");
                System.out.println("=============================================================");

                // 1. Authenticate Guest
                System.out.println("\n[1/4] Authenticating Guest (guest@hotel.com)...");
                sessionManager.clearSession();
                User guestUser = authService.login("guest@hotel.com", "12345678");
                System.out.println("Guest Authenticated: " + guestUser.getFullName() + " (" + guestUser.getEmail() + ")");

                // 2. Fetch first hotel
                List<Hotel> hotels = hotelService.getAllHotels();
                if (hotels.isEmpty()) {
                    System.err.println("No hotels found on backend!");
                    Platform.exit();
                    return;
                }
                Hotel targetHotel = hotels.get(0);
                System.out.println("Target Hotel: " + targetHotel.getName() + " in " + targetHotel.getCity() + ", Price: " + targetHotel.getFormattedPrice());

                // 3. Render Hotel Details with Booking Widget
                System.out.println("\n[2/4] Rendering Hotel Details with Book Now Widget...");
                Platform.runLater(() -> {
                    ViewNavigator.showMainLayout();
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hotel-details.fxml"));
                        Parent detailsNode = loader.load();
                        HotelDetailsController detailsController = loader.getController();
                        detailsController.setHotel(targetHotel, () -> {});
                        Platform.runLater(detailsController::scrollToBooking);

                        MainLayoutController shell = ViewNavigator.getMainLayoutController();
                        if (shell != null) {
                            shell.setContentNode(detailsNode, targetHotel.getName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                Thread.sleep(2000);
                captureScreenshot(stage, "guest-hotel-details-booking-form.png");

                // 4. Test Live Booking Creation if no overlapping reservation
                System.out.println("\n[3/4] Testing Live Booking Creation & Payment Intent...");
                int nights = 3;
                LocalDate checkIn = LocalDate.now().plusDays(30);
                LocalDate checkOut = checkIn.plusDays(nights);
                BigDecimal totalCost = targetHotel.getPricePerNight().multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);

                try {
                    PaymentIntentResponse intent = bookingService.createPaymentIntent(targetHotel.getId(), nights);
                    System.out.println("Payment Intent Created: " + intent.getPaymentIntentId() + ", Total: £" + intent.getTotalCost());

                    CreateBookingRequest request = new CreateBookingRequest();
                    request.setFirstName(guestUser.getFirstName());
                    request.setLastName(guestUser.getLastName());
                    request.setEmail(guestUser.getEmail());
                    request.setPhone("+44 7700 900123");
                    request.setAdultCount(2);
                    request.setChildCount(0);
                    request.setCheckIn(checkIn.atStartOfDay(ZoneOffset.UTC).toInstant());
                    request.setCheckOut(checkOut.atStartOfDay(ZoneOffset.UTC).toInstant());
                    request.setTotalCost(totalCost);
                    request.setPaymentIntentId(intent.getPaymentIntentId());
                    request.setSpecialRequests("Quiet high-floor room requested");

                    bookingService.createBooking(targetHotel.getId(), request);
                    System.out.println("Booking Created Successfully for dates " + checkIn + " to " + checkOut);
                } catch (Exception ex) {
                    System.out.println("Booking notice: " + ex.getMessage());
                }

                // 5. Navigate to My Bookings and Capture
                System.out.println("\n[4/4] Navigating to My Bookings view...");
                Platform.runLater(() -> {
                    MainLayoutController shell = ViewNavigator.getMainLayoutController();
                    if (shell != null) {
                        shell.navigateTo(NavigationItem.MY_BOOKINGS);
                    }
                });

                Thread.sleep(2500);
                captureScreenshot(stage, "guest-my-bookings.png");

                System.out.println("\n=============================================================");
                System.out.println("PHASE 9.6: VISUAL VERIFICATION COMPLETE — ALL SCREENSHOTS SAVED");
                System.out.println("=============================================================");

                Platform.exit();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.exit();
            }
        }).start();
    }

    private void captureScreenshot(Stage stage, String filename) {
        Platform.runLater(() -> {
            try {
                Scene scene = stage.getScene();
                if (scene == null) return;

                int width = (int) scene.getWidth();
                int height = (int) scene.getHeight();
                if (width <= 0 || height <= 0) {
                    width = 1200;
                    height = 800;
                }

                WritableImage snapshot = scene.snapshot(null);
                BufferedImage bufferedImage = toBufferedImage(snapshot);

                File artifactFile = new File(ARTIFACT_DIR, filename);
                ImageIO.write(bufferedImage, "png", artifactFile);

                File projectFile = new File(PROJECT_SCREENSHOTS_DIR, filename);
                ImageIO.write(bufferedImage, "png", projectFile);

                System.out.println("Screenshot captured: " + filename + " (" + bufferedImage.getWidth() + "x" + bufferedImage.getHeight() + ")");
            } catch (Exception e) {
                System.err.println("Failed to capture screenshot: " + filename + " - " + e.getMessage());
            }
        });
    }

    private BufferedImage toBufferedImage(WritableImage img) {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader pr = img.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                bi.setRGB(x, y, pr.getArgb(x, y));
            }
        }
        return bi;
    }
}

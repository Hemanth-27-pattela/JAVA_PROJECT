import {
  BrowserRouter as Router,
  Route,
  Routes,
  Navigate,
} from "react-router-dom";
import Layout from "./layouts/Layout";
import AuthLayout from "./layouts/AuthLayout";
import ScrollToTop from "./components/ScrollToTop";
import { Toaster } from "./components/ui/sonner";
import Register from "./pages/Register";
import SignIn from "./pages/SignIn";
import AddHotel from "./pages/AddHotel";
import useAppContext from "./hooks/useAppContext";
import MyHotels from "./pages/MyHotels";
import EditHotel from "./pages/EditHotel";
import Search from "./pages/Search";
import Detail from "./pages/Detail";
import Booking from "./pages/Booking";
import MyBookings from "./pages/MyBookings";
import Home from "./pages/Home";
import ApiDocs from "./pages/ApiDocs";
import ApiStatus from "./pages/ApiStatus";
import AnalyticsDashboard from "./pages/AnalyticsDashboard";
import AuthCallback from "./pages/AuthCallback";
import AdminRoute from "./components/AdminRoute";
import OwnerRoute from "./components/OwnerRoute";
import RequestOwnerAccess from "./pages/RequestOwnerAccess";
import AdminLayout from "./layouts/AdminLayout";
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminHotels from "./pages/admin/AdminHotels";
import AdminUsers from "./pages/admin/AdminUsers";
import AdminReviews from "./pages/admin/AdminReviews";
import AdminBookings from "./pages/admin/AdminBookings";
import AdminActivity from "./pages/admin/AdminActivity";
import AdminOwnerRequests from "./pages/admin/AdminOwnerRequests";

const App = () => {
  const { isLoggedIn } = useAppContext();
  return (
    <Router>
      <ScrollToTop />
      <Routes>
        <Route
          path="/"
          element={
            <Layout>
              <Home />
            </Layout>
          }
        />
        <Route
          path="/search"
          element={
            <Layout>
              <Search />
            </Layout>
          }
        />
        <Route
          path="/detail/:hotelId"
          element={
            <Layout>
              <Detail />
            </Layout>
          }
        />
        <Route
          path="/api-docs"
          element={
            <Layout>
              <ApiDocs />
            </Layout>
          }
        />
        <Route
          path="/api-status"
          element={
            <Layout>
              <ApiStatus />
            </Layout>
          }
        />
        <Route
          path="/business-insights"
          element={
            <OwnerRoute>
              <Layout>
                <AnalyticsDashboard />
              </Layout>
            </OwnerRoute>
          }
        />
        <Route
          path="/register"
          element={
            <AuthLayout>
              <Register />
            </AuthLayout>
          }
        />
        <Route
          path="/sign-in"
          element={
            <AuthLayout>
              <SignIn />
            </AuthLayout>
          }
        />
        <Route
          path="/auth/callback"
          element={
            <Layout>
              <AuthCallback />
            </Layout>
          }
        />

        {/* Guest workflow: Request hotel owner status */}
        <Route
          path="/request-owner-access"
          element={
            <Layout>
              <RequestOwnerAccess />
            </Layout>
          }
        />

        {/* Role-gated property management routes */}
        <Route
          path="/my-hotels"
          element={
            <OwnerRoute>
              <Layout>
                <MyHotels />
              </Layout>
            </OwnerRoute>
          }
        />
        <Route
          path="/add-hotel"
          element={
            <OwnerRoute>
              <Layout>
                <AddHotel />
              </Layout>
            </OwnerRoute>
          }
        />
        <Route
          path="/edit-hotel/:hotelId"
          element={
            <OwnerRoute>
              <Layout>
                <EditHotel />
              </Layout>
            </OwnerRoute>
          }
        />

        <Route
          path="/my-bookings"
          element={
            <Layout>
              <MyBookings />
            </Layout>
          }
        />

        {/* Hotel-domain admin shell — role gate via /users/me */}
        <Route
          path="/admin"
          element={
            <AdminRoute>
              <AdminLayout />
            </AdminRoute>
          }
        >
          <Route index element={<AdminDashboard />} />
          <Route path="owner-requests" element={<AdminOwnerRequests />} />
          <Route path="hotels" element={<AdminHotels />} />
          <Route path="users" element={<AdminUsers />} />
          <Route path="reviews" element={<AdminReviews />} />
          <Route path="bookings" element={<AdminBookings />} />
          <Route path="activity" element={<AdminActivity />} />
        </Route>

        {isLoggedIn && (
          <Route
            path="/hotel/:hotelId/booking"
            element={
              <Layout>
                <Booking />
              </Layout>
            }
          />
        )}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
      <Toaster />
    </Router>
  );
};

export default App;

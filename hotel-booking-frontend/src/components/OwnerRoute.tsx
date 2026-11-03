import { Navigate } from "react-router-dom";
import { useQuery } from "react-query";
import * as apiClient from "../api-client";
import useAppContext from "../hooks/useAppContext";
import UnauthorizedPage from "../pages/UnauthorizedPage";

type Props = {
  children: React.ReactNode;
};

/**
 * Gate: must be logged in and have HOTEL_OWNER or ADMIN role.
 * Standard USER guests are shown an unauthorized notice with access-request options.
 */
const OwnerRoute = ({ children }: Props) => {
  const { isLoggedIn } = useAppContext();

  const { data: user, isLoading } = useQuery(
    "fetchCurrentUser",
    apiClient.fetchCurrentUser,
    { enabled: isLoggedIn }
  );

  if (!isLoggedIn) {
    return <Navigate to="/sign-in" replace />;
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="h-10 w-48 bg-slate-200 rounded-xl animate-pulse" />
      </div>
    );
  }

  const role = user?.role ? user.role.toLowerCase() : "";
  const isOwnerOrAdmin = role === "hotel_owner" || role === "admin";

  if (!isOwnerOrAdmin) {
    return <UnauthorizedPage />;
  }

  return <>{children}</>;
};

export default OwnerRoute;


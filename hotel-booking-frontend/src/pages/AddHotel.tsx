import { useMutation, useQueryClient } from "react-query";
import { useNavigate } from "react-router-dom";
import ManageHotelForm from "../forms/ManageHotelForm/ManageHotelForm";
import useAppContext from "../hooks/useAppContext";
import * as apiClient from "../api-client";
import { invalidateHotelQueries } from "../lib/invalidate-queries";

const AddHotel = () => {
  const { showToast } = useAppContext();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { mutate, isLoading } = useMutation(apiClient.addMyHotel, {
    onSuccess: async () => {
      // Refresh My Hotels / Home / Search caches without full reload
      await invalidateHotelQueries(queryClient);
      showToast({
        title: "Hotel Added Successfully",
        description:
          "Your hotel has been added to the platform successfully! Redirecting to My Hotels...",
        type: "SUCCESS",
      });
      setTimeout(() => {
        navigate("/my-hotels");
      }, 1500);
    },
    onError: (error: unknown) => {
      const err = error as {
        response?: {
          status?: number;
          data?: { message?: string };
        };
        message?: string;
      };
      const status = err.response?.status;
      const responseMsg = err.response?.data?.message || err.message || "";

      if (status === 403) {
        showToast({
          title: "Access Denied (403)",
          description:
            "Your account does not have permission to create hotels. Verified Hotel Owner or Admin privileges are required.",
          type: "ERROR",
        });
      } else if (
        status === 500 &&
        (responseMsg.toLowerCase().includes("cloudinary") ||
          responseMsg.toLowerCase().includes("image") ||
          responseMsg.toLowerCase().includes("upload"))
      ) {
        showToast({
          title: "Cloudinary Upload Failure",
          description:
            responseMsg ||
            "Failed to upload hotel images to Cloudinary. Please verify Cloudinary configuration and network connectivity.",
          type: "ERROR",
        });
      } else {
        showToast({
          title: "Failed to Add Hotel",
          description:
            responseMsg ||
            "There was an error saving your hotel. Please verify form details and try again.",
          type: "ERROR",
        });
      }
    },
  });

  const handleSave = (hotelFormData: FormData) => {
    mutate(hotelFormData);
  };

  return <ManageHotelForm onSave={handleSave} isLoading={isLoading} showBack />;
};

export default AddHotel;

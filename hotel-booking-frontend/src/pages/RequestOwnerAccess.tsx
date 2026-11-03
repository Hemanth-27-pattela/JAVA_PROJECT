import React, { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "react-query";
import { Link } from "react-router-dom";
import * as apiClient from "../api-client";
import useAppContext from "../hooks/useAppContext";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Textarea } from "../components/ui/textarea";
import { Label } from "../components/ui/label";
import {
  Building2,
  Clock,
  RefreshCw,
  ArrowRight,
  ShieldCheck,
} from "lucide-react";

const RequestOwnerAccess = () => {
  const { isLoggedIn, showToast } = useAppContext();
  const queryClient = useQueryClient();

  const [formData, setFormData] = useState<apiClient.CreateOwnerRequestData>({
    businessName: "",
    businessEmail: "",
    businessPhone: "",
    businessAddress: "",
    justification: "",
  });

  const { data: currentUser, isLoading: isUserLoading } = useQuery(
    "fetchCurrentUser",
    apiClient.fetchCurrentUser,
    {
      enabled: isLoggedIn,
      onSuccess: (data) => {
        if (data?.email && !formData.businessEmail) {
          setFormData((prev) => ({ ...prev, businessEmail: data.email }));
        }
      },
    }
  );

  const {
    data: pendingRequest,
    isLoading: isPendingLoading,
    refetch: refetchPending,
  } = useQuery(
    "fetchMyPendingOwnerRequest",
    apiClient.fetchMyPendingOwnerRequest,
    { enabled: isLoggedIn }
  );

  const submitMutation = useMutation(apiClient.submitOwnerRequest, {
    onSuccess: async () => {
      showToast({
        title: "Request Submitted Successfully",
        description:
          "Your application to become a Hotel Owner has been submitted for administrative review.",
        type: "SUCCESS",
      });
      await queryClient.invalidateQueries("fetchMyPendingOwnerRequest");
      await queryClient.invalidateQueries("fetchMyOwnerRequests");
    },
    onError: (error: unknown) => {
      const err = error as { response?: { status?: number; data?: { message?: string } } };
      const message =
        err.response?.data?.message ||
        "Could not submit request. You may already have a pending submission.";
      showToast({
        title: "Submission Failed",
        description: message,
        type: "ERROR",
      });
    },
  });

  const refreshMutation = useMutation(apiClient.refreshToken, {
    onSuccess: async (data) => {
      await Promise.all([
        queryClient.invalidateQueries("validateToken"),
        queryClient.invalidateQueries("fetchCurrentUser"),
        queryClient.invalidateQueries("fetchMyPendingOwnerRequest"),
      ]);
      const role = data.role ? data.role.toLowerCase() : "";
      if (role === "hotel_owner" || role === "admin") {
        showToast({
          title: "Congratulations! Access Granted",
          description:
            "Your Hotel Owner application has been approved. You can now add and manage properties.",
          type: "SUCCESS",
        });
      } else {
        showToast({
          title: "Still Pending",
          description:
            "Your application is still under review by our team. Please check back shortly.",
          type: "INFO" as any,
        });
      }
    },
    onError: () => {
      void refetchPending();
    },
  });

  if (!isLoggedIn) {
    return (
      <div className="max-w-xl mx-auto py-12 px-4 text-center">
        <Building2 className="h-12 w-12 text-primary-600 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-slate-900 mb-2">
          Sign In to Request Hotel Owner Access
        </h1>
        <p className="text-slate-600 mb-6">
          You must be logged in to apply for property management privileges.
        </p>
        <Link to="/sign-in">
          <Button className="bg-primary-600 hover:bg-primary-700">
            Sign In
          </Button>
        </Link>
      </div>
    );
  }

  if (isUserLoading || isPendingLoading) {
    return (
      <div className="max-w-xl mx-auto py-12 px-4">
        <div className="h-48 bg-slate-100 rounded-2xl animate-pulse" />
      </div>
    );
  }

  const role = currentUser?.role ? currentUser.role.toLowerCase() : "";
  const isAlreadyOwner = role === "hotel_owner" || role === "admin";

  // Case 1: Already has owner permissions
  if (isAlreadyOwner) {
    return (
      <div className="max-w-xl mx-auto py-12 px-4">
        <div className="bg-white rounded-2xl p-8 border border-emerald-200 shadow-sm text-center">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 mb-4">
            <ShieldCheck className="h-8 w-8" />
          </div>
          <h1 className="text-2xl font-bold text-slate-900 mb-2">
            You Are a Verified Hotel Owner
          </h1>
          <p className="text-slate-600 mb-6">
            Your account already has full Hotel Owner permissions. You can create, edit, and manage all your properties.
          </p>
          <div className="flex justify-center gap-3">
            <Link to="/my-hotels">
              <Button className="bg-primary-600 hover:bg-primary-700 flex items-center gap-2">
                <Building2 className="h-4 w-4" />
                Go to My Hotels
              </Button>
            </Link>
            <Link to="/add-hotel">
              <Button variant="outline" className="flex items-center gap-2">
                Add New Hotel
                <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // Case 2: Pending request exists
  if (pendingRequest && pendingRequest.status === "PENDING") {
    return (
      <div className="max-w-2xl mx-auto py-12 px-4">
        <div className="bg-white rounded-2xl p-8 border border-blue-200 shadow-sm">
          <div className="flex items-start gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-blue-100 text-blue-600">
              <Clock className="h-6 w-6" />
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800">
                  Pending Review
                </span>
                <span className="text-xs text-slate-500">
                  Submitted {new Date(pendingRequest.createdAt).toLocaleDateString()}
                </span>
              </div>
              <h1 className="text-2xl font-bold text-slate-900 mb-2">
                Your Hotel Owner Request is Under Review
              </h1>
              <p className="text-sm text-slate-600 mb-4 leading-relaxed">
                Thank you for applying to list properties with us! Our administration team is reviewing your application for <strong className="font-semibold text-slate-800">{pendingRequest.businessName}</strong>.
              </p>

              <div className="bg-slate-50 rounded-xl p-4 border border-slate-200 mb-6 text-xs text-slate-700 space-y-1.5">
                <div><span className="font-medium text-slate-900">Business Name:</span> {pendingRequest.businessName}</div>
                <div><span className="font-medium text-slate-900">Contact Email:</span> {pendingRequest.businessEmail}</div>
                {pendingRequest.businessPhone && (
                  <div><span className="font-medium text-slate-900">Phone:</span> {pendingRequest.businessPhone}</div>
                )}
                {pendingRequest.businessAddress && (
                  <div><span className="font-medium text-slate-900">Address:</span> {pendingRequest.businessAddress}</div>
                )}
              </div>

              <div className="flex flex-col sm:flex-row gap-3">
                <Button
                  onClick={() => refreshMutation.mutate()}
                  disabled={refreshMutation.isLoading}
                  className="bg-primary-600 hover:bg-primary-700 flex items-center gap-2"
                >
                  <RefreshCw className={`h-4 w-4 ${refreshMutation.isLoading ? "animate-spin" : ""}`} />
                  Check Approval Status
                </Button>
                <Link to="/">
                  <Button variant="outline">
                    Back to Home
                  </Button>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Case 3: Fresh application form
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.businessName.trim() || !formData.businessEmail.trim()) {
      showToast({
        title: "Required Fields Missing",
        description: "Please provide both business name and contact email.",
        type: "ERROR",
      });
      return;
    }
    submitMutation.mutate(formData);
  };

  return (
    <div className="max-w-2xl mx-auto py-10 px-4">
      <div className="bg-white rounded-2xl p-8 border border-slate-200 shadow-sm">
        <div className="flex items-center gap-3 mb-4">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary-100 text-primary-600">
            <Building2 className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Become a Hotel Owner
            </h1>
            <p className="text-sm text-slate-500">
              Apply to publish and manage hotel properties on the platform
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 mt-6">
          <div>
            <Label htmlFor="businessName" className="text-sm font-semibold text-slate-800">
              Business / Property Group Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="businessName"
              placeholder="e.g. Grand Horizon Hospitality Ltd."
              required
              value={formData.businessName}
              onChange={(e) =>
                setFormData({ ...formData, businessName: e.target.value })
              }
              className="mt-1"
            />
          </div>

          <div>
            <Label htmlFor="businessEmail" className="text-sm font-semibold text-slate-800">
              Official Business Contact Email <span className="text-red-500">*</span>
            </Label>
            <Input
              id="businessEmail"
              type="email"
              placeholder="management@grandhorizon.com"
              required
              value={formData.businessEmail}
              onChange={(e) =>
                setFormData({ ...formData, businessEmail: e.target.value })
              }
              className="mt-1"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <Label htmlFor="businessPhone" className="text-sm font-semibold text-slate-800">
                Business Telephone
              </Label>
              <Input
                id="businessPhone"
                type="tel"
                placeholder="+1 (555) 019-2834"
                value={formData.businessPhone || ""}
                onChange={(e) =>
                  setFormData({ ...formData, businessPhone: e.target.value })
                }
                className="mt-1"
              />
            </div>

            <div>
              <Label htmlFor="businessAddress" className="text-sm font-semibold text-slate-800">
                Registered Business Address
              </Label>
              <Input
                id="businessAddress"
                placeholder="City, State, Country"
                value={formData.businessAddress || ""}
                onChange={(e) =>
                  setFormData({ ...formData, businessAddress: e.target.value })
                }
                className="mt-1"
              />
            </div>
          </div>

          <div>
            <Label htmlFor="justification" className="text-sm font-semibold text-slate-800">
              Experience / Properties to List
            </Label>
            <Textarea
              id="justification"
              rows={3}
              placeholder="Tell us about the properties you manage or plan to list on our booking platform..."
              value={formData.justification || ""}
              onChange={(e) =>
                setFormData({ ...formData, justification: e.target.value })
              }
              className="mt-1"
            />
          </div>

          <div className="pt-2 flex items-center justify-between">
            <Link to="/" className="text-sm text-slate-600 hover:text-slate-900">
              Cancel
            </Link>
            <Button
              type="submit"
              disabled={submitMutation.isLoading}
              className="bg-primary-600 hover:bg-primary-700 text-white min-w-[140px]"
            >
              {submitMutation.isLoading ? "Submitting..." : "Submit Application"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default RequestOwnerAccess;

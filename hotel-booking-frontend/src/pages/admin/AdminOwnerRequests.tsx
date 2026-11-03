import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "react-query";
import * as apiClient from "../../api-client";
import useAppContext from "../../hooks/useAppContext";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import {
  CheckCircle,
  XCircle,
  Clock,
  Building,
  UserCheck,
  MessageSquare,
} from "lucide-react";

const AdminOwnerRequests = () => {
  const queryClient = useQueryClient();
  const { showToast } = useAppContext();
  const [filter, setFilter] = useState<string>("ALL");
  const [reviewNotes, setReviewNotes] = useState<Record<string, string>>({});

  const { data: requests, isLoading } = useQuery(
    ["fetchAdminOwnerRequests", filter],
    () => apiClient.fetchAdminOwnerRequests(filter)
  );

  const approveMutation = useMutation(
    ({ id, notes }: { id: string; notes?: string }) =>
      apiClient.approveOwnerRequest(id, { reviewNotes: notes }),
    {
      onSuccess: () => {
        showToast({
          title: "Request Approved",
          description: "The user has been promoted to Hotel Owner successfully.",
          type: "SUCCESS",
        });
        void queryClient.invalidateQueries("fetchAdminOwnerRequests");
        void queryClient.invalidateQueries("fetchAdminUsers");
      },
      onError: (err: unknown) => {
        const message =
          (err as { response?: { data?: { message?: string } } })?.response?.data
            ?.message || "Failed to approve request.";
        showToast({
          title: "Approval Failed",
          description: message,
          type: "ERROR",
        });
      },
    }
  );

  const rejectMutation = useMutation(
    ({ id, notes }: { id: string; notes?: string }) =>
      apiClient.rejectOwnerRequest(id, { reviewNotes: notes }),
    {
      onSuccess: () => {
        showToast({
          title: "Request Rejected",
          description: "The hotel owner request has been rejected.",
          type: "INFO" as any,
        });
        void queryClient.invalidateQueries("fetchAdminOwnerRequests");
      },
      onError: (err: unknown) => {
        const message =
          (err as { response?: { data?: { message?: string } } })?.response?.data
            ?.message || "Failed to reject request.";
        showToast({
          title: "Rejection Failed",
          description: message,
          type: "ERROR",
        });
      },
    }
  );

  const handleNotesChange = (id: string, text: string) => {
    setReviewNotes((prev) => ({ ...prev, [id]: text }));
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
            <UserCheck className="h-6 w-6 text-primary-600" />
            Hotel Owner Access Requests
          </h1>
          <p className="text-sm text-slate-500">
            Review and approve guest applications to list and manage hotel properties
          </p>
        </div>

        {/* Filter buttons */}
        <div className="flex items-center gap-1.5 bg-slate-200/70 p-1 rounded-xl text-xs font-medium">
          {["ALL", "PENDING", "APPROVED", "REJECTED"].map((tab) => (
            <button
              key={tab}
              onClick={() => setFilter(tab)}
              className={`px-3 py-1.5 rounded-lg transition-colors ${
                filter === tab
                  ? "bg-white text-slate-900 shadow-sm"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              {tab.charAt(0) + tab.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-24 bg-white rounded-xl animate-pulse border border-slate-200" />
          ))}
        </div>
      ) : !requests || requests.length === 0 ? (
        <div className="bg-white rounded-2xl p-12 text-center border border-slate-200">
          <Building className="h-10 w-10 text-slate-400 mx-auto mb-3" />
          <h3 className="text-lg font-semibold text-slate-800 mb-1">
            No Requests Found
          </h3>
          <p className="text-sm text-slate-500">
            {filter === "ALL"
              ? "No owner access requests have been submitted yet."
              : `There are currently no ${filter.toLowerCase()} requests.`}
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {requests.map((req) => (
            <div
              key={req.id}
              className="bg-white rounded-xl p-5 border border-slate-200 shadow-sm transition hover:border-slate-300"
            >
              <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-3 border-b border-slate-100">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-base font-bold text-slate-900">
                      {req.businessName}
                    </span>
                    <span
                      className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                        req.status === "PENDING"
                          ? "bg-amber-100 text-amber-800"
                          : req.status === "APPROVED"
                          ? "bg-emerald-100 text-emerald-800"
                          : "bg-rose-100 text-rose-800"
                      }`}
                    >
                      {req.status === "PENDING" && <Clock className="h-3 w-3" />}
                      {req.status === "APPROVED" && <CheckCircle className="h-3 w-3" />}
                      {req.status === "REJECTED" && <XCircle className="h-3 w-3" />}
                      {req.status}
                    </span>
                  </div>
                  <div className="text-xs text-slate-500 flex flex-wrap items-center gap-3">
                    <span>
                      Applicant: <strong className="font-medium text-slate-700">{req.userName || req.userEmail}</strong> ({req.userEmail})
                    </span>
                    <span>•</span>
                    <span>
                      Business Email: <strong className="font-medium text-slate-700">{req.businessEmail}</strong>
                    </span>
                    {req.businessPhone && (
                      <>
                        <span>•</span>
                        <span>Phone: {req.businessPhone}</span>
                      </>
                    )}
                    <span>•</span>
                    <span>
                      Submitted: {new Date(req.createdAt).toLocaleString()}
                    </span>
                  </div>
                </div>

                {/* Status or Actions */}
                {req.status === "PENDING" ? (
                  <div className="flex items-center gap-2 shrink-0">
                    <Button
                      size="sm"
                      onClick={() =>
                        approveMutation.mutate({
                          id: req.id,
                          notes: reviewNotes[req.id],
                        })
                      }
                      disabled={approveMutation.isLoading || rejectMutation.isLoading}
                      className="bg-emerald-600 hover:bg-emerald-700 text-white flex items-center gap-1.5 text-xs h-8 px-3"
                    >
                      <CheckCircle className="h-3.5 w-3.5" />
                      Approve & Promote
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() =>
                        rejectMutation.mutate({
                          id: req.id,
                          notes: reviewNotes[req.id],
                        })
                      }
                      disabled={approveMutation.isLoading || rejectMutation.isLoading}
                      className="text-rose-600 border-rose-200 hover:bg-rose-50 flex items-center gap-1.5 text-xs h-8 px-3"
                    >
                      <XCircle className="h-3.5 w-3.5" />
                      Reject
                    </Button>
                  </div>
                ) : (
                  <div className="text-right text-xs text-slate-500">
                    <div>Reviewed by: {req.reviewedByEmail || "Administrator"}</div>
                    {req.reviewedAt && (
                      <div>{new Date(req.reviewedAt).toLocaleDateString()}</div>
                    )}
                  </div>
                )}
              </div>

              {/* Justification & Review notes */}
              <div className="mt-3 text-xs text-slate-700 space-y-2">
                {req.businessAddress && (
                  <div>
                    <span className="font-medium text-slate-900">Address:</span>{" "}
                    {req.businessAddress}
                  </div>
                )}
                {req.justification && (
                  <div>
                    <span className="font-medium text-slate-900">Justification / Experience:</span>{" "}
                    <span className="text-slate-600 italic">"{req.justification}"</span>
                  </div>
                )}
                {req.reviewNotes && (
                  <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-200 text-slate-700">
                    <span className="font-medium text-slate-900">Admin Notes:</span>{" "}
                    {req.reviewNotes}
                  </div>
                )}
                {req.status === "PENDING" && (
                  <div className="pt-2 flex items-center gap-2">
                    <MessageSquare className="h-3.5 w-3.5 text-slate-400 shrink-0" />
                    <Input
                      placeholder="Optional review notes or rejection reason..."
                      value={reviewNotes[req.id] || ""}
                      onChange={(e) => handleNotesChange(req.id, e.target.value)}
                      className="h-8 text-xs bg-slate-50 border-slate-200"
                    />
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AdminOwnerRequests;

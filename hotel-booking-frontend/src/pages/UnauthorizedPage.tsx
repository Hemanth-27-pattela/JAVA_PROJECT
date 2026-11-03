import { Link } from "react-router-dom";
import { ShieldAlert, ArrowLeft, Building2 } from "lucide-react";
import { Button } from "../components/ui/button";

const UnauthorizedPage = () => {
  return (
    <div className="min-h-[70vh] flex items-center justify-center px-4 py-12">
      <div className="max-w-md w-full text-center bg-white p-8 rounded-2xl shadow-lg border border-slate-200">
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-amber-100 text-amber-600 mb-6">
          <ShieldAlert className="h-8 w-8" />
        </div>
        <h1 className="text-2xl font-bold text-slate-900 mb-2">
          Hotel Owner Access Required
        </h1>
        <p className="text-sm text-slate-600 mb-6 leading-relaxed">
          Your account is currently registered as a standard Guest (<code className="text-xs bg-slate-100 px-1.5 py-0.5 rounded font-mono text-slate-800">USER</code>). 
          Adding and managing hotels is restricted to verified <strong className="font-semibold text-slate-800">Hotel Owners</strong> and Administrators.
        </p>

        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <Link to="/request-owner-access">
            <Button className="w-full sm:w-auto bg-primary-600 hover:bg-primary-700 text-white flex items-center gap-2">
              <Building2 className="h-4 w-4" />
              Become a Hotel Owner
            </Button>
          </Link>
          <Link to="/">
            <Button variant="outline" className="w-full sm:w-auto flex items-center gap-2">
              <ArrowLeft className="h-4 w-4" />
              Return Home
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default UnauthorizedPage;


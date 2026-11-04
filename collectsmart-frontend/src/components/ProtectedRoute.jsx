import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ allowedRoles }) {
const { user } = useAuth();

// Check whether a valid login exists
const token = localStorage.getItem("collectsmart_token");

if (!token || !user) {
    return <Navigate to="/login" replace />;
}

// Check user role
if (allowedRoles && !allowedRoles.includes(user.role)) {
    if (user.role === "FIELD_OFFICER") {
        return <Navigate to="/officer" replace />;
    }

    if (user.role === "MANAGER") {
        return <Navigate to="/" replace />;
    }

    return <Navigate to="/login" replace />;
}

return <Outlet />;

}
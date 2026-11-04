import { createContext, useContext, useState } from "react";
import API from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
const [user, setUser] = useState(() => {
const storedUser = localStorage.getItem("collectsmart_user");
return storedUser ? JSON.parse(storedUser) : null;
});

const [loading, setLoading] = useState(false);

const login = async (email, password) => {
    setLoading(true);

    try {
        const response = await API.post("/auth/login", {
            email,
            password
        });

        const data = response.data;

        const userData = {
            email: data.email,
            role: data.role,
            userId: data.userId,
            fieldOfficerId: data.fieldOfficerId
        };

        localStorage.setItem("collectsmart_token", data.token);
        localStorage.setItem("collectsmart_user", JSON.stringify(userData));

        setUser(userData);

        return userData;
    } finally {
        setLoading(false);
    }
};

const logout = () => {
    localStorage.removeItem("collectsmart_token");
    localStorage.removeItem("collectsmart_user");
    setUser(null);
};

return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
        {children}
    </AuthContext.Provider>
);

}

export function useAuth() {
return useContext(AuthContext);
}
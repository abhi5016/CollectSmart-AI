import axios from "axios";

const API = axios.create({
baseURL: "http://localhost:8081/api",
headers: {
"Content-Type": "application/json"
}
});

// Automatically attach JWT token to every API request
API.interceptors.request.use(
(config) => {
const token = localStorage.getItem("collectsmart_token");

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
},
(error) => Promise.reject(error)

);

// Handle expired or invalid authentication
API.interceptors.response.use(
(response) => response,
(error) => {
if (error.response?.status === 401) {
localStorage.removeItem("collectsmart_token");
localStorage.removeItem("collectsmart_user");
}

    return Promise.reject(error);
}

);

export default API;
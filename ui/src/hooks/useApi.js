/**
 * Custom hook for API calls with x-api-key header.
 * Automatically selects the correct API key based on the current user role.
 */
export const useApi = () => {
  const API_BASE = "http://localhost:8080";

  // Get current user from localStorage to determine role
  const getCurrentUser = () => {
    return localStorage.getItem('currentUser') || 'admin';
  };

  // Get API key based on user role
  const getApiKey = () => {
    const user = getCurrentUser();
    return user === 'admin' ? 'admin-key' : 'user-key';
  };

  const fetchWithAuth = async (endpoint, options = {}) => {
    const url = `${API_BASE}${endpoint}`;
    const headers = {
      "x-api-key": getApiKey(),
      "X-TestNext-User": getCurrentUser(),
      "Content-Type": "application/json",
      ...options.headers,
    };
    const response = await fetch(url, {
      ...options,
      headers,
    });
    if (!response.ok) {
      throw new Error(`API error: ${response.status} ${response.statusText}`);
    }
    return response.json().catch(() => response.ok ? { ok: true } : {});
  };

  return {
    get: (endpoint) => fetchWithAuth(endpoint, { method: "GET" }),
    post: (endpoint, body) =>
      fetchWithAuth(endpoint, { method: "POST", body: JSON.stringify(body) }),
    put: (endpoint, body) =>
      fetchWithAuth(endpoint, { method: "PUT", body: JSON.stringify(body) }),
    delete: (endpoint) => fetchWithAuth(endpoint, { method: "DELETE" }),
  };
};

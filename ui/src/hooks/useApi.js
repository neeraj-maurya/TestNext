/**
 * Custom hook for API calls with x-api-key header.
 * Automatically selects the correct API key based on the current user role.
 */
export const useApi = () => {
  const API_BASE = "http://localhost:8080";

  // Get auth header from localStorage
  const getAuthHeader = () => {
    return localStorage.getItem('authHeader');
  };

  const fetchWithAuth = async (endpoint, options = {}) => {
    const url = `${API_BASE}${endpoint}`;
    const authHeader = getAuthHeader();

    const headers = {
      "Content-Type": "application/json",
      ...options.headers,
    };

    if (authHeader) {
      headers["Authorization"] = authHeader;
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      // Handle unauthorized (e.g., redirect to login)
      localStorage.removeItem('currentUser');
      localStorage.removeItem('authHeader');
      window.location.href = '/';
      throw new Error('Unauthorized');
    }

    if (!response.ok) {
      let errorInfo = `API error: ${response.status} ${response.statusText}`;
      try {
        const errorJson = await response.json();
        // If backend sends { "error": "Message" }, use that.
        if (errorJson && errorJson.error) {
          errorInfo = errorJson.error;
        } else {
          errorInfo = JSON.stringify(errorJson);
        }
        // Attach full response data to error for callers to inspect if needed
        const err = new Error(errorInfo);
        err.response = { data: errorJson, status: response.status };
        throw err;
      } catch (e) {
        // Fallback if not JSON
        throw new Error(errorInfo);
      }
    }

    // Handle empty responses (like 204 No Content)
    const text = await response.text();
    try {
      return text ? JSON.parse(text) : {};
    } catch (e) {
      // If parsing fails, it might be plain text (e.g. API Key)
      return text;
    }
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

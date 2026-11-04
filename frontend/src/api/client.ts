const ACCESS_KEY = "petchess.accessToken";
const REFRESH_KEY = "petchess.refreshToken";

export interface AuthTokens {
  token: string;
  refreshToken: string;
}

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export const tokenStorage = {
  getAccess: () => localStorage.getItem(ACCESS_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  set(tokens: AuthTokens) {
    localStorage.setItem(ACCESS_KEY, tokens.token);
    localStorage.setItem(REFRESH_KEY, tokens.refreshToken);
  },
  clear() {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
  },
  // Access-токен живёт очень недолго и обновляется автоматически,
  // поэтому "пользователь залогинен" = в хранилище есть refresh-токен.
  hasSession: () => localStorage.getItem(REFRESH_KEY) !== null,
};

// Если несколько запросов одновременно получили 401, refresh делаем один раз.
let refreshPromise: Promise<boolean> | null = null;

function refreshTokens(): Promise<boolean> {
  const refreshToken = tokenStorage.getRefresh();
  if (!refreshToken) return Promise.resolve(false);

  if (!refreshPromise) {
    refreshPromise = (async () => {
      try {
        const response = await fetch("/api/auth/refresh", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken }),
        });
        if (!response.ok) return false;
        tokenStorage.set((await response.json()) as AuthTokens);
        return true;
      } catch {
        return false;
      } finally {
        refreshPromise = null;
      }
    })();
  }
  return refreshPromise;
}

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  /** false - не слать токен и не пытаться обновлять сессию (login, register). */
  auth?: boolean;
}

function parseJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

function extractMessage(data: unknown, fallback: string): string {
  if (data && typeof data === "object" && "message" in data) {
    const message = (data as { message: unknown }).message;
    if (typeof message === "string" && message) return message;
  }
  return fallback;
}

export async function request<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { method = "GET", body, auth = true } = options;

  const send = () => {
    const headers: Record<string, string> = {};
    if (body !== undefined) headers["Content-Type"] = "application/json";
    const token = tokenStorage.getAccess();
    if (auth && token) headers["Authorization"] = `Bearer ${token}`;
    return fetch(`/api${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  };

  let response = await send();

  // 403 тоже учитываем: без настроенного entry point Spring Security отдаёт 403 вместо 401.
  if (auth && (response.status === 401 || response.status === 403)) {
    if (await refreshTokens()) {
      response = await send();
    } else {
      tokenStorage.clear();
      window.location.assign("/login");
      throw new ApiError(401, "Session expired");
    }
  }

  const text = await response.text();
  const data = text ? parseJson(text) : null;

  if (!response.ok) {
    throw new ApiError(
      response.status,
      extractMessage(data, `Request failed (${response.status})`),
    );
  }
  return data as T;
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message;
  return "Cannot reach the server. Please try again later.";
}

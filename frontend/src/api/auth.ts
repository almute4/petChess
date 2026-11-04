import { request, tokenStorage, type AuthTokens } from "./client";

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface UpdateUserPayload {
  username: string;
  email: string;
  /** Если не передавать, пароль на сервере не меняется. */
  password?: string;
}

export function register(username: string, email: string, password: string) {
  return request<User>("/auth/register", {
    method: "POST",
    auth: false,
    body: { username, email, password },
  });
}

export async function login(email: string, password: string) {
  const tokens = await request<AuthTokens>("/auth/login", {
    method: "POST",
    auth: false,
    body: { email, password },
  });
  tokenStorage.set(tokens);
}

export function logout() {
  tokenStorage.clear();
}

export function getCurrentUser() {
  return request<User>("/users/me");
}

export function updateUser(id: number, payload: UpdateUserPayload) {
  return request<User>(`/users/${id}`, { method: "PUT", body: payload });
}

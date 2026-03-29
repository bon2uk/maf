export interface UserResponse {
  id: string;
  email: string;
  first_name: string;
  last_name: string;
  avatar_url?: string;
  role: string;
  created_at: string;
  updated_at: string;
}

export interface UpdateUserRequest {
  first_name?: string;
  last_name?: string;
  email?: string;
}

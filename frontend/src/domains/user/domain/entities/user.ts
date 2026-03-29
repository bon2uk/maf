import { User, UserRole } from "../types";

export class UserEntity implements User {
  constructor(
    public readonly id: string,
    public readonly email: string,
    public readonly firstName: string,
    public readonly lastName: string,
    public readonly role: UserRole,
    public readonly createdAt: Date,
    public readonly updatedAt: Date,
    public readonly avatarUrl?: string
  ) {}

  get fullName(): string {
    return `${this.firstName} ${this.lastName}`.trim();
  }

  get initials(): string {
    const first = this.firstName?.[0] || "";
    const last = this.lastName?.[0] || "";
    return `${first}${last}`.toUpperCase();
  }

  isAdmin(): boolean {
    return this.role === "ADMIN";
  }

  isManager(): boolean {
    return this.role === "MANAGER" || this.role === "ADMIN";
  }
}

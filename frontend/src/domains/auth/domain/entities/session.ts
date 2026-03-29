import { AuthTokens } from "../types";

export class Session {
  constructor(
    public readonly accessToken: string,
    public readonly refreshToken?: string,
    public readonly expiresAt?: Date
  ) {}

  static fromTokens(tokens: AuthTokens): Session {
    const expiresAt = tokens.expiresIn
      ? new Date(Date.now() + tokens.expiresIn * 1000)
      : undefined;

    return new Session(tokens.accessToken, tokens.refreshToken, expiresAt);
  }

  isExpired(): boolean {
    if (!this.expiresAt) return false;
    return new Date() > this.expiresAt;
  }

  toTokens(): AuthTokens {
    return {
      accessToken: this.accessToken,
      refreshToken: this.refreshToken,
    };
  }
}

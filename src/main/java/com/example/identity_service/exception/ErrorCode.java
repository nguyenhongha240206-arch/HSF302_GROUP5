package com.example.identity_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_KEY(10000, "Invalid Key", HttpStatus.BAD_REQUEST),
  USER_EXISTED(1001, "User already exist", HttpStatus.BAD_REQUEST),
  USERNAME_INVALID(1002, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
  PASSWORD_INVALID(1003, "Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND(1004, "User not found", HttpStatus.NOT_FOUND),
  UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
  UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN);

  ErrorCode(int code, String message, HttpStatusCode statusCode) {
    this.code = code;
    this.statusCode = statusCode;
    this.message = message;
  }

  private int code;
  private HttpStatusCode statusCode;
  private String message;
}

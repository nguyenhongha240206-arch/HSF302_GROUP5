package com.example.identity_service.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class UserCreationRequest {

  @Size(min = 3, message = "USERNAME_INVALID")
  private String username;

  @Size(min = 6, message = "PASSWORD_INVALID")
  private String password;

  private String firstName;
  private String lastName;
  private LocalDate birthDate;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }
}

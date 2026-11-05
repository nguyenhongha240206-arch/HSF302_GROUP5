package com.example.identity_service.dto.request;

import java.time.LocalDate;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

  private String password;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private Set<String> roles;
}

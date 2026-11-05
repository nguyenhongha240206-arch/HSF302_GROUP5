package com.example.identity_service.dto.response;

import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private String id;
  private String username;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private Set<RoleResponse> roles;
}

package com.example.identity_service.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionRequest {

  private String name;
  private String description;
}
